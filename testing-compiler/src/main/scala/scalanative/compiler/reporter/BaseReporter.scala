package scala.scalanative.compiler.reporter

import scala.reflect.internal.util.Position
import scala.scalanative.compiler.CompatReporter
import scala.tools.nsc.Settings

private[scalanative] abstract class BaseReporter(
    override val settings: Settings)
    extends CompatReporter {
  override def add(pos: Position, msg: String, severity: Severity): Unit =
    severity match {
      case ERROR => handleError(pos, msg)
      case _     => ()
    }

  def handleError(pos: Position, msg: String): Unit

  override def displayPrompt(): Unit = ()
}
