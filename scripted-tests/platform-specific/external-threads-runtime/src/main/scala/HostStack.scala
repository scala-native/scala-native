import scala.scalanative.runtime.{Intrinsics, fromRawPtr, toRawPtr}
import scala.scalanative.unsafe._

object ExternalAlternateStack {
  @noinline
  private def readByte(array: Array[Byte], index: Int): Byte = array(index)
  @exported("external_altstack_race")
  def alternateStackRace(ready: CFuncPtr0[Unit], done: CFuncPtr0[Int]): Unit = {
    val retained = new Array[Byte](65536)
    retained(65535) = 42
    ready()
    while (done() == 0) {
      val garbage = new Array[Byte](4096)
      garbage(0) = 1
      assert(readByte(retained, 65535) == 42)
    }
    assert(readByte(retained, 65535) == 42)
  }

}
