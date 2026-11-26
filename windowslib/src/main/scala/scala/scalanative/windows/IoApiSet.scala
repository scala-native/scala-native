package scala.scalanative.windows

import scala.scalanative.unsafe._

import HandleApi.Handle

@link("kernel32")
@extern()
object IoApiSet {
  def CancelSynchronousIo(thread: Handle): Boolean = extern
}
