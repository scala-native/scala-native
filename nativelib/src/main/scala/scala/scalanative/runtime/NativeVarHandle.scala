// format: off
package scala.scalanative.runtime

/** Exact, unboxed signatures used by compiler-lowered field access. */
private[runtime] trait NativeVarHandle {
  import NativeVarHandle.{AccessOperation, BitwiseOperation, MemoryOrder}
  
  def isExactVariableType(variableType: Class[_]): Boolean
  def invokeAdapted(operation: AccessOperation, receiver: AnyRef, expected: AnyRef, value: AnyRef, expectedType: Class[_], valueType: Class[_], resultType: Class[_], mode: MemoryOrder, bitwiseOperation: BitwiseOperation): AnyRef
  
  // Signature validation
  protected def signatureMismatch(requested: String): Nothing
  final def unsupportedSignature(requested: String): Nothing = signatureMismatch(requested)

  // Boolean fields
  def getBoolean(receiver: AnyRef, mode: MemoryOrder): Boolean = unsupportedSignature("getBoolean")
  def setBoolean(receiver: AnyRef, value: Boolean, mode: MemoryOrder): Unit = unsupportedSignature("setBoolean")
  def compareBoolean(receiver: AnyRef, expected: Boolean, desired: Boolean, mode: MemoryOrder): Boolean = unsupportedSignature("compareBoolean")
  def weakCompareBoolean(receiver: AnyRef, expected: Boolean, desired: Boolean, mode: MemoryOrder): Boolean = unsupportedSignature("weakCompareBoolean")
  def compareExchangeBoolean(receiver: AnyRef, expected: Boolean, desired: Boolean, mode: MemoryOrder): Boolean = unsupportedSignature("compareExchangeBoolean")
  def exchangeBoolean(receiver: AnyRef, value: Boolean, mode: MemoryOrder): Boolean = unsupportedSignature("exchangeBoolean")
  def addBoolean(receiver: AnyRef, value: Boolean, mode: MemoryOrder): Boolean = unsupportedSignature("addBoolean")
  def bitwiseBoolean(receiver: AnyRef, value: Boolean, operation: BitwiseOperation, mode: MemoryOrder): Boolean = unsupportedSignature("bitwiseBoolean")

  // Byte fields
  def getByte(receiver: AnyRef, mode: MemoryOrder): Byte = unsupportedSignature("getByte")
  def setByte(receiver: AnyRef, value: Byte, mode: MemoryOrder): Unit = unsupportedSignature("setByte")
  def compareByte(receiver: AnyRef, expected: Byte, desired: Byte, mode: MemoryOrder): Boolean = unsupportedSignature("compareByte")
  def weakCompareByte(receiver: AnyRef, expected: Byte, desired: Byte, mode: MemoryOrder): Boolean = unsupportedSignature("weakCompareByte")
  def compareExchangeByte(receiver: AnyRef, expected: Byte, desired: Byte, mode: MemoryOrder): Byte = unsupportedSignature("compareExchangeByte")
  def exchangeByte(receiver: AnyRef, value: Byte, mode: MemoryOrder): Byte = unsupportedSignature("exchangeByte")
  def addByte(receiver: AnyRef, value: Byte, mode: MemoryOrder): Byte = unsupportedSignature("addByte")
  def bitwiseByte(receiver: AnyRef, value: Byte, operation: BitwiseOperation, mode: MemoryOrder): Byte = unsupportedSignature("bitwiseByte")

  // Short fields
  def getShort(receiver: AnyRef, mode: MemoryOrder): Short = unsupportedSignature("getShort")
  def setShort(receiver: AnyRef, value: Short, mode: MemoryOrder): Unit = unsupportedSignature("setShort")
  def compareShort(receiver: AnyRef, expected: Short, desired: Short, mode: MemoryOrder): Boolean = unsupportedSignature("compareShort")
  def weakCompareShort(receiver: AnyRef, expected: Short, desired: Short, mode: MemoryOrder): Boolean = unsupportedSignature("weakCompareShort")
  def compareExchangeShort(receiver: AnyRef, expected: Short, desired: Short, mode: MemoryOrder): Short = unsupportedSignature("compareExchangeShort")
  def exchangeShort(receiver: AnyRef, value: Short, mode: MemoryOrder): Short = unsupportedSignature("exchangeShort")
  def addShort(receiver: AnyRef, value: Short, mode: MemoryOrder): Short = unsupportedSignature("addShort")
  def bitwiseShort(receiver: AnyRef, value: Short, operation: BitwiseOperation, mode: MemoryOrder): Short = unsupportedSignature("bitwiseShort")

  // Char fields
  def getChar(receiver: AnyRef, mode: MemoryOrder): Char = unsupportedSignature("getChar")
  def setChar(receiver: AnyRef, value: Char, mode: MemoryOrder): Unit = unsupportedSignature("setChar")
  def compareChar(receiver: AnyRef, expected: Char, desired: Char, mode: MemoryOrder): Boolean = unsupportedSignature("compareChar")
  def weakCompareChar(receiver: AnyRef, expected: Char, desired: Char, mode: MemoryOrder): Boolean = unsupportedSignature("weakCompareChar")
  def compareExchangeChar(receiver: AnyRef, expected: Char, desired: Char, mode: MemoryOrder): Char = unsupportedSignature("compareExchangeChar")
  def exchangeChar(receiver: AnyRef, value: Char, mode: MemoryOrder): Char = unsupportedSignature("exchangeChar")
  def addChar(receiver: AnyRef, value: Char, mode: MemoryOrder): Char = unsupportedSignature("addChar")
  def bitwiseChar(receiver: AnyRef, value: Char, operation: BitwiseOperation, mode: MemoryOrder): Char = unsupportedSignature("bitwiseChar")

  // Int fields
  def getInt(receiver: AnyRef, mode: MemoryOrder): Int = unsupportedSignature("getInt")
  def setInt(receiver: AnyRef, value: Int, mode: MemoryOrder): Unit = unsupportedSignature("setInt")
  def compareInt(receiver: AnyRef, expected: Int, desired: Int, mode: MemoryOrder): Boolean = unsupportedSignature("compareInt")
  def weakCompareInt(receiver: AnyRef, expected: Int, desired: Int, mode: MemoryOrder): Boolean = unsupportedSignature("weakCompareInt")
  def compareExchangeInt(receiver: AnyRef, expected: Int, desired: Int, mode: MemoryOrder): Int = unsupportedSignature("compareExchangeInt")
  def exchangeInt(receiver: AnyRef, value: Int, mode: MemoryOrder): Int = unsupportedSignature("exchangeInt")
  def addInt(receiver: AnyRef, value: Int, mode: MemoryOrder): Int = unsupportedSignature("addInt")
  def bitwiseInt(receiver: AnyRef, value: Int, operation: BitwiseOperation, mode: MemoryOrder): Int = unsupportedSignature("bitwiseInt")

  // Long fields
  def getLong(receiver: AnyRef, mode: MemoryOrder): Long = unsupportedSignature("getLong")
  def setLong(receiver: AnyRef, value: Long, mode: MemoryOrder): Unit = unsupportedSignature("setLong")
  def compareLong(receiver: AnyRef, expected: Long, desired: Long, mode: MemoryOrder): Boolean = unsupportedSignature("compareLong")
  def weakCompareLong(receiver: AnyRef, expected: Long, desired: Long, mode: MemoryOrder): Boolean = unsupportedSignature("weakCompareLong")
  def compareExchangeLong(receiver: AnyRef, expected: Long, desired: Long, mode: MemoryOrder): Long = unsupportedSignature("compareExchangeLong")
  def exchangeLong(receiver: AnyRef, value: Long, mode: MemoryOrder): Long = unsupportedSignature("exchangeLong")
  def addLong(receiver: AnyRef, value: Long, mode: MemoryOrder): Long = unsupportedSignature("addLong")
  def bitwiseLong(receiver: AnyRef, value: Long, operation: BitwiseOperation, mode: MemoryOrder): Long = unsupportedSignature("bitwiseLong")

  // Float fields
  def getFloat(receiver: AnyRef, mode: MemoryOrder): Float = unsupportedSignature("getFloat")
  def setFloat(receiver: AnyRef, value: Float, mode: MemoryOrder): Unit = unsupportedSignature("setFloat")
  def compareFloat(receiver: AnyRef, expected: Float, desired: Float, mode: MemoryOrder): Boolean = unsupportedSignature("compareFloat")
  def weakCompareFloat(receiver: AnyRef, expected: Float, desired: Float, mode: MemoryOrder): Boolean = unsupportedSignature("weakCompareFloat")
  def compareExchangeFloat(receiver: AnyRef, expected: Float, desired: Float, mode: MemoryOrder): Float = unsupportedSignature("compareExchangeFloat")
  def exchangeFloat(receiver: AnyRef, value: Float, mode: MemoryOrder): Float = unsupportedSignature("exchangeFloat")
  def addFloat(receiver: AnyRef, value: Float, mode: MemoryOrder): Float = unsupportedSignature("addFloat")
  def bitwiseFloat(receiver: AnyRef, value: Float, operation: BitwiseOperation, mode: MemoryOrder): Float = unsupportedSignature("bitwiseFloat")

  // Double fields
  def getDouble(receiver: AnyRef, mode: MemoryOrder): Double = unsupportedSignature("getDouble")
  def setDouble(receiver: AnyRef, value: Double, mode: MemoryOrder): Unit = unsupportedSignature("setDouble")
  def compareDouble(receiver: AnyRef, expected: Double, desired: Double, mode: MemoryOrder): Boolean = unsupportedSignature("compareDouble")
  def weakCompareDouble(receiver: AnyRef, expected: Double, desired: Double, mode: MemoryOrder): Boolean = unsupportedSignature("weakCompareDouble")
  def compareExchangeDouble(receiver: AnyRef, expected: Double, desired: Double, mode: MemoryOrder): Double = unsupportedSignature("compareExchangeDouble")
  def exchangeDouble(receiver: AnyRef, value: Double, mode: MemoryOrder): Double = unsupportedSignature("exchangeDouble")
  def addDouble(receiver: AnyRef, value: Double, mode: MemoryOrder): Double = unsupportedSignature("addDouble")
  def bitwiseDouble(receiver: AnyRef, value: Double, operation: BitwiseOperation, mode: MemoryOrder): Double = unsupportedSignature("bitwiseDouble")

  // Reference fields
  def getReference(receiver: AnyRef, mode: MemoryOrder): AnyRef = unsupportedSignature("getReference")
  def getBoxedReference(receiver: AnyRef, resultType: Class[_], mode: MemoryOrder): AnyRef = getReference(receiver, mode)
  def setReference(receiver: AnyRef, value: AnyRef, mode: MemoryOrder): Unit = unsupportedSignature("setReference")
  def compareReference(receiver: AnyRef, expected: AnyRef, desired: AnyRef, mode: MemoryOrder): Boolean = unsupportedSignature("compareReference")
  def weakCompareReference(receiver: AnyRef, expected: AnyRef, desired: AnyRef, mode: MemoryOrder): Boolean = unsupportedSignature("weakCompareReference")
  def compareExchangeReference(receiver: AnyRef, expected: AnyRef, desired: AnyRef, mode: MemoryOrder): AnyRef = unsupportedSignature("compareExchangeReference")
  def exchangeReference(receiver: AnyRef, value: AnyRef, mode: MemoryOrder): AnyRef = unsupportedSignature("exchangeReference")
  def addReference(receiver: AnyRef, value: AnyRef, mode: MemoryOrder): AnyRef = unsupportedSignature("addReference")
  def bitwiseReference(receiver: AnyRef, value: AnyRef, operation: BitwiseOperation, mode: MemoryOrder): AnyRef = unsupportedSignature("bitwiseReference")
}

/** Values shared by the compiler lowering and the Native implementation. */
private[runtime] object NativeVarHandle {

  type AccessOperation = Int
  object AccessOperation {
    final val Get = 0
    final val Set = 1
    final val Compare = 2
    final val WeakCompare = 3
    final val CompareExchange = 4
    final val Exchange = 5
    final val Add = 6
    final val Bitwise = 7
  }

  type MemoryOrder = Int
  object MemoryOrder {
    final val Plain = 0
    final val Volatile = 1
    final val Acquire = 2
    final val Release = 3
  }

  type BitwiseOperation = Int
  object BitwiseOperation {
    final val Or = 0
    final val And = 1
    final val Xor = 2
  }


}
