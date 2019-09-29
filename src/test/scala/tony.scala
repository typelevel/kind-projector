// code by tony morris (slightly modified)
// sent to scala-users recently

trait Functor[F[_]] {
  def fmap[A, B](f: A => B): F[A] => F[B]
}

trait FlatMap[F[_]] extends Functor[F] {
  def flatMap[A, B](f: A => F[B]): F[A] => F[B]
}

// implicitly is so ugly
object FlatMap {
  def apply[A[_]](implicit ev:FlatMap[A]) = ev
}

trait Semigroupoid[~>[_, _]] {
  def compose[A, B, C]: (A ~> B) => (B ~> C) => (A ~> C)
}

case class Kleisli[A, F[_], B](k: A => F[B])

object Main {
  // this was what tony wrote
  def KleisliSemigroupoid[F[_]: FlatMap]: Semigroupoid[({ type lam[a, b]=Kleisli[a, F, b] })#lam] = {
    new Semigroupoid[({ type lam[a, b]=Kleisli[a, F, b] })#lam] {
      def compose[A, B, C] = {
        f => g => Kleisli(a => implicitly[FlatMap[F]].flatMap(g.k)(f k a))
      }
    }
  }
  
  // using the plugin, this should be the same (but hopefully easier to read)
  def KleisliSemigroupoid2[F[_]: FlatMap]: Semigroupoid[Kleisli[*, F, *]] = {
    new Semigroupoid[Kleisli[*, F, *]] {
      def compose[A, B, C] = {
        f => g => Kleisli(a => FlatMap[F].flatMap(g.k)(f.k(a)))
      }
    }
  }
}
