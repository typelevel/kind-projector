package d_m

import scala.reflect.macros.ParseException
import scala.tools.nsc.Global
import scala.tools.nsc.reporters.StoreReporter

class StringParser[G <: Global](val global: G) {
  import global._
  def parse(code: String): Tree = {
    val sreporter = new StoreReporter()
    val oldReporter = global.reporter
    try {
      global.reporter = sreporter
      val parser = newUnitParser(new CompilationUnit(newSourceFile(code, "<kp>")))
      val tree = gen.mkTreeOrBlock(parser.parseStatsOrPackages())
      sreporter.infos.foreach {
        case sreporter.Info(pos, msg, sreporter.ERROR) =>
          throw ParseException(pos, msg)
      }
      tree
    } finally global.reporter = oldReporter
  }
}

