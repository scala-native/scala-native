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
    case Define(attrs, Global.Member(_, sig), _, _, _, _) =>
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
      // debug metadata
      localNames: LocalNames = Map.empty,
      lexicalScopes: List[Define.LexicalScope] = Nil
  )(implicit val pos: Position)
      extends Defn
  object Define {
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
