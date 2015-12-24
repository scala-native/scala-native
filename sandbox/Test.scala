package test

import java.{lang => jl}

class C {
  override def hashCode = 0
  override def toString = ""
  override def equals(other: Any) = false
}

object HashCode {
  def object_##(obj: jl.Object): Unit = obj.##
  def object_##(obj: C): Unit         = obj.##

  def box_##(box: jl.Boolean): Unit   = box.##
  def box_##(box: jl.Character): Unit = box.##
  def box_##(box: jl.Byte): Unit      = box.##
  def box_##(box: jl.Short): Unit     = box.##
  def box_##(box: jl.Integer): Unit   = box.##
  def box_##(box: jl.Long): Unit      = box.##
  def box_##(box: jl.Float): Unit     = box.##
  def box_##(box: jl.Double): Unit    = box.##

  def primitive_##(v: Boolean): Unit = v.##
  def primitive_##(v: Char): Unit    = v.##
  def primitive_##(v: Byte): Unit    = v.##
  def primitive_##(v: Short): Unit   = v.##
  def primitive_##(v: Int): Unit     = v.##
  def primitive_##(v: Long): Unit    = v.##
  def primitive_##(v: Float): Unit   = v.##
  def primitive_##(v: Double): Unit  = v.##

  def object_hashCode(obj: jl.Object): Unit = obj.hashCode
  def object_hashCode(obj: C): Unit         = obj.hashCode

  def box_hashCode(box: jl.Boolean): Unit   = box.hashCode
  def box_hashCode(box: jl.Character): Unit = box.hashCode
  def box_hashCode(box: jl.Byte): Unit      = box.hashCode
  def box_hashCode(box: jl.Short): Unit     = box.hashCode
  def box_hashCode(box: jl.Integer): Unit   = box.hashCode
  def box_hashCode(box: jl.Long): Unit      = box.hashCode
  def box_hashCode(box: jl.Float): Unit     = box.hashCode
  def box_hashCode(box: jl.Double): Unit    = box.hashCode

  def primitive_hashCode(v: Boolean): Unit = v.hashCode
  def primitive_hashCode(v: Char): Unit    = v.hashCode
  def primitive_hashCode(v: Byte): Unit    = v.hashCode
  def primitive_hashCode(v: Short): Unit   = v.hashCode
  def primitive_hashCode(v: Int): Unit     = v.hashCode
  def primitive_hashCode(v: Long): Unit    = v.hashCode
  def primitive_hashCode(v: Float): Unit   = v.hashCode
  def primitive_hashCode(v: Double): Unit  = v.hashCode
}

object ToString {
  def object_toString(obj: jl.Object): Unit = obj.toString
  def object_toString(obj: C): Unit         = obj.toString

  def box_toString(box: jl.Boolean): Unit   = box.toString
  def box_toString(box: jl.Character): Unit = box.toString
  def box_toString(box: jl.Byte): Unit      = box.toString
  def box_toString(box: jl.Short): Unit     = box.toString
  def box_toString(box: jl.Integer): Unit   = box.toString
  def box_toString(box: jl.Long): Unit      = box.toString
  def box_toString(box: jl.Float): Unit     = box.toString
  def box_toString(box: jl.Double): Unit    = box.toString

  def boxCompanion_toString(v: Boolean): Unit = jl.Boolean.toString(v)
  def boxCompanion_toString(v: Byte): Unit    = jl.Byte.toString(v)
  def boxCompanion_toString(v: Short): Unit   = jl.Short.toString(v)
  def boxCompanion_toString(v: Int): Unit     = jl.Integer.toString(v)
  def boxCompanion_toString(v: Long): Unit    = jl.Long.toString(v)
  def boxCompanion_toString(v: Float): Unit   = jl.Float.toString(v)
  def boxCompanion_toString(v: Double): Unit  = jl.Double.toString(v)

  def boxCompanion_toStringWithRadix(v: Int): Unit  = jl.Integer.toString(v, 10)
  def boxCompanion_toStringWithRadix(v: Long): Unit = jl.Long.toString(v, 10)

  def boxCompanion_toUnsignedString(v: Int): Unit  = jl.Integer.toUnsignedString(v)
  def boxCompanion_toUnsignedString(v: Long): Unit = jl.Long.toUnsignedString(v)

  def boxCompanion_toUnsignedStringWithRadix(v: Int): Unit  = jl.Integer.toUnsignedString(v, 10)
  def boxCompanion_toUnsignedStringWithRadix(v: Long): Unit = jl.Long.toUnsignedString(v, 10)

  def primitive_toString(v: Boolean): Unit = v.toString
  def primitive_toString(v: Char): Unit    = v.toString
  def primitive_toString(v: Byte): Unit    = v.toString
  def primitive_toString(v: Short): Unit   = v.toString
  def primitive_toString(v: Int): Unit     = v.toString
  def primitive_toString(v: Long): Unit    = v.toString
  def primitive_toString(v: Float): Unit   = v.toString
  def primitive_toString(v: Double): Unit  = v.toString
}

