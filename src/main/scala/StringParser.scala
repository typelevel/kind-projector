package d_m

import scala.tools.nsc.Global
import scala.tools.nsc.reporters.StoreReporter

class StringParser[G <: Global](val global: G) {
  import global._
  def parse(code: String): Option[Tree] = {
    val oldReporter = global.reporter
    try {
      val r = new StoreReporter()
      global.reporter = r
      val tree = newUnitParser(code).templateStats().headOption
      if (r.infos.isEmpty) tree else None
    } finally {
      global.reporter = oldReporter
    }
  }
}

