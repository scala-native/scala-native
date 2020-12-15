package scala.scalanative.compiler.reporter

import scala.reflect.internal.util.{NoPosition, Position}
import scala.scalanative.api
import scala.tools.nsc.Settings

private[scalanative] class CollectErrorsReporter(
    override val settings: Settings)
    extends BaseReporter(settings) {

  private val buffer = List.newBuilder[api.CompilerError]

  def handleError(pos: Position, msg: String): Unit = {
    val line = if (pos == NoPosition) 0 else pos.start
    buffer += CompilerError(line, msg)
  }

  def errors: List[api.CompilerError] = buffer.result()
}
