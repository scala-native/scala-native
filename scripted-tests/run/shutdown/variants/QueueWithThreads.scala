import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.CountDownLatch
import scala.scalanative.runtime.NativeExecutionContext

object Test {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
    def spawnRunnable(name: String)(fn: => Unit) =
      NativeExecutionContext.queue
        .execute(() => { fn; println(s"task $name done") })

    def spawnThread(name: String)(fn: => Unit) = {
      val t = new Thread(() => { fn; println(s"thread $name done") })
      t.setName(name)
      t.start()
    }

    spawnThread("T1") {
      val latch1 = new CountDownLatch(1)
      spawnRunnable("R1") { latch1.countDown() }
      spawnThread("T2") {
        latch1.await() // blocks until T1, R1 are done
        val latch2 = new CountDownLatch(1)
        val latch3 = new CountDownLatch(3)
        spawnThread("T3") {
          spawnRunnable("R2") { latch2.await(); latch3.countDown() }
        }
        spawnThread("T4") {
          spawnRunnable("R3") { latch2.await(); latch3.countDown() }
        }
        spawnRunnable("R4") { latch2.await(); latch3.countDown() }
        latch2.countDown()
        latch3.await()
      }
    }
  }
}
