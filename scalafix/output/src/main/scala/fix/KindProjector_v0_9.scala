
package fix

object `package` {
  // some type-level helper methods
  def foo[T] = ()
  def bar[T[_]] = ()
  def baz[T[_, _]] = ()
  def qux[T[_[_], _]] = ()

  type EitherT[M[_], A, B]

  def hk1[T[_[_]]] = ()
  def hk2[T[_, _[_]]] = ()

  def hkVar1[T[_[+_]]] = ()
  def hkVar2[T[_[-_, +_]]] = ()

  type Q[_[+_], _[_]]
  type P[_[-_, +_]]
}

object KindProjector_v0_9_Test {
  // should not be changed by the rewrite
  foo[Either[Int, Double]]
  foo[Tuple3[Int, Int, Double]]

  // Inline Syntax
  bar[Tuple2[?, Int]]
  bar[Either[Int, +?]]
  baz[Function2[-?, Int, +?]]
  qux[EitherT[?[_], Int, ?]]

  // Function Syntax
  bar[λ[A => (A, A)]]
  baz[λ[(A, B) => Either[B, A]]]
  bar[λ[A => Either[A, List[A]]]]

  // with variance
  bar[Function1[-?, Double]]
  baz[Function2[-?, Int, +?]]
  bar[λ[`+A` => Either[List[A], List[A]]]]

  // with higher-kinded types as type parameters
  hk1[λ[A[_] => List[A[Int]]]]
  hk2[λ[(A, B[_]) => B[A]]]

  // with variance on higher-kinded sub-parameters
  hkVar1[λ[`x[+_]` => Q[x, List]]]
  hkVar2[λ[`f[-_, +_]` => P[f]]]
}
