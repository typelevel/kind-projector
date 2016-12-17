package forall

import scala.language.reflectiveCalls

object Foralls {
  val nil: ∀[A => List[A]] = ∀[A => List[A]](Nil)
  val is: List[Int] = nil[Int]

  val right: ∀[A => Either[A, Int]] = ∀[A => Either[A, Int]](Right(42))
  val e: Either[String, Int] = right[String]

  trait Monoid[A] {
    def zero: A
    def combine(a1: A, a2: A): A
  }

  def listMonoid[A]: Monoid[List[A]] = new Monoid[List[A]] {
    def zero = Nil
    def combine(l1: List[A], l2: List[A]): List[A] = l1 ++ l2
  }

  type MonoidK[F[_]] = ∀[A => Monoid[F[A]]]
  val listMonoidK: MonoidK[List] = ∀[A => Monoid[List[A]]](listMonoid)

  val l: List[Int] = listMonoidK().combine(List(1, 2, 3), List(4, 5, 6))

  type ~>[F[_], G[_]] = ∀[A => F[A] => G[A]]
  val headOption = ∀[A => List[A] => Option[A]](_.headOption)

  val h: Option[Int] = headOption()(l)


  type Forall[F[_]] = ∀[A => F[A]]

  // nested ∀s not yet working
  // type Exists[F[_]] = ∀[B => ∀[A => F[A] => B] => B]

}
