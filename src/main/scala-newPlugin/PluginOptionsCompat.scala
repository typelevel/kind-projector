package d_m

import scala.tools.nsc.plugins.Plugin

trait PluginOptionsCompat {
  def pluginOptions(plugin: Plugin) = plugin.options
}

//compatibility stub
trait PluginCompat
