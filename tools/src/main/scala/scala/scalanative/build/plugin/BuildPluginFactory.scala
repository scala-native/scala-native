package scala.scalanative.build
package plugin

import scalanative.build.{BuildException, Filter}

object BuildPluginFactory {
  def create(name: String): BuildPlugin =
    name match {
      case "gc" => new GcBuildPlugin
      case "javalib" => new JavalibBuildPlugin
      case _ =>
        throw new BuildException(
          s"Build plugin not found: ${Filter.buildPluginKey}=${name}")
    }
}
