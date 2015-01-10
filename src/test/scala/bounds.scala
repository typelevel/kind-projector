package bounds

trait Leibniz[-L, +H >: L, A >: L <: H, B >: L <: H]

object Test {
  trait Foo
  trait Bar extends Foo

  def outer[A >: Bar <: Foo] = {
    def test[F[_ >: Bar <: Foo]] = 999
    test[λ[`b >: Bar <: Foo` => Leibniz[Bar, Foo, A, b]]]
  }
}
