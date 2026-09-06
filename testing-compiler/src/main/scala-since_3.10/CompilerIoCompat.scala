package scala.scalanative

import java.nio.file.Path

import dotty.tools.dotc.util.SourceFile
import dotty.tools.io.AbstractFile

object CompilerIoCompat {
  def writeFile(file: AbstractFile, bytes: Array[Byte]): Unit = {
    val output = file.output
    output.write(bytes)
    output.close()
  }

  def sourceFile(
      file: AbstractFile,
      base: Path,
      codec: scala.io.Codec
  ): SourceFile = {
    val sourceRoot = AbstractFile.getDirectory(base, base.toString)
    SourceFile(file, sourceRoot, codec)
  }

  def sourcePath(file: AbstractFile): String =
    file.jpath.toAbsolutePath.normalize().toString
}
