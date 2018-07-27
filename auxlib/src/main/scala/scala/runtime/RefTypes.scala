package scala.runtime

import java.io.Serializable

@inline
class BooleanRef(var elem: Boolean) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object BooleanRef {
  @inline def create(elem: Boolean): BooleanRef = new BooleanRef(elem)
  @inline def zero(): BooleanRef                = new BooleanRef(false)
}

@inline
class VolatileBooleanRef(var elem: Boolean) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object VolatileBooleanRef {
  @inline def create(elem: Boolean): VolatileBooleanRef =
    new VolatileBooleanRef(elem)
  @inline def zero(): VolatileBooleanRef = new VolatileBooleanRef(false)
}

@inline
class CharRef(var elem: Char) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object CharRef {
  @inline def create(elem: Char): CharRef = new CharRef(elem)
  @inline def zero(): CharRef             = new CharRef(0.toChar)
}

@inline
class VolatileCharRef(var elem: Char) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object VolatileCharRef {
  @inline def create(elem: Char): VolatileCharRef = new VolatileCharRef(elem)
  @inline def zero(): VolatileCharRef             = new VolatileCharRef(0.toChar)
}

@inline
class ByteRef(var elem: Byte) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object ByteRef {
  @inline def create(elem: Byte): ByteRef = new ByteRef(elem)
  @inline def zero(): ByteRef             = new ByteRef(0)
}

@inline
class VolatileByteRef(var elem: Byte) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object VolatileByteRef {
  @inline def create(elem: Byte): VolatileByteRef = new VolatileByteRef(elem)
  @inline def zero(): VolatileByteRef             = new VolatileByteRef(0)
}

@inline
class ShortRef(var elem: Short) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object ShortRef {
  @inline def create(elem: Short): ShortRef = new ShortRef(elem)
  @inline def zero(): ShortRef              = new ShortRef(0)
}

@inline
class VolatileShortRef(var elem: Short) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object VolatileShortRef {
  @inline def create(elem: Short): VolatileShortRef = new VolatileShortRef(elem)
  @inline def zero(): VolatileShortRef              = new VolatileShortRef(0)
}

@inline
class IntRef(var elem: Int) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object IntRef {
  @inline def create(elem: Int): IntRef = new IntRef(elem)
  @inline def zero(): IntRef            = new IntRef(0)
}

@inline
class VolatileIntRef(var elem: Int) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object VolatileIntRef {
  @inline def create(elem: Int): VolatileIntRef = new VolatileIntRef(elem)
  @inline def zero(): VolatileIntRef            = new VolatileIntRef(0)
}

@inline
class LongRef(var elem: Long) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object LongRef {
  @inline def create(elem: Long): LongRef = new LongRef(elem)
  @inline def zero(): LongRef             = new LongRef(0)
}

@inline
class VolatileLongRef(var elem: Long) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object VolatileLongRef {
  @inline def create(elem: Long): VolatileLongRef = new VolatileLongRef(elem)
  @inline def zero(): VolatileLongRef             = new VolatileLongRef(0)
}

@inline
class FloatRef(var elem: Float) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object FloatRef {
  @inline def create(elem: Float): FloatRef = new FloatRef(elem)
  @inline def zero(): FloatRef              = new FloatRef(0)
}

@inline
class VolatileFloatRef(var elem: Float) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object VolatileFloatRef {
  @inline def create(elem: Float): VolatileFloatRef = new VolatileFloatRef(elem)
  @inline def zero(): VolatileFloatRef              = new VolatileFloatRef(0)
}

@inline
class DoubleRef(var elem: Double) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object DoubleRef {
  @inline def create(elem: Double): DoubleRef = new DoubleRef(elem)
  @inline def zero(): DoubleRef               = new DoubleRef(0)
}

@inline
class VolatileDoubleRef(var elem: Double) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object VolatileDoubleRef {
  @inline def create(elem: Double): VolatileDoubleRef =
    new VolatileDoubleRef(elem)
  @inline def zero(): VolatileDoubleRef = new VolatileDoubleRef(0)
}

@inline
class ObjectRef[A](var elem: A) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object ObjectRef {
  @inline def create[A](elem: A): ObjectRef[A] = new ObjectRef(elem)
  @inline def zero(): ObjectRef[Object]        = new ObjectRef(null)
}

@inline
class VolatileObjectRef[A](var elem: A) extends Serializable {
  override def toString() = String.valueOf(elem)
}
object VolatileObjectRef {
  @inline def create[A](elem: A): VolatileObjectRef[A] =
    new VolatileObjectRef(elem)
  @inline def zero(): VolatileObjectRef[Object] = new VolatileObjectRef(null)
}
