import java.lang.management.ManagementFactory
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

// Allocation pattern from #5120, with nonzero exit on uncaught worker failures.
object HeapGrowthRepro {
  def main(args: Array[String]): Unit = {
    Thread.setDefaultUncaughtExceptionHandler(
      new Thread.UncaughtExceptionHandler {
        override def uncaughtException(
            thread: Thread,
            error: Throwable
        ): Unit = {
          // Keep error reporting independent of debug-symbol stack unwinding.
          System.err.println(
            s"FAILED ${thread.getName}: ${error.getClass.getName}: ${error.getMessage}"
          )
          scala.scalanative.libc.stdlib._Exit(1)
        }
      }
    )
    val memory = ManagementFactory.getMemoryMXBean()
    val gc = ManagementFactory.getGarbageCollectorMXBeans().get(0)
    val threads = args.lift(0).map(_.toInt).getOrElse(16)
    val seconds = args.lift(1).map(_.toInt).getOrElse(600)
    val stop = new AtomicBoolean(false)
    val chunks = new AtomicLong(0)
    val workers = (1 to threads).map { id =>
      val worker = new Thread(
        () => {
          val live = new Array[Array[String]](512)
          var i = 0
          while (!stop.get()) {
            live(i % live.length) = Array.tabulate(64)(j => s"worker-$id-$i-$j")
            i += 1
            if (i % 1024 == 0) chunks.addAndGet(1024)
          }
        },
        s"worker-$id"
      )
      worker.start()
      worker
    }
    def mb(bytes: Long): String = f"${bytes / (1024.0 * 1024)}%.1f"
    println(s"threads=$threads seconds=$seconds")
    try {
      for (second <- 1 to seconds) {
        Thread.sleep(1000L)
        if (second % 5 == 0)
          println(
            s"t=${second}s heap_mb=${mb(memory.getHeapMemoryUsage().getCommitted())} collections=${gc.getCollectionCount()} chunks=${chunks.get()}"
          )
      }
    } finally {
      stop.set(true)
      workers.foreach(_.join())
    }
  }
}
