import scala.scalanative.runtime.{Intrinsics, fromRawPtr, toRawPtr}
import scala.scalanative.unsafe._

object ExternalCompilerEntry {
  @exported("external_boxed_function_pointer")
  def functionPointer(): Ptr[Byte] = CFuncPtr
    .toPtr(
      CFuncPtr1.fromScalaFunction((value: Int) => {
        assert(Thread.currentThread().getThreadGroup() != null)
        val data = new Array[Byte](65536)
        data(65535) = 19
        System.gc()
        assert(data(65535) == 19)
        if (value < 0)
          throw new IllegalArgumentException("boxed callback unwind")
        value + 1
      })
    )
    .asInstanceOf[Ptr[Byte]]
}

object ExternalColdAccessors {
  // The first accessor call must enter before loading this module.
  @exportAccessors("external_cold_get", "external_cold_set")
  var value: Int = {
    assert(Thread.currentThread().getThreadGroup() != null)
    val data = new Array[Byte](65536)
    data(65535) = 41
    System.gc()
    data(65535).toInt
  }
}
