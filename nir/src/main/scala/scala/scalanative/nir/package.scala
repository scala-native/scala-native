package scala.scalanative

package object nir {

  /** The name of a variable in program sources. */
  type LocalName = String

  /** A map from SSA identifier to its name in program sources.
   *
   *  Local variables get lowered to an static assignment that is assigned to a
   *  unique identifier in the context of its definition. Instances of this type
   *  are used to maintain the correspondance between an SSA ID and its
   *  corresponding name in program sources.
   */
  type LocalNames = Map[Local, LocalName]

  /** The identifier of a lexical scope. */
  case class ScopeId(id: Int) extends AnyVal {

    /** Returns `true` iff `this` is the top-level scope. */
    def isTopLevel: Boolean = this.id == ScopeId.TopLevel.id

  }

  object ScopeId {

    /** Returns the innermost scope containing `id`. */
    def of(id: Local): ScopeId = ScopeId(id.id.toInt)

    /** The top-level scope. */
    val TopLevel = ScopeId(0)

  }

}
