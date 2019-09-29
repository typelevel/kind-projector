package functor

trait Functor[M[_]] {
  def fmap[A, B](fa: M[A])(f: A => B): M[B]
}

class EitherRightFunctor[L] extends Functor[Either[L, *]] {
  def fmap[A, B](fa: Either[L, A])(f: A => B): Either[L, B] =
    fa match {
      case Right(a) => Right(f(a))
      case Left(l) => Left(l)
    }
}

class EitherLeftFunctor[R] extends Functor[Lambda[A => Either[A, R]]] {
  def fmap[A, B](fa: Either[A, R])(f: A => B): Either[B, R] =
    fa match {
      case Right(r) => Right(r)
      case Left(a) => Left(f(a))
    }
}
