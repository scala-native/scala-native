package java.util.zip

trait Checksum {
  def getValue(): Long
  def reset(): Unit
  def update(v: Int)
  def update(buf: Array[Byte]): Unit
  def update(buf: Array[Byte], off: Int, nbytes: Int): Unit
}
