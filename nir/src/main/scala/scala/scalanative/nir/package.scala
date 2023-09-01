package scala.scalanative

package object nir {
  type LocalName = String
  type LocalNames = Map[Local, LocalName]
  case class ScopeId(id: Int) extends AnyVal {
    def isTopLevel: Boolean = this.id == ScopeId.TopLevel.id
  }
  object ScopeId {
    def of(id: Local): ScopeId = ScopeId(id.id.toInt)
    val TopLevel = ScopeId(0)
  }
}
