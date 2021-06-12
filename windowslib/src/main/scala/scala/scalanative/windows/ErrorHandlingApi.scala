package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

@extern()
object ErrorHandlingApi {
  def GetLastError(): UInt = extern
}
