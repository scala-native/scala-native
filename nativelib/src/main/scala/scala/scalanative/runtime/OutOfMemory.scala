package scala.scalanative.runtime

import scala.scalanative.unsafe.exported

private[runtime] object OutOfMemory {
  private var fallback: OutOfMemoryError = _

  def preallocateFallback(): Unit = {
    val error = createError()
    error.asInstanceOf[Throwable].clearStackTrace()
    fallback = error
  }

  @exported("scalanative_createOutOfMemoryError")
  def createError(): OutOfMemoryError = new OutOfMemoryError(
    "Scala Native heap space"
  )

  def isFallback(error: Throwable): Boolean =
    error.asInstanceOf[AnyRef] eq fallback

  @exported("scalanative_throwOutOfMemoryErrorFallback")
  def throwFallback(): Unit = throw fallback
}
