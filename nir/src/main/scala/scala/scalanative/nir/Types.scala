package scala.scalanative
package nir

import util.unsupported

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

  final def show: String   = nir.Show(this)
  final def mangle: String = nir.Mangle(this)
}

object Type {
  final case object None extends Type

  // low-level second-class types

  final case object Void   extends Type
  final case object Vararg extends Type

  // low-level primitive types

  sealed abstract class Primitive(val width: Int) extends Type
  final case object Ptr                           extends Type

  sealed abstract class I(width: Int, val signed: Boolean)
      extends Primitive(width)
  object I {
    def unapply(i: I): Some[(Int, Boolean)] = Some((i.width, i.signed))
  }
  final case object Bool   extends I(1, signed = false)
  final case object Char   extends I(16, signed = false)
  final case object Byte   extends I(8, signed = true)
  final case object UByte  extends I(8, signed = false)
  final case object Short  extends I(16, signed = true)
  final case object UShort extends I(16, signed = false)
  final case object Int    extends I(32, signed = true)
  final case object UInt   extends I(32, signed = false)
  final case object Long   extends I(64, signed = true)
  final case object ULong  extends I(64, signed = false)

  sealed abstract class F(width: Int) extends Primitive(width)
  object F { def unapply(f: F): Some[Int] = Some(f.width) }
  final case object Float  extends F(32)
  final case object Double extends F(64)

  // low-level composite types

  final case class ArrayValue(ty: Type, n: Int)         extends Type
  final case class StructValue(tys: Seq[Type])          extends Type
  final case class Function(args: Seq[Type], ret: Type) extends Type

  // high-level types

  final case object Null         extends Type
  final case object Nothing      extends Type
  final case object Virtual      extends Type
  final case class Var(ty: Type) extends Type

  sealed abstract class RefKind                              extends Type
  final case object Unit                                     extends RefKind
  final case class Array(ty: Type, nullable: Boolean = true) extends RefKind
  final case class Ref(name: Global,
                       exact: Boolean = false,
                       nullable: Boolean = true)
      extends RefKind

  val unbox = Map[Type, Type](
    Type.Ref(Global.Top("java.lang.Boolean"))               -> Type.Bool,
    Type.Ref(Global.Top("java.lang.Character"))             -> Type.Char,
    Type.Ref(Global.Top("scala.scalanative.native.UByte"))  -> Type.UByte,
    Type.Ref(Global.Top("java.lang.Byte"))                  -> Type.Byte,
    Type.Ref(Global.Top("scala.scalanative.native.UShort")) -> Type.UShort,
    Type.Ref(Global.Top("java.lang.Short"))                 -> Type.Short,
    Type.Ref(Global.Top("scala.scalanative.native.UInt"))   -> Type.UInt,
    Type.Ref(Global.Top("java.lang.Integer"))               -> Type.Int,
    Type.Ref(Global.Top("scala.scalanative.native.ULong"))  -> Type.ULong,
    Type.Ref(Global.Top("java.lang.Long"))                  -> Type.Long,
    Type.Ref(Global.Top("java.lang.Float"))                 -> Type.Float,
    Type.Ref(Global.Top("java.lang.Double"))                -> Type.Double
  )

  val box = unbox.map { case (k, v) => (v, k) }

  val typeToArray = Map[Type, Global](
    Type.Bool   -> Global.Top("scala.scalanative.runtime.BooleanArray"),
    Type.Char   -> Global.Top("scala.scalanative.runtime.CharArray"),
    Type.Byte   -> Global.Top("scala.scalanative.runtime.ByteArray"),
    Type.Short  -> Global.Top("scala.scalanative.runtime.ShortArray"),
    Type.Int    -> Global.Top("scala.scalanative.runtime.IntArray"),
    Type.Long   -> Global.Top("scala.scalanative.runtime.LongArray"),
    Type.Float  -> Global.Top("scala.scalanative.runtime.FloatArray"),
    Type.Double -> Global.Top("scala.scalanative.runtime.DoubleArray"),
    Type.Unit   -> Global.Top("scala.scalanative.runtime.UnitArray"),
    Rt.Object   -> Global.Top("scala.scalanative.runtime.ObjectArray")
  )
  val arrayToType =
    typeToArray.map { case (k, v) => (v, k) }
  def toArrayClass(ty: Type): Global = ty match {
    case _ if typeToArray.contains(ty) =>
      typeToArray(ty)
    case Type.Ref(name, _, _)
        if name == Global.Top("scala.runtime.BoxedUnit") =>
      typeToArray(Type.Unit)
    case _ =>
      typeToArray(Rt.Object)
  }
  def fromArrayClass(name: Global): Option[Type] =
    arrayToType.get(name)
  def isArray(clsTy: Type.Ref): Boolean =
    isArray(clsTy.name)
  def isArray(clsName: Global): Boolean =
    arrayToType.contains(clsName)
}
