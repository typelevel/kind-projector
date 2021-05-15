package underscores

trait Functor[M[_]] {
  def fmap[A, B](fa: M[A])(f: A => B): M[B]
}

class EitherRightFunctor[L] extends Functor[Either[L, _]] {
  def fmap[A, B](fa: Either[L, A])(f: A => B): Either[L, B] =
    fa match {
      case Right(a) => Right(f(a))
      case Left(l) => Left(l)
    }
}
