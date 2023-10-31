package scala.scalanative

package object nir {

  type LocalName = String

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
