package nested

// // From https://github.com/non/kind-projector/issues/20
// import scala.language.higherKinds

object KindProjectorWarnings {
  trait Foo[F[_], A]
  trait Bar[A, B]

  def f[G[_]]: Unit = ()

  f[Foo[Bar[Int, *], *]] // shadowing warning
}
