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
  bar[Either[Int, ?]]
  baz[Tuple3[Int, ?, ?]]

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
}
