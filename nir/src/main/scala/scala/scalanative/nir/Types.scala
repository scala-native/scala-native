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

  final def show: String   = nir.Show(this)
  final def mangle: String = nir.Mangle(this)
}

object Type {

  /** Value types are either primitive or aggregate. */
  sealed abstract class ValueKind extends Type

  /** Primitive value types. */
  sealed abstract class PrimitiveKind(val width: Int) extends ValueKind
  final case object Bool                              extends PrimitiveKind(1)
  final case object Ptr                               extends PrimitiveKind(64)

  sealed abstract class I(width: Int, val signed: Boolean)
      extends PrimitiveKind(width)
  object I {
    def unapply(i: I): Some[(Int, Boolean)] = Some((i.width, i.signed))
  }
  final case object Char  extends I(16, signed = false)
  final case object Byte  extends I(8, signed = true)
  final case object Short extends I(16, signed = true)
  final case object Int   extends I(32, signed = true)
  final case object Long  extends I(64, signed = true)

  sealed abstract class F(width: Int) extends PrimitiveKind(width)
  object F { def unapply(f: F): Some[Int] = Some(f.width) }
  final case object Float  extends F(32)
  final case object Double extends F(64)

  /** Aggregate value types. */
  sealed abstract class AggregateKind           extends ValueKind
  final case class ArrayValue(ty: Type, n: Int) extends AggregateKind
  final case class StructValue(tys: Seq[Type])  extends AggregateKind

  /** Reference types. */
  sealed abstract class RefKind extends Type {
    final def className: Global = this match {
      case Type.Null            => Rt.BoxedNull.name
      case Type.Unit            => Rt.BoxedUnit.name
      case Type.Array(ty, _)    => toArrayClass(ty)
      case Type.Ref(name, _, _) => name
    }
    final def isExact: Boolean = this match {
      case Type.Null         => true
      case Type.Unit         => true
      case _: Type.Array     => true
      case Type.Ref(_, e, _) => e
    }
    final def isNullable: Boolean = this match {
      case Type.Null         => true
      case Type.Unit         => false
      case Type.Array(_, n)  => n
      case Type.Ref(_, _, n) => n
    }
  }
  final case object Null                                     extends RefKind
  final case object Unit                                     extends RefKind
  final case class Array(ty: Type, nullable: Boolean = true) extends RefKind
  final case class Ref(name: Global,
                       exact: Boolean = false,
                       nullable: Boolean = true)
      extends RefKind

  /** Second-class types. */
  sealed abstract class SpecialKind                     extends Type
  final case object Vararg                              extends SpecialKind
  final case object Nothing                             extends SpecialKind
  final case object Virtual                             extends SpecialKind
  final case class Var(ty: Type)                        extends SpecialKind
  final case class Function(args: Seq[Type], ret: Type) extends SpecialKind

  val boxesTo = Seq[(Type, Type)](
    Type.Ref(Global.Top("scala.scalanative.unsigned.UByte"))      -> Type.Byte,
    Type.Ref(Global.Top("scala.scalanative.unsigned.UShort"))     -> Type.Short,
    Type.Ref(Global.Top("scala.scalanative.unsigned.UInt"))       -> Type.Int,
    Type.Ref(Global.Top("scala.scalanative.unsigned.ULong"))      -> Type.Long,
    Type.Ref(Global.Top("scala.scalanative.unsafe.CArray"))       -> Type.Ptr,
    Type.Ref(Global.Top("scala.scalanative.unsafe.CVarArgList"))  -> Type.Ptr,
    Type.Ref(Global.Top("scala.scalanative.runtime.CFuncRawPtr")) -> Type.Ptr,
    Type.Ref(Global.Top("scala.scalanative.unsafe.Ptr"))          -> Type.Ptr,
    Type.Ref(Global.Top("java.lang.Boolean"))                     -> Type.Bool,
    Type.Ref(Global.Top("java.lang.Character"))                   -> Type.Char,
    Type.Ref(Global.Top("java.lang.Byte"))                        -> Type.Byte,
    Type.Ref(Global.Top("java.lang.Short"))                       -> Type.Short,
    Type.Ref(Global.Top("java.lang.Integer"))                     -> Type.Int,
    Type.Ref(Global.Top("java.lang.Long"))                        -> Type.Long,
    Type.Ref(Global.Top("java.lang.Float"))                       -> Type.Float,
    Type.Ref(Global.Top("java.lang.Double"))                      -> Type.Double
  )

