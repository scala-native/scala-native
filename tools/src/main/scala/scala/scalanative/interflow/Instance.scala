package scala.scalanative
package interflow

import java.util.Arrays
import scalanative.nir.Val
import scalanative.linker.Class

sealed abstract class Instance extends Cloneable {
  def cls: Class

  override def clone(): Instance = this match {
    case EscapedInstance(cls, escapedValue) =>
      EscapedInstance(cls, escapedValue)
    case VirtualInstance(kind, cls, values) =>
      VirtualInstance(kind, cls, values.clone())
  }

  override def toString: String = this match {
    case EscapedInstance(cls, escapedValue) =>
      s"EscapedInstance(${cls.name.show}, ${escapedValue.show})"
    case VirtualInstance(kind, cls, values) =>
      s"VirtualInstance($kind, $cls, ${values.map(_.show)})"
  }
}

final case class EscapedInstance(val cls: Class, val escapedValue: Val)
    extends Instance {
  assert(!escapedValue.isVirtual)
}

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

  override def toString: String =
    s"VirtualInstance($kind, ${cls.name.show}, Array(${values.map(_.show).mkString(", ")}))"
}
