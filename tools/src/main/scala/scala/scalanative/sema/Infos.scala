package scala.scalanative
package sema

import scala.collection.mutable
import scalanative.nir._
import scalanative.util.unreachable

sealed abstract class Node {
  var in: Scope = _

  def inTop: Boolean   = in.isInstanceOf[Top]
  def inClass: Boolean = in.isInstanceOf[Class]
  def inTrait: Boolean = in.isInstanceOf[Trait]
  def attrs: Attrs
  def name: Global
}

sealed abstract class Scope extends Node {
  val methods = mutable.UnrolledBuffer.empty[Method]
  val fields  = mutable.UnrolledBuffer.empty[Field]
  val calls   = mutable.Set.empty[String]
}

final class Struct(val attrs: Attrs, val name: Global, val tys: Seq[nir.Type])
    extends Scope

final class Trait(val attrs: Attrs,
                  val name: Global,
                  val traitNames: Seq[Global])
    extends Scope {
  val traits       = mutable.UnrolledBuffer.empty[Trait]
  val implementors = mutable.Set.empty[Class]
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
  var allocated  = false

  var parent: Option[Class]                 = _
  var resolved: mutable.Map[String, Method] = _

  def isStaticModule: Boolean =
    !in.asInstanceOf[Top].nodes.contains(name member "init")

  def resolve(sig: String): Option[Method] =
    resolved.get(sig)

  def resolveImpl(sig: String): Option[Method] = {
    val top = this.in.asInstanceOf[Top]

    def impl(cls: Class, sig: String): Option[Method] =
      top.nodes
        .get(cls.name member sig)
        .fold[Option[Method]] {
          cls.parent.flatMap(impl(_, sig))
        } { impl =>
          Some(impl.asInstanceOf[Method])
        }

    sig match {
      // We short-circuit scala_== and scala_## to immeditately point to the
      // equals and hashCode implementation for the reference types to avoid
      // double virtual dispatch overhead.
      case Rt.ScalaEqualsSig =>
        val scalaImpl = impl(this, Rt.ScalaEqualsSig).get
        val javaImpl  = impl(this, Rt.JavaEqualsSig).get
        if (javaImpl.in.name != Rt.Object.name &&
            scalaImpl.in.name == Rt.Object.name) {
          Some(javaImpl)
        } else {
          Some(scalaImpl)
        }
      case Rt.ScalaHashCodeSig =>
        val scalaImpl = impl(this, Rt.ScalaHashCodeSig).get
        val javaImpl  = impl(this, Rt.JavaHashCodeSig).get
        if (javaImpl.in.name != Rt.Object.name &&
            scalaImpl.in.name == Rt.Object.name) {
          Some(javaImpl)
        } else {
          Some(scalaImpl)
        }
      case _ =>
        impl(this, sig)
    }
  }
}

final class Method(val attrs: Attrs,
                   val name: Global,
                   val ty: nir.Type,
                   val insts: Seq[Inst])
    extends Node {
  val value =
    if (isConcrete) Val.Global(name, Type.Ptr)
    else Val.Null
  def isConcrete =
    insts.nonEmpty
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

  def targets(ty: Type, sig: String): mutable.Set[Method] = {
    implicit val top = this

    val out = mutable.Set.empty[Method]

    def add(cls: Class): Unit = {
      if (cls.allocated) {
        cls.resolve(sig).foreach { impl =>
          out += impl
        }
      }
    }

    ty match {
      case ClassRef(cls) =>
        add(cls)
        cls.subclasses.foreach(add)
      case TraitRef(trt) =>
        trt.implementors.foreach(add)
      case _ =>
        ()
    }

    out
  }
}
