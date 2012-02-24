object Test {
  type ? = Unit

  def foo[T] = ()
  def bar[T] = ()

  //foo[({type L[X] = Either[Int, X]})#L]
  foo[Either[Int, ?]]

  //bar[({type L[X,Y] = Tuple3[Int, X, Y]})#L]
  bar[Tuple3[Int, ?, ?]]
}
