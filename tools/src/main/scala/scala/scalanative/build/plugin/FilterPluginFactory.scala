package scala.scalanative.build
package plugin

import scalanative.build.{BuildException, Filter}

private[build] object FilterPluginFactory {
  def create(name: String): FilterPlugin =
    name match {
      case "gc"      => new GcFilterPlugin
      case "javalib" => new JavalibFilterPlugin
      case _ =>
        throw new BuildException(
          s"Build plugin not found: ${Filter.filterPluginKey}=${name}"
        )
    }
}
