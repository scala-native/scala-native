package java.nio

abstract class MappedByteBuffer private[nio] (
    _capacity: Int,
    private[nio] override val _mappedData: MappedByteBufferData,
    _arrayOffset: Int,
    initialPosition: Int,
    initialLimit: Int,
    isReadOnly: Boolean
) extends ByteBuffer(_capacity, null, _mappedData, _arrayOffset) {

  def force(): MappedByteBuffer

  def isLoaded(): Boolean

  def load(): MappedByteBuffer

  @inline override def position(newPosition: Int): MappedByteBuffer = {
    super.position(newPosition)
    this
  }

  @inline override def limit(newLimit: Int): MappedByteBuffer = {
    super.limit(newLimit)
    this
  }

  @inline override def mark(): MappedByteBuffer = {
    super.mark()
    this
  }

  @inline override def reset(): MappedByteBuffer = {
    super.reset()
    this
  }

  @inline override def clear(): MappedByteBuffer = {
    super.clear()
    this
  }

  @inline override def flip(): MappedByteBuffer = {
    super.flip()
    this
  }

  @inline override def rewind(): MappedByteBuffer = {
    super.rewind()
    this
  }

}
