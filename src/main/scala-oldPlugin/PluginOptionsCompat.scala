package d_m

import scala.tools.nsc.plugins.Plugin

trait PluginOptionsCompat {
  def pluginOptions(plugin: Plugin) = plugin.asInstanceOf[PluginCompat].options
}

trait PluginCompat extends Plugin {
  var options: List[String] = _
  override def processOptions(options: List[String], error: String => Unit): Unit = {
    this.options = options
    init(options, error)
  }
  def init(options: List[String], error: String => Unit): Boolean
}
