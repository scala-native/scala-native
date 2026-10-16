import scala.scalanative.unsafe._

@extern object Interceptor {
  def installSignalInterceptor(): Unit = extern
  def checkSignalInterceptor(): Unit = extern
}

object Main {
  def main(args: Array[String]): Unit = {
    val intercept = !args.contains("plain")
    if (intercept) Interceptor.installSignalInterceptor()
    val failure = new java.util.concurrent.atomic.AtomicReference[Throwable]()
    val workers = Array.tabulate(4) { _ =>
      new Thread(new Runnable {
        def run(): Unit = try {
          val retained = new Array[Array[Byte]](64)
          for (i <- 0 until 2000) {
            val bytes = new Array[Byte](4096)
            bytes(0) = i.toByte
            retained(i % retained.length) = bytes
            if (i % 100 == 0) System.gc()
          }
          assert(retained.forall(_ != null))
        } catch { case t: Throwable => failure.set(t) }
      })
    }
    workers.foreach(_.start())
    workers.foreach(_.join())
    if (failure.get() != null) throw failure.get()
    if (intercept) Interceptor.checkSignalInterceptor()
  }
}
