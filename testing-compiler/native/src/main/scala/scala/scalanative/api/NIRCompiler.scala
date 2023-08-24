package scala.scalanative.api

import java.nio.file.Path

trait NIRCompiler {
    def compile(source: String): Array[Path]
    def compile(base: Path): Array[Path]
}
