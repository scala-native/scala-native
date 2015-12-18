package native
package nir

sealed abstract class Val
object Val {
  final case object None                    extends Val
  final case object Zero                    extends Val //
  final case object True                    extends Val // bool
  final case object False                   extends Val // bool
  final case class I8    (value: Byte)      extends Val // i8
  final case class I16   (value: Short)     extends Val // i16
  final case class I32   (value: Int)       extends Val // i32
  final case class I64   (value: Long)      extends Val // i64
  final case class F32   (value: Float)     extends Val // f32
  final case class F64   (value: Double)    extends Val // f64
  final case class Struct(values: Seq[Val]) extends Val //
  final case class Array (values: Seq[Val]) extends Val //
  final case class Name  (name: nir.Name)   extends Val //

  //scala
  final case object Null extends Val // null
  final case object Unit extends Val // unit
}
