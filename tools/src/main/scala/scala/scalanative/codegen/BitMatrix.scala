package scala.scalanative

private[scalanative] class BitMatrix private (
    bits: Array[Long],
    columns: Int
) {
  import BitMatrix.{AddressBitsPerWord, ElementSize, RightBits}

  def set(row: Int, col: Int): Unit = {
    val bitIndex = row * columns + col
    val len = (bitIndex >> AddressBitsPerWord) + 1
    bits(len - 1) |= 1L << (bitIndex & RightBits)
  }

  def toSeq = bits.toSeq
}
private[scalanative] object BitMatrix {
  private[scalanative] final val AddressBitsPerWord = 6 // Int Based 2^6 = 64
  private[scalanative] final val ElementSize = 1 << AddressBitsPerWord
  private[scalanative] final val RightBits = ElementSize - 1

  def apply(rows: Int, columns: Int): BitMatrix = {
    val nbits = rows * columns
    val length = (nbits + RightBits) >> AddressBitsPerWord
    new BitMatrix(new Array[Long](length), columns)
  }
}
