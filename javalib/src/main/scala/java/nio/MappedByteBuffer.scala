package java.nio

import java.nio.channels.FileChannel
import scala.scalanative.windows.HandleApi._

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

}
