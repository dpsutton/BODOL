(ns bodol.match
  (:require [bodol.types :as t]
            [bodol.monad :refer [state-id]]
            [clojure.core.logic :as log])
  (:import [bodol.types LCons LNumber LSymbol]))



(comment
  (find-match
   (list (LNumber. 0))
   (first (bodol.lambda/parse-def (t/clj->ltype '(0 -> 0 n -> (+ n 1))))))

  (find-match
   (list (t/clj->ltype '(13 37)))
   (first (bodol.lambda/parse-def (t/clj->ltype '((a b) -> (+ a b))))))

  (find-match
   (list (t/clj->ltype '(1 2 3 4 5)))
   (first (bodol.lambda/parse-def (t/clj->ltype '((head . tail) -> foo))))))



(defmulti matchable
  (fn [v]
    (cond
     (sequential? v) :seq
     :else (type v))))
(defmethod matchable :seq [v]
  (if (= v ())
    nil
    (let [v (seq v)
          a (first v)
          d (rest v)]
      (log/lcons (matchable a) (matchable d)))))
(defmethod matchable LCons [v]
  (log/lcons (matchable (t/car v)) (matchable (t/cdr v))))
(defmethod matchable :default [v] v)

(declare log-list)

(defmulti matcher
  (fn [v _]
    (cond
     (sequential? v) :seq
     :else (type v))))
(defmethod matcher LSymbol [v lvars]
  (let [lvar (log/lvar (t/-value v))]
    [lvar (conj lvars lvar)]))
(defmethod matcher LCons [v lvars]
  (if (= v ())
    nil
    (log-list (t/car v) (t/cdr v) lvars)))
(defmethod matcher :seq [v lvars]
  (if (= v ())
    nil
    (log-list (first v) (rest v) lvars)))
(defmethod matcher :default [v lvars]
  [v lvars])

(defn- log-list [a d lvars]
  (let [[a lvars-a] (matcher a [])
        [d lvars-d] (matcher d [])]
    [(log/lcons a d)
     (concat lvars lvars-a lvars-d)]))

(defmulti reconv #(cond (log/lcons? %) :cons
                        (sequential? %) :seq
                        :else (type %)))
(defmethod reconv :seq [v]
  (apply t/cons-list (map reconv v)))
(defmethod reconv :cons [v]
  (LCons. (reconv (clojure.core.logic.protocols/lfirst v))
          (reconv (clojure.core.logic.protocols/lnext v))))
(defmethod reconv :default [v] v)



(defn match-clause [args clause]
  (let [args (matchable args)
        [pattern lvars] (matcher (:args clause) [])
        match (first
               (log/run 1 [q]
                 (log/== args pattern)
                 (log/== q lvars)))
        bindings (and match (zipmap (map :oname lvars)
                                    (map reconv match)))]
    (and bindings
         [clause (fn [scope] [nil (merge scope bindings)])])))

(defn find-match [args clauses]
  (if (zero? (count args)) ; if arity = 0, matching would be pointless
    [(first clauses) state-id]
    (some (partial match-clause args) clauses)))
