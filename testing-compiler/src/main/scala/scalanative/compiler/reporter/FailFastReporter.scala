package scala.scalanative.compiler.reporter

import scala.reflect.internal.util.Position
import scala.scalanative.api
import scala.tools.nsc.Settings

/**
 * Reporter that ignores INFOs and WARNINGs, but directly aborts the compilation
 * on ERRORs.
 */
private[scalanative] class FailFastReporter(override val settings: Settings)
    extends BaseReporter(settings) {
  def handleError(pos: Position, msg: String): Unit =
    throw new api.CompilationFailedException(msg)
}
