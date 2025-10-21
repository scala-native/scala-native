package scala.scalanative.compiler

import scala.reflect.internal.util.Position
import scala.tools.nsc.reporters.AbstractReporter

private[scalanative] trait CompatReporter extends AbstractReporter {
  def add(pos: Position, msg: String, severity: Severity): Unit

  override def display(pos: Position, msg: String, severity: Severity): Unit =
    add(pos, msg, severity)

  override def displayPrompt(): Unit = ()
}
