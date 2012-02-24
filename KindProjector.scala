package d_m

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent
import nsc.transform.Transform
import nsc.transform.InfoTransform
import nsc.transform.TypingTransformers
import nsc.symtab.Flags._
import nsc.ast.TreeDSL
import nsc.typechecker

class KindProjector(val global: Global) extends Plugin {
  val name = "kind-projector"
  val description = "Does something complicated"
  val components = List[PluginComponent](new KindRewriter(this, global))
}

class KindRewriter(plugin:Plugin, val global:Global) extends PluginComponent
with Transform with TypingTransformers with TreeDSL {
  import global._

  val runsAfter = List("parser");
  val phaseName = "kind-projector"

  def newTransformer(unit:CompilationUnit) = new MyTransformer(unit)

  class MyTransformer(unit:CompilationUnit) extends TypingTransformer(unit) {

    val qmark = newTypeName("$qmark")

    override def transform(tree: Tree): Tree = {
      def mylog(s:String) = unit.warning(tree.pos, s)

      def createInnerTypeParam(name:String) = TypeDef(
        Modifiers(0),
        newTypeName(name),
        List[TypeDef](),
        TypeBoundsTree(
          Select(Select(Ident("_root_"), "scala"), "Nothing"),
          Select(Select(Ident("_root_"), "scala"), "Any")
        )
      )

      tree match {
        case AppliedTypeTree(t, args) => {
          val args2 = args.zipWithIndex.map {
            case (Ident(`qmark`), i) => Ident("X$kp%d" format i)
            case (arg, i) => super.transform(arg)
          }

          val innerNames = args2.map(_.toString).filter(_ startsWith "X$kp")

          //println("t is %s" format t)
          //println("args are %s" format args)
          //println("args2 are %s" format args2)
          //println("inner types are %s" format innerNames)

          SelectFromTypeTree(
            CompoundTypeTree(
              Template(
                List(Select(Select(Ident("_root_"), "scala"), "AnyRef")),
                ValDef(Modifiers(0), "_", TypeTree(), EmptyTree),
                List(
                  TypeDef(
                    Modifiers(0),
                    newTypeName("L$kp"),
                    innerNames.map(s => createInnerTypeParam(s)),
                    AppliedTypeTree(t, args2)
                  )
                )
              )
            ),
            newTypeName("L$kp")
          )
        }

        case _ => super.transform(tree)
      }
    }
  }
}
