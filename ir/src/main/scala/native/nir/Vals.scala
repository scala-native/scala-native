package native
package nir

sealed trait Val
object Val {
  final case object None                    extends Val
  final case object Zero                    extends Val
  final case object True                    extends Val
  final case object False                   extends Val
  final case class I8    (value: Byte)      extends Val
  final case class I16   (value: Short)     extends Val
  final case class I32   (value: Int)       extends Val
  final case class I64   (value: Long)      extends Val
  final case class F32   (value: Float)     extends Val
  final case class F64   (value: Double)    extends Val
  final case class Struct(values: Seq[Val]) extends Val
  final case class Array (values: Seq[Val]) extends Val
  final case class Name  (name: nir.Name)   extends Val

  //scala
  final case object Null extends Val
  final case object Unit extends Val
}
