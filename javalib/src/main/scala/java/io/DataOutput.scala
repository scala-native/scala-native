package java.io

trait DataOutput {
  def write(b: Array[Byte]): Unit
  def write(b: Array[Byte], off: Int, len: Int): Unit
  def write(b: Int): Unit
  def writeBoolean(v: Boolean): Unit
  def writeByte(v: Int): Unit
  def writeBytes(s: String): Unit
  def writeChar(v: Int): Unit
  def writeChars(s: String): Unit
  def writeDouble(v: Double): Unit
  def writeFloat(v: Float): Unit
  def writeInt(v: Int): Unit
  def writeLong(v: Long): Unit
  def writeShort(v: Int): Unit
  def writeUTF(s: String): Unit
}
