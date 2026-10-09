package scala.scalanative.runtime

import scala.annotation.switch

import scala.scalanative.annotation.alwaysinline
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
  import NativeVarHandle.{AccessOperation, BitwiseOperation, FieldBinding, MemoryOrder}
  import MemoryOrder._

  // Shared dispatch and memory ordering
  private abstract class Handle[T](binding: FieldBinding, protected val variableType: Class[_], coordinateType: Class[_], exactBehavior: Boolean)
      extends java.lang.invoke._VarHandle
      with VarHandleAdaptation {
    protected def copyWithBehavior(exact: Boolean): java.lang.invoke._VarHandle

    override def varType(): Class[_] = variableType
    override def coordinateTypes(): java.util.List[Class[_]] =
      if (coordinateType == null) java.util.Collections.emptyList[Class[_]]()
      else java.util.Collections.singletonList[Class[_]](coordinateType)

    override def hasInvokeExactBehavior(): Boolean = exactBehavior
    override def withInvokeBehavior(): java.lang.invoke._VarHandle =
      if (!exactBehavior) this else copyWithBehavior(false)
    override def withInvokeExactBehavior(): java.lang.invoke._VarHandle =
      if (exactBehavior) this else copyWithBehavior(true)

    override def toString(): String = s"VarHandle[varType=${variableType.getName}, coord=${coordinateTypes()}]"

    override def isAccessModeSupported(mode: java.lang.invoke._VarHandle.AccessMode): Boolean = {
      import java.lang.invoke._VarHandle.AccessMode._
      if (mode == null) throw new NullPointerException
      mode match {
        case GET_AND_ADD | GET_AND_ADD_ACQUIRE | GET_AND_ADD_RELEASE =>
          variableType.isPrimitive && variableType != classOf[Boolean]
        case GET_AND_BITWISE_OR | GET_AND_BITWISE_OR_ACQUIRE | GET_AND_BITWISE_OR_RELEASE | GET_AND_BITWISE_AND | GET_AND_BITWISE_AND_ACQUIRE |
            GET_AND_BITWISE_AND_RELEASE | GET_AND_BITWISE_XOR | GET_AND_BITWISE_XOR_ACQUIRE | GET_AND_BITWISE_XOR_RELEASE =>
          variableType.isPrimitive && variableType != classOf[Float] && variableType != classOf[Double]
        case _ => true
      }
    }

    @alwaysinline override def validateInvocation(
        operation: AccessOperation,
        coordinate: Class[_],
        expected: Class[_],
        value: Class[_],
        result: Class[_]
    ): Unit = {
      import AccessOperation._
      val noType = classOf[Unit]
      // Coordinate arity is part of the signature in both invocation modes.
      if ((coordinateType == null) != (coordinate == null))
        signatureMismatch("access with incompatible coordinates")
      if (exactBehavior) {
        val expectedResult = (operation: @switch) match {
          case Set                   => noType
          case Compare | WeakCompare => classOf[Boolean]
          case _                     => variableType
        }
        val compares = operation == Compare || operation == WeakCompare || operation == CompareExchange
        if ((coordinateType != null && coordinate != coordinateType) ||
            (compares && expected != variableType) ||
            (operation != Get && value != variableType) || result != expectedResult)
          signatureMismatch("exact access with incompatible method type")
      }
    }

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
    @alwaysinline protected def pointer(receiver: AnyRef): Ptr[T] = fromRawPtr[T](binding.pointer(receiver))
  }
  @alwaysinline private def order(mode: MemoryOrder): memory_order = (mode: @switch) match {
    case Plain    => memory_order_relaxed
    case Volatile => memory_order_seq_cst
    case Acquire  => memory_order_acquire
    case Release  => memory_order_release
    case _        => memory_order_relaxed
  }

  @alwaysinline private def failureOrder(mode: MemoryOrder): memory_order = (mode: @switch) match {
    case Plain    => memory_order_relaxed
    case Volatile => memory_order_seq_cst
    case Acquire  => memory_order_acquire
    case Release  => memory_order_relaxed
    case _        => memory_order_relaxed
  }

  // Boolean fields
  private final class BooleanHandle(binding: FieldBinding, coordinateType: Class[_], exactBehavior: Boolean = false)
      extends Handle[Boolean](binding, classOf[Boolean], coordinateType, exactBehavior) {
    override protected def copyWithBehavior(exact: Boolean): java.lang.invoke._VarHandle = new BooleanHandle(binding, coordinateType, exact)

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
    @alwaysinline override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToBoolean(getBoolean(receiver, mode))
    override def addBoolean(receiver: AnyRef, value: Boolean, mode: MemoryOrder): Boolean = throw new UnsupportedOperationException(
      "getAndAdd is unsupported for boolean VarHandles"
    )

    @alwaysinline private def atomic(receiver: AnyRef) = pointer(receiver).atomic
    @alwaysinline override def getBoolean(receiver: AnyRef, mode: MemoryOrder): Boolean =
      (if (mode == Volatile) atomic(receiver).load() else atomic(receiver).load(order(mode)))
    @alwaysinline override def setBoolean(receiver: AnyRef, value: Boolean, mode: MemoryOrder): Unit =
      (if (mode == Volatile) atomic(receiver).store(value) else atomic(receiver).store(value, order(mode)))
    @alwaysinline override def compareBoolean(receiver: AnyRef, expected: Boolean, desired: Boolean, mode: MemoryOrder): Boolean =
      if (mode == Volatile) atomic(receiver).compareExchangeStrong(expected, desired)
      else atomic(receiver).compareExchangeStrong(expected, desired, order(mode), failureOrder(mode))
    @alwaysinline override def weakCompareBoolean(receiver: AnyRef, expected: Boolean, desired: Boolean, mode: MemoryOrder): Boolean =
      if (mode == Volatile) atomic(receiver).compareExchangeWeak(expected, desired)
      else atomic(receiver).compareExchangeWeak(expected, desired, order(mode), failureOrder(mode))
    @alwaysinline override def compareExchangeBoolean(receiver: AnyRef, expected: Boolean, desired: Boolean, mode: MemoryOrder): Boolean = {
      val witness = stackalloc[Boolean]()
      !witness = expected
      if (mode == Volatile) atomic(receiver).compareExchangeStrong(witness, desired)
      else atomic(receiver).compareExchangeStrong(witness, desired, order(mode), failureOrder(mode))
      !witness
    }
    @alwaysinline override def exchangeBoolean(receiver: AnyRef, value: Boolean, mode: MemoryOrder): Boolean =
      (if (mode == Volatile) atomic(receiver).exchange(value) else atomic(receiver).exchange(value, order(mode)))
    @alwaysinline override def bitwiseBoolean(receiver: AnyRef, value: Boolean, operation: BitwiseOperation, mode: MemoryOrder): Boolean = {
      val a = atomic(receiver)
      val v = value
      val result = (operation: @switch) match {
        case BitwiseOperation.Or  => (if (mode == Volatile) a.fetchOr(v) else a.fetchOr(v, order(mode)))
        case BitwiseOperation.And => (if (mode == Volatile) a.fetchAnd(v) else a.fetchAnd(v, order(mode)))
        case BitwiseOperation.Xor => (if (mode == Volatile) a.fetchXor(v) else a.fetchXor(v, order(mode)))
        case _                    =>
          throw new IllegalArgumentException("invalid VarHandle bitwise operation")
      }
      result
    }
  }

  // Byte fields
  private final class ByteHandle(binding: FieldBinding, coordinateType: Class[_], exactBehavior: Boolean = false)
      extends Handle[Byte](binding, classOf[Byte], coordinateType, exactBehavior) {
    override protected def copyWithBehavior(exact: Boolean): java.lang.invoke._VarHandle = new ByteHandle(binding, coordinateType, exact)

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
    @alwaysinline override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToByte(getByte(receiver, mode))
    @alwaysinline override def getShort(receiver: AnyRef, mode: MemoryOrder): Short = getByte(receiver, mode).toShort
    @alwaysinline override def getInt(receiver: AnyRef, mode: MemoryOrder): Int = getByte(receiver, mode).toInt
    @alwaysinline override def getLong(receiver: AnyRef, mode: MemoryOrder): Long = getByte(receiver, mode).toLong
    @alwaysinline override def getFloat(receiver: AnyRef, mode: MemoryOrder): Float = getByte(receiver, mode).toFloat
    @alwaysinline override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = getByte(receiver, mode).toDouble
    @alwaysinline private def atomic(receiver: AnyRef) = pointer(receiver).atomic
    @alwaysinline override def getByte(receiver: AnyRef, mode: MemoryOrder): Byte =
      (if (mode == Volatile) atomic(receiver).load() else atomic(receiver).load(order(mode)))
    @alwaysinline override def setByte(receiver: AnyRef, value: Byte, mode: MemoryOrder): Unit =
      (if (mode == Volatile) atomic(receiver).store(value) else atomic(receiver).store(value, order(mode)))
    @alwaysinline override def compareByte(receiver: AnyRef, expected: Byte, desired: Byte, mode: MemoryOrder): Boolean =
      if (mode == Volatile) atomic(receiver).compareExchangeStrong(expected, desired)
      else atomic(receiver).compareExchangeStrong(expected, desired, order(mode), failureOrder(mode))
    @alwaysinline override def weakCompareByte(receiver: AnyRef, expected: Byte, desired: Byte, mode: MemoryOrder): Boolean =
      if (mode == Volatile) atomic(receiver).compareExchangeWeak(expected, desired)
      else atomic(receiver).compareExchangeWeak(expected, desired, order(mode), failureOrder(mode))
    @alwaysinline override def compareExchangeByte(receiver: AnyRef, expected: Byte, desired: Byte, mode: MemoryOrder): Byte = {
      val witness = stackalloc[Byte]()
      !witness = expected
      if (mode == Volatile) atomic(receiver).compareExchangeStrong(witness, desired)
      else atomic(receiver).compareExchangeStrong(witness, desired, order(mode), failureOrder(mode))
      !witness
    }
    @alwaysinline override def exchangeByte(receiver: AnyRef, value: Byte, mode: MemoryOrder): Byte =
      (if (mode == Volatile) atomic(receiver).exchange(value) else atomic(receiver).exchange(value, order(mode)))
    @alwaysinline override def addByte(receiver: AnyRef, value: Byte, mode: MemoryOrder): Byte =
      (if (mode == Volatile) atomic(receiver).fetchAdd(value) else atomic(receiver).fetchAdd(value, order(mode)))
    @alwaysinline override def bitwiseByte(receiver: AnyRef, value: Byte, operation: BitwiseOperation, mode: MemoryOrder): Byte = {
      val a = atomic(receiver)
      val v = value
      val result = (operation: @switch) match {
        case BitwiseOperation.Or  => (if (mode == Volatile) a.fetchOr(v) else a.fetchOr(v, order(mode)))
        case BitwiseOperation.And => (if (mode == Volatile) a.fetchAnd(v) else a.fetchAnd(v, order(mode)))
        case BitwiseOperation.Xor => (if (mode == Volatile) a.fetchXor(v) else a.fetchXor(v, order(mode)))
        case _                    =>
          throw new IllegalArgumentException("invalid VarHandle bitwise operation")
      }
      result
    }
  }

  // Short fields
  private final class ShortHandle(binding: FieldBinding, coordinateType: Class[_], exactBehavior: Boolean = false)
      extends Handle[Short](binding, classOf[Short], coordinateType, exactBehavior) {
    override protected def copyWithBehavior(exact: Boolean): java.lang.invoke._VarHandle = new ShortHandle(binding, coordinateType, exact)

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
    @alwaysinline override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToShort(getShort(receiver, mode))
    @alwaysinline override def getInt(receiver: AnyRef, mode: MemoryOrder): Int = getShort(receiver, mode).toInt
    @alwaysinline override def getLong(receiver: AnyRef, mode: MemoryOrder): Long = getShort(receiver, mode).toLong
    @alwaysinline override def getFloat(receiver: AnyRef, mode: MemoryOrder): Float = getShort(receiver, mode).toFloat
    @alwaysinline override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = getShort(receiver, mode).toDouble
    @alwaysinline private def atomic(receiver: AnyRef) = pointer(receiver).atomic
    @alwaysinline override def getShort(receiver: AnyRef, mode: MemoryOrder): Short =
      (if (mode == Volatile) atomic(receiver).load() else atomic(receiver).load(order(mode)))
    @alwaysinline override def setShort(receiver: AnyRef, value: Short, mode: MemoryOrder): Unit =
      (if (mode == Volatile) atomic(receiver).store(value) else atomic(receiver).store(value, order(mode)))
    @alwaysinline override def compareShort(receiver: AnyRef, expected: Short, desired: Short, mode: MemoryOrder): Boolean =
      if (mode == Volatile) atomic(receiver).compareExchangeStrong(expected, desired)
      else atomic(receiver).compareExchangeStrong(expected, desired, order(mode), failureOrder(mode))
    @alwaysinline override def weakCompareShort(receiver: AnyRef, expected: Short, desired: Short, mode: MemoryOrder): Boolean =
      if (mode == Volatile) atomic(receiver).compareExchangeWeak(expected, desired)
      else atomic(receiver).compareExchangeWeak(expected, desired, order(mode), failureOrder(mode))
    @alwaysinline override def compareExchangeShort(receiver: AnyRef, expected: Short, desired: Short, mode: MemoryOrder): Short = {
      val witness = stackalloc[Short]()
      !witness = expected
      if (mode == Volatile) atomic(receiver).compareExchangeStrong(witness, desired)
      else atomic(receiver).compareExchangeStrong(witness, desired, order(mode), failureOrder(mode))
      !witness
    }
    @alwaysinline override def exchangeShort(receiver: AnyRef, value: Short, mode: MemoryOrder): Short =
      (if (mode == Volatile) atomic(receiver).exchange(value) else atomic(receiver).exchange(value, order(mode)))
    @alwaysinline override def addShort(receiver: AnyRef, value: Short, mode: MemoryOrder): Short =
      (if (mode == Volatile) atomic(receiver).fetchAdd(value) else atomic(receiver).fetchAdd(value, order(mode)))
    @alwaysinline override def bitwiseShort(receiver: AnyRef, value: Short, operation: BitwiseOperation, mode: MemoryOrder): Short = {
      val a = atomic(receiver)
      val v = value
      val result = (operation: @switch) match {
        case BitwiseOperation.Or  => (if (mode == Volatile) a.fetchOr(v) else a.fetchOr(v, order(mode)))
        case BitwiseOperation.And => (if (mode == Volatile) a.fetchAnd(v) else a.fetchAnd(v, order(mode)))
        case BitwiseOperation.Xor => (if (mode == Volatile) a.fetchXor(v) else a.fetchXor(v, order(mode)))
        case _                    =>
          throw new IllegalArgumentException("invalid VarHandle bitwise operation")
      }
      result
    }
  }

  // Char fields
  private final class CharHandle(binding: FieldBinding, coordinateType: Class[_], exactBehavior: Boolean = false)
      extends Handle[Char](binding, classOf[Char], coordinateType, exactBehavior) {
    override protected def copyWithBehavior(exact: Boolean): java.lang.invoke._VarHandle = new CharHandle(binding, coordinateType, exact)

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
    @alwaysinline override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToCharacter(getChar(receiver, mode))
    @alwaysinline override def getInt(receiver: AnyRef, mode: MemoryOrder): Int = getChar(receiver, mode).toInt
    @alwaysinline override def getLong(receiver: AnyRef, mode: MemoryOrder): Long = getChar(receiver, mode).toLong
    @alwaysinline override def getFloat(receiver: AnyRef, mode: MemoryOrder): Float = getChar(receiver, mode).toFloat
    @alwaysinline override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = getChar(receiver, mode).toDouble
    // Reinterpret the field address using its unsigned atomic storage type.
    @alwaysinline private def atomic(receiver: AnyRef) = pointer(receiver).asInstanceOf[Ptr[UShort]].atomic
    @alwaysinline override def getChar(receiver: AnyRef, mode: MemoryOrder): Char =
      ((if (mode == Volatile) atomic(receiver).load() else atomic(receiver).load(order(mode)))).toInt.toChar
    @alwaysinline override def setChar(receiver: AnyRef, value: Char, mode: MemoryOrder): Unit =
      (if (mode == Volatile) atomic(receiver).store((value).toInt.toUShort) else atomic(receiver).store((value).toInt.toUShort, order(mode)))
    @alwaysinline override def compareChar(receiver: AnyRef, expected: Char, desired: Char, mode: MemoryOrder): Boolean =
      (if (mode == Volatile) atomic(receiver).compareExchangeStrong((expected).toInt.toUShort, (desired).toInt.toUShort)
       else atomic(receiver).compareExchangeStrong((expected).toInt.toUShort, (desired).toInt.toUShort, order(mode), failureOrder(mode)))
    @alwaysinline override def weakCompareChar(receiver: AnyRef, expected: Char, desired: Char, mode: MemoryOrder): Boolean =
      (if (mode == Volatile) atomic(receiver).compareExchangeWeak((expected).toInt.toUShort, (desired).toInt.toUShort)
       else atomic(receiver).compareExchangeWeak((expected).toInt.toUShort, (desired).toInt.toUShort, order(mode), failureOrder(mode)))
    @alwaysinline override def compareExchangeChar(receiver: AnyRef, expected: Char, desired: Char, mode: MemoryOrder): Char = {
      val witness = stackalloc[UShort]()
      !witness = (expected).toInt.toUShort
      (if (mode == Volatile) atomic(receiver).compareExchangeStrong(witness, (desired).toInt.toUShort)
       else atomic(receiver).compareExchangeStrong(witness, (desired).toInt.toUShort, order(mode), failureOrder(mode)))
      (!witness).toInt.toChar
    }
    @alwaysinline override def exchangeChar(receiver: AnyRef, value: Char, mode: MemoryOrder): Char =
      ((if (mode == Volatile) atomic(receiver).exchange((value).toInt.toUShort)
        else atomic(receiver).exchange((value).toInt.toUShort, order(mode)))).toInt.toChar
    @alwaysinline override def addChar(receiver: AnyRef, value: Char, mode: MemoryOrder): Char =
      ((if (mode == Volatile) atomic(receiver).fetchAdd((value).toInt.toUShort)
        else atomic(receiver).fetchAdd((value).toInt.toUShort, order(mode)))).toInt.toChar
    @alwaysinline override def bitwiseChar(receiver: AnyRef, value: Char, operation: BitwiseOperation, mode: MemoryOrder): Char = {
      val a = atomic(receiver)
      val v = (value).toInt.toUShort
      val result = (operation: @switch) match {
        case BitwiseOperation.Or  => (if (mode == Volatile) a.fetchOr(v) else a.fetchOr(v, order(mode)))
        case BitwiseOperation.And => (if (mode == Volatile) a.fetchAnd(v) else a.fetchAnd(v, order(mode)))
        case BitwiseOperation.Xor => (if (mode == Volatile) a.fetchXor(v) else a.fetchXor(v, order(mode)))
        case _                    =>
          throw new IllegalArgumentException("invalid VarHandle bitwise operation")
      }
      (result).toInt.toChar
    }
  }

  // Int fields
  private final class IntHandle(binding: FieldBinding, coordinateType: Class[_], exactBehavior: Boolean = false)
      extends Handle[Int](binding, classOf[Int], coordinateType, exactBehavior) {
    override protected def copyWithBehavior(exact: Boolean): java.lang.invoke._VarHandle = new IntHandle(binding, coordinateType, exact)

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
    @alwaysinline override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToInteger(getInt(receiver, mode))
    @alwaysinline override def getLong(receiver: AnyRef, mode: MemoryOrder): Long = getInt(receiver, mode).toLong
    @alwaysinline override def getFloat(receiver: AnyRef, mode: MemoryOrder): Float = getInt(receiver, mode).toFloat
    @alwaysinline override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = getInt(receiver, mode).toDouble
    @alwaysinline private def atomic(receiver: AnyRef) = pointer(receiver).atomic
    @alwaysinline override def getInt(receiver: AnyRef, mode: MemoryOrder): Int =
      (if (mode == Volatile) atomic(receiver).load() else atomic(receiver).load(order(mode)))
    @alwaysinline override def setInt(receiver: AnyRef, value: Int, mode: MemoryOrder): Unit =
      (if (mode == Volatile) atomic(receiver).store(value) else atomic(receiver).store(value, order(mode)))
    @alwaysinline override def compareInt(receiver: AnyRef, expected: Int, desired: Int, mode: MemoryOrder): Boolean =
      if (mode == Volatile) atomic(receiver).compareExchangeStrong(expected, desired)
      else atomic(receiver).compareExchangeStrong(expected, desired, order(mode), failureOrder(mode))
    @alwaysinline override def weakCompareInt(receiver: AnyRef, expected: Int, desired: Int, mode: MemoryOrder): Boolean =
      if (mode == Volatile) atomic(receiver).compareExchangeWeak(expected, desired)
      else atomic(receiver).compareExchangeWeak(expected, desired, order(mode), failureOrder(mode))
    @alwaysinline override def compareExchangeInt(receiver: AnyRef, expected: Int, desired: Int, mode: MemoryOrder): Int = {
      val witness = stackalloc[Int]()
      !witness = expected
      if (mode == Volatile) atomic(receiver).compareExchangeStrong(witness, desired)
      else atomic(receiver).compareExchangeStrong(witness, desired, order(mode), failureOrder(mode))
      !witness
    }
    @alwaysinline override def exchangeInt(receiver: AnyRef, value: Int, mode: MemoryOrder): Int =
      (if (mode == Volatile) atomic(receiver).exchange(value) else atomic(receiver).exchange(value, order(mode)))
    @alwaysinline override def addInt(receiver: AnyRef, value: Int, mode: MemoryOrder): Int =
      (if (mode == Volatile) atomic(receiver).fetchAdd(value) else atomic(receiver).fetchAdd(value, order(mode)))
    @alwaysinline override def bitwiseInt(receiver: AnyRef, value: Int, operation: BitwiseOperation, mode: MemoryOrder): Int = {
      val a = atomic(receiver)
      val v = value
      val result = (operation: @switch) match {
        case BitwiseOperation.Or  => (if (mode == Volatile) a.fetchOr(v) else a.fetchOr(v, order(mode)))
        case BitwiseOperation.And => (if (mode == Volatile) a.fetchAnd(v) else a.fetchAnd(v, order(mode)))
        case BitwiseOperation.Xor => (if (mode == Volatile) a.fetchXor(v) else a.fetchXor(v, order(mode)))
        case _                    =>
          throw new IllegalArgumentException("invalid VarHandle bitwise operation")
      }
      result
    }
  }

  // Long fields
  private final class LongHandle(binding: FieldBinding, coordinateType: Class[_], exactBehavior: Boolean = false)
      extends Handle[Long](binding, classOf[Long], coordinateType, exactBehavior) {
    override protected def copyWithBehavior(exact: Boolean): java.lang.invoke._VarHandle = new LongHandle(binding, coordinateType, exact)

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
    @alwaysinline override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToLong(getLong(receiver, mode))
    @alwaysinline override def getFloat(receiver: AnyRef, mode: MemoryOrder): Float = getLong(receiver, mode).toFloat
    @alwaysinline override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = getLong(receiver, mode).toDouble
    @alwaysinline private def atomic(receiver: AnyRef) = pointer(receiver).atomic
    @alwaysinline override def getLong(receiver: AnyRef, mode: MemoryOrder): Long =
      (if (mode == Volatile) atomic(receiver).load() else atomic(receiver).load(order(mode)))
    @alwaysinline override def setLong(receiver: AnyRef, value: Long, mode: MemoryOrder): Unit =
      (if (mode == Volatile) atomic(receiver).store(value) else atomic(receiver).store(value, order(mode)))
    @alwaysinline override def compareLong(receiver: AnyRef, expected: Long, desired: Long, mode: MemoryOrder): Boolean =
      if (mode == Volatile) atomic(receiver).compareExchangeStrong(expected, desired)
      else atomic(receiver).compareExchangeStrong(expected, desired, order(mode), failureOrder(mode))
    @alwaysinline override def weakCompareLong(receiver: AnyRef, expected: Long, desired: Long, mode: MemoryOrder): Boolean =
      if (mode == Volatile) atomic(receiver).compareExchangeWeak(expected, desired)
      else atomic(receiver).compareExchangeWeak(expected, desired, order(mode), failureOrder(mode))
    @alwaysinline override def compareExchangeLong(receiver: AnyRef, expected: Long, desired: Long, mode: MemoryOrder): Long = {
      val witness = stackalloc[Long]()
      !witness = expected
      if (mode == Volatile) atomic(receiver).compareExchangeStrong(witness, desired)
      else atomic(receiver).compareExchangeStrong(witness, desired, order(mode), failureOrder(mode))
      !witness
    }
    @alwaysinline override def exchangeLong(receiver: AnyRef, value: Long, mode: MemoryOrder): Long =
      (if (mode == Volatile) atomic(receiver).exchange(value) else atomic(receiver).exchange(value, order(mode)))
    @alwaysinline override def addLong(receiver: AnyRef, value: Long, mode: MemoryOrder): Long =
      (if (mode == Volatile) atomic(receiver).fetchAdd(value) else atomic(receiver).fetchAdd(value, order(mode)))
    @alwaysinline override def bitwiseLong(receiver: AnyRef, value: Long, operation: BitwiseOperation, mode: MemoryOrder): Long = {
      val a = atomic(receiver)
      val v = value
      val result = (operation: @switch) match {
        case BitwiseOperation.Or  => (if (mode == Volatile) a.fetchOr(v) else a.fetchOr(v, order(mode)))
        case BitwiseOperation.And => (if (mode == Volatile) a.fetchAnd(v) else a.fetchAnd(v, order(mode)))
        case BitwiseOperation.Xor => (if (mode == Volatile) a.fetchXor(v) else a.fetchXor(v, order(mode)))
        case _                    =>
          throw new IllegalArgumentException("invalid VarHandle bitwise operation")
      }
      result
    }
  }

  // Float fields
  private final class FloatHandle(binding: FieldBinding, coordinateType: Class[_], exactBehavior: Boolean = false)
      extends Handle[Float](binding, classOf[Float], coordinateType, exactBehavior) {
    override protected def copyWithBehavior(exact: Boolean): java.lang.invoke._VarHandle = new FloatHandle(binding, coordinateType, exact)

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
    @alwaysinline override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToFloat(getFloat(receiver, mode))
    @alwaysinline override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = getFloat(receiver, mode).toDouble
    override def bitwiseFloat(receiver: AnyRef, value: Float, operation: BitwiseOperation, mode: MemoryOrder): Float =
      throw new UnsupportedOperationException(
        "bitwise operations are unsupported for float VarHandles"
      )

    // Atomic operations work on the raw floating-point bits, not numeric conversions.
    @alwaysinline private def atomic(receiver: AnyRef) = pointer(receiver).asInstanceOf[Ptr[Int]].atomic
    @alwaysinline override def getFloat(receiver: AnyRef, mode: MemoryOrder): Float =
      java.lang.Float.intBitsToFloat((if (mode == Volatile) atomic(receiver).load() else atomic(receiver).load(order(mode))))
    @alwaysinline override def setFloat(receiver: AnyRef, value: Float, mode: MemoryOrder): Unit =
      (if (mode == Volatile) atomic(receiver).store(java.lang.Float.floatToRawIntBits(value))
       else atomic(receiver).store(java.lang.Float.floatToRawIntBits(value), order(mode)))
    @alwaysinline override def compareFloat(receiver: AnyRef, expected: Float, desired: Float, mode: MemoryOrder): Boolean =
      (if (mode == Volatile) atomic(receiver).compareExchangeStrong(java.lang.Float.floatToRawIntBits(expected), java.lang.Float.floatToRawIntBits(desired))
       else
         atomic(receiver).compareExchangeStrong(
           java.lang.Float.floatToRawIntBits(expected),
           java.lang.Float.floatToRawIntBits(desired),
           order(mode),
           failureOrder(mode)
         ))
    @alwaysinline override def weakCompareFloat(receiver: AnyRef, expected: Float, desired: Float, mode: MemoryOrder): Boolean =
      (if (mode == Volatile) atomic(receiver).compareExchangeWeak(java.lang.Float.floatToRawIntBits(expected), java.lang.Float.floatToRawIntBits(desired))
       else
         atomic(receiver).compareExchangeWeak(
           java.lang.Float.floatToRawIntBits(expected),
           java.lang.Float.floatToRawIntBits(desired),
           order(mode),
           failureOrder(mode)
         ))
    @alwaysinline override def compareExchangeFloat(receiver: AnyRef, expected: Float, desired: Float, mode: MemoryOrder): Float = {
      val witness = stackalloc[Int]()
      !witness = java.lang.Float.floatToRawIntBits(expected)
      (if (mode == Volatile) atomic(receiver).compareExchangeStrong(witness, java.lang.Float.floatToRawIntBits(desired))
       else atomic(receiver).compareExchangeStrong(witness, java.lang.Float.floatToRawIntBits(desired), order(mode), failureOrder(mode)))
      java.lang.Float.intBitsToFloat(!witness)
    }
    @alwaysinline override def exchangeFloat(receiver: AnyRef, value: Float, mode: MemoryOrder): Float =
      java.lang.Float.intBitsToFloat(
        (if (mode == Volatile) atomic(receiver).exchange(java.lang.Float.floatToRawIntBits(value))
         else atomic(receiver).exchange(java.lang.Float.floatToRawIntBits(value), order(mode)))
      )
    @alwaysinline override def addFloat(receiver: AnyRef, value: Float, mode: MemoryOrder): Float = {
      val a = atomic(receiver)
      val witness = stackalloc[Int]()
      !witness = a.load(failureOrder(mode))
      var previous = !witness
      var done = false
      while (!done) {
        previous = !witness
        done =
          (if (mode == Volatile) a.compareExchangeStrong(witness, java.lang.Float.floatToRawIntBits(java.lang.Float.intBitsToFloat(previous) + value))
           else
             a.compareExchangeStrong(
               witness,
               java.lang.Float.floatToRawIntBits(java.lang.Float.intBitsToFloat(previous) + value),
               order(mode),
               failureOrder(mode)
             ))
      }
      java.lang.Float.intBitsToFloat(previous)
    }
  }

  // Double fields
  private final class DoubleHandle(binding: FieldBinding, coordinateType: Class[_], exactBehavior: Boolean = false)
      extends Handle[Double](binding, classOf[Double], coordinateType, exactBehavior) {
    override protected def copyWithBehavior(exact: Boolean): java.lang.invoke._VarHandle = new DoubleHandle(binding, coordinateType, exact)

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
    @alwaysinline override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = scala.runtime.BoxesRunTime.boxToDouble(getDouble(receiver, mode))
    override def bitwiseDouble(receiver: AnyRef, value: Double, operation: BitwiseOperation, mode: MemoryOrder): Double =
      throw new UnsupportedOperationException("bitwise operations are unsupported for double VarHandles")

    // Atomic operations work on the raw floating-point bits, not numeric conversions.
    @alwaysinline private def atomic(receiver: AnyRef) = pointer(receiver).asInstanceOf[Ptr[Long]].atomic
    @alwaysinline override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double =
      java.lang.Double.longBitsToDouble((if (mode == Volatile) atomic(receiver).load() else atomic(receiver).load(order(mode))))
    @alwaysinline override def setDouble(receiver: AnyRef, value: Double, mode: MemoryOrder): Unit =
      (if (mode == Volatile) atomic(receiver).store(java.lang.Double.doubleToRawLongBits(value))
       else atomic(receiver).store(java.lang.Double.doubleToRawLongBits(value), order(mode)))
    @alwaysinline override def compareDouble(receiver: AnyRef, expected: Double, desired: Double, mode: MemoryOrder): Boolean =
      (if (mode == Volatile)
         atomic(receiver).compareExchangeStrong(java.lang.Double.doubleToRawLongBits(expected), java.lang.Double.doubleToRawLongBits(desired))
       else
         atomic(receiver).compareExchangeStrong(
           java.lang.Double.doubleToRawLongBits(expected),
           java.lang.Double.doubleToRawLongBits(desired),
           order(mode),
           failureOrder(mode)
         ))
    @alwaysinline override def weakCompareDouble(receiver: AnyRef, expected: Double, desired: Double, mode: MemoryOrder): Boolean =
      (if (mode == Volatile) atomic(receiver).compareExchangeWeak(java.lang.Double.doubleToRawLongBits(expected), java.lang.Double.doubleToRawLongBits(desired))
       else
         atomic(receiver).compareExchangeWeak(
           java.lang.Double.doubleToRawLongBits(expected),
           java.lang.Double.doubleToRawLongBits(desired),
           order(mode),
           failureOrder(mode)
         ))
    @alwaysinline override def compareExchangeDouble(receiver: AnyRef, expected: Double, desired: Double, mode: MemoryOrder): Double = {
      val witness = stackalloc[Long]()
      !witness = java.lang.Double.doubleToRawLongBits(expected)
      (if (mode == Volatile) atomic(receiver).compareExchangeStrong(witness, java.lang.Double.doubleToRawLongBits(desired))
       else atomic(receiver).compareExchangeStrong(witness, java.lang.Double.doubleToRawLongBits(desired), order(mode), failureOrder(mode)))
      java.lang.Double.longBitsToDouble(!witness)
    }
    @alwaysinline override def exchangeDouble(receiver: AnyRef, value: Double, mode: MemoryOrder): Double =
      java.lang.Double.longBitsToDouble(
        (if (mode == Volatile) atomic(receiver).exchange(java.lang.Double.doubleToRawLongBits(value))
         else atomic(receiver).exchange(java.lang.Double.doubleToRawLongBits(value), order(mode)))
      )
    @alwaysinline override def addDouble(receiver: AnyRef, value: Double, mode: MemoryOrder): Double = {
      val a = atomic(receiver)
      val witness = stackalloc[Long]()
      !witness = a.load(failureOrder(mode))
      var previous = !witness
      var done = false
      while (!done) {
        previous = !witness
        done =
          (if (mode == Volatile) a.compareExchangeStrong(witness, java.lang.Double.doubleToRawLongBits(java.lang.Double.longBitsToDouble(previous) + value))
           else
             a.compareExchangeStrong(
               witness,
               java.lang.Double.doubleToRawLongBits(java.lang.Double.longBitsToDouble(previous) + value),
               order(mode),
               failureOrder(mode)
             ))
      }
      java.lang.Double.longBitsToDouble(previous)
    }
  }

  // Reference fields
  private final class ReferenceHandle(binding: FieldBinding, variableType: Class[_], coordinateType: Class[_], exactBehavior: Boolean = false)
      extends Handle[AnyRef](binding, variableType, coordinateType, exactBehavior) {
    override protected def copyWithBehavior(exact: Boolean): java.lang.invoke._VarHandle = new ReferenceHandle(binding, variableType, coordinateType, exact)

    @alwaysinline private def unboxedRead(receiver: AnyRef, resultType: Class[_], mode: MemoryOrder): AnyRef = {
      if (!VarHandleConversions.canConvert(variableType, resultType)) signatureMismatch(s"get returning ${resultType.getName}")
      VarHandleConversions.convert(getReference(receiver, mode), variableType, resultType)
    }

    @alwaysinline override def getBoolean(receiver: AnyRef, mode: MemoryOrder): Boolean =
      unboxedRead(receiver, classOf[Boolean], mode).asInstanceOf[java.lang.Boolean].booleanValue()
    @alwaysinline override def getByte(receiver: AnyRef, mode: MemoryOrder): Byte =
      unboxedRead(receiver, classOf[Byte], mode).asInstanceOf[java.lang.Byte].byteValue()
    @alwaysinline override def getShort(receiver: AnyRef, mode: MemoryOrder): Short =
      unboxedRead(receiver, classOf[Short], mode).asInstanceOf[java.lang.Short].shortValue()
    @alwaysinline override def getChar(receiver: AnyRef, mode: MemoryOrder): Char =
      unboxedRead(receiver, classOf[Char], mode).asInstanceOf[java.lang.Character].charValue()
    @alwaysinline override def getInt(receiver: AnyRef, mode: MemoryOrder): Int =
      unboxedRead(receiver, classOf[Int], mode).asInstanceOf[java.lang.Integer].intValue()
    @alwaysinline override def getLong(receiver: AnyRef, mode: MemoryOrder): Long =
      unboxedRead(receiver, classOf[Long], mode).asInstanceOf[java.lang.Long].longValue()
    @alwaysinline override def getFloat(receiver: AnyRef, mode: MemoryOrder): Float =
      unboxedRead(receiver, classOf[Float], mode).asInstanceOf[java.lang.Float].floatValue()
    @alwaysinline override def getDouble(receiver: AnyRef, mode: MemoryOrder): Double =
      unboxedRead(receiver, classOf[Double], mode).asInstanceOf[java.lang.Double].doubleValue()

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

    @alwaysinline private def atomic(receiver: AnyRef) = pointer(receiver).atomic
    @alwaysinline override def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef =
      (if (mode == Volatile) atomic(receiver).load() else atomic(receiver).load(order(mode)))
    @alwaysinline override def setReference(receiver: AnyRef, value: AnyRef, mode: MemoryOrder): Unit =
      (if (mode == Volatile) atomic(receiver).store(value) else atomic(receiver).store(value, order(mode)))
    @alwaysinline override def compareReference(receiver: AnyRef, expected: AnyRef, desired: AnyRef, mode: MemoryOrder): Boolean =
      if (mode == Volatile) atomic(receiver).compareExchangeStrong(expected, desired)
      else atomic(receiver).compareExchangeStrong(expected, desired, order(mode), failureOrder(mode))
    @alwaysinline override def weakCompareReference(receiver: AnyRef, expected: AnyRef, desired: AnyRef, mode: MemoryOrder): Boolean =
      if (mode == Volatile) atomic(receiver).compareExchangeWeak(expected, desired)
      else atomic(receiver).compareExchangeWeak(expected, desired, order(mode), failureOrder(mode))
    @alwaysinline override def compareExchangeReference(receiver: AnyRef, expected: AnyRef, desired: AnyRef, mode: MemoryOrder): AnyRef = {
      val witness = stackalloc[AnyRef]()
      !witness = expected
      if (mode == Volatile) atomic(receiver).compareExchangeStrong(witness, desired)
      else atomic(receiver).compareExchangeStrong(witness, desired, order(mode), failureOrder(mode))
      !witness
    }
    @alwaysinline override def exchangeReference(receiver: AnyRef, value: AnyRef, mode: MemoryOrder): AnyRef =
      (if (mode == Volatile) atomic(receiver).exchange(value) else atomic(receiver).exchange(value, order(mode)))
  }

  // Handle construction
  def createBooleanHandle(binding: FieldBinding, coordinateType: Class[_]): java.lang.invoke._VarHandle =
    new BooleanHandle(binding, coordinateType)
  def createByteHandle(binding: FieldBinding, coordinateType: Class[_]): java.lang.invoke._VarHandle =
    new ByteHandle(binding, coordinateType)
  def createShortHandle(binding: FieldBinding, coordinateType: Class[_]): java.lang.invoke._VarHandle =
    new ShortHandle(binding, coordinateType)
  def createCharHandle(binding: FieldBinding, coordinateType: Class[_]): java.lang.invoke._VarHandle =
    new CharHandle(binding, coordinateType)
  def createIntHandle(binding: FieldBinding, coordinateType: Class[_]): java.lang.invoke._VarHandle =
    new IntHandle(binding, coordinateType)
  def createLongHandle(binding: FieldBinding, coordinateType: Class[_]): java.lang.invoke._VarHandle =
    new LongHandle(binding, coordinateType)
  def createFloatHandle(binding: FieldBinding, coordinateType: Class[_]): java.lang.invoke._VarHandle =
    new FloatHandle(binding, coordinateType)
  def createDoubleHandle(binding: FieldBinding, coordinateType: Class[_]): java.lang.invoke._VarHandle =
    new DoubleHandle(binding, coordinateType)
  def createReferenceHandle(binding: FieldBinding, coordinateType: Class[_], variableType: Class[_]): java.lang.invoke._VarHandle =
    new ReferenceHandle(binding, variableType, coordinateType)
}