  val unbox = boxesTo.toMap

  val box = boxesTo.map { case (l, r) => (r, l) }.toMap

  val boxClasses = unbox.keys.map {
    case ty: Type.Ref =>
      ty.name
    case _ =>
      unreachable
  }.toSeq

  def isPtrBox(ty: Type): Boolean = ty match {
    case refty: Type.RefKind =>
      box.get(Type.Ref(refty.className)) == Some(Type.Ptr)
    case _ =>
      false
  }

  val typeToArray = Map[Type, Global](
    Type.Bool    -> Global.Top("scala.scalanative.runtime.BooleanArray"),
    Type.Char    -> Global.Top("scala.scalanative.runtime.CharArray"),
    Type.Byte    -> Global.Top("scala.scalanative.runtime.ByteArray"),
    Type.Short   -> Global.Top("scala.scalanative.runtime.ShortArray"),
    Type.Int     -> Global.Top("scala.scalanative.runtime.IntArray"),
    Type.Long    -> Global.Top("scala.scalanative.runtime.LongArray"),
    Type.Float   -> Global.Top("scala.scalanative.runtime.FloatArray"),
    Type.Double  -> Global.Top("scala.scalanative.runtime.DoubleArray"),
    Rt.BoxedUnit -> Global.Top("scala.scalanative.runtime.BoxedUnitArray"),
    Rt.Object    -> Global.Top("scala.scalanative.runtime.ObjectArray")
  )
  val arrayToType =
    typeToArray.map { case (k, v) => (v, k) }
  def toArrayClass(ty: Type): Global = ty match {
    case _ if typeToArray.contains(ty) =>
      typeToArray(ty)
    case Type.Ref(name, _, _) if name == Rt.BoxedUnit =>
      typeToArray(Rt.BoxedUnit)
    case _ =>
      typeToArray(Rt.Object)
  }
  def fromArrayClass(name: Global): Option[Type] =
    arrayToType.get(name)
  def isArray(clsTy: Type.Ref): Boolean =
    isArray(clsTy.name)
  def isArray(clsName: Global): Boolean =
    arrayToType.contains(clsName)

  def typeToName(tpe: Type): Global = tpe match {
    case Rt.BoxedUnit       => Global.Top("scala.scalanative.runtime.PrimitiveUnit")
    case Bool               => Global.Top("scala.scalanative.runtime.PrimitiveBoolean")
    case Char               => Global.Top("scala.scalanative.runtime.PrimitiveChar")
    case Byte               => Global.Top("scala.scalanative.runtime.PrimitiveByte")
    case Short              => Global.Top("scala.scalanative.runtime.PrimitiveShort")
    case Int                => Global.Top("scala.scalanative.runtime.PrimitiveInt")
    case Long               => Global.Top("scala.scalanative.runtime.PrimitiveLong")
    case Float              => Global.Top("scala.scalanative.runtime.PrimitiveFloat")
    case Double             => Global.Top("scala.scalanative.runtime.PrimitiveDouble")
    case Ref(name, _, _)    => name
    case Array(tpe, _)      => toArrayClass(tpe)
    case ArrayValue(tpe, _) => toArrayClass(tpe)
    case Function(args, _)  => Global.Top(s"scala.Function${args.length}")
    case _ =>
      throw new Exception(s"typeToName: unexpected type ${tpe.show}")
  }
}
