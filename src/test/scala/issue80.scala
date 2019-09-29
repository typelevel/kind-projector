trait ~~>[A[_[_]], B[_[_]]] {
  def apply[X[_]](a: A[X]): B[X]
}

trait Bifunctor[F[_[_[_]], _[_[_]]]] {
  def bimap[A[_[_]], B[_[_]], C[_[_]], D[_[_]]](fab: F[A, B])(f: A ~~> C, g: B ~~> D): F[C, D]
}

final case class Coproduct[A[_[_]], B[_[_]], X[_]](run: Either[A[X], B[X]])

object Coproduct {
  def coproductBifunctor[X[_]]: Bifunctor[Coproduct[*[_[_]], *[_[_]], X]] =
    new Bifunctor[Coproduct[*[_[_]], *[_[_]], X]] {
      def bimap[A[_[_]], B[_[_]], C[_[_]], D[_[_]]](abx: Coproduct[A, B, X])(f: A ~~> C, g: B ~~> D): Coproduct[C, D, X] =
        abx.run match {
          case Left(ax)  => Coproduct(Left(f(ax)))
          case Right(bx) => Coproduct(Right(g(bx)))
        }
    }
}
