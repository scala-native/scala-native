package scala.scalanative
package nir
import scala.scalanative.nir.Defn.Define
import scala.collection.immutable.NumericRange

sealed abstract class Defn {
  def name: Global
  def attrs: Attrs
  def pos: Position
  final def show: String = nir.Show(this)
  final def isEntryPoint = this match {
    case Define(attrs, Global.Member(_, sig), _, _, _) =>
      sig.isClinit || attrs.isExtern
    case _ => false
  }
}

object Defn {
  // low-level
  final case class Var(attrs: Attrs, name: Global, ty: Type, rhs: Val)(implicit
      val pos: Position
  ) extends Defn
  final case class Const(attrs: Attrs, name: Global, ty: Type, rhs: Val)(
      implicit val pos: Position
  ) extends Defn
  final case class Declare(attrs: Attrs, name: Global, ty: Type)(implicit
      val pos: Position
  ) extends Defn
  final case class Define(
      attrs: Attrs,
      name: Global,
      ty: Type,
      insts: Seq[Inst],
      debugInfo: Define.DebugInfo = Define.DebugInfo.empty
  )(implicit val pos: Position)
      extends Defn
  object Define {
    case class DebugInfo(
        localNames: LocalNames,
        lexicalScopes: Seq[LexicalScope]
    ) {
      def scopeOf(id: Local): Option[LexicalScope] = {
        val idV = id.id
        val containedBy = lexicalScopes.filter{scope => 
          val LocalRange(start,end) = scope.range
          idV >= start.id && idV <= end.id
        }
        if(containedBy.isEmpty) None
        else if(containedBy.size == 1) Some(containedBy.head)
        else {
          println(id)
          println(containedBy)
          val res = containedBy.minBy(_.range.end.id)
          println(res)
          Some(res)
        }
      }
    }
    object DebugInfo {
      val empty: DebugInfo =
        DebugInfo(localNames = Map.empty, lexicalScopes = Nil)
    }
    case class LocalRange(start: Local, end: Local)
    case class LexicalScope(id: Local, parent: Local, range: LocalRange)
  }

  // high-level
  final case class Trait(attrs: Attrs, name: Global, traits: Seq[Global])(
      implicit val pos: Position
  ) extends Defn
  final case class Class(
      attrs: Attrs,
      name: Global,
      parent: Option[Global],
      traits: Seq[Global]
  )(implicit val pos: Position)
      extends Defn
  final case class Module(
      attrs: Attrs,
      name: Global,
      parent: Option[Global],
      traits: Seq[Global]
  )(implicit val pos: Position)
      extends Defn
}
