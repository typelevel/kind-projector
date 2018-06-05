package fix

import scala.meta._
import scalafix._

sealed trait Variance
object Variance {
  case object Invariant extends Variance
  case object Covariant extends Variance
  case object Contravariant extends Variance
}
import Variance.{ Contravariant, Covariant, Invariant }

final case class KindProjector_v0_9(index: SemanticdbIndex)
    extends SemanticRule(index, "KindProjector_v0_9") {

  private def tparamVariance(tparam: Type.Param): Variance = {
    val varianceMods =
      tparam.mods.filter(m => m.is[Mod.Covariant] || m.is[Mod.Contravariant])
    varianceMods match {
      case Nil                       => Variance.Invariant
      case List(Mod.Covariant())     => Variance.Covariant
      case List(Mod.Contravariant()) => Variance.Contravariant
      case _                         =>
        val tparamStructure = {tparam.structure}
        val msg = s"Multiple variance modifiers on type parameter: $tparamStructure"
        throw new IllegalStateException(msg)
    }
  }

  private def tparamHasVariance(tparam: Type.Param): Boolean =
    tparamVariance(tparam) match {
      case Invariant     => false
      case Covariant     => true
      case Contravariant => true
    }

  private def tparamContainsVariance(tparam: Type.Param): Boolean =
    tparamHasVariance(tparam) || (tparam.tparams exists tparamContainsVariance)

  override def fix(ctx: RuleCtx): Patch = {
    // val TypeLambda1 = Type.Name("Lambda")
    val TypeLambda2 = Type.Name("λ")

    // From:   bar[({ type L[X] = Either[Int, X] })#L]
    // To:     bar[Either[Int, ?]]
    def rewriteTypeApply(tp: Type.Project, tparams: List[Type.Param], ta: Type.Apply): Patch = {
      val Type.Apply(tpe, args) = ta

      val tparamMap = tparams.map(tparam => tparam.name.value -> tparam).toMap

      // If each type parameter (tparams) is only used once in the body and in the same order
      // Then inline syntax, with '?' placeholder, can be used.
      val canUseInlineSyntax = {
        println()
        println(s"tparams: $tparams")
        println(s"args: $args")

        val tparamNames = tparams map (_.name.value)

        def getTypeParamNames(tpe: Type): List[String] = tpe match {
          case Type.Name(name)       =>
            tparamMap get name match {
              case None        => Nil
              case Some(value) => println(s"tparam found: $value"); List(name)
            }
            // if (tparamMap contains name) List(name) else Nil
          case t @ Type.Apply(tpe, args) =>
            println(s"Digging deeper on $t")
            args flatMap getTypeParamNames
//            Nil // getTypeParamNames(tpe) // ++
          case _                     => Nil
        }

        val tparamNamesInArgs = args flatMap getTypeParamNames

        println(s"tparamNamesInArgs: " + tparamNamesInArgs)
        println(s"tparamNames: " + tparamNames)

        val canUseInlineSyntax = tparamNamesInArgs sameElements tparamNames
        println(s"canUseInlineSyntax: " + canUseInlineSyntax)
        canUseInlineSyntax
      }

      def inlineSyntax = {
        val args2 = args map {
          case arg @ Type.Name(name) =>
            tparamMap get name match {
              case None         => arg
              case Some(tparam) =>
                val tparams = tparam.tparams match {
                  case Nil     => ""
                  case tparams => tparams map (_.syntax) mkString("[", ", ", "]")
                }
                tparamVariance(tparam) match {
                  case Variance.Invariant     => Type.Name(s"?$tparams")
                  case Variance.Covariant     => Type.Name(s"+?$tparams")
                  case Variance.Contravariant => Type.Name(s"-?$tparams")
                }
            }
          case arg                   => arg
        }
        Type.Apply(tpe, args2)
      }

      def functionSytax = {
        val params = tparams map { tparam =>
          val name = tparam.name.value
          val tparams = tparam.tparams match {
            case Nil     => ""
            case tparams => tparams map (_.syntax) mkString("[", ", ", "]")
          }
          tparamVariance(tparam) match {
            case Variance.Invariant     =>
              val name2 = Type.Name(s"$name$tparams")
              if (tparamContainsVariance(tparam)) {
                val escapedName = Type.Name(s"`$name$tparams`")
                if (escapedName.toString startsWith "``") name2 else escapedName
              } else
                name2
            case Variance.Covariant     => Type.Name(s"`+$name$tparams`")
            case Variance.Contravariant => Type.Name(s"`-$name$tparams`")
          }
        }
        Type.Apply(TypeLambda2, List(Type.Function(params, ta)))
      }

      val ta2 = if (canUseInlineSyntax) inlineSyntax else functionSytax
      ctx.replaceTree(tp, ta2.syntax)
    }

    // From:   bar[({ type R[A] = (A, A) })#R]
    // To:     bar[λ[A => (A, A)]]
    def rewriteTypeTuple(tp: Type.Project, tparams: List[Type.Param], tt: Type.Tuple): Patch = {
      val params = tparams map (tparam => Type.Name(tparam.name.value))
      val ta = Type.Apply(TypeLambda2, List(Type.Function(params, tt)))
      ctx.replaceTree(tp, ta.syntax)
    }

    def rewrite(tp: Type.Project, tparams: List[Type.Param], tpe: Type): Patch = tpe match {
      case ta: Type.Apply => rewriteTypeApply(tp, tparams, ta)
      case tt: Type.Tuple => rewriteTypeTuple(tp, tparams, tt)
      case _              => Patch.empty
    }

    ctx.tree.collect {
      case tp @ Type.Project(Type.Refine(None, List(Defn.Type(_, name1, tparams, tpe))), name2)
        if name1.value == name2.value => rewrite(tp, tparams, tpe)
    }.asPatch
  }

}
