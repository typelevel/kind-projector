package d_m

import scala.reflect.internal.SymbolTable

trait Extractors {
  val global: SymbolTable
  import global._

  def isTermLambdaMarker(tree: Tree): Boolean
  def freshName(s: String): String

  object TermLambdaMarker {
    def unapply(tree: Tree): Boolean = isTermLambdaMarker(tree)
  }
  object UnappliedType {
    def unapply(tree: Tree): Option[List[Tree]] = tree match {
      case AppliedTypeTree(fun, args) => Some(fun :: args)
      case _                          => None
    }
  }
  object TermLambdaTypeArgs {
    def unapply(tree: Tree): Option[List[Tree]] = tree match {
      case TypeApply(TermLambdaMarker(), targs) => Some(targs)
      case _                                    => None
    }
  }
  object Function1Tree {
    private def freshTerm(): TermName = newTermName(freshName("pf"))
    private def casesFunction(name: TermName, cases: List[CaseDef]) = name -> Match(Ident(name), cases)

    def unapply(tree: Tree): Option[(TermName, Tree)] = tree match {
      case Function(ValDef(_, name, _, _) :: Nil, body) => Some(name -> body)
      case Match(EmptyTree, cases)                      => Some(casesFunction(freshTerm(), cases))
      case _                                            => None
    }
  }
  object PolyLambda {
    def unapply(tree: Tree): Option[(TermName, List[Tree], Tree)] = tree match {
      case Apply(Select(TermLambdaTypeArgs(targs), method), arg :: Nil) => Some((method.toTermName, targs, arg))
      case Apply(TermLambdaTypeArgs(targs), arg :: Nil)                 => Some((nme.apply, targs, arg))
      case _                                                            => None
    }
  }
}
