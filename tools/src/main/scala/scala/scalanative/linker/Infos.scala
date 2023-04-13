package scala.scalanative
package linker

import scala.collection.mutable
import scalanative.nir._

sealed abstract class Info {
  def attrs: Attrs
  def name: Global
  def position: Position
}

sealed abstract class ScopeInfo extends Info {
  val members = mutable.UnrolledBuffer.empty[MemberInfo]
  val calls = mutable.Set.empty[Sig]
  val responds = mutable.Map.empty[Sig, Global]

  def isClass: Boolean = this.isInstanceOf[Class]
  def isTrait: Boolean = this.isInstanceOf[Trait]
  def is(info: ScopeInfo): Boolean
  def targets(sig: Sig): mutable.Set[Global]
  def implementors: mutable.SortedSet[Class]

  lazy val linearized: Seq[ScopeInfo] = {
    val out = mutable.UnrolledBuffer.empty[ScopeInfo]

    def loop(info: ScopeInfo): Unit = info match {
      case info: Class =>
        out += info
        info.traits.reverse.foreach(loop)
        info.parent.foreach(loop)
      case info: Trait =>
        out += info
        info.traits.reverse.foreach(loop)
    }

    def overwrite(l: Seq[ScopeInfo]): Seq[ScopeInfo] = {
      val indexes = mutable.Map.empty[ScopeInfo, Int]
      l.zipWithIndex.foreach {
        case (v, idx) =>
          indexes(v) = idx
      }
      l.zipWithIndex.collect {
        case (v, idx) if indexes(v) == idx =>
          v
      }
    }

    loop(this)
    overwrite(out.toSeq)
  }
}

sealed abstract class MemberInfo extends Info {
  def owner: Info
}

final class Unavailable(val name: Global) extends Info {
  def attrs: Attrs =
    util.unsupported(s"unavailable ${name.show} has no attrs")

  def position: Position =
    util.unsupported(s"unavailable ${name.show} has no position")
}

final class Trait(val attrs: Attrs, val name: Global, val traits: Seq[Trait])(
    implicit val position: Position
) extends ScopeInfo {
  val implementors = mutable.SortedSet.empty[Class]
  val subtraits = mutable.Set.empty[Trait]

  def targets(sig: Sig): mutable.Set[Global] = {
    val out = mutable.Set.empty[Global]

    def add(cls: Class): Unit =
      if (cls.allocated) {
        cls.resolve(sig).foreach { impl => out += impl }
      }

    implementors.foreach(add)

    out
  }

  def is(info: ScopeInfo): Boolean = {
    (info eq this) || {
      info match {
        case info: Trait =>
          info.subtraits.contains(this)
        case _ =>
          info.name == Rt.Object.name
      }
    }
  }
}

final class Class(
    val attrs: Attrs,
    val name: Global,
    val parent: Option[Class],
    val traits: Seq[Trait],
    val isModule: Boolean
)(implicit val position: Position)
    extends ScopeInfo {
  val implementors = mutable.SortedSet[Class](this)
  val subclasses = mutable.Set.empty[Class]
  val defaultResponds = mutable.Map.empty[Sig, Global]
  var allocated = false

  lazy val fields: Seq[Field] = {
    val out = mutable.UnrolledBuffer.empty[Field]
    def add(info: Class): Unit = {
      info.parent.foreach(add)
      info.members.foreach {
        case info: Field => out += info
        case _           => ()
      }
    }
    add(this)
    out.toSeq
  }

  lazy val hasFinalFields: Boolean = fields.exists(_.attrs.isFinal)

  val ty: Type =
    Type.Ref(name)
  def isConstantModule(implicit top: Result): Boolean = {
    val hasNoFields =
      fields.isEmpty
    val hasEmptyOrNoCtor = {
      val ctor = name member Sig.Ctor(Seq.empty)
      top.infos
        .get(ctor)
        .fold[Boolean] {
          true
        } {
          case meth: Method =>
            meth.insts match {
              case Array(_: Inst.Label, _: Inst.Ret) =>
                true
              case _ =>
                false
            }
          case _ =>
            false
        }
    }
    val isWhitelisted =
      interflow.Whitelist.constantModules.contains(name)

    isModule && (isWhitelisted || attrs.isExtern || (hasEmptyOrNoCtor && hasNoFields))
  }
  def resolve(sig: Sig): Option[Global] = {
    responds.get(sig).orElse(defaultResponds.get(sig))
  }
  def targets(sig: Sig): mutable.Set[Global] = {
    val out = mutable.Set.empty[Global]

    def add(cls: Class): Unit =
      if (cls.allocated) {
        cls.resolve(sig).foreach { impl => out += impl }
      }

    add(this)
    subclasses.foreach(add)

    out
  }
  def is(info: ScopeInfo): Boolean = {
    (info eq this) || {
      info match {
        case info: Trait =>
          info.implementors.contains(this)
        case info: Class =>
          info.subclasses.contains(this)
      }
    }
  }
}

object Class {
  implicit val classOrdering: Ordering[Class] = new Ordering[Class] {
    override def compare(x: Class, y: Class): Int =
      Global.globalOrdering.compare(x.name, y.name)
  }
}

final class Method(
    val attrs: Attrs,
    val owner: Info,
    val name: Global,
    val ty: Type,
    val insts: Array[Inst]
)(implicit val position: Position)
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

final class Field(
    val attrs: Attrs,
    val owner: Info,
    val name: Global,
    val isConst: Boolean,
    val ty: nir.Type,
    val init: Val
)(implicit val position: Position)
    extends MemberInfo {
  lazy val index: Int =
    owner.asInstanceOf[Class].fields.indexOf(this)
}

final class Result(
    val infos: mutable.Map[Global, Info],
    val entries: Seq[Global],
    val unavailable: Seq[Global],
    val referencedFrom: mutable.Map[Global, Global],
    val links: Seq[Attr.Link],
    val defns: Seq[Defn],
    val dynsigs: Seq[Sig],
    val dynimpls: Seq[Global],
    val resolvedVals: mutable.Map[String, Val]
) {
  lazy val ObjectClass = infos(Rt.Object.name).asInstanceOf[Class]
  lazy val StringClass = infos(Rt.StringName).asInstanceOf[Class]
  lazy val StringValueField = infos(Rt.StringValueName).asInstanceOf[Field]
  lazy val StringOffsetField = infos(Rt.StringOffsetName).asInstanceOf[Field]
  lazy val StringCountField = infos(Rt.StringCountName).asInstanceOf[Field]
  lazy val StringCachedHashCodeField = infos(Rt.StringCachedHashCodeName)
    .asInstanceOf[Field]
}
