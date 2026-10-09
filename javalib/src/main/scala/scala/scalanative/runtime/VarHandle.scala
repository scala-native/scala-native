package scala.scalanative.runtime

import scala.annotation.switch

import scala.scalanative.libc.stdatomic._
import scala.scalanative.libc.stdatomic.memory_order._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

// scalafmt: { maxColumn = 160}

/** Runtime half of the compiler-only VarHandle implementation.
 *
 *  Lookup is resolved by the compiler plugin. The resulting handle contains a direct field-address binding and implements the internal access protocol.
 */
object VarHandle {
  import NativeVarHandle.{AccessOperation, BitwiseOperation, MemoryOrder}
  import MemoryOrder._

  // Shared dispatch and memory ordering
  private abstract class Handle(instanceBinding: AnyRef => Ptr[Byte], staticBinding: () => Ptr[Byte], protected val variableType: Class[_])
      extends java.lang.invoke.VarHandle
      with VarHandleAdaptation {
    protected def boxedVariableType: Class[_] = variableType

    override def getBoxedReference(receiver: AnyRef, resultType: Class[_], mode: MemoryOrder): AnyRef = {
      if (variableType.isPrimitive && !resultType.isAssignableFrom(boxedVariableType))
        signatureMismatch(s"get returning ${resultType.getName}")
      getReference(receiver, mode)
    }

    override protected def signatureMismatch(requested: String): Nothing = {
      val operation =
        if (requested.startsWith("VarHandle.")) requested
        else s"VarHandle.$requested"
      val name = variableType.getName
      val fieldType =
        if (variableType.isPrimitive) name.head.toUpper.toString + name.tail
        else name
      throw new java.lang.invoke.WrongMethodTypeException(s"cannot perform $operation on a field of type $fieldType; unsupported type conversion")
    }
    protected def pointer(receiver: AnyRef): RawPtr =
      if (staticBinding == null) toRawPtr(instanceBinding(receiver))
      else toRawPtr(staticBinding())
  }
  private def order(mode: MemoryOrder): memory_order = (mode: @switch) match {
    case Plain    => memory_order_relaxed
    case Volatile => memory_order_seq_cst
    case Acquire  => memory_order_acquire
    case Release  => memory_order_release
    case _        => memory_order_relaxed
  }

  private def failureOrder(mode: MemoryOrder): memory_order = (mode: @switch) match {
    case Plain    => memory_order_relaxed
    case Volatile => memory_order_seq_cst
    case Acquire  => memory_order_acquire
    case Release  => memory_order_relaxed
    case _        => memory_order_relaxed
  }

  // Boolean fields
  private final class BooleanHandle(instance: AnyRef => Ptr[Byte], static: () => Ptr[Byte]) extends Handle(instance, static, classOf[Boolean]) {
    @scala.scalanative.annotation.alwaysinline
    override protected def invokeAdaptedOperation(
        operation: AccessOperation,
        receiver: AnyRef,
        expected: AnyRef,
        value: AnyRef,
        resultType: Class[_],
        mode: MemoryOrder,
        bitwiseOperation: BitwiseOperation
    ): AnyRef = invokeBoolean(operation, receiver, expected, value, resultType, mode, bitwiseOperation)
    override protected def boxedVariableType: Class[_] = classOf[java.lang.Boolean]
    override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToBoolean(getBoolean(receiver, mode))
    override def addBoolean(receiver: AnyRef, value: Boolean, mode: MemoryOrder): Boolean = throw new UnsupportedOperationException(
      "getAndAdd is unsupported for boolean VarHandles"
    )

