package scala.scalanative
package nir
import scala.scalanative.nir.Defn.Define

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
        lexicalScopes: Seq[DebugInfo.LexicalScope]
    ) {
      lazy val lexicalScopeOf: Map[ScopeId, DebugInfo.LexicalScope] =
        lexicalScopes.map {
          case scope @ DebugInfo.LexicalScope(id, _) => (id, scope)
        }.toMap
    }
    object DebugInfo {
      val empty: DebugInfo = DebugInfo(
        localNames = Map.empty,
        lexicalScopes = Seq(LexicalScope.TopLevel)
      )

      case class LexicalScope(id: ScopeId, parent: ScopeId) {
        def isTopLevel: Boolean = id.isTopLevel
      }
      object LexicalScope {
        val TopLevel = LexicalScope(ScopeId.TopLevel, ScopeId.TopLevel)
        implicit val ordering: Ordering[LexicalScope] = Ordering.by(_.id.id)
      }
    }

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
