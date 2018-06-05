
package fix

object KindProjector_v0_9_only_Test {
  qux[EitherT[?[_], Int, ?]]

  bar[λ[A => Either[A, List[A]]]]

  hk1[λ[A[_] => List[A[Int]]]]

  hkVar1[λ[`x[+_]` => Q[x, List]]]
}
