import scala.scalanative.runtime.{Intrinsics, fromRawPtr, toRawPtr}
import scala.scalanative.unsafe._

object ExternalCallbacks {
  private val local = new ThreadLocal[java.lang.Long]()
  private object Module { val value = new String("external-thread-module") }

  @exported("external_identity")
  def identity(): Long = {
    assert(Module.value.length == 22)
    assert(Thread.currentThread().getThreadGroup() != null)
    Thread.currentThread().getId()
  }

  @exported("external_local_set")
  def localSet(value: Long): Unit = local.set(java.lang.Long.valueOf(value))

  @exported("external_local_get")
  def localGet(): Long = {
    val value = local.get()
    if (value == null) -1L else value.longValue()
  }

  @exported("external_collect")
  def collect(): Unit = System.gc()

  private var lastThrown: java.lang.ref.WeakReference[Throwable] = null

  @exported("external_throw")
  def throwFromCallback(): Unit = {
    val exception = new IllegalStateException("external callback unwind")
    lastThrown = new java.lang.ref.WeakReference(exception)
    throw exception
  }

  @exported("external_exception_check")
  def checkException(): Unit = {
    val exception = lastThrown.get()
    assert(exception != null, "native exception lost its Scala object")
    assert(exception.getMessage() == "external callback unwind")
  }

  @exported("external_exception_reclaimed")
  def checkExceptionReclaimed(): Unit = {
    var remaining = 8
    while (remaining > 0) {
      System.gc()
      remaining -= 1
    }
    assert(
      lastThrown.get() == null,
      "released native exception remains reachable"
    )
  }

  @exported("external_nested_throw")
  def nestedThrow(): Unit = {
    val before = Thread.currentThread()
    var caught = false
    try
      NativeBridge.blockingVoidCall(
        CFuncPtr0.fromScalaFunction(() => throwFromCallback())
      )
    catch {
      case exception: IllegalStateException =>
        assert(exception.getMessage() == "external callback unwind")
        assert(Thread.currentThread() eq before)
        pressure(4, 65536)
        caught = true
    }
    assert(caught)
  }

  @exported("external_scala_worker_identity")
  def scalaWorkerIdentity(): Unit =
    if (scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled) {
      val succeeded = new java.util.concurrent.atomic.AtomicBoolean(false)
      val worker = new Thread(new Runnable {
        def run(): Unit = {
          val before = Thread.currentThread()
          local.set(java.lang.Long.valueOf(97))
          assert(
            NativeBridge.blockingCall(
              CFuncPtr0.fromScalaFunction(() => identity())
            ) == before.getId()
          )
          assert(Thread.currentThread() eq before)
          assert(local.get().longValue() == 97)
          succeeded.set(true)
        }
      })
      worker.start()
      worker.join()
      assert(succeeded.get(), "Scala-created worker callback failed")
    }

  @noinline
  private def readByte(array: Array[Byte], index: Int): Byte = array(index)

  @noinline
  private def readObject(array: Array[AnyRef], index: Int): AnyRef = array(
    index
  )

  @noinline
  private def pressure(iterations: Int, bytes: Int): Long = {
    val small = new Array[Byte](31)
    val large = new Array[Byte](bytes max 65536)
    val objects = Array[AnyRef](new Object(), new String("retained"))
    small(30) = 7
    large(large.length - 1) = 11
    var i = 0
    while (i < iterations) {
      val garbage = new Array[Byte](bytes max 1024)
      garbage(0) = i.toByte
      System.gc()
      assert(readByte(small, 30) == 7, "small array changed after collection")
      assert(
        readByte(large, large.length - 1) == 11,
        "large array changed after collection"
      )
      assert(
        readObject(objects, 1).toString == "retained",
        "object array changed after collection"
      )
      i += 1
    }
    small(30).toLong + large(large.length - 1)
  }

  @exported("external_pressure")
  def allocate(iterations: Int, bytes: Int): Long = pressure(iterations, bytes)

  @exported("external_object_new")
  def newObject(root: Ptr[Ptr[Byte]]): Unit = {
    val array = new Array[Byte](65536)
    array(65535) = 42
    !root = fromRawPtr[Byte](Intrinsics.castObjectToRawPtr(array))
  }

  @exported("external_object_check")
  def checkObject(ptr: Ptr[Byte]): Int =
    Intrinsics
      .castRawPtrToObject(toRawPtr(ptr))
      .asInstanceOf[Array[Byte]](65535)
      .toInt

  @exported("external_function_pointer")
  def functionPointer(): Ptr[Byte] = CFuncPtr
    .toPtr(
      CFuncPtr0.fromScalaFunction(() => identity())
    )
    .asInstanceOf[Ptr[Byte]]

  @exported("external_nested")
  def nested(fn: CFuncPtr0[Long]): Long = {
    val before = Thread.currentThread()
    val value = NativeBridge.blockingCall(fn)
    assert(Thread.currentThread() eq before)
    value
  }

  @exported("external_nested_plain")
  def nestedPlain(fn: CFuncPtr0[Long]): Long = NativeBridge.plainCall(fn)

  private var activeEntries = 0
  @exported("external_exclusive")
  def exclusive(): Int = {
    activeEntries += 1
    assert(activeEntries == 1)
    pressure(2, 65536)
    assert(activeEntries == 1)
    activeEntries -= 1
    1
  }

  @exportAccessors("external_get_value", "external_set_value")
  var value: Long = 0L
}

@extern
object NativeBridge {
  @blocking
  @name("external_bridge_blocking_void")
  def blockingVoidCall(fn: CFuncPtr0[Unit]): Unit = extern
  @blocking
  @name("external_bridge_blocking")
  def blockingCall(fn: CFuncPtr0[Long]): Long = extern
  @name("external_bridge_plain")
  def plainCall(fn: CFuncPtr0[Long]): Long = extern
}