object FromString {
  def boxFromString: Unit = {
    new jl.Boolean("false")
    new jl.Byte("0")
    new jl.Short("0")
    new jl.Integer("0")
    new jl.Long("0")
    new jl.Float("0.0")
    new jl.Double("0.0")
  }

  def parse: Unit = {
    jl.Boolean.parseBoolean("false")
    jl.Byte.parseByte("0")
    jl.Byte.parseByte("0", 10)
    jl.Short.parseShort("0")
    jl.Short.parseShort("0", 10)
    jl.Integer.parseInt("0")
    jl.Integer.parseInt("0", 10)
    jl.Long.parseLong("0")
    jl.Long.parseLong("0", 10)
    jl.Float.parseFloat("0.0")
    jl.Double.parseDouble("0.0")
  }

  def boxStringValueOf: Unit = {
    jl.Boolean.valueOf("false")
    jl.Byte.valueOf("0")
    jl.Byte.valueOf("0", 10)
    jl.Short.valueOf("0")
    jl.Short.valueOf("0", 10)
    jl.Integer.valueOf("0")
    jl.Integer.valueOf("0", 10)
    jl.Long.valueOf("0")
    jl.Long.valueOf("0", 10)
    jl.Float.valueOf("0.0")
    jl.Double.valueOf("0.0")
  }

  def unsignedParse: Unit = {
    jl.Integer.parseUnsignedInt("0")
    jl.Integer.parseUnsignedInt("0", 10)
    jl.Long.parseUnsignedLong("0")
    jl.Long.parseUnsignedLong("0", 10)
  }
}

object BoxingOps {
  def boxFromValue: Unit = {
    new jl.Boolean(false)
    new jl.Character('0')
    new jl.Byte(0.toByte)
    new jl.Short(0.toShort)
    new jl.Integer(0)
    new jl.Long(0L)
    new jl.Float(0.0f)
    new jl.Double(0.0d)
  }

  def boxValueOf: Unit = {
    jl.Boolean.valueOf(false)
    jl.Character.valueOf('0')
    jl.Byte.valueOf(0.toByte)
    jl.Short.valueOf(0.toShort)
    jl.Integer.valueOf(0)
    jl.Long.valueOf(0L)
    jl.Float.valueOf(0.0f)
    jl.Double.valueOf(0.0d)
  }

  def unboxBoolean(box: jl.Boolean): Unit = box.booleanValue
  def unboxCharacter(box: jl.Character): Unit = box.charValue
  def unboxByte(box: jl.Byte): Unit = {
    box.byteValue
    box.shortValue
    box.intValue
    box.longValue
    box.floatValue
    box.doubleValue
  }
  def unboxShort(box: jl.Short): Unit = {
    box.byteValue
    box.shortValue
    box.intValue
    box.longValue
    box.floatValue
    box.doubleValue
  }
  def unboxInteger(box: jl.Integer): Unit = {
    box.byteValue
    box.shortValue
    box.intValue
    box.longValue
    box.floatValue
    box.doubleValue
  }
  def unboxLong(box: jl.Long): Unit = {
    box.byteValue
    box.shortValue
    box.intValue
    box.longValue
    box.floatValue
    box.doubleValue
  }
}

object IsInstanceOf {
  def object_isInstanceOf(obj: jl.Object): Unit = obj.isInstanceOf[C]
  def object_isInstanceOf(obj: C): Unit         = obj.isInstanceOf[C]

  def box_isInstanceOf(box: jl.Boolean): Unit   = box.isInstanceOf[C]
  def box_isInstanceOf(box: jl.Character): Unit = box.isInstanceOf[C]
  def box_isInstanceOf(box: jl.Byte): Unit      = box.isInstanceOf[C]
  def box_isInstanceOf(box: jl.Short): Unit     = box.isInstanceOf[C]
  def box_isInstanceOf(box: jl.Integer): Unit   = box.isInstanceOf[C]
  def box_isInstanceOf(box: jl.Long): Unit      = box.isInstanceOf[C]
  def box_isInstanceOf(box: jl.Float): Unit     = box.isInstanceOf[C]
  def box_isInstanceOf(box: jl.Double): Unit    = box.isInstanceOf[C]
}

object AsInstanceOf {
  def object_asInstanceOf(obj: jl.Object): Unit = obj.asInstanceOf[C]
  def object_asInstanceOf(obj: C): Unit         = obj.asInstanceOf[C]

  def box_asInstanceOf(box: jl.Boolean): Unit   = box.asInstanceOf[C]
  def box_asInstanceOf(box: jl.Character): Unit = box.asInstanceOf[C]
  def box_asInstanceOf(box: jl.Byte): Unit      = box.asInstanceOf[C]
  def box_asInstanceOf(box: jl.Short): Unit     = box.asInstanceOf[C]
  def box_asInstanceOf(box: jl.Integer): Unit   = box.asInstanceOf[C]
  def box_asInstanceOf(box: jl.Long): Unit      = box.asInstanceOf[C]
  def box_asInstanceOf(box: jl.Float): Unit     = box.asInstanceOf[C]
  def box_asInstanceOf(box: jl.Double): Unit    = box.asInstanceOf[C]

