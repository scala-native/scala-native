import scala.scalanative.runtime.{Intrinsics, fromRawPtr, toRawPtr}
import scala.scalanative.unsafe._

object ExternalLifecycleChecks {
  private val local = new ThreadLocal[Array[Byte]]()
  private var identity: java.lang.ref.WeakReference[Thread] = null
  private var payload: java.lang.ref.WeakReference[Array[Byte]] = null

  @exported("external_test_track_identity")
  def trackIdentity(): Unit = {
    identity = new java.lang.ref.WeakReference(Thread.currentThread())
    val value = new Array[Byte](65536)
    local.set(value)
    payload = new java.lang.ref.WeakReference(value)
  }

  @exported("external_test_check_reclaimed")
  def checkReclaimed(checkIdentity: Int): Unit = {
    var remaining = 8
    while (remaining > 0) {
      System.gc()
      remaining -= 1
    }
    if (payload.get() != null) {
      println(
        "reclamation failure: released identity retained ThreadLocal payload"
      )
      throw new AssertionError("released identity retained ThreadLocal payload")
    }
    if (checkIdentity != 0 && identity.get() != null) {
      println("reclamation failure: released external identity remains reachable")
      throw new AssertionError("released external identity remains reachable")
    }
  }
}
