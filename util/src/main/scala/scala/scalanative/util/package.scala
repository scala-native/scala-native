package scala.scalanative

import java.nio.ByteBuffer

package object util {

  /** Marker methods, called whenever a specific control-flow branch
   *  should never happen.
   */
  def unreachable: Nothing =
    throw UnreachableException

  /** Marker method, called whenever a specific control-flow branch
   *  is not supported.
   */
  def unsupported(v: Any): Nothing =
    throw UnsupportedException(s"$v (${v.getClass})")

  def unsupported(s: String = ""): Nothing =
    throw UnsupportedException(s)

  /** Scope-managed resource. */
  type Resource = java.lang.AutoCloseable

  /** Acquire given resource in implicit scope. */
  def acquire[R <: Resource](res: R)(implicit in: Scope): R = {
    in.acquire(res)
    res
  }

  /** Defer cleanup until the scope closes. */
  def defer(f: => Unit)(implicit in: Scope): Unit = {
    in.acquire(new Resource {
      def close(): Unit = f
    })
  }

  /** Print running time of closure to stdout. */
  def time[T](msg: String)(f: => T): T = {
    import java.lang.System.nanoTime
    val start = nanoTime()
    val res   = f
    val end   = nanoTime()
    println(s"$msg (${(end - start).toFloat / 1000000} ms)")
    res
  }

  def procs: Int =
    java.lang.Runtime.getRuntime.availableProcessors

  def partitionBy[T](elems: Seq[T])(f: T => Any): Map[Int, Seq[T]] =
    partitionBy(elems, procs * procs)(f)

  def partitionBy[T](elems: Seq[T], batches: Int)(
      f: T => Any): Map[Int, Seq[T]] =
    elems.groupBy { elem =>
      Math.abs(f(elem).##) % batches
    }
}