  def primitive_asInstanceOf(v: Boolean): Unit = v.asInstanceOf[C]
  def primitive_asInstanceOf(v: Char): Unit    = v.asInstanceOf[C]
  def primitive_asInstanceOf(v: Byte): Unit    = v.asInstanceOf[C]
  def primitive_asInstanceOf(v: Short): Unit   = v.asInstanceOf[C]
  def primitive_asInstanceOf(v: Int): Unit     = v.asInstanceOf[C]
  def primitive_asInstanceOf(v: Long): Unit    = v.asInstanceOf[C]
  def primitive_asInstanceOf(v: Float): Unit   = v.asInstanceOf[C]
  def primitive_asInstanceOf(v: Double): Unit  = v.asInstanceOf[C]
}

object UnsignedOps {
  def Int_divideUnsigned: Unit     = jl.Integer.divideUnsigned(0, 0)
  def Int_remainderUnsigned: Unit  = jl.Integer.remainderUnsigned(0, 0)
  def Long_divideUnsigned: Unit    = jl.Long.divideUnsigned(0L, 0L)
  def Long_remainderUnsigned: Unit = jl.Long.remainderUnsigned(0L, 0L)

  def Byte_toUnsignedInt: Unit   = jl.Byte.toUnsignedInt(0.toByte)
  def Byte_toUnsignedLong: Unit  = jl.Byte.toUnsignedLong(0.toByte)
  def Short_toUnsignedInt: Unit  = jl.Short.toUnsignedInt(0.toShort)
  def Short_toUnsignedLong: Unit = jl.Short.toUnsignedLong(0.toShort)
  def Int_toUnsignedLong: Unit   = jl.Integer.toUnsignedLong(0)
}

object Equals {
  def object_equals(l: jl.Object, r: jl.Object): Unit = l equals r
  def object_equals(l: C, r: C): Unit                 = l equals r

  def box_equals(l: jl.Boolean, r: jl.Boolean): Unit     = l equals r
  def box_equals(l: jl.Character, r: jl.Character): Unit = l equals r
  def box_equals(l: jl.Byte, r: jl.Byte): Unit           = l equals r
  def box_equals(l: jl.Short, r: jl.Short): Unit         = l equals r
  def box_equals(l: jl.Integer, r: jl.Integer): Unit     = l equals r
  def box_equals(l: jl.Long, r: jl.Long): Unit           = l equals r
  def box_equals(l: jl.Float, r: jl.Float): Unit         = l equals r
  def box_equals(l: jl.Double, r: jl.Double): Unit       = l equals r

  def primitive_equals(l: Boolean, r: Boolean): Unit = l equals r
  def primitive_equals(l: Char, r: Char): Unit       = l equals r
  def primitive_equals(l: Byte, r: Byte): Unit       = l equals r
  def primitive_equals(l: Short, r: Short): Unit     = l equals r
  def primitive_equals(l: Int, r: Int): Unit         = l equals r
  def primitive_equals(l: Long, r: Long): Unit       = l equals r
  def primitive_equals(l: Float, r: Float): Unit     = l equals r
  def primitive_equals(l: Double, r: Double): Unit   = l equals r

  def object_==(l: jl.Object, r: jl.Object): Unit = l == r
  def object_==(l: C, r: C): Unit                 = l == r

  def box_==(l: jl.Boolean, r: jl.Boolean): Unit     = l == r
  def box_==(l: jl.Character, r: jl.Character): Unit = l == r
  def box_==(l: jl.Byte, r: jl.Byte): Unit           = l == r
  def box_==(l: jl.Short, r: jl.Short): Unit         = l == r
  def box_==(l: jl.Integer, r: jl.Integer): Unit     = l == r
  def box_==(l: jl.Long, r: jl.Long): Unit           = l == r
  def box_==(l: jl.Float, r: jl.Float): Unit         = l == r
  def box_==(l: jl.Double, r: jl.Double): Unit       = l == r

  def primitive_==(l: Boolean, r: Boolean): Unit = l == r
  def primitive_==(l: Char, r: Char): Unit       = l == r
  def primitive_==(l: Byte, r: Byte): Unit       = l == r
  def primitive_==(l: Short, r: Short): Unit     = l == r
  def primitive_==(l: Int, r: Int): Unit         = l == r
  def primitive_==(l: Long, r: Long): Unit       = l == r
  def primitive_==(l: Float, r: Float): Unit     = l == r
  def primitive_==(l: Double, r: Double): Unit   = l == r

  def object_eq(l: jl.Object, r: jl.Object): Unit = l eq r
  def object_eq(l: C, r: C): Unit                 = l eq r

  def object_ne(l: jl.Object, r: jl.Object): Unit = l ne r
  def object_ne(l: C, r: C): Unit                 = l ne r
}

// TODO:
// def primitiveCompare: Unit = ???
// def boxedCompare: Unit = ???
// def unsignedCompare: Unit = ???
