package d_m

import org.junit.Test

trait ~>[-F[_], +G[_]] {
  def apply[A](x: F[A]): G[A]
}
trait ~>>[-F[_], +G[_]] {
  def dingo[B](x: F[B]): G[B]
}
final case class Const[A, B](getConst: A)

class PolyLambdas {
  type ToSelf[F[_]] = F ~> F

  val kf1 = Lambda[Option ~> Vector](_.iterator.toVector)

  val kf2 = λ[Vector ~> Option] {
    case Vector(x) => Some(x)
    case _         => None
  }

  val kf3 = λ[ToSelf[Vector]](_.reverse)

  val kf4 = λ[Option ~>> Option].dingo(_ flatMap (_ => None))

  val kf5 = λ[Map[*, Int] ~> Map[*, Long]](_.map { case (k, v) => (k, v.toLong) }.toMap)

  val kf6 = λ[ToSelf[Map[*, Int]]](_.map { case (k, v) => (k, v * 2) }.toMap)

  implicit class FGOps[F[_], A](x: F[A]) {
    def ntMap[G[_]](kf: F ~> G): G[A] = kf(x)
  }

  // Scala won't infer the unary type constructor alias from a
  // tuple. I'm not sure how it even could, so we'll let it slide.
  type PairWithInt[A] = (A, Int)
  def mkPair[A](x: A, y: Int): PairWithInt[A] = x -> y
  val pairMap = λ[ToSelf[PairWithInt]] { case (k, v) => (k, v * 2) }
  val tupleTakeFirst = λ[λ[A => (A, Int)] ~> List](x => List(x._1))

  // All these formulations should be equivalent.
  def const1[A]                              = λ[ToSelf[Const[A, *]]](x => x)
  def const2[A] : ToSelf[Const[A, *]]        = λ[Const[A, *] ~> Const[A, *]](x => x)
  def const3[A] : Const[A, *] ~> Const[A, *] = λ[ToSelf[Const[A, *]]](x => x)
  def const4[A]                              = λ[Const[A, *] ~> Const[A, *]](x => x)
  def const5[A] : ToSelf[Const[A, *]]        = λ[ToSelf[λ[B => Const[A, B]]]](x => x)
  def const6[A] : Const[A, *] ~> Const[A, *] = λ[ToSelf[λ[B => Const[A, B]]]](x => x)

  @Test
  def polylambda(): Unit = {
    assert(kf1(None) == Vector())
    assert(kf1(Some("a")) == Vector("a"))
    assert(kf1(Some(5d)) == Vector(5d))
    assert(kf2(Vector(5)) == Some(5))
    assert(kf3(Vector(1, 2)) == Vector(2, 1))
    assert(kf4.dingo(Some(5)) == None)
    assert(kf5(Map("a" -> 5)) == Map("a" -> 5))
    assert(kf6(Map("a" -> 5)) == Map("a" -> 10))

    assert((mkPair("a", 1) ntMap pairMap) == ("a" -> 2))
    assert((mkPair(Some(true), 1) ntMap pairMap) == (Some(true) -> 2))

    assert(mkPair('a', 1).ntMap(tupleTakeFirst) == List('a'))
    // flatten works, whereas it would be a static error in the
    // line above. That's pretty poly!
    assert(mkPair(Some(true), 1).ntMap(tupleTakeFirst).flatten == List(true))
  }
}
