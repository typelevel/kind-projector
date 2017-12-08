package fix

import scala.meta._
import scalafix._

final case class KindProjector_v0_9(index: SemanticdbIndex)
    extends SemanticRule(index, "KindProjector_v0_9") {

  override def fix(ctx: RuleCtx): Patch = {
    ctx.debugIndex()
    println(s"Tree.syntax: " + ctx.tree.syntax)
    println(s"Tree.structure: " + ctx.tree.structure)
    Patch.empty
  }

}
