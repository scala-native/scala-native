package scala.scalanative
package linker

import scala.collection.mutable
import scala.scalanative.linker.LinktimeIntrinsicCallsResolver.FoundServiceProviders

sealed abstract class Info {
  def attrs: nir.Attrs
  def name: nir.Global
  def position: nir.SourcePosition
}

sealed abstract class ScopeInfo extends Info {
  override def name: nir.Global.Top
  def ty: nir.Type.Ref = nir.Type.Ref(name)
  val members = mutable.UnrolledBuffer.empty[MemberInfo]
  val calls = mutable.Set.empty[nir.Sig]
  val responds = mutable.Map.empty[nir.Sig, nir.Global.Member]

  def methods = calls.toSeq.filter(_.isVirtual)

  def isClass: Boolean = this.isInstanceOf[Class]
  def isTrait: Boolean = this.isInstanceOf[Trait]
  def is(info: ScopeInfo): Boolean
  def targets(sig: nir.Sig): mutable.Set[nir.Global.Member]
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

final class Unavailable(val name: nir.Global) extends Info {
  def attrs: nir.Attrs =
    util.unsupported(s"unavailable ${name.show} has no attrs")

  def position: nir.SourcePosition =
    util.unsupported(s"unavailable ${name.show} has no position")
}

final class Trait(
    val attrs: nir.Attrs,
    val name: nir.Global.Top,
    val traits: Seq[Trait]
)(implicit
    val position: nir.SourcePosition
) extends ScopeInfo {
  val implementors = mutable.SortedSet.empty[Class]
  val subtraits = mutable.Set.empty[Trait]

  def targets(sig: nir.Sig): mutable.Set[nir.Global.Member] = {
    val out = mutable.Set.empty[nir.Global.Member]

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
          info.name == nir.Rt.Object.name
      }
    }
  }
}

final class Class(
    val attrs: nir.Attrs,
    val name: nir.Global.Top,
    val parent: Option[Class],
    val traits: Seq[Trait],
    val isModule: Boolean
)(implicit val position: nir.SourcePosition)
    extends ScopeInfo {
  val implementors = mutable.SortedSet[Class](this)
  val subclasses = mutable.Set.empty[Class]
  val defaultResponds = mutable.Map.empty[nir.Sig, nir.Global.Member]
  var allocations = 0
  def allocated = allocations > 0

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
  lazy val hasFinalSafePublishFields: Boolean =
    fields.exists(_.attrs.finalWithSafePublish)

  def isConstantModule(implicit
      analysis: ReachabilityAnalysis.Result
  ): Boolean = {
    val hasNoFields =
      fields.isEmpty
    val hasEmptyOrNoCtor = {
      val ctor = name.member(nir.Sig.Ctor(Seq.empty))
      analysis.infos
        .get(ctor)
        .fold[Boolean] {
          true
        } {
          case meth: Method =>
            meth.insts match {
              case Array(_: nir.Inst.Label, _: nir.Inst.Ret) =>
                true
              case _ =>
                false
            }
          case _ =>
            false
        }
    }
    val isAllowlisted =
      interflow.Allowlist.constantModules.contains(name)

    isModule && (isAllowlisted || attrs.isExtern || (hasEmptyOrNoCtor && hasNoFields))
  }
  def resolve(sig: nir.Sig): Option[nir.Global.Member] = {
    responds.get(sig).orElse(defaultResponds.get(sig))
  }
  def targets(sig: nir.Sig): mutable.Set[nir.Global.Member] = {
    val out = mutable.Set.empty[nir.Global.Member]

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
      nir.Global.globalOrdering.compare(x.name, y.name)
  }
}

final class Method(
    val attrs: nir.Attrs,
    val owner: Info,
    val name: nir.Global.Member,
    val ty: nir.Type.Function,
    val insts: Array[nir.Inst],
    val debugInfo: nir.Defn.Define.DebugInfo
)(implicit val position: nir.SourcePosition)
    extends MemberInfo {
  val value: nir.Val =
    if (isConcrete) {
      nir.Val.Global(name, nir.Type.Ptr)
    } else {
      nir.Val.Null
    }
  def isConcrete: Boolean =
    insts.nonEmpty
}

