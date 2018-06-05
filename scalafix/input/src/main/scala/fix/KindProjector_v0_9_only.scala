/* ONLY
rule = "class:fix.KindProjector_v0_9"
*/
package fix

object KindProjector_v0_9_only_Test {
  qux[({ type L[M[_], B] = EitherT[M, Int, B] })#L]

  bar[({ type R[A] = Either[A, List[A]] })#R]

  hk1[({ type R[A[_]] = List[A[Int]] })#R]

  hkVar1[({ type R[x[+_]] = Q[x, List] })#R]
}
