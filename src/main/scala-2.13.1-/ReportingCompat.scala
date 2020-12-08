package d_m

import scala.tools.nsc._

/** Taken from ScalaJS, copyright Sébastien Doeraene
 *
 * Original at
 * https://github.com/scala-js/scala-js/blob/950fcda1e480d54b50e75f4cce5bd4135cb58a53/compiler/src/main/scala/org/scalajs/nscplugin/CompatComponent.scala
 * 
 * Modified with only superficial understanding of the code, taking only the bits
 * that are needed here for configurable warnings
 *
 */
trait ReportingCompat {
  
  val global: Global

  implicit final class GlobalCompat(self: global.type) {

    object runReporting {
      def warning(pos: global.Position, msg: String, cat: Any, site: global.Symbol): Unit =
        global.reporter.warning(pos, msg)
    }
  }

  object Reporting {
    object WarningCategory {
      val Deprecation: Any = null
    }
  }
}