package scala.scalanative.runtime

import scala.annotation.switch

import scala.scalanative.annotation.alwaysinline

// scalafmt: { maxColumn = 160}

/** Checked JVM-style argument adaptation, isolated from the atomic implementations. */
private[runtime] trait VarHandleAdaptation extends NativeVarHandle {
  import NativeVarHandle.{AccessOperation, BitwiseOperation, MemoryOrder}
  protected def invokeAdaptedOperation(
      operation: AccessOperation,
      receiver: AnyRef,
      expected: AnyRef,
      value: AnyRef,
      resultType: Class[_],
      mode: MemoryOrder,
      bitwiseOperation: BitwiseOperation
  ): AnyRef
  protected def variableType: Class[_]
  protected def boxedVariableType: Class[_]

  @alwaysinline override final def isExactVariableType(tpe: Class[_]): Boolean = variableType eq tpe

  // Validate the complete signature before converting arguments or mutating a field.
  @alwaysinline private def exactOperand(source: Class[_]): Boolean = source == variableType || (variableType.isPrimitive && source == boxedVariableType)
  @alwaysinline private def validate(source: Class[_], target: Class[_]): Unit =
    if (source != target && !VarHandleConversions.canConvert(source, target)) signatureMismatch(s"operation with ${source.getName} returning ${target.getName}")
  @alwaysinline private def operand(value: AnyRef, source: Class[_]): AnyRef =
    if (source == classOf[Unit]) null
    else if (source == variableType) value
    else if (variableType.isPrimitive && source == boxedVariableType) {
      if (value == null) throw new NullPointerException("null VarHandle operand")
      value
    } else VarHandleConversions.convert(value, source, variableType)

  override final def invokeAdapted(
      operation: AccessOperation,
      receiver: AnyRef,
      expected: AnyRef,
      value: AnyRef,
      expectedType: Class[_],
      valueType: Class[_],
      resultType: Class[_],
      mode: MemoryOrder,
      bitwiseOperation: BitwiseOperation
  ): AnyRef = {
    val returnsBoolean = operation == AccessOperation.Compare || operation == AccessOperation.WeakCompare
    val witnessType: Class[_] = if (returnsBoolean) classOf[Boolean] else variableType
    if (expectedType != classOf[Unit] && !exactOperand(expectedType)) validate(expectedType, variableType)
    if (valueType != classOf[Unit] && !exactOperand(valueType)) validate(valueType, variableType)
    if (resultType != classOf[Unit] && operation != AccessOperation.Set && resultType != witnessType &&
        !(witnessType == variableType && variableType.isPrimitive && resultType == boxedVariableType)) validate(witnessType, resultType)
    val convertedExpected = operand(expected, expectedType)
    val convertedValue = operand(value, valueType)
    invokeAdaptedOperation(operation, receiver, convertedExpected, convertedValue, resultType, mode, bitwiseOperation)
  }

  // Primitive signatures were checked before dispatch, so compatible reference
  // results need boxing only; exact primitive results need no further conversion.
  @alwaysinline private def primitiveResult(value: AnyRef, source: Class[_], target: Class[_]): AnyRef =
    if (source == target || !target.isPrimitive) value
    else VarHandleConversions.convert(value, source, target)

  // Boolean witnesses and operations
  @alwaysinline private def resultBoolean(value: Boolean, target: Class[_]): AnyRef =
    if (target == classOf[Unit]) null else primitiveResult(scala.runtime.BoxesRunTime.boxToBoolean(value), classOf[Boolean], target)
  @inline protected final def invokeBoolean(
      operation: AccessOperation,
      receiver: AnyRef,
      expected: AnyRef,
      value: AnyRef,
      resultType: Class[_],
      mode: MemoryOrder,
      bitwiseOperation: BitwiseOperation
  ): AnyRef = {
    def desired: Boolean = value.asInstanceOf[java.lang.Boolean].booleanValue()
    def witness: Boolean = expected.asInstanceOf[java.lang.Boolean].booleanValue()
    (operation: @switch) match {
      case AccessOperation.Get             => resultBoolean(getBoolean(receiver, mode), resultType)
      case AccessOperation.Set             => { setBoolean(receiver, desired, mode); null }
      case AccessOperation.Compare         => resultBoolean(compareBoolean(receiver, witness, desired, mode), resultType)
      case AccessOperation.WeakCompare     => resultBoolean(weakCompareBoolean(receiver, witness, desired, mode), resultType)
      case AccessOperation.CompareExchange => resultBoolean(compareExchangeBoolean(receiver, witness, desired, mode), resultType)
      case AccessOperation.Exchange        => resultBoolean(exchangeBoolean(receiver, desired, mode), resultType)
      case AccessOperation.Add             => resultBoolean(addBoolean(receiver, desired, mode), resultType)
      case AccessOperation.Bitwise         => resultBoolean(bitwiseBoolean(receiver, desired, bitwiseOperation, mode), resultType)
      case _                               => throw new IllegalArgumentException("invalid VarHandle operation")
    }
  }

  // Byte witnesses and operations
  @alwaysinline private def resultByte(value: Byte, target: Class[_]): AnyRef =
    if (target == classOf[Unit]) null else primitiveResult(scala.runtime.BoxesRunTime.boxToByte(value), classOf[Byte], target)
  @inline protected final def invokeByte(
      operation: AccessOperation,
      receiver: AnyRef,
      expected: AnyRef,
      value: AnyRef,
      resultType: Class[_],
      mode: MemoryOrder,
      bitwiseOperation: BitwiseOperation
  ): AnyRef = {
    def desired: Byte = value.asInstanceOf[java.lang.Byte].byteValue()
    def witness: Byte = expected.asInstanceOf[java.lang.Byte].byteValue()
    (operation: @switch) match {
      case AccessOperation.Get             => resultByte(getByte(receiver, mode), resultType)
      case AccessOperation.Set             => { setByte(receiver, desired, mode); null }
      case AccessOperation.Compare         => resultBoolean(compareByte(receiver, witness, desired, mode), resultType)
      case AccessOperation.WeakCompare     => resultBoolean(weakCompareByte(receiver, witness, desired, mode), resultType)
      case AccessOperation.CompareExchange => resultByte(compareExchangeByte(receiver, witness, desired, mode), resultType)
      case AccessOperation.Exchange        => resultByte(exchangeByte(receiver, desired, mode), resultType)
      case AccessOperation.Add             => resultByte(addByte(receiver, desired, mode), resultType)
      case AccessOperation.Bitwise         => resultByte(bitwiseByte(receiver, desired, bitwiseOperation, mode), resultType)
      case _                               => throw new IllegalArgumentException("invalid VarHandle operation")
    }
  }

  // Short witnesses and operations
  @alwaysinline private def resultShort(value: Short, target: Class[_]): AnyRef =
    if (target == classOf[Unit]) null else primitiveResult(scala.runtime.BoxesRunTime.boxToShort(value), classOf[Short], target)
  @inline protected final def invokeShort(
      operation: AccessOperation,
      receiver: AnyRef,
      expected: AnyRef,
      value: AnyRef,
      resultType: Class[_],
      mode: MemoryOrder,
      bitwiseOperation: BitwiseOperation
  ): AnyRef = {
    def desired: Short = value.asInstanceOf[java.lang.Short].shortValue()
    def witness: Short = expected.asInstanceOf[java.lang.Short].shortValue()
    (operation: @switch) match {
      case AccessOperation.Get             => resultShort(getShort(receiver, mode), resultType)
      case AccessOperation.Set             => { setShort(receiver, desired, mode); null }
      case AccessOperation.Compare         => resultBoolean(compareShort(receiver, witness, desired, mode), resultType)
      case AccessOperation.WeakCompare     => resultBoolean(weakCompareShort(receiver, witness, desired, mode), resultType)
      case AccessOperation.CompareExchange => resultShort(compareExchangeShort(receiver, witness, desired, mode), resultType)
      case AccessOperation.Exchange        => resultShort(exchangeShort(receiver, desired, mode), resultType)
      case AccessOperation.Add             => resultShort(addShort(receiver, desired, mode), resultType)
      case AccessOperation.Bitwise         => resultShort(bitwiseShort(receiver, desired, bitwiseOperation, mode), resultType)
      case _                               => throw new IllegalArgumentException("invalid VarHandle operation")
    }
  }

  // Char witnesses and operations
  @alwaysinline private def resultChar(value: Char, target: Class[_]): AnyRef =
    if (target == classOf[Unit]) null else primitiveResult(scala.runtime.BoxesRunTime.boxToCharacter(value), classOf[Char], target)
  @inline protected final def invokeChar(
      operation: AccessOperation,
      receiver: AnyRef,
      expected: AnyRef,
      value: AnyRef,
      resultType: Class[_],
      mode: MemoryOrder,
      bitwiseOperation: BitwiseOperation
  ): AnyRef = {
    def desired: Char = value.asInstanceOf[java.lang.Character].charValue()
    def witness: Char = expected.asInstanceOf[java.lang.Character].charValue()
    (operation: @switch) match {
      case AccessOperation.Get             => resultChar(getChar(receiver, mode), resultType)
      case AccessOperation.Set             => { setChar(receiver, desired, mode); null }
      case AccessOperation.Compare         => resultBoolean(compareChar(receiver, witness, desired, mode), resultType)
      case AccessOperation.WeakCompare     => resultBoolean(weakCompareChar(receiver, witness, desired, mode), resultType)
      case AccessOperation.CompareExchange => resultChar(compareExchangeChar(receiver, witness, desired, mode), resultType)
      case AccessOperation.Exchange        => resultChar(exchangeChar(receiver, desired, mode), resultType)
      case AccessOperation.Add             => resultChar(addChar(receiver, desired, mode), resultType)
      case AccessOperation.Bitwise         => resultChar(bitwiseChar(receiver, desired, bitwiseOperation, mode), resultType)
      case _                               => throw new IllegalArgumentException("invalid VarHandle operation")
    }
  }

  // Int witnesses and operations
  @alwaysinline private def resultInt(value: Int, target: Class[_]): AnyRef =
    if (target == classOf[Unit]) null else primitiveResult(scala.runtime.BoxesRunTime.boxToInteger(value), classOf[Int], target)
  @inline protected final def invokeInt(
      operation: AccessOperation,
      receiver: AnyRef,
      expected: AnyRef,
      value: AnyRef,
      resultType: Class[_],
      mode: MemoryOrder,
      bitwiseOperation: BitwiseOperation
  ): AnyRef = {
    def desired: Int = value.asInstanceOf[java.lang.Integer].intValue()
    def witness: Int = expected.asInstanceOf[java.lang.Integer].intValue()
    (operation: @switch) match {
      case AccessOperation.Get             => resultInt(getInt(receiver, mode), resultType)
      case AccessOperation.Set             => { setInt(receiver, desired, mode); null }
      case AccessOperation.Compare         => resultBoolean(compareInt(receiver, witness, desired, mode), resultType)
      case AccessOperation.WeakCompare     => resultBoolean(weakCompareInt(receiver, witness, desired, mode), resultType)
      case AccessOperation.CompareExchange => resultInt(compareExchangeInt(receiver, witness, desired, mode), resultType)
      case AccessOperation.Exchange        => resultInt(exchangeInt(receiver, desired, mode), resultType)
      case AccessOperation.Add             => resultInt(addInt(receiver, desired, mode), resultType)
      case AccessOperation.Bitwise         => resultInt(bitwiseInt(receiver, desired, bitwiseOperation, mode), resultType)
      case _                               => throw new IllegalArgumentException("invalid VarHandle operation")
    }
  }

  // Long witnesses and operations
  @alwaysinline private def resultLong(value: Long, target: Class[_]): AnyRef =
    if (target == classOf[Unit]) null else primitiveResult(scala.runtime.BoxesRunTime.boxToLong(value), classOf[Long], target)
  @inline protected final def invokeLong(
      operation: AccessOperation,
      receiver: AnyRef,
      expected: AnyRef,
      value: AnyRef,
      resultType: Class[_],
      mode: MemoryOrder,
      bitwiseOperation: BitwiseOperation
  ): AnyRef = {
    def desired: Long = value.asInstanceOf[java.lang.Long].longValue()
    def witness: Long = expected.asInstanceOf[java.lang.Long].longValue()
    (operation: @switch) match {
      case AccessOperation.Get             => resultLong(getLong(receiver, mode), resultType)
      case AccessOperation.Set             => { setLong(receiver, desired, mode); null }
      case AccessOperation.Compare         => resultBoolean(compareLong(receiver, witness, desired, mode), resultType)
      case AccessOperation.WeakCompare     => resultBoolean(weakCompareLong(receiver, witness, desired, mode), resultType)
      case AccessOperation.CompareExchange => resultLong(compareExchangeLong(receiver, witness, desired, mode), resultType)
      case AccessOperation.Exchange        => resultLong(exchangeLong(receiver, desired, mode), resultType)
      case AccessOperation.Add             => resultLong(addLong(receiver, desired, mode), resultType)
      case AccessOperation.Bitwise         => resultLong(bitwiseLong(receiver, desired, bitwiseOperation, mode), resultType)
      case _                               => throw new IllegalArgumentException("invalid VarHandle operation")
    }
  }

  // Float witnesses and operations
  @alwaysinline private def resultFloat(value: Float, target: Class[_]): AnyRef =
    if (target == classOf[Unit]) null else primitiveResult(scala.runtime.BoxesRunTime.boxToFloat(value), classOf[Float], target)
  @inline protected final def invokeFloat(
      operation: AccessOperation,
      receiver: AnyRef,
      expected: AnyRef,
      value: AnyRef,
      resultType: Class[_],
      mode: MemoryOrder,
      bitwiseOperation: BitwiseOperation
  ): AnyRef = {
    def desired: Float = value.asInstanceOf[java.lang.Float].floatValue()
    def witness: Float = expected.asInstanceOf[java.lang.Float].floatValue()
    (operation: @switch) match {
      case AccessOperation.Get             => resultFloat(getFloat(receiver, mode), resultType)
      case AccessOperation.Set             => { setFloat(receiver, desired, mode); null }
      case AccessOperation.Compare         => resultBoolean(compareFloat(receiver, witness, desired, mode), resultType)
      case AccessOperation.WeakCompare     => resultBoolean(weakCompareFloat(receiver, witness, desired, mode), resultType)
      case AccessOperation.CompareExchange => resultFloat(compareExchangeFloat(receiver, witness, desired, mode), resultType)
      case AccessOperation.Exchange        => resultFloat(exchangeFloat(receiver, desired, mode), resultType)
      case AccessOperation.Add             => resultFloat(addFloat(receiver, desired, mode), resultType)
      case AccessOperation.Bitwise         => resultFloat(bitwiseFloat(receiver, desired, bitwiseOperation, mode), resultType)
      case _                               => throw new IllegalArgumentException("invalid VarHandle operation")
    }
  }

  // Double witnesses and operations
  @alwaysinline private def resultDouble(value: Double, target: Class[_]): AnyRef =
    if (target == classOf[Unit]) null else primitiveResult(scala.runtime.BoxesRunTime.boxToDouble(value), classOf[Double], target)
  @inline protected final def invokeDouble(
      operation: AccessOperation,
      receiver: AnyRef,
      expected: AnyRef,
      value: AnyRef,
      resultType: Class[_],
      mode: MemoryOrder,
      bitwiseOperation: BitwiseOperation
  ): AnyRef = {
    def desired: Double = value.asInstanceOf[java.lang.Double].doubleValue()
    def witness: Double = expected.asInstanceOf[java.lang.Double].doubleValue()
    (operation: @switch) match {
      case AccessOperation.Get             => resultDouble(getDouble(receiver, mode), resultType)
      case AccessOperation.Set             => { setDouble(receiver, desired, mode); null }
      case AccessOperation.Compare         => resultBoolean(compareDouble(receiver, witness, desired, mode), resultType)
      case AccessOperation.WeakCompare     => resultBoolean(weakCompareDouble(receiver, witness, desired, mode), resultType)
      case AccessOperation.CompareExchange => resultDouble(compareExchangeDouble(receiver, witness, desired, mode), resultType)
      case AccessOperation.Exchange        => resultDouble(exchangeDouble(receiver, desired, mode), resultType)
      case AccessOperation.Add             => resultDouble(addDouble(receiver, desired, mode), resultType)
      case AccessOperation.Bitwise         => resultDouble(bitwiseDouble(receiver, desired, bitwiseOperation, mode), resultType)
      case _                               => throw new IllegalArgumentException("invalid VarHandle operation")
    }
  }

  // Reference witnesses and operations
  @alwaysinline private def resultReference(value: AnyRef, target: Class[_]): AnyRef =
    if (target == classOf[Unit]) null else if (target == variableType) value else VarHandleConversions.convert(value, variableType, target)
  @inline protected final def invokeReference(
      operation: AccessOperation,
      receiver: AnyRef,
      expected: AnyRef,
      value: AnyRef,
      resultType: Class[_],
      mode: MemoryOrder,
      bitwiseOperation: BitwiseOperation
  ): AnyRef = {
    def desired: AnyRef = value
    def witness: AnyRef = expected
    (operation: @switch) match {
      case AccessOperation.Get             => resultReference(getReference(receiver, mode), resultType)
      case AccessOperation.Set             => { setReference(receiver, desired, mode); null }
      case AccessOperation.Compare         => resultBoolean(compareReference(receiver, witness, desired, mode), resultType)
      case AccessOperation.WeakCompare     => resultBoolean(weakCompareReference(receiver, witness, desired, mode), resultType)
      case AccessOperation.CompareExchange => resultReference(compareExchangeReference(receiver, witness, desired, mode), resultType)
      case AccessOperation.Exchange        => resultReference(exchangeReference(receiver, desired, mode), resultType)
      case AccessOperation.Add             => resultReference(addReference(receiver, desired, mode), resultType)
      case AccessOperation.Bitwise         => resultReference(bitwiseReference(receiver, desired, bitwiseOperation, mode), resultType)
      case _                               => throw new IllegalArgumentException("invalid VarHandle operation")
    }
  }
}
