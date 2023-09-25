package scala.scalanative.compiler

import scala.tools.nsc.reporters.FilteringReporter
import scala.reflect.internal.util.Position

private[scalanative] trait CompatReporter extends FilteringReporter {
  def add(pos: Position, msg: String, severity: Severity): Unit

  @deprecated
  override def doReport(pos: Position, msg: String, severity: Severity): Unit =
    add(pos, msg, severity)
}
