package scala.scalanative

import java.nio.file.Path

package object build {
  sealed trait CompilationOutput {
    def path: Path
  }
  case class ObjectFile(path: Path) extends CompilationOutput
  case class Library(path: Path) extends CompilationOutput
  
}
