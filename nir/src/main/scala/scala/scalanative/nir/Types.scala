package scala.scalanative
package nir

import util.{unreachable, unsupported}

sealed abstract class Type {

  final def elemty(path: Seq[Val]): Type = (this, path) match {
    case (_, Seq()) =>
      this
    case (Type.StructValue(tys), Val.Int(idx) +: rest) =>
      tys(idx).elemty(rest)
    case (Type.ArrayValue(ty, n), idx +: rest) =>
      ty.elemty(rest)
    case _ =>
      unsupported(s"${this}.elemty($path)")
  }

  def =?=(other: Type) = Type.normalize(this) == Type.normalize(other)

  def hasKnownSize: Boolean = this match {
    case Type.Null | Type.Ptr   => true
    case _: Type.RefKind        => false
    case Type.ArrayValue(ty, _) => ty.hasKnownSize
    case Type.StructValue(tys)  => tys.forall(_.hasKnownSize)
    case _                      => true
  }

  /** A textual representation of `this`. */
  final def show: String = nir.Show(this)

  /** The mangled representation of `this`. */
  final def mangle: String = nir.Mangle(this)

}

object Type {

  /** The type of an aggregate or primitive value. */
  sealed abstract class ValueKind extends Type

  /** A primitive value type.
   *
   *  @param width
   *    The bit width of the type's instances.
   */
  sealed abstract class PrimitiveKind(val width: Int) extends ValueKind

  /** The type of an integer. */
  sealed trait I extends ValueKind {

    /** `true` iff instances of this type are signed. */
    val signed: Boolean

  }

  /** The type of a fixed-size integer.
   *
   *  @param width
   *    The bit width of the type's instances.
   *  @param signed
   *    `true` iff the type's instances are signed.
   */
  sealed abstract class FixedSizeI(width: Int, val signed: Boolean)
      extends PrimitiveKind(width)
      with I

  /** The type of a floating-point number.
   *
   *  @param width
   *    The bit width of the type's instances.
   */
  sealed abstract class F(width: Int) extends PrimitiveKind(width)

  /** The type of pointers. */
  case object Ptr extends ValueKind

  /** The type of Boolean values. */
  case object Bool extends PrimitiveKind(1)

  /** The type of a value suitable to represent the size of a container. */
  case object Size extends ValueKind with I {
    val signed = true
  }

  /** The type of a 16-bit unsigned integer. */
  case object Char extends FixedSizeI(16, signed = false)

  /** The type of a 8-bit signed integer. */
  case object Byte extends FixedSizeI(8, signed = true)

  /** The type of a 16-bit signed integer. */
  case object Short extends FixedSizeI(16, signed = true)

  /** The type of a 32-bit signed integer. */
  case object Int extends FixedSizeI(32, signed = true)

  /** The type of a 64-bit signed integer. */
  case object Long extends FixedSizeI(64, signed = true)

  /** The type of a 128-bit signed integer. */
  case object Int128 extends FixedSizeI(128, signed = true)

  /** The type of a 32-bit IEEE 754 single-precision float. */
  case object Float extends F(32)

  /** The type of a 64-bit IEEE 754 single-precision float. */
  case object Double extends F(64)

  /** The type of an aggregate. */
  sealed abstract class AggregateKind extends ValueKind

  /** The type of a homogeneous collection of data members. */
  final case class ArrayValue(ty: Type, n: Int) extends AggregateKind

  /** The type of a heterogeneous collection of data members. */
  final case class StructValue(tys: Seq[Type]) extends AggregateKind

  /** A reference type. */
  sealed abstract class RefKind extends Type {

    /** The identifier of the class corresponding to this type. */
    final def className: Global.Top = this match {
      case Type.Null     => Rt.BoxedNull.name
      case Type.Unit     => Rt.BoxedUnit.name
      case a: Type.Array => toArrayClass(a.ty)
      case r: Type.Ref   => r.name
    }

    /** `true` iff the referenced type is exactly the type denoted by `this`.
     *
     *  Given an instance `r` of `RefKind` denoting a reference to some time
     *  `T`, `r.isExact` holds iff the referenced type is exactly `T` and not a
     *  subtype thereof. The optimizer may be able to compute the exact variant
     *  of an arbitrary reference after it has replaced a virtual call.
     */
    final def isExact: Boolean = this match {
      case Type.Null     => true
      case Type.Unit     => true
      case _: Type.Array => true
      case r: Type.Ref   => r.exact
    }

