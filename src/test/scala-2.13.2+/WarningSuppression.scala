//don't actually run anything, just demonstrate this compiles with fatal warnings
object Example {
  def bar[T[_]] = ()

  @scala.annotation.nowarn("cat=deprecation")
  def f() = bar[Either[Int, ?]]
}