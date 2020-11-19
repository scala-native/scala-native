package scala.scalanative.compiler.reporter

import scala.scalanative.api

private[scalanative] case class CompilerError(pos: Int, msg: String)
    extends api.CompilerError {
  override def getPosition: Integer = pos

  override def getErrorMsg: String = msg

  override def equals(obj: Any): Boolean = obj match {
    case err: api.CompilerError =>
      (err.getErrorMsg == getErrorMsg) &&
        (err.getPosition == getPosition)
    case _ =>
      false
  }
}
