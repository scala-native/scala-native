package java.nio

abstract class MappedByteBuffer(_capacity: Int,
                                _array: Array[Byte],
                                _arrayOffset: Int)
    extends ByteBuffer(_capacity, _array, _arrayOffset) {}
