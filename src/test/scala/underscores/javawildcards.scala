package underscores

object shouldWork extends sun.invoke.WrapperInstance {
  override def getWrapperInstanceType: Class[a] forSome { type a } = ???
  override def getWrapperInstanceTarget: java.lang.invoke.MethodHandle = ???
}
