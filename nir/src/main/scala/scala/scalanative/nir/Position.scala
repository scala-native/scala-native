package scala.scalanative.nir

import java.nio.file.Paths
import scala.util.Try
import java.nio.file.Path

final case class Position(
    /** Source file. */
    source: Position.SourceFile,
    /** Zero-based line number. */
    line: Int,
    /** Zero-based column number. */
    column: Int
) {

  lazy val path: Option[Path] = {
    source.getScheme() match {
      case "file"           => Some(Paths.get(source))
      case "https" | "http" => Some(Paths.get(source.getRawPath()))
      case _                => None

    }
  }

  lazy val filename: Option[String] = path.map(_.getFileName().toString)

  lazy val dir: Option[String] = source.getScheme() match {
    case "file" => path.map(_.getParent().toString)
    case "http" | "https" =>
      val fullStr = source.toString()
      filename.map(fname => fullStr.stripSuffix(fname))
    case _ => None
  }

  /** One-based line number */
  def sourceLine: Int = line + 1

  /** One-based column number */
  def sourceColumn: Int = column + 1
  def show: String = s"$source:$sourceLine:$sourceColumn"

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
    def apply(f: String): SourceFile = new java.net.URI(f)
  }
  val NoPosition = Position(SourceFile(""), 0, 0)
}
