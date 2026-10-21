import scala.scalanative.unsafe._

object ExternalVirtualCallbacks {
  private val local = new ThreadLocal[java.lang.Long]()

  private def callback(): Long = {
    val logical = Thread.currentThread()
    assert(logical.isVirtual())
    assert(logical.getThreadGroup().getName() == "VirtualThreads")
    val data = new Array[Byte](65536)
    data(65535) = 29
    System.gc()
    assert(data(65535) == 29)
    assert(Thread.currentThread() eq logical)
    assert(local.get() != null)
    logical.getId()
  }

  private def nestedCallback(): Long = {
    val logical = Thread.currentThread()
    assert(
      NativeBridge.plainCall(
        CFuncPtr0.fromScalaFunction(() => callback())
      ) == logical.getId()
    )
    assert(
      NativeBridge.blockingCall(
        CFuncPtr0.fromScalaFunction(() => callback())
      ) == logical.getId()
    )
    ExternalCallbacks.nestedThrow()
    assert(Thread.currentThread() eq logical)
    logical.getId()
  }

  @exported("external_virtual_worker_identity")
  def workers(iterations: Int, count: Int): Unit = {
    assert(scala.scalanative.meta.LinktimeInfo.isVirtualThreadsSupported)
    val host = Thread.currentThread()
    val failure = new java.util.concurrent.atomic.AtomicReference[Throwable]()
    val ready = new java.util.concurrent.CountDownLatch(count)
    val start = new java.util.concurrent.CountDownLatch(1)
    val threads = Array.tabulate(count) { index =>
      Thread
        .ofVirtual()
        .unstarted(new Runnable {
          def run(): Unit = try {
            val logical = Thread.currentThread()
            assert(
              local.get() == null,
              "virtual worker inherited another worker's local"
            )
            local.set(java.lang.Long.valueOf(index + 101L))
            ready.countDown()
            start.await()
            var iteration = 0
            while (iteration < iterations) {
              assert(
                NativeBridge.blockingCall(
                  CFuncPtr0.fromScalaFunction(() => nestedCallback())
                ) == logical.getId()
              )
              assert(Thread.currentThread() eq logical)
              assert(local.get().longValue() == index + 101L)
              // Yield only after all native callback scopes have returned.
              Thread.`yield`()
              assert(Thread.currentThread() eq logical)
              assert(local.get().longValue() == index + 101L)
              iteration += 1
            }
          } catch {
            case error: Throwable => failure.compareAndSet(null, error)
          }
        })
    }
    threads.foreach(_.start())
    try
      assert(
        ready.await(10L, java.util.concurrent.TimeUnit.SECONDS),
        "virtual workers did not reach the start barrier"
      )
    finally start.countDown()
    threads.foreach { thread =>
      thread.join(10000L)
      assert(!thread.isAlive(), "virtual worker did not terminate")
      assert(thread.getState() == Thread.State.TERMINATED)
    }
    val error = failure.get()
    if (error != null) throw error
    assert(Thread.currentThread() eq host)
  }
}
