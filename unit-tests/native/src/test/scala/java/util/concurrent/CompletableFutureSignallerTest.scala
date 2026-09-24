package java.util.concurrent

import org.junit.Assert._
import org.junit.Test

class CompletableFutureSignallerTest {

  @Test def untimedSignallerIsNotReleasableUntilThreadCleared(): Unit = {
    val signaller = new CompletableFuture.Signaller(
      /* interruptible = */ false,
      /* nanos = */ 0L,
      /* deadline = */ 0L
    )
    assertFalse(
      "an untimed, non-interrupted, still-owned Signaller must not be releasable",
      signaller.isReleasable()
    )

    // simulates what tryFire() does when the future actually completes
    signaller.thread = null
    assertTrue(signaller.isReleasable())
  }

  @Test def timedSignallerIsNotReleasableBeforeDeadline(): Unit = {
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30)
    val signaller = new CompletableFuture.Signaller(
      /* interruptible = */ false,
      /* nanos = */ deadline - System.nanoTime(),
      /* deadline = */ deadline
    )
    assertFalse(
      "a timed Signaller must not be releasable before its deadline",
      signaller.isReleasable()
    )
  }

  @Test def timedSignallerIsReleasableAfterDeadline(): Unit = {
    val deadline = System.nanoTime() - 1L // already in the past
    val signaller = new CompletableFuture.Signaller(
      /* interruptible = */ false,
      /* nanos = */ 1L,
      /* deadline = */ deadline
    )
    assertTrue(
      "a timed Signaller must be releasable once its deadline has passed",
      signaller.isReleasable()
    )
  }

  @Test def interruptibleSignallerIsReleasableOnceInterrupted(): Unit = {
    val signaller = new CompletableFuture.Signaller(
      /* interruptible = */ true,
      /* nanos = */ 0L,
      /* deadline = */ 0L
    )
    assertFalse(signaller.isReleasable())

    Thread.currentThread().interrupt()
    try
      assertTrue(
        "an interruptible Signaller must be releasable once its thread is interrupted",
        signaller.isReleasable()
      )
    finally Thread.interrupted() // clear the interrupt flag we just set
  }
}
