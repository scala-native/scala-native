package native
package nir

sealed trait Type
object Type {
  final case object None                                extends Type
  final case object Void                                extends Type
  final case object Bool                                extends Type
  final case object I8                                  extends Type
  final case object I16                                 extends Type
  final case object I32                                 extends Type
  final case object I64                                 extends Type
  final case object F32                                 extends Type
  final case object F64                                 extends Type
  final case class Array   (ty: Defn, n: Int)           extends Type
  final case class Ptr     (ty: Defn)                   extends Type
  final case class Function(ret: Defn, args: Seq[Defn]) extends Type
  final case class Struct  (name: Name)                 extends Type

  // scala
  final case object Unit                extends Type
  final case object Nothing             extends Type
  final case object Null                extends Type
  final case class Class(name: Name)    extends Type
  final case class ArrayClass(ty: Type) extends Type
}
