package scala.scalanative
package nir

import scala.scalanative.nir.Defn.Define

/** A definition in NIR.
 *
 *  Programs in NIR are represented as a sequence of definitions denoting types,
 *  methods and fields. Definitions fall into two categories:
 *
 *    - Top-level definitions: these represent classes, modules, traits, or
 *      global variables and constants.
 *    - Member definitions: these represent fields or methods.
 *
 *  Classes and modules inherit from a single parent with the exception of
 *  `java.lang.Object`, which sits at the top of the hierarchy. They may
 *  additionally implement traits.
 *
 *  Definitions may also carry attributes providing further information about
 *  their semantics (e.g., whether a method may be inlined). Attributes are also
 *  used to mark special-purpose definitions, such as stubs, proxies and FFIs.
 */
sealed abstract class Defn extends Positioned {

  /** Returns the name of the definition. */
  def name: Global

  /** Returns the attributes of the definition. */
  def attrs: Attrs

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

  /** A variable definition corresponding to either a field in class or module,
   *  or to a top-level global variable.
   */
  final case class Var(
      attrs: Attrs,
      name: Global.Member,
      ty: Type,
      rhs: Val
  )(implicit val pos: SourcePosition)
      extends Defn

  /** A unique, read-only instance of some type.
   *
   *  A constant definition is distinct from a constant literal, which would be
   *  represented by a `Val`.
   */
  final case class Const(
      attrs: Attrs,
      name: Global.Member,
      ty: Type,
      rhs: Val
  )(implicit val pos: SourcePosition)
      extends Defn

  /** A method declaration.
   *
   *  Methods of abstract classes and traits can be declared without a
   *  definition and are resolved at runtime through dynamic dispatch.
   */
  final case class Declare(
      attrs: Attrs,
      name: Global.Member,
      ty: Type.Function
  )(implicit val pos: SourcePosition)
      extends Defn

  /** A method definition. */
  final case class Define(
      attrs: Attrs,
      name: Global.Member,
      ty: Type.Function,
      insts: Seq[Inst],
      debugInfo: Define.DebugInfo = Define.DebugInfo.empty
  )(implicit val pos: SourcePosition)
      extends Defn {
    private[scalanative] lazy val hasUnwind = insts.exists {
      case nir.Inst.Let(_, _, unwind)   => unwind ne nir.Next.None
      case nir.Inst.Throw(_, unwind)    => unwind ne nir.Next.None
      case nir.Inst.Unreachable(unwind) => unwind ne nir.Next.None
      case _                            => false
    }
  }

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
          srcPosition: SourcePosition
      ) {

        /** Returns `true` iff `this` is the top-level scope. */
        def isTopLevel: Boolean =
          id.isTopLevel

      }

      object LexicalScope {

        /** Returns a top-level scope covering the given site in the source
         *  program.
         */
        def TopLevel(defnPosition: SourcePosition) =
          LexicalScope(ScopeId.TopLevel, ScopeId.TopLevel, defnPosition)

        /** An abstract top-level scope. */
        final val AnyTopLevel =
          TopLevel(SourcePosition.NoPosition)

        /** The order between lexical scopes. */
        implicit val ordering: Ordering[LexicalScope] =
          Ordering.by(_.id.id)

      }

    }

  }

  /** The NIR representation of a Scala trait. */
  final case class Trait(
      attrs: Attrs,
      name: Global.Top,
      traits: Seq[Global.Top]
  )(implicit val pos: SourcePosition)
      extends Defn

  /** The NIR representation of a Scala class. */
  final case class Class(
      attrs: Attrs,
      name: Global.Top,
      parent: Option[Global.Top],
      traits: Seq[Global.Top]
  )(implicit val pos: SourcePosition)
      extends Defn

  /** The NIR representation of a Scala module. */
  final case class Module(
      attrs: Attrs,
      name: Global.Top,
      parent: Option[Global.Top],
      traits: Seq[Global.Top]
  )(implicit val pos: SourcePosition)
      extends Defn

}