    private def atomic(receiver: AnyRef) = fromRawPtr[Boolean](pointer(receiver)).atomic
    override def getBoolean(receiver: AnyRef, mode: MemoryOrder): Boolean = atomic(receiver).load(order(mode))
    override def setBoolean(receiver: AnyRef, value: Boolean, mode: MemoryOrder): Unit = atomic(receiver).store(value, order(mode))
    override def compareBoolean(receiver: AnyRef, expected: Boolean, desired: Boolean, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeStrong(expected, desired, order(mode), failureOrder(mode))
    override def weakCompareBoolean(receiver: AnyRef, expected: Boolean, desired: Boolean, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeWeak(expected, desired, order(mode), failureOrder(mode))
    override def compareExchangeBoolean(receiver: AnyRef, expected: Boolean, desired: Boolean, mode: MemoryOrder): Boolean = {
      val witness = stackalloc[Boolean]()
      !witness = expected
      atomic(receiver).compareExchangeStrong(witness, desired, order(mode), failureOrder(mode))
      !witness
    }
    override def exchangeBoolean(receiver: AnyRef, value: Boolean, mode: MemoryOrder): Boolean = atomic(receiver).exchange(value, order(mode))
    override def bitwiseBoolean(receiver: AnyRef, value: Boolean, operation: BitwiseOperation, mode: MemoryOrder): Boolean = {
      val a = atomic(receiver)
      val v = value
      val result = (operation: @switch) match {
        case BitwiseOperation.Or  => a.fetchOr(v, order(mode))
        case BitwiseOperation.And => a.fetchAnd(v, order(mode))
        case BitwiseOperation.Xor => a.fetchXor(v, order(mode))
        case _                    =>
          throw new IllegalArgumentException("invalid VarHandle bitwise operation")
      }
      result
    }
  }

  // Byte fields
  private final class ByteHandle(instance: AnyRef => Ptr[Byte], static: () => Ptr[Byte]) extends Handle(instance, static, classOf[Byte]) {
    @scala.scalanative.annotation.alwaysinline
    override protected def invokeAdaptedOperation(
        operation: AccessOperation,
        receiver: AnyRef,
        expected: AnyRef,
        value: AnyRef,
        resultType: Class[_],
        mode: MemoryOrder,
        bitwiseOperation: BitwiseOperation
    ): AnyRef = invokeByte(operation, receiver, expected, value, resultType, mode, bitwiseOperation)
    override protected def boxedVariableType: Class[_] = classOf[java.lang.Byte]
    override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToByte(getByte(receiver, mode))
    override def getShort(receiver: AnyRef, mode: MemoryOrder): Short = getByte(receiver, mode).toShort
    override def getInt(receiver: AnyRef, mode: MemoryOrder): Int = getByte(receiver, mode).toInt
    override def getLong(receiver: AnyRef, mode: MemoryOrder): Long = getByte(receiver, mode).toLong
    override def getFloat(receiver: AnyRef, mode: MemoryOrder): Float = getByte(receiver, mode).toFloat
    override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = getByte(receiver, mode).toDouble
    private def atomic(receiver: AnyRef) = fromRawPtr[Byte](pointer(receiver)).atomic
    override def getByte(receiver: AnyRef, mode: MemoryOrder): Byte = atomic(receiver).load(order(mode))
    override def setByte(receiver: AnyRef, value: Byte, mode: MemoryOrder): Unit = atomic(receiver).store(value, order(mode))
    override def compareByte(receiver: AnyRef, expected: Byte, desired: Byte, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeStrong(expected, desired, order(mode), failureOrder(mode))
    override def weakCompareByte(receiver: AnyRef, expected: Byte, desired: Byte, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeWeak(expected, desired, order(mode), failureOrder(mode))
    override def compareExchangeByte(receiver: AnyRef, expected: Byte, desired: Byte, mode: MemoryOrder): Byte = {
      val witness = stackalloc[Byte]()
      !witness = expected
      atomic(receiver).compareExchangeStrong(witness, desired, order(mode), failureOrder(mode))
      !witness
    }
    override def exchangeByte(receiver: AnyRef, value: Byte, mode: MemoryOrder): Byte = atomic(receiver).exchange(value, order(mode))
    override def addByte(receiver: AnyRef, value: Byte, mode: MemoryOrder): Byte = atomic(receiver).fetchAdd(value, order(mode))
    override def bitwiseByte(receiver: AnyRef, value: Byte, operation: BitwiseOperation, mode: MemoryOrder): Byte = {
      val a = atomic(receiver)
      val v = value
      val result = (operation: @switch) match {
        case BitwiseOperation.Or  => a.fetchOr(v, order(mode))
        case BitwiseOperation.And => a.fetchAnd(v, order(mode))
        case BitwiseOperation.Xor => a.fetchXor(v, order(mode))
        case _                    =>
          throw new IllegalArgumentException("invalid VarHandle bitwise operation")
      }
      result
    }
  }

  // Short fields
  private final class ShortHandle(instance: AnyRef => Ptr[Byte], static: () => Ptr[Byte]) extends Handle(instance, static, classOf[Short]) {
    @scala.scalanative.annotation.alwaysinline
    override protected def invokeAdaptedOperation(
        operation: AccessOperation,
        receiver: AnyRef,
        expected: AnyRef,
        value: AnyRef,
        resultType: Class[_],
        mode: MemoryOrder,
        bitwiseOperation: BitwiseOperation
    ): AnyRef = invokeShort(operation, receiver, expected, value, resultType, mode, bitwiseOperation)
    override protected def boxedVariableType: Class[_] = classOf[java.lang.Short]
    override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToShort(getShort(receiver, mode))
    override def getInt(receiver: AnyRef, mode: MemoryOrder): Int = getShort(receiver, mode).toInt
    override def getLong(receiver: AnyRef, mode: MemoryOrder): Long = getShort(receiver, mode).toLong
    override def getFloat(receiver: AnyRef, mode: MemoryOrder): Float = getShort(receiver, mode).toFloat
    override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = getShort(receiver, mode).toDouble
    private def atomic(receiver: AnyRef) = fromRawPtr[Short](pointer(receiver)).atomic
    override def getShort(receiver: AnyRef, mode: MemoryOrder): Short = atomic(receiver).load(order(mode))
    override def setShort(receiver: AnyRef, value: Short, mode: MemoryOrder): Unit = atomic(receiver).store(value, order(mode))
    override def compareShort(receiver: AnyRef, expected: Short, desired: Short, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeStrong(expected, desired, order(mode), failureOrder(mode))
    override def weakCompareShort(receiver: AnyRef, expected: Short, desired: Short, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeWeak(expected, desired, order(mode), failureOrder(mode))
    override def compareExchangeShort(receiver: AnyRef, expected: Short, desired: Short, mode: MemoryOrder): Short = {
      val witness = stackalloc[Short]()
      !witness = expected
      atomic(receiver).compareExchangeStrong(witness, desired, order(mode), failureOrder(mode))
      !witness
    }
    override def exchangeShort(receiver: AnyRef, value: Short, mode: MemoryOrder): Short = atomic(receiver).exchange(value, order(mode))
    override def addShort(receiver: AnyRef, value: Short, mode: MemoryOrder): Short = atomic(receiver).fetchAdd(value, order(mode))
    override def bitwiseShort(receiver: AnyRef, value: Short, operation: BitwiseOperation, mode: MemoryOrder): Short = {
      val a = atomic(receiver)
      val v = value
      val result = (operation: @switch) match {
        case BitwiseOperation.Or  => a.fetchOr(v, order(mode))
        case BitwiseOperation.And => a.fetchAnd(v, order(mode))
        case BitwiseOperation.Xor => a.fetchXor(v, order(mode))
        case _                    =>
          throw new IllegalArgumentException("invalid VarHandle bitwise operation")
      }
      result
    }
  }

  // Char fields
  private final class CharHandle(instance: AnyRef => Ptr[Byte], static: () => Ptr[Byte]) extends Handle(instance, static, classOf[Char]) {
    @scala.scalanative.annotation.alwaysinline
    override protected def invokeAdaptedOperation(
        operation: AccessOperation,
        receiver: AnyRef,
        expected: AnyRef,
        value: AnyRef,
        resultType: Class[_],
        mode: MemoryOrder,
        bitwiseOperation: BitwiseOperation
    ): AnyRef = invokeChar(operation, receiver, expected, value, resultType, mode, bitwiseOperation)
    override protected def boxedVariableType: Class[_] = classOf[java.lang.Character]
    override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToCharacter(getChar(receiver, mode))
    override def getInt(receiver: AnyRef, mode: MemoryOrder): Int = getChar(receiver, mode).toInt
    override def getLong(receiver: AnyRef, mode: MemoryOrder): Long = getChar(receiver, mode).toLong
    override def getFloat(receiver: AnyRef, mode: MemoryOrder): Float = getChar(receiver, mode).toFloat
    override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = getChar(receiver, mode).toDouble
    private def atomic(receiver: AnyRef) = fromRawPtr[UShort](pointer(receiver)).atomic
    override def getChar(receiver: AnyRef, mode: MemoryOrder): Char = (atomic(receiver).load(order(mode))).toInt.toChar
    override def setChar(receiver: AnyRef, value: Char, mode: MemoryOrder): Unit = atomic(receiver).store((value).toInt.toUShort, order(mode))
    override def compareChar(receiver: AnyRef, expected: Char, desired: Char, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeStrong((expected).toInt.toUShort, (desired).toInt.toUShort, order(mode), failureOrder(mode))
    override def weakCompareChar(receiver: AnyRef, expected: Char, desired: Char, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeWeak((expected).toInt.toUShort, (desired).toInt.toUShort, order(mode), failureOrder(mode))
    override def compareExchangeChar(receiver: AnyRef, expected: Char, desired: Char, mode: MemoryOrder): Char = {
      val witness = stackalloc[UShort]()
      !witness = (expected).toInt.toUShort
      atomic(receiver).compareExchangeStrong(witness, (desired).toInt.toUShort, order(mode), failureOrder(mode))
      (!witness).toInt.toChar
    }
    override def exchangeChar(receiver: AnyRef, value: Char, mode: MemoryOrder): Char =
      (atomic(receiver).exchange((value).toInt.toUShort, order(mode))).toInt.toChar
    override def addChar(receiver: AnyRef, value: Char, mode: MemoryOrder): Char = (atomic(receiver).fetchAdd((value).toInt.toUShort, order(mode))).toInt.toChar
    override def bitwiseChar(receiver: AnyRef, value: Char, operation: BitwiseOperation, mode: MemoryOrder): Char = {
      val a = atomic(receiver)
      val v = (value).toInt.toUShort
      val result = (operation: @switch) match {
        case BitwiseOperation.Or  => a.fetchOr(v, order(mode))
        case BitwiseOperation.And => a.fetchAnd(v, order(mode))
        case BitwiseOperation.Xor => a.fetchXor(v, order(mode))
        case _                    =>
          throw new IllegalArgumentException("invalid VarHandle bitwise operation")
      }
      (result).toInt.toChar
    }
  }

  // Int fields
  private final class IntHandle(instance: AnyRef => Ptr[Byte], static: () => Ptr[Byte]) extends Handle(instance, static, classOf[Int]) {
    @scala.scalanative.annotation.alwaysinline
    override protected def invokeAdaptedOperation(
        operation: AccessOperation,
        receiver: AnyRef,
        expected: AnyRef,
        value: AnyRef,
        resultType: Class[_],
        mode: MemoryOrder,
        bitwiseOperation: BitwiseOperation
    ): AnyRef = invokeInt(operation, receiver, expected, value, resultType, mode, bitwiseOperation)
    override protected def boxedVariableType: Class[_] = classOf[java.lang.Integer]
    override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToInteger(getInt(receiver, mode))
    override def getLong(receiver: AnyRef, mode: MemoryOrder): Long = getInt(receiver, mode).toLong
    override def getFloat(receiver: AnyRef, mode: MemoryOrder): Float = getInt(receiver, mode).toFloat
    override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = getInt(receiver, mode).toDouble
    private def atomic(receiver: AnyRef) = fromRawPtr[Int](pointer(receiver)).atomic
    override def getInt(receiver: AnyRef, mode: MemoryOrder): Int = atomic(receiver).load(order(mode))
    override def setInt(receiver: AnyRef, value: Int, mode: MemoryOrder): Unit = atomic(receiver).store(value, order(mode))
    override def compareInt(receiver: AnyRef, expected: Int, desired: Int, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeStrong(expected, desired, order(mode), failureOrder(mode))
    override def weakCompareInt(receiver: AnyRef, expected: Int, desired: Int, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeWeak(expected, desired, order(mode), failureOrder(mode))
    override def compareExchangeInt(receiver: AnyRef, expected: Int, desired: Int, mode: MemoryOrder): Int = {
      val witness = stackalloc[Int]()
      !witness = expected
      atomic(receiver).compareExchangeStrong(witness, desired, order(mode), failureOrder(mode))
      !witness
    }
    override def exchangeInt(receiver: AnyRef, value: Int, mode: MemoryOrder): Int = atomic(receiver).exchange(value, order(mode))
    override def addInt(receiver: AnyRef, value: Int, mode: MemoryOrder): Int = atomic(receiver).fetchAdd(value, order(mode))
    override def bitwiseInt(receiver: AnyRef, value: Int, operation: BitwiseOperation, mode: MemoryOrder): Int = {
      val a = atomic(receiver)
      val v = value
      val result = (operation: @switch) match {
        case BitwiseOperation.Or  => a.fetchOr(v, order(mode))
        case BitwiseOperation.And => a.fetchAnd(v, order(mode))
        case BitwiseOperation.Xor => a.fetchXor(v, order(mode))
        case _                    =>
          throw new IllegalArgumentException("invalid VarHandle bitwise operation")
      }
      result
    }
  }

  // Long fields
  private final class LongHandle(instance: AnyRef => Ptr[Byte], static: () => Ptr[Byte]) extends Handle(instance, static, classOf[Long]) {
    @scala.scalanative.annotation.alwaysinline
    override protected def invokeAdaptedOperation(
        operation: AccessOperation,
        receiver: AnyRef,
        expected: AnyRef,
        value: AnyRef,
        resultType: Class[_],
        mode: MemoryOrder,
        bitwiseOperation: BitwiseOperation
    ): AnyRef = invokeLong(operation, receiver, expected, value, resultType, mode, bitwiseOperation)
    override protected def boxedVariableType: Class[_] = classOf[java.lang.Long]
    override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToLong(getLong(receiver, mode))
    override def getFloat(receiver: AnyRef, mode: MemoryOrder): Float = getLong(receiver, mode).toFloat
    override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = getLong(receiver, mode).toDouble
    private def atomic(receiver: AnyRef) = fromRawPtr[Long](pointer(receiver)).atomic
    override def getLong(receiver: AnyRef, mode: MemoryOrder): Long = atomic(receiver).load(order(mode))
    override def setLong(receiver: AnyRef, value: Long, mode: MemoryOrder): Unit = atomic(receiver).store(value, order(mode))
    override def compareLong(receiver: AnyRef, expected: Long, desired: Long, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeStrong(expected, desired, order(mode), failureOrder(mode))
    override def weakCompareLong(receiver: AnyRef, expected: Long, desired: Long, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeWeak(expected, desired, order(mode), failureOrder(mode))
    override def compareExchangeLong(receiver: AnyRef, expected: Long, desired: Long, mode: MemoryOrder): Long = {
      val witness = stackalloc[Long]()
      !witness = expected
      atomic(receiver).compareExchangeStrong(witness, desired, order(mode), failureOrder(mode))
      !witness
    }
    override def exchangeLong(receiver: AnyRef, value: Long, mode: MemoryOrder): Long = atomic(receiver).exchange(value, order(mode))
    override def addLong(receiver: AnyRef, value: Long, mode: MemoryOrder): Long = atomic(receiver).fetchAdd(value, order(mode))
    override def bitwiseLong(receiver: AnyRef, value: Long, operation: BitwiseOperation, mode: MemoryOrder): Long = {
      val a = atomic(receiver)
      val v = value
      val result = (operation: @switch) match {
        case BitwiseOperation.Or  => a.fetchOr(v, order(mode))
        case BitwiseOperation.And => a.fetchAnd(v, order(mode))
        case BitwiseOperation.Xor => a.fetchXor(v, order(mode))
        case _                    =>
          throw new IllegalArgumentException("invalid VarHandle bitwise operation")
      }
      result
    }
  }

  // Float fields
  private final class FloatHandle(instance: AnyRef => Ptr[Byte], static: () => Ptr[Byte]) extends Handle(instance, static, classOf[Float]) {
    @scala.scalanative.annotation.alwaysinline
    override protected def invokeAdaptedOperation(
        operation: AccessOperation,
        receiver: AnyRef,
        expected: AnyRef,
        value: AnyRef,
        resultType: Class[_],
        mode: MemoryOrder,
        bitwiseOperation: BitwiseOperation
    ): AnyRef = invokeFloat(operation, receiver, expected, value, resultType, mode, bitwiseOperation)
    override protected def boxedVariableType: Class[_] = classOf[java.lang.Float]
    override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToFloat(getFloat(receiver, mode))
    override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = getFloat(receiver, mode).toDouble
    override def bitwiseFloat(receiver: AnyRef, value: Float, operation: BitwiseOperation, mode: MemoryOrder): Float = throw new UnsupportedOperationException(
      "bitwise operations are unsupported for float VarHandles"
    )

    private def atomic(receiver: AnyRef) = fromRawPtr[Int](pointer(receiver)).atomic
    override def getFloat(receiver: AnyRef, mode: MemoryOrder): Float = java.lang.Float.intBitsToFloat(atomic(receiver).load(order(mode)))
    override def setFloat(receiver: AnyRef, value: Float, mode: MemoryOrder): Unit =
      atomic(receiver).store(java.lang.Float.floatToRawIntBits(value), order(mode))
    override def compareFloat(receiver: AnyRef, expected: Float, desired: Float, mode: MemoryOrder): Boolean = atomic(receiver).compareExchangeStrong(
      java.lang.Float.floatToRawIntBits(expected),
      java.lang.Float.floatToRawIntBits(desired),
      order(mode),
      failureOrder(mode)
    )
    override def weakCompareFloat(receiver: AnyRef, expected: Float, desired: Float, mode: MemoryOrder): Boolean = atomic(receiver).compareExchangeWeak(
      java.lang.Float.floatToRawIntBits(expected),
      java.lang.Float.floatToRawIntBits(desired),
      order(mode),
      failureOrder(mode)
    )
    override def compareExchangeFloat(receiver: AnyRef, expected: Float, desired: Float, mode: MemoryOrder): Float = {
      val witness = stackalloc[Int]()
      !witness = java.lang.Float.floatToRawIntBits(expected)
      atomic(receiver).compareExchangeStrong(witness, java.lang.Float.floatToRawIntBits(desired), order(mode), failureOrder(mode))
      java.lang.Float.intBitsToFloat(!witness)
    }
    override def exchangeFloat(receiver: AnyRef, value: Float, mode: MemoryOrder): Float =
      java.lang.Float.intBitsToFloat(atomic(receiver).exchange(java.lang.Float.floatToRawIntBits(value), order(mode)))
    override def addFloat(receiver: AnyRef, value: Float, mode: MemoryOrder): Float = {
      val a = atomic(receiver)
      val witness = stackalloc[Int]()
      !witness = a.load(failureOrder(mode))
      var previous = !witness
      var done = false
      while (!done) {
        previous = !witness
        done =
          a.compareExchangeStrong(witness, java.lang.Float.floatToRawIntBits(java.lang.Float.intBitsToFloat(previous) + value), order(mode), failureOrder(mode))
      }
      java.lang.Float.intBitsToFloat(previous)
    }
  }

  // Double fields
  private final class DoubleHandle(instance: AnyRef => Ptr[Byte], static: () => Ptr[Byte]) extends Handle(instance, static, classOf[Double]) {
    @scala.scalanative.annotation.alwaysinline
    override protected def invokeAdaptedOperation(
        operation: AccessOperation,
        receiver: AnyRef,
        expected: AnyRef,
        value: AnyRef,
        resultType: Class[_],
        mode: MemoryOrder,
        bitwiseOperation: BitwiseOperation
    ): AnyRef = invokeDouble(operation, receiver, expected, value, resultType, mode, bitwiseOperation)
    override protected def boxedVariableType: Class[_] = classOf[java.lang.Double]
    override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToDouble(getDouble(receiver, mode))
    override def bitwiseDouble(receiver: AnyRef, value: Double, operation: BitwiseOperation, mode: MemoryOrder): Double =
      throw new UnsupportedOperationException("bitwise operations are unsupported for double VarHandles")

    private def atomic(receiver: AnyRef) = fromRawPtr[Long](pointer(receiver)).atomic
    override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = java.lang.Double.longBitsToDouble(atomic(receiver).load(order(mode)))
    override def setDouble(receiver: AnyRef, value: Double, mode: MemoryOrder): Unit =
      atomic(receiver).store(java.lang.Double.doubleToRawLongBits(value), order(mode))
    override def compareDouble(receiver: AnyRef, expected: Double, desired: Double, mode: MemoryOrder): Boolean = atomic(receiver).compareExchangeStrong(
      java.lang.Double.doubleToRawLongBits(expected),
      java.lang.Double.doubleToRawLongBits(desired),
      order(mode),
      failureOrder(mode)
    )
    override def weakCompareDouble(receiver: AnyRef, expected: Double, desired: Double, mode: MemoryOrder): Boolean = atomic(receiver).compareExchangeWeak(
      java.lang.Double.doubleToRawLongBits(expected),
      java.lang.Double.doubleToRawLongBits(desired),
      order(mode),
      failureOrder(mode)
    )
    override def compareExchangeDouble(receiver: AnyRef, expected: Double, desired: Double, mode: MemoryOrder): Double = {
      val witness = stackalloc[Long]()
      !witness = java.lang.Double.doubleToRawLongBits(expected)
      atomic(receiver).compareExchangeStrong(witness, java.lang.Double.doubleToRawLongBits(desired), order(mode), failureOrder(mode))
      java.lang.Double.longBitsToDouble(!witness)
    }
    override def exchangeDouble(receiver: AnyRef, value: Double, mode: MemoryOrder): Double =
      java.lang.Double.longBitsToDouble(atomic(receiver).exchange(java.lang.Double.doubleToRawLongBits(value), order(mode)))
    override def addDouble(receiver: AnyRef, value: Double, mode: MemoryOrder): Double = {
      val a = atomic(receiver)
      val witness = stackalloc[Long]()
      !witness = a.load(failureOrder(mode))
      var previous = !witness
      var done = false
      while (!done) {
        previous = !witness
        done = a.compareExchangeStrong(
          witness,
          java.lang.Double.doubleToRawLongBits(java.lang.Double.longBitsToDouble(previous) + value),
          order(mode),
          failureOrder(mode)
        )
      }
      java.lang.Double.longBitsToDouble(previous)
    }
  }

  // Reference fields
  private final class ReferenceHandle(instance: AnyRef => Ptr[Byte], static: () => Ptr[Byte], variableType: Class[_])
      extends Handle(instance, static, variableType) {
    @scala.scalanative.annotation.alwaysinline
    override protected def invokeAdaptedOperation(
        operation: AccessOperation,
        receiver: AnyRef,
        expected: AnyRef,
        value: AnyRef,
        resultType: Class[_],
        mode: MemoryOrder,
        bitwiseOperation: BitwiseOperation
    ): AnyRef = invokeReference(operation, receiver, expected, value, resultType, mode, bitwiseOperation)
    override def addReference(receiver: AnyRef, value: AnyRef, mode: MemoryOrder): AnyRef = throw new UnsupportedOperationException(
      "getAndAdd is unsupported for reference VarHandles"
    )
    override def bitwiseReference(receiver: AnyRef, value: AnyRef, operation: BitwiseOperation, mode: MemoryOrder): AnyRef =
      throw new UnsupportedOperationException("bitwise operations are unsupported for reference VarHandles")

    private def atomic(receiver: AnyRef) = fromRawPtr[AnyRef](pointer(receiver)).atomic
    override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = atomic(receiver).load(order(mode))
    override def setReference(receiver: AnyRef, value: AnyRef, mode: MemoryOrder): Unit = atomic(receiver).store(value, order(mode))
    override def compareReference(receiver: AnyRef, expected: AnyRef, desired: AnyRef, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeStrong(expected, desired, order(mode), failureOrder(mode))
    override def weakCompareReference(receiver: AnyRef, expected: AnyRef, desired: AnyRef, mode: MemoryOrder): Boolean =
      atomic(receiver).compareExchangeWeak(expected, desired, order(mode), failureOrder(mode))
    override def compareExchangeReference(receiver: AnyRef, expected: AnyRef, desired: AnyRef, mode: MemoryOrder): AnyRef = {
      val witness = stackalloc[AnyRef]()
      !witness = expected
      atomic(receiver).compareExchangeStrong(witness, desired, order(mode), failureOrder(mode))
      !witness
    }
    override def exchangeReference(receiver: AnyRef, value: AnyRef, mode: MemoryOrder): AnyRef = atomic(receiver).exchange(value, order(mode))
  }

  // Handle construction
  def createBooleanHandle(instanceBinding: AnyRef => Ptr[Byte], staticBinding: () => Ptr[Byte]): java.lang.invoke.VarHandle =
    new BooleanHandle(instanceBinding, staticBinding)
  def createByteHandle(instanceBinding: AnyRef => Ptr[Byte], staticBinding: () => Ptr[Byte]): java.lang.invoke.VarHandle =
    new ByteHandle(instanceBinding, staticBinding)
  def createShortHandle(instanceBinding: AnyRef => Ptr[Byte], staticBinding: () => Ptr[Byte]): java.lang.invoke.VarHandle =
    new ShortHandle(instanceBinding, staticBinding)
  def createCharHandle(instanceBinding: AnyRef => Ptr[Byte], staticBinding: () => Ptr[Byte]): java.lang.invoke.VarHandle =
    new CharHandle(instanceBinding, staticBinding)
  def createIntHandle(instanceBinding: AnyRef => Ptr[Byte], staticBinding: () => Ptr[Byte]): java.lang.invoke.VarHandle =
    new IntHandle(instanceBinding, staticBinding)
  def createLongHandle(instanceBinding: AnyRef => Ptr[Byte], staticBinding: () => Ptr[Byte]): java.lang.invoke.VarHandle =
    new LongHandle(instanceBinding, staticBinding)
  def createFloatHandle(instanceBinding: AnyRef => Ptr[Byte], staticBinding: () => Ptr[Byte]): java.lang.invoke.VarHandle =
    new FloatHandle(instanceBinding, staticBinding)
  def createDoubleHandle(instanceBinding: AnyRef => Ptr[Byte], staticBinding: () => Ptr[Byte]): java.lang.invoke.VarHandle =
    new DoubleHandle(instanceBinding, staticBinding)
  def createReferenceHandle(instanceBinding: AnyRef => Ptr[Byte], staticBinding: () => Ptr[Byte], variableType: Class[_]): java.lang.invoke.VarHandle =
    new ReferenceHandle(instanceBinding, staticBinding, variableType)
}
