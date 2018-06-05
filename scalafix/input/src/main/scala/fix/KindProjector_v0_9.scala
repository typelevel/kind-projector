/*
rule = "class:fix.KindProjector_v0_9"
*/
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
  bar[({ type L[A] = Tuple2[A, Int] })#L]
  bar[({ type L[+A] = Either[Int, A] })#L]
  baz[({ type L[-A, +B] = Function2[A, Int, B] })#L]
  qux[({ type L[M[_], B] = EitherT[M, Int, B] })#L]

  // Function Syntax
  bar[({ type R[A] = (A, A) })#R]
  baz[({ type R[A, B] = Either[B, A] })#R]
  bar[({ type R[A] = Either[A, List[A]] })#R]

  // with variance
  bar[({ type R[-A] = Function1[A, Double] })#R]
  baz[({ type R[-A, +B] = Function2[A, Int, B] })#R]
  bar[({ type R[+A] = Either[List[A], List[A]] })#R]

  // with higher-kinded types as type parameters
  hk1[({ type R[A[_]] = List[A[Int]] })#R]
  hk2[({ type R[A, B[_]] = B[A] })#R]

  // with variance on higher-kinded sub-parameters
  hkVar1[({ type R[x[+_]] = Q[x, List] })#R]
  hkVar2[({ type R[f[-_, +_]] = P[f] })#R]
}
