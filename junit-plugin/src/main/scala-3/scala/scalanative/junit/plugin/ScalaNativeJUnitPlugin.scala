package scala.scalanative.junit.plugin

import dotty.tools.dotc
import dotc.plugins._
import dotc.transform
import dotc.core
import core.Contexts._

/** The Scala Native JUnit plugin replaces reflection based test lookup.
 *
 *  For each JUnit test `my.pkg.X`, it generates a bootstrapper module/object
 *  `my.pkg.X\$scalanative\$junit\$bootstrapper` implementing
 *  `scala.scalanative.junit.Bootstrapper`.
 *
 *  The test runner uses these objects to obtain test metadata and dispatch to
 *  relevant methods.
 */

class ScalaNativeJUnitPlugin extends StandardPlugin {
  val name: String = "scalanative-junit"
  val description: String = "Makes JUnit test classes invokable in Scala Native"

  @annotation.nowarn("cat=deprecation")
  override def init(options: List[String]): List[PluginPhase] =
    ScalaNativeJUnitBootstrappers() :: Nil
}
