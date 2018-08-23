package scala.scalanative
package linker

import scala.collection.mutable
import scalanative.nir._

sealed abstract class Info {
  def attrs: Attrs
  def name: Global
}

sealed abstract class ScopeInfo extends Info {
  val members = mutable.UnrolledBuffer.empty[MemberInfo]
  val calls   = mutable.Set.empty[String]
}

sealed abstract class MemberInfo extends Info {
  def owner: ScopeInfo
}

final class Struct(val attrs: Attrs, val name: Global, val tys: Seq[nir.Type])
    extends ScopeInfo

final class Trait(val attrs: Attrs, val name: Global, val traits: Seq[Trait])
    extends ScopeInfo {
  val implementors = mutable.Set.empty[Class]
}

final class Class(val attrs: Attrs,
                  val name: Global,
                  val parent: Option[Class],
                  val traits: Seq[Trait],
                  val isModule: Boolean)
    extends ScopeInfo {
  var allocated  = false
  val subclasses = mutable.Set.empty[Class]
  val responds   = mutable.Map.empty[String, Global]

  val ty: Type =
    Type.Class(name)
  def isStaticModule(implicit top: Result): Boolean =
    isModule && !top.infos.contains(name member "init")
  def resolve(sig: String): Option[Global] =
    responds.get(sig)
}

final class Method(val attrs: Attrs,
                   val owner: ScopeInfo,
                   val name: Global,
                   val insts: Seq[Inst])
    extends MemberInfo {
  val value: Val =
    if (isConcrete) {
      Val.Global(name, Type.Ptr)
    } else {
      Val.Null
    }
  def isConcrete: Boolean =
    insts.nonEmpty
}

final class Field(val attrs: Attrs,
                  val owner: ScopeInfo,
                  val name: Global,
                  val isConst: Boolean,
                  val ty: nir.Type,
                  val init: Val)
    extends MemberInfo

final class Result(val infos: mutable.Map[Global, Info],
                   val entries: Seq[Global],
                   val unavailable: Seq[Global],
                   val links: Seq[Attr.Link],
                   val defns: Seq[Defn],
                   val dynsigs: Seq[String],
                   val dynimpls: Seq[Global]) {
  def targets(ty: Type, sig: String): mutable.Set[Global] = {
    val out = mutable.Set.empty[Global]

    def add(cls: Class): Unit =
      if (cls.allocated) {
        cls.resolve(sig).foreach { impl =>
          out += impl
        }
      }

    ty match {
      case Type.Module(name) =>
        val cls = infos(name).asInstanceOf[Class]
        add(cls)
      case Type.Class(name) =>
        val cls = infos(name).asInstanceOf[Class]
        add(cls)
        cls.subclasses.foreach(add)
      case Type.Trait(name) =>
        val trt = infos(name).asInstanceOf[Trait]
        trt.implementors.foreach(add)
      case _ =>
        ()
    }

    out
  }
}
