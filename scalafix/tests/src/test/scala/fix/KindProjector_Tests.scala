package fix

import scala.meta._
import scalafix._
import scalafix.testkit._

class KindProjector_Tests
  extends SemanticRuleSuite(
    SemanticdbIndex.load(Classpath(AbsolutePath(BuildInfo.inputClassdirectory))),
    AbsolutePath(BuildInfo.inputSourceroot),
    Seq(AbsolutePath(BuildInfo.outputSourceroot))
  ) {
  runAllTests()
}