    /** `true` iff instances of this type are nullable. */
    final def isNullable: Boolean = this match {
      case Type.Null     => true
      case Type.Unit     => false
      case a: Type.Array => a.nullable
      case r: Type.Ref   => r.nullable
    }

  }

  /** The null reference type. */
  case object Null extends RefKind

  /** The unit type. */
  case object Unit extends RefKind

  /** The type of an array reference.
   *
   *  An `Array` is a reference to `scala.Array[T]`. It contains a header
   *  followed by a tail allocated buffer, which typically sit on the heap. That
   *  is unlike `ArrayValue`, which corresponds to LLVM's fixed-size array type.
   */
  final case class Array(ty: Type, nullable: Boolean = true) extends RefKind

  /** The type of a reference. */
  final case class Ref(
      name: Global.Top,
      exact: Boolean = false,
      nullable: Boolean = true
  ) extends RefKind

  /** Second-class types. */
  sealed abstract class SpecialKind extends Type
  case object Vararg extends SpecialKind
  case object Nothing extends SpecialKind
  case object Virtual extends SpecialKind
  final case class Var(ty: Type) extends SpecialKind
  final case class Function(args: Seq[Type], ret: Type) extends SpecialKind

  object unsigned {
    val Size = Type.Ref(Global.Top("scala.scalanative.unsigned.USize"))
    val Byte = Type.Ref(Global.Top("scala.scalanative.unsigned.UByte"))
    val Short = Type.Ref(Global.Top("scala.scalanative.unsigned.UShort"))
    val Int = Type.Ref(Global.Top("scala.scalanative.unsigned.UInt"))
    val Long = Type.Ref(Global.Top("scala.scalanative.unsigned.ULong"))

    val values: Set[nir.Type] = Set(Size, Byte, Short, Int, Long)
  }
  private val unsignedBoxesTo = Seq[(Type, Type)](
    unsigned.Size -> Type.Size,
    unsigned.Byte -> Type.Byte,
    unsigned.Short -> Type.Short,
    unsigned.Int -> Type.Int,
    unsigned.Long -> Type.Long
  )

  val boxesTo: Seq[(Type, Type)] = unsignedBoxesTo ++ Seq(
    Type.Ref(Global.Top("scala.scalanative.unsafe.CArray")) -> Type.Ptr,
    Type.Ref(Global.Top("scala.scalanative.unsafe.CVarArgList")) -> Type.Ptr,
    Type.Ref(Global.Top("scala.scalanative.unsafe.Ptr")) -> Type.Ptr,
    Type.Ref(Global.Top("java.lang.Boolean")) -> Type.Bool,
    Type.Ref(Global.Top("java.lang.Character")) -> Type.Char,
    Type.Ref(Global.Top("scala.scalanative.unsafe.Size")) -> Type.Size,
    Type.Ref(Global.Top("java.lang.Byte")) -> Type.Byte,
    Type.Ref(Global.Top("java.lang.Short")) -> Type.Short,
    Type.Ref(Global.Top("java.lang.Integer")) -> Type.Int,
    Type.Ref(Global.Top("java.lang.Long")) -> Type.Long,
    Type.Ref(Global.Top("java.lang.Float")) -> Type.Float,
    Type.Ref(Global.Top("java.lang.Double")) -> Type.Double
  ) ++ 0.until(22).map { n =>
    Type.Ref(Global.Top(s"scala.scalanative.unsafe.CFuncPtr$n")) -> Type.Ptr
  }

  val unbox = boxesTo.toMap

  val box = boxesTo.map { case (l, r) => (r, l) }.toMap

  val boxClasses = unbox.keys.map {
    case ty: Type.Ref =>
      ty.name
    case _ =>
      unreachable
  }.toSeq

  private[scalanative] def isBoxOf(primitiveType: Type)(boxType: Type) =
    unbox
      .get(normalize(boxType))
      .contains(primitiveType)
  def isPtrBox(ty: Type): Boolean = isBoxOf(Type.Ptr)(ty)
  def isPtrType(ty: Type): Boolean =
    ty == Type.Ptr || ty.isInstanceOf[Type.RefKind]
  def isSizeBox(ty: Type): Boolean = isBoxOf(Type.Size)(ty)
  def isUnsignedType(ty: Type): Boolean =
    unsigned.values.contains(normalize(ty))

