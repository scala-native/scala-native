package scala.scalanative
package nir
import scala.scalanative.nir.Defn.Define

/** A definition in NIR.
 *
 *  Programs in NIR are represented as a sequence of definitions denoting types,
 *  methods and fields. Definitions fall into two categories:
 *
 *    - Top-level definitions: these represent classes, modules, or traits.
 *      Classes and modules inherit from a single parent with the exception of
 *      `java.lang.Object`, which sits at the top of the hierarchy. They may
 *      additionally implement traits.
 *    - Member definitions: these represent fields or methods.
 *
 *  Definitions may carry attributes providing further information about their
 *  semantics.
 */
sealed abstract class Defn {

  /** Returns the name of the definition. */
  def name: Global

  /** Returns the attributes of the definition. */
  def attrs: Attrs

  /** Returns the site in the program sources corresponding to the definition.
   */
  def pos: Position

  /** Returns a textual representation of `this`. */
  final def show: String =
    nir.Show(this)

  /** Returns `true` iff `this` is considered as an entry point by reachability
   *  analysis.
   */
  final def isEntryPoint = this match {
    case Define(attrs, Global.Member(_, sig), _, _, _) =>
      sig.isClinit || attrs.isExtern
    case _ => false
  }

}

object Defn {

  // === Member definitions ==== //

  /** A variable definition corresponding to a field in class or module. */
  final case class Var(
      attrs: Attrs,
      name: Global.Member,
      ty: Type,
      rhs: Val
  )(implicit val pos: Position)
      extends Defn

  /** A constant value. */
  final case class Const(
      attrs: Attrs,
      name: Global.Member,
      ty: Type,
      rhs: Val
  )(implicit val pos: Position)
      extends Defn

  /** A method declaration. */
  final case class Declare(
      attrs: Attrs,
      name: Global.Member,
      ty: Type.Function
  )(implicit val pos: Position)
      extends Defn

  /** A method definition. */
  final case class Define(
      attrs: Attrs,
      name: Global.Member,
      ty: Type.Function,
      insts: Seq[Inst],
      debugInfo: Define.DebugInfo = Define.DebugInfo.empty
  )(implicit val pos: Position)
      extends Defn

  object Define {

    /** A set of metadata about a definition for debugging purposes. */
    case class DebugInfo(
        localNames: LocalNames,
        lexicalScopes: Seq[DebugInfo.LexicalScope]
    ) {

      /** A map from scope ID to its value. */
      lazy val lexicalScopeOf: Map[ScopeId, DebugInfo.LexicalScope] =
        lexicalScopes.map {
          case scope @ DebugInfo.LexicalScope(id, _, _) => (id, scope)
        }.toMap

    }

    object DebugInfo {

      /** An empty set of debug metadata. */
      val empty: DebugInfo = DebugInfo(
        localNames = Map.empty,
        lexicalScopes = Seq(LexicalScope.AnyTopLevel)
      )

      /** A lexical scope in the program sources. */
      case class LexicalScope(
          id: ScopeId,
          parent: ScopeId,
          srcPosition: Position
      ) {

        /** Returns `true` iff `this` is the top-level scope. */
        def isTopLevel: Boolean =
          id.isTopLevel

      }

      object LexicalScope {

        /** Returns a top-level scope covering the given site in the source
         *  program.
         */
        def TopLevel(defnPosition: Position) =
          LexicalScope(ScopeId.TopLevel, ScopeId.TopLevel, defnPosition)

        /** An abstract top-level scope. */
        final val AnyTopLevel =
          TopLevel(Position.NoPosition)

        /** The order between lexical scopes. */
        implicit val ordering: Ordering[LexicalScope] =
          Ordering.by(_.id.id)

      }

    }

  }

  // === Top-level definitions ==== //

  /** The NIR representation of a Scala trait. */
  final case class Trait(
      attrs: Attrs,
      name: Global.Top,
      traits: Seq[Global.Top]
  )(implicit val pos: Position)
      extends Defn

  /** The NIR representation of a Scala class. */
  final case class Class(
      attrs: Attrs,
      name: Global.Top,
      parent: Option[Global.Top],
      traits: Seq[Global.Top]
  )(implicit val pos: Position)
      extends Defn

  /** The NIR representation of a Scala module. */
  final case class Module(
      attrs: Attrs,
      name: Global.Top,
      parent: Option[Global.Top],
      traits: Seq[Global.Top]
  )(implicit val pos: Position)
      extends Defn

}
