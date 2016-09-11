package d_m

trait ~>[-F[_], +G[_]] {
  def apply[A](x: F[A]): G[A]
}
trait ~>>[-F[_], +G[_]] {
  def dingo[B](x: F[B]): G[B]
}

object PolyLambdas {
  type ToSelf[F[_]] = F ~> F

  val kf1 = Lambda[Option ~> Vector](_.toVector)

  val kf2 = λ[Vector ~> Option] {
    case Vector(x) => Some(x)
    case _         => None
  }

  val kf3 = λ[ToSelf[Vector]](_.reverse)

  val kf4 = λ[Option ~>> Option].dingo(_ flatMap (_ => None))

  def main(args: Array[String]): Unit = {
    assert(kf1(None) == Vector())
    assert(kf1(Some("a")) == Vector("a"))
    assert(kf1(Some(5d)) == Vector(5d))
    assert(kf2(Vector(5)) == Some(5))
    assert(kf3(Vector(1, 2)) == Vector(2, 1))
    assert(kf4.dingo(Some(5)) == None)
  }
}
