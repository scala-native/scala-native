package scala.scalanative.nir

import java.nio.file.{Path, Paths}

import scala.util.Try

sealed case class NIRSource(directory: Path, path: Path) {
  def debugName = s"${directory}:${path}"
  def exists: Boolean = this ne NIRSource.None
}
object NIRSource {
  object None extends NIRSource(null, null) {
    override def debugName: String = "<no-source>"
    override def toString(): String = s"NIRSource($debugName)"
  }
}

final case class SourcePosition(
    /** Scala source file containing definition of element */
    source: SourceFile,
    /** Zero-based line number in the source. */
    line: Int,
    /** Zero-based column number in the source */
    column: Int,
    /** NIR file coordinates used to deserialize the symbol, populated only when
     *  linking
     */
    nirSource: NIRSource = NIRSource.None
) {

  /** One-based line number */
  def sourceLine: Int = line + 1

  /** One-based column number */
  def sourceColumn: Int = column + 1
  def show: String = {
    val source = this.source match {
      case SourceFile.Virtual              => "<virtual>"
      case SourceFile.Relative(pathString) => pathString
    }
    s"$source:$sourceLine:$sourceColumn"
  }

  def isEmpty: Boolean = this eq SourcePosition.NoPosition
  def isDefined: Boolean = !isEmpty
  def orElse(other: => SourcePosition): SourcePosition =
    if (isEmpty) other
    else this
}

object SourcePosition {
  val NoPosition = SourcePosition(SourceFile.Virtual, 0, 0)
}

sealed trait SourceFile {
  def filename: Option[String] = this match {
    case SourceFile.Virtual          => None
    case source: SourceFile.Relative =>
      Option(source.path.getFileName()).map(_.toString())
  }
  def directory: Option[String] = this match {
    case SourceFile.Virtual          => None
    case source: SourceFile.Relative =>
      Option(source.path.getParent()).map(_.toString())
  }
}
object SourceFile {

  /** An abstract file without location, e.g. in-memory source or generated */
  case object Virtual extends SourceFile

  /** Relative path to source file based on the workspace path. Used for
   *  providing source files defined from the local project dependencies.
   *  @param pathString
   *    path relative to `-sourceroot` setting defined when compiling source -
   *    typically it's root directory of workspace
   */
  case class Relative(pathString: String) extends SourceFile {
    lazy val path: Path = Paths.get(pathString)
  }
}