  object NothingType {
    def unapply(v: nir.Type): Option[nir.Type] =
      if (isNothing(v)) Some(v) else None
  }
  def isNothing(ty: Type): Boolean = ty match {
    case nir.Type.Nothing         => true
    case nir.Type.Ref(name, _, _) => name == nir.Rt.RuntimeNothing.name
    case _                        => false
  }
  object NullType {
    def unapply(v: nir.Type): Option[nir.Type] =
      if (isNull(v)) Some(v) else None
  }
  def isNull(ty: Type): Boolean = ty match {
    case nir.Type.Null            => true
    case nir.Type.Ref(name, _, _) => name == nir.Rt.RuntimeNull.name
    case _                        => false
  }

  def normalize(ty: Type): Type = ty match {
    case ArrayValue(ty, n)          => ArrayValue(normalize(ty), n)
    case StructValue(tys)           => StructValue(tys.map(normalize))
    case Array(ty, nullable)        => Array(normalize(ty))
    case Ref(name, exact, nullable) => Ref(name)
    case Var(ty)                    => Var(normalize(ty))
    case Function(args, ret) => Function(args.map(normalize), normalize(ret))
    case other               => other
  }

  val typeToArray = Map[Type, Global.Top](
    Type.Bool -> Global.Top("scala.scalanative.runtime.BooleanArray"),
    Type.Char -> Global.Top("scala.scalanative.runtime.CharArray"),
    Type.Byte -> Global.Top("scala.scalanative.runtime.ByteArray"),
    Type.Short -> Global.Top("scala.scalanative.runtime.ShortArray"),
    Type.Int -> Global.Top("scala.scalanative.runtime.IntArray"),
    Type.Long -> Global.Top("scala.scalanative.runtime.LongArray"),
    Type.Float -> Global.Top("scala.scalanative.runtime.FloatArray"),
    Type.Double -> Global.Top("scala.scalanative.runtime.DoubleArray"),
    Rt.Object -> Global.Top("scala.scalanative.runtime.ObjectArray")
  )
  val arrayToType: Map[Global.Top, Type] =
    typeToArray.map { case (k, v) => (v, k) } ++ Map(
      Global.Top("scala.scalanative.runtime.BlobArray") -> Type.Byte
    )
  def toArrayClass(ty: Type): Global.Top = ty match {
    case _ if typeToArray.contains(ty) =>
      typeToArray(ty)
    case _ =>
      typeToArray(Rt.Object)
  }
  def fromArrayClass(name: Global.Top): Option[Type] =
    arrayToType.get(name)
  def isArray(clsTy: Type.Ref): Boolean =
    isArray(clsTy.name)
  def isArray(clsName: Global.Top): Boolean =
    arrayToType.contains(clsName)

  def typeToName(tpe: Type): Global.Top = tpe match {
    case Rt.BoxedUnit => Global.Top("scala.scalanative.runtime.PrimitiveUnit")
    case Bool   => Global.Top("scala.scalanative.runtime.PrimitiveBoolean")
    case Char   => Global.Top("scala.scalanative.runtime.PrimitiveChar")
    case Byte   => Global.Top("scala.scalanative.runtime.PrimitiveByte")
    case Short  => Global.Top("scala.scalanative.runtime.PrimitiveShort")
    case Int    => Global.Top("scala.scalanative.runtime.PrimitiveInt")
    case Long   => Global.Top("scala.scalanative.runtime.PrimitiveLong")
    case Float  => Global.Top("scala.scalanative.runtime.PrimitiveFloat")
    case Double => Global.Top("scala.scalanative.runtime.PrimitiveDouble")
    case Ref(name, _, _)    => name
    case Array(tpe, _)      => toArrayClass(tpe)
    case ArrayValue(tpe, _) => toArrayClass(tpe)
    case Function(args, _)  => Global.Top(s"scala.Function${args.length}")
    case _ =>
      throw new Exception(s"typeToName: unexpected type ${tpe.show}")
  }

}
