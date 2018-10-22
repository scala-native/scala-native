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
  val calls   = mutable.Set.empty[Sig]

  def targets(sig: Sig): mutable.Set[Global]
}

sealed abstract class MemberInfo extends Info {
  def owner: Info
}

final class Unavailable(val name: Global) extends Info {
  def attrs: Attrs =
    util.unsupported(s"unavailable ${name.show} has no attrs")
}

final class Trait(val attrs: Attrs, val name: Global, val traits: Seq[Trait])
    extends ScopeInfo {
  val implementors = mutable.Set.empty[Class]

  def targets(sig: Sig): mutable.Set[Global] = {
    val out = mutable.Set.empty[Global]

    def add(cls: Class): Unit =
      if (cls.allocated) {
        cls.resolve(sig).foreach { impl =>
          out += impl
        }
      }

    implementors.foreach(add)

    out
  }
}

final class Class(val attrs: Attrs,
                  val name: Global,
                  val parent: Option[Class],
                  val traits: Seq[Trait],
                  val isModule: Boolean)
    extends ScopeInfo {
  var allocated  = false
  val subclasses = mutable.Set.empty[Class]
  val responds   = mutable.Map.empty[Sig, Global]

  val ty: Type =
    Type.Ref(name)
  def isStaticModule(implicit top: Result): Boolean =
    isModule && !top.infos.contains(name.member(Sig.Ctor(Seq())))
  def resolve(sig: Sig): Option[Global] =
    responds.get(sig)
  def targets(sig: Sig): mutable.Set[Global] = {
    val out = mutable.Set.empty[Global]

    def add(cls: Class): Unit =
      if (cls.allocated) {
        cls.resolve(sig).foreach { impl =>
          out += impl
        }
      }

    add(this)
    subclasses.foreach(add)

    out
  }
}

final class Method(val attrs: Attrs,
                   val owner: Info,
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
                  val owner: Info,
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
                   val dynsigs: Seq[Sig],
                   val dynimpls: Seq[Global])
