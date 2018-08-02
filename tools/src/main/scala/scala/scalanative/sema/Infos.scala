package scala.scalanative
package sema

import scala.collection.mutable
import util.unreachable
import nir._

sealed abstract class Node {
  var id: Int   = -1
  var in: Scope = _

  def inTop: Boolean   = in.isInstanceOf[Top]
  def inClass: Boolean = in.isInstanceOf[Class]
  def inTrait: Boolean = in.isInstanceOf[Trait]
  def attrs: Attrs
  def name: Global
}

sealed abstract class Scope extends Node {
  val members = mutable.UnrolledBuffer.empty[Node]
  val methods = mutable.UnrolledBuffer.empty[Method]
  val fields  = mutable.UnrolledBuffer.empty[Field]
}

final class Struct(val attrs: Attrs, val name: Global, val tys: Seq[nir.Type])
    extends Scope

final class Trait(val attrs: Attrs,
                  val name: Global,
                  val traitNames: Seq[Global])
    extends Scope {
  val traits = mutable.UnrolledBuffer.empty[Trait]
}

final class Class(val attrs: Attrs,
                  val name: Global,
                  val parentName: Option[Global],
                  val traitNames: Seq[Global],
                  val isModule: Boolean)
    extends Scope {
  val ty         = Type.Class(name)
  val subclasses = mutable.UnrolledBuffer.empty[Class]
  val traits     = mutable.UnrolledBuffer.empty[Trait]

  var parent: Option[Class] = _
  var range: Range          = _

  def isStaticModule: Boolean =
    !in.asInstanceOf[Top].nodes.contains(name member "init")
}

final class Method(val attrs: Attrs,
                   val name: Global,
                   val ty: nir.Type,
                   val isConcrete: Boolean)
    extends Node {
  val overrides = mutable.UnrolledBuffer.empty[Method]
  val overriden = mutable.UnrolledBuffer.empty[Method]
  val value =
    if (isConcrete) Val.Global(name, Type.Ptr)
    else Val.Null
  def isVirtual =
    !isConcrete || overriden.nonEmpty
  def isStatic =
    !isVirtual
}

final class Field(val attrs: Attrs, val name: Global, val ty: nir.Type)
    extends Node

final class Top(val nodes: mutable.Map[Global, Node],
                val structs: Seq[Struct],
                val classes: Seq[Class],
                val traits: Seq[Trait],
                override val methods: mutable.UnrolledBuffer[Method],
                override val fields: mutable.UnrolledBuffer[Field])
    extends Scope {
  def name  = Global.None
  def attrs = Attrs.None
}
