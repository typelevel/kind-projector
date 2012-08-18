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
  val components = new KindRewriter(this, global) :: Nil
}

class KindRewriter(plugin:Plugin, val global:Global) extends PluginComponent
with Transform with TypingTransformers with TreeDSL {
  import global._

  val runsAfter = "parser" :: Nil
  val phaseName = "kind-projector"

  def newTransformer(unit:CompilationUnit) = new MyTransformer(unit)

  class MyTransformer(unit:CompilationUnit) extends TypingTransformer(unit) {
    // we reserve two names: "Lambda" and "?"
    val tlambda = newTypeName("Lambda")
    val placeholder = newTypeName("$qmark")

    override def transform(tree: Tree): Tree = {
      /**
       * We use this to create type parameters inside our type project, e.g.
       * the A in: ({type L[A] = (A, Int) => A})#L.
       */
      def createInnerTypeParam(name:String) = TypeDef(
        Modifiers(PARAM),
        newTypeName(name),
        Nil,
        TypeBoundsTree(
          Select(Select(Ident("_root_"), "scala"), newTypeName("Nothing")),
          Select(Select(Ident("_root_"), "scala"), newTypeName("Any"))
        )
      )

      /**
       * Given the list a::as, this method finds the last argument in the list
       * (the "subtree") and returns that separately from the other arguments.
       * The stack is just used to enable tail recursion, and a and as are
       * passed separately to avoid dealing with empty lists.
       */
      def parseLambda(a:Tree, as:List[Tree], stack:List[Tree]):(List[Tree], Tree) = {
        as match {
          case Nil => (stack.reverse, a)
          case head :: tail => parseLambda(head, tail, a :: stack)
        }
      }

      /**
       * Builds the horrendous type projection tree. To remind the reader,
       * given List("A", "B") and <(A, Int, B)> we are generating a tree for
       * ({type L[A, B] = (A, Int, B)})#L.
       */
      def buildTypeProjection(innerNames:List[String], subtree:Tree) = {
        SelectFromTypeTree(
          CompoundTypeTree(
            Template(
              Select(Select(Ident("_root_"), "scala"), newTypeName("AnyRef")) :: Nil,
              ValDef(Modifiers(0), "_", TypeTree(), EmptyTree),
              TypeDef(
                Modifiers(0),
                newTypeName("L_kp"),
                innerNames.map(s => createInnerTypeParam(s)),
                super.transform(subtree)
              ) :: Nil
            )
          ),
          newTypeName("L_kp")
        )
      }

      tree match {
        // this case deals with situations like Lambda[(A, B) => ...] where
        // the type lambda's type parameters might be repeated (or used in a
        // different order from the one they appeared in, or ignored).
        case AppliedTypeTree(Ident(`tlambda`), AppliedTypeTree(_, a :: as) :: Nil) => {
          val (args, subtree) = parseLambda(a, as, Nil)

          val innerNames = args.map {
            case Ident(name) => name.toString
            case x => {
              unit.error(x.pos, "Identifier expected, found %s" format x); ""
            }
          }

          buildTypeProjection(innerNames, subtree)
        }

        // this case catches situations like Either[Int, ?]. we catch all trees
        // that could match and see if they contain the placeholder (?). if not
        // then we just transform the tree normally. otherwise, we give the
        // placeholders unique names based on their position in the type
        // argument list, and do our own transformation.
        case AppliedTypeTree(t, as) => {
          // create a new type argument list, catching placeholders and create
          // individual identifiers for them.
          val args = as.zipWithIndex.map {
            case (Ident(`placeholder`), i) => Ident(newTypeName("X_kp%d" format i))
            case (a, i) => super.transform(a)
          }

          // get a list of all names generated for placeholders.
          val innerNames = args.map(_.toString).filter(_ startsWith "X_kp")

          // if we didn't have any placeholders use the normal transformation.
          // otherwise build a type projection.
          if (innerNames.isEmpty) {
            super.transform(tree)
          } else {
            buildTypeProjection(innerNames, AppliedTypeTree(t, args))
          }
        }

        // if neither of our special cases matched this tree, just continue
        case _ => super.transform(tree)
      }
    }
  }
}
