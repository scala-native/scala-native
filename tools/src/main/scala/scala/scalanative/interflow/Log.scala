package scala.scalanative
package interflow

trait Log { self: Interflow =>
  private def show: Boolean =
    false

  def in[T](msg: String)(f: => T): T = {
    log(msg)
    pushContext(msg)
    try {
      if (show) {
        val start = System.nanoTime
        val res   = f
        val end   = System.nanoTime
        log(s"done $msg (${(end - start) / 1000000D})")
        res
      }
      else f
    } catch {
      case e: Throwable =>
        log("unwinding " + msg + " due to: " + e.toString)
        throw e
    } finally {
      popContext()
    }
  }

  def log(msg: => String): Unit =
    if (show) {
      println(("  " * contextDepth()) + msg)
    }

  def debug[T](msg: String)(f: => T): T = {
    log(s"computing $msg")
    val res = f
    log(s"debug $msg = $res")
    res
  }
}
