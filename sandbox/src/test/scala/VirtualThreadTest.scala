import org.junit.Test
import org.junit.Assert._
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.TimeUnit

class VirtualThreadTest {
  @Test def sleep(): Unit = {
    val start = System.nanoTime()
    val sleepTimeMs = 1000
    val vThread = Thread
      .ofVirtual()
      .start(() => {
        Thread.sleep(sleepTimeMs) // Sleep for 1 second
      })
    vThread.join()
    val duration = (System.nanoTime() - start) / 1e6
    assert(duration >= sleepTimeMs, "Thread did not sleep properly")
  }

  @Test def timedParking(): Unit = {
    val start = System.nanoTime()
    val sleepTimeMs = 1000
    val vThread = Thread
      .ofVirtual()
      .start(() => {
        LockSupport.parkNanos(
          TimeUnit.MILLISECONDS.toNanos(sleepTimeMs)
        ) // Sleep for 1 second
      })
    vThread.join()
    val duration = (System.nanoTime() - start) / 1e6

    assert(duration >= sleepTimeMs, "Thread did not sleep properly")
  }

  @Test def timedParkingUntil(): Unit = {
    val start = System.nanoTime()
    val deadline = System.currentTimeMillis() + 100
    val vThread = Thread
      .ofVirtual()
      .start(() => LockSupport.parkUntil(deadline))
    vThread.join()
    
    assert(System.currentTimeMillis() >= deadline, "Thread did not sleep properly")
  }

  @Test def parking(): Unit = {
    val start = System.nanoTime()
    val vThread = Thread
      .ofVirtual()
      .start(() => LockSupport.park())
    Thread.sleep(100)
    assert(vThread.getState() == Thread.State.WAITING)
    LockSupport.unpark(vThread)
    vThread.join(100)
    assert(vThread.getState() == Thread.State.TERMINATED)
  }
}
