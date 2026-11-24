package scala.scalanative.windows

import scala.scalanative.unsafe._

import HandleApi.Handle

// ioapiset.h
@link("kernel32")
@extern()
object IoApi {
  def CancelSynchronousIo(thread: Handle): Boolean = extern
}