final class Field(
    val attrs: nir.Attrs,
    val owner: Info,
    val name: nir.Global.Member,
    val isConst: Boolean,
    val ty: nir.Type,
    val init: nir.Val
)(implicit val position: nir.SourcePosition)
    extends MemberInfo {
  lazy val index: Int =
    owner.asInstanceOf[Class].fields.indexOf(this)
}

sealed trait ReachabilityAnalysis {
  def defns: Seq[nir.Defn]
  def foundServiceProviders: FoundServiceProviders
  def isSuccessful: Boolean = this.isInstanceOf[ReachabilityAnalysis.Result]
}

object ReachabilityAnalysis {
  final class Failure(
      val defns: Seq[nir.Defn],
      val unreachable: Seq[Reach.UnreachableSymbol],
      val unsupportedFeatures: Seq[Reach.UnsupportedFeature],
      val foundServiceProviders: FoundServiceProviders
  ) extends ReachabilityAnalysis
  final class Result(
      val infos: mutable.Map[nir.Global, Info],
      val entries: Seq[nir.Global],
      val links: Seq[nir.Attr.Link],
      val linkCppRuntime: Boolean,
      val preprocessorDefinitions: Seq[nir.Attr.Define],
      val defns: Seq[nir.Defn],
      val dynsigs: Seq[nir.Sig],
      val dynimpls: Seq[nir.Global.Member],
      val resolvedVals: mutable.Map[String, nir.Val],
      val foundServiceProviders: FoundServiceProviders
  ) extends ReachabilityAnalysis {
    lazy val ObjectClass = infos(nir.Rt.Object.name).asInstanceOf[Class]
    lazy val StringClass = infos(nir.Rt.StringName).asInstanceOf[Class]
    lazy val StringValueField =
      infos(nir.Rt.StringValueName).asInstanceOf[Field]
    lazy val StringOffsetField =
      infos(nir.Rt.StringOffsetName).asInstanceOf[Field]
    lazy val StringCountField =
      infos(nir.Rt.StringCountName).asInstanceOf[Field]
    lazy val StringCachedHashCodeField = infos(nir.Rt.StringCachedHashCodeName)
      .asInstanceOf[Field]

    private[scalanative] object references {
      def isReachable(
          symbol: nir.Global.Member,
          from: nir.Global.Member
      ): Boolean = {
        val todo = mutable.Queue.empty[nir.Global.Member]
        methodDirectSymbolRefs.get(from).foreach(todo ++= _.toList)
        val visited = mutable.HashSet.empty[nir.Global.Member]
        while (todo.nonEmpty) {
          val next = todo.dequeue()
          if (visited.add(next)) {
            if (next == symbol) return true
            methodDirectSymbolRefs.get(next).foreach(todo ++= _.toList)
          }
        }
        false
      }

      def isSelfRecursive(symbol: nir.Global.Member): Boolean =
        isReachable(symbol = symbol, from = symbol)

      // Graph of direct references between symbols
      lazy val methodDirectSymbolRefs
          : Map[nir.Global.Member, Set[nir.Global.Member]] = {
        val buf = collection.immutable.HashMap
          .newBuilder[nir.Global.Member, Set[nir.Global.Member]]
        buf.sizeHint(defns.size)

        defns.foreach { defn =>
          infos.get(defn.name).foreach {
            case method: linker.Method =>
              val symbols = mutable.Set.empty[nir.Global.Member]
              new nir.Traverse {
                override def onVal(value: nir.Val): Unit = value match {
                  case nir.Val.Global(sym: nir.Global.Member, _) =>
                    symbols += sym
                  case _ => super.onVal(value)
                }
                // We should also check for Op.Method targets but it adds way too much complexity
              }.onInsts(method.insts)
              buf += ((method.name, symbols.toSet))
            case _ => ()
          }
        }
        buf.result()
      }
    }
  }
}
