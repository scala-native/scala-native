import java.util.concurrent.CountDownLatch

class Boom extends RuntimeException("BOOM")
object Boom {
  val t: String = {
    println(s"throw in ${Thread.currentThread()}")
    throw new Boom()
  }
}

object Test {
  private final def doWork(cdl: CountDownLatch): Unit = {
    cdl.await()
    Boom.t.##
    sys.error("Expected exception, but nothing was thrown")
  }

  def checkException(thread: Task, ex: Throwable) = ex match {
    case _: Boom                  => // ok
    case ex: NoClassDefFoundError =>
      ex.getCause() match {
        case _: ExceptionInInitializerError => // ok
        case ex => sys.error(s"Unexpected couse in $thread: $ex")
      }
    case ex => sys.error(s"Unexpected error in $thread: $ex")
  }

  class Task(task: Runnable) extends Thread {
    var exception: Throwable = _
    override def run(): Unit =
      try task.run()
      catch {
        case ex: Throwable =>
          exception = ex
          throw ex
      }
  }

  def main(args: Array[String]): Unit = {
    val cdl = new CountDownLatch(1)
    val t1 = new Task(() => doWork(cdl))
    val t2 = new Task(() => doWork(cdl))
    t1.start()
    t2.start()
    Thread.sleep(500)
    cdl.countDown()

    t1.join(1000)
    assert(
      !t1.isAlive(),
      s"thread1: ${t1} is still alive (state: ${t1.getState()})"
    )
    checkException(t1, t1.exception)

    t2.join(1000)
    assert(
      !t2.isAlive(),
      s"thread2: ${t2} is still alive (state: ${t2.getState()})"
    )
    checkException(t2, t2.exception)
  }
}
