package scala.scalanative.runtime

import scala.scalanative.unsafe._

object ExternalThreadBootstrap {
  @exported("external_bootstrap_initialize")
  def initialize(): Unit = initLibrary()

  @noinline private def readByte(array: scala.Array[Byte], index: Int): Byte =
    array(
      index
    )
  // Exercises allocation and collection on a mutator admitted by the C host.
  @exported("mutator_lifecycle_pressure")
  def pressure(): Int = {
    var checksum = 0
    var i = 0
    while (i < 4) {
      val bytes = new scala.Array[Byte](65536)
      bytes(0) = (i + 1).toByte
      bytes(bytes.length - 1) = (i + 2).toByte
      System.gc()
      checksum += readByte(bytes, 0) + readByte(bytes, bytes.length - 1)
      i += 1
    }
    checksum
  }
}
