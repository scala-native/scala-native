package scala.scalanative
package interflow

import java.util.Arrays

import scalanative.linker.Class

private[interflow] sealed abstract class Instance(implicit
    val srcPosition: nir.SourcePosition,
    val scopeId: nir.ScopeId
) extends Cloneable {
  def ty: nir.Type = this match {
    case EscapedInstance(value) =>
      value.ty
    case DelayedInstance(op) =>
      op.resty
    case VirtualInstance(_, cls, _, _) =>
      nir.Type.Ref(cls.name, exact = true, nullable = false)
  }

  override def clone(): Instance = this match {
    case inst: EscapedInstance => inst.copy()
    case inst: DelayedInstance => inst.copy()
    case inst: VirtualInstance => inst.copy(values = inst.values.clone())
  }

  override def toString: String = this match {
    case EscapedInstance(value) =>
      s"EscapedInstance(${value.show})"
    case DelayedInstance(op) =>
      s"DelayedInstance(${op.show})"
    case VirtualInstance(kind, cls, values, zone) =>
      val allocation = zone.fold("Heap")(instance => s"SafeZone{$instance}")
      s"VirtualInstance($kind, ${cls.name.show}, Array(${values.map(_.show)}), $allocation)"
  }
}

private[interflow] final case class EscapedInstance(val escapedValue: nir.Val)(
    implicit
    srcPosition: nir.SourcePosition,
    scopeId: nir.ScopeId
) extends Instance {
  def this(escapedValue: nir.Val, instance: Instance) =
    this(escapedValue)(instance.srcPosition, instance.scopeId)
}

private[interflow] final case class DelayedInstance(val delayedOp: nir.Op)(
    implicit
    srcPosition: nir.SourcePosition,
    scopeId: nir.ScopeId
) extends Instance

private[interflow] final case class VirtualInstance(
    kind: Kind,
    cls: Class,
    values: Array[nir.Val],
    zone: Option[nir.Val]
)(implicit srcPosition: nir.SourcePosition, scopeId: nir.ScopeId)
    extends Instance {

  // We can't use case class generated equals, due to the fact
  // that equals on arrays does reference equality by default.
  override def equals(other: Any): Boolean = other match {
    case other: VirtualInstance =>
      kind == other.kind &&
        cls == other.cls &&
        Arrays.equals(
          values.asInstanceOf[Array[Object]],
          other.values.asInstanceOf[Array[Object]]
        ) && zone == other.zone
    case _ =>
      false
  }
}
