package java.nio

import java.nio.channels.{FileChannel, NonWritableChannelException}

abstract class MappedByteBuffer private[nio] (
    mode: FileChannel.MapMode,
    _capacity: Int,
    _array: GenArray[Byte],
    _arrayOffset: Int
) extends HeapByteBuffer(
      _capacity,
      _array,
      _arrayOffset,
      _arrayOffset,
      _capacity,
      mode == FileChannel.MapMode.READ_ONLY
    ) {

  def force(): MappedByteBuffer

  def isLoaded(): Boolean

  def load(): MappedByteBuffer

}
