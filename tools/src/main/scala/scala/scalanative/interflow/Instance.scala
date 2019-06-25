package scala.scalanative
package interflow

import java.util.Arrays
import scalanative.nir.{Type, Val, Op}
import scalanative.linker.Class

sealed abstract class Instance extends Cloneable {
  def ty: Type = this match {
    case EscapedInstance(value) =>
      value.ty
    case DelayedInstance(op) =>
      op.resty
    case VirtualInstance(_, cls, _) =>
      Type.Ref(cls.name, exact = true, nullable = false)
  }

  override def clone(): Instance = this match {
    case EscapedInstance(value) =>
      EscapedInstance(value)
    case DelayedInstance(op) =>
      DelayedInstance(op)
    case VirtualInstance(kind, cls, values) =>
      VirtualInstance(kind, cls, values.clone())
  }

  override def toString: String = this match {
    case EscapedInstance(value) =>
      s"EscapedInstance(${value.show})"
    case DelayedInstance(op) =>
      s"DelayedInstance(${op.show})"
    case VirtualInstance(kind, cls, values) =>
      s"VirtualInstance($kind, ${cls.name.show}, Array(${values.map(_.show)}))"
  }
}

final case class EscapedInstance(val escapedValue: Val) extends Instance

final case class DelayedInstance(val delayedOp: Op) extends Instance

final case class VirtualInstance(val kind: Kind,
                                 val cls: Class,
                                 var values: Array[Val])
    extends Instance {

  // We can't use case class generated equals, due to the fact
  // that equals on arrays does reference equality by default.
  override def equals(other: Any): Boolean = other match {
    case other: VirtualInstance =>
      kind == other.kind &&
        cls == other.cls &&
        Arrays.equals(values.asInstanceOf[Array[Object]],
                      other.values.asInstanceOf[Array[Object]])
    case _ =>
      false
  }
}
