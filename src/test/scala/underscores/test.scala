package underscores

object Test {
  // some type-level helper methods
  def foo[T] = ()
  def bar[T[_]] = ()
  def baz[T[_, _]] = ()

  // used for seeing what kind of tree we will start with
  type ?? = Unit
  foo[Either[Int, ??]]
  foo[Tuple3[Int, ??, ??]]

  // used for seeing what kind of tree we want to end up with
  bar[({type L[X] = Either[Int, X]})#L]
  baz[({type L[X,Y] = Tuple3[Int, X, Y]})#L]

  // used to test the plugin
  bar[Either[Int, _]]
  baz[Tuple3[Int, _, _]]
  baz[Tuple3[_, Int, _]]

  // should not be changed by the plugin
  foo[Either[Int, Double]]
  foo[Tuple3[Int, Int, Double]]

  // xyz
  type Fake[A] = A
  foo[Fake[(Int, Double) => Either[Double, Int]]]
  baz[Lambda[(A, B) => Either[B, A]]]

  class Graph { type Node }
  foo[Graph { type Node = Int }]
  bar[Lambda[N => Graph { type Node = N }]]
  //bar[Graph { type Node = ? }] // TODO, maybe?
  //bar[Graph#?Node] // TODO, maybe?

  // higher order
  def qux[T[_[_]]] = ()
  qux[({type L[A[_]] = Unit})#L]
  qux[Lambda[A[_] => Unit]]
  qux[Lambda[A[B] => Unit]]

  trait Functor[F[_]]
  trait EitherT[F[_], A, B]
  qux[Functor[_[_]]]
  qux[EitherT[_[_], Int, Double]]

  // higher higher order
  def vex[T[_[_[_]]]] = ()
  vex[({type L[A[_[_]]] = Unit})#L]
  vex[Lambda[A[_[_]] => Unit]]
  vex[Lambda[A[B[_]] => Unit]]
  vex[Lambda[A[_[C]] => Unit]]
  vex[Lambda[A[B[C]] => Unit]]

  trait FunctorK[F[_[_]]]
  vex[FunctorK[_[_[_]]]]

  def hex[T[_[_[_[_]]]]] = ()
  hex[({type L[A[_[_[_]]]] = Unit})#L]
  hex[Lambda[A[_[_[_]]] => Unit]]

  // covariant
  def mux[T[+_]] = ()
  mux[({type L[+A] = Either[A, Int]})#L]
  mux[Either[`+_`, Int]]
  mux[Lambda[`+A` => Either[A, Int]]]
  mux[Lambda[+[A] => Either[A, Int]]]

  // contravariant
  def bux[T[-_, +_]] = ()
  bux[({type L[-A, +B] = Function2[A, Int, B]})#L]
  bux[Function2[`-_`, Int, `+_`]]
  bux[Lambda[(`-A`, `+B`) => Function2[A, Int, B]]]
  bux[Lambda[(-[A], +[B]) => Function2[A, Int, B]]]

  // higher-kinded variance
  trait ~>[-F[_], +G[_]]
  def tux[T[-F[_]]] = ()
  def hux[T[+G[_]]] = ()
  tux[~>[`-_`[_], Option]]
  hux[~>[Option, `+_`[_]]]
  tux[Lambda[`-F[_]` => ~>[F, Option]]]
  hux[Lambda[`+G[_]` => ~>[Option, G]]]
}
