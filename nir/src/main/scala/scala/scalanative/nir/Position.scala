package scala.scalanative.nir

final case class Position(
    /** Source file. */
    source: Position.SourceFile,
    /** Zero-based line number. */
    line: Int,
    /** Zero-based column number. */
    column: Int
) {
  def show: String = s"$line:$column"

  def isEmpty: Boolean = {
    def isEmptySlowPath(): Boolean = {
      source.getScheme == null && source.getRawAuthority == null &&
      source.getRawQuery == null && source.getRawFragment == null
    }
    source.getRawPath == "" && isEmptySlowPath()
  }

  def isDefined: Boolean = !isEmpty

  def orElse(that: => Position): Position = if (isDefined) this else that
}

object Position {
  type SourceFile = java.net.URI

  object SourceFile {
    def apply(f: java.io.File): SourceFile = f.toURI
    def apply(f: String): SourceFile       = new java.net.URI(f)
  }
  val NoPosition = Position(SourceFile(""), 0, 0)
}
