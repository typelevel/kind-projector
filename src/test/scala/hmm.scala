package hmm

import shapeless._

class TC[A]

object TC {

  def apply[A](implicit ev: TC[A]) =
    ev

  implicit val IntTC: TC[Int] =
    new TC[Int]

  implicit def deriveHNil: TC[HNil] =
    new TC[HNil]

  implicit def deriveHCons[H, T <: HList](implicit H: TC[H], T: TC[T]): TC[H :: T] =
    new TC[H :: T]

  // not required for this example
  implicit def deriveInstance[F, G](implicit gen: Generic.Aux[F, G], G: TC[G]): TC[F] =
    new TC[F]
}

object test {
  TC[Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
   Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
   Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int :: HNil]

  TC[Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int :: HNil]

  TC[Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int ::
    Int :: Int :: Int :: Int :: Int :: Int :: Int :: Int :: HNil]
}
