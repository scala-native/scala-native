package scala.scalanative

import java.nio.file.Path

package object build {
  sealed trait CompilationResult {
    def path: Path
  }
  case class ObjectFile(path: Path) extends CompilationResult
  case class Library(path: Path) extends CompilationResult

}
