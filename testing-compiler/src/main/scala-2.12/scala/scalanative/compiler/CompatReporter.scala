package scala.scalanative.compiler

import scala.tools.nsc.reporters.AbstractReporter
import scala.reflect.internal.util.Position

private[scalanative] trait CompatReporter extends AbstractReporter {
  def add(pos: Position, msg: String, severity: Severity): Unit

  override def display(pos: Position, msg: String, severity: Severity): Unit =
    add(pos, msg, severity)

  override def displayPrompt(): Unit = ()
}
