package scala.scalanative

import java.nio.file.Path

import dotty.tools.dotc.util.SourceFile
import dotty.tools.io.AbstractFile

object CompilerIoCompat {
  def writeFile(file: AbstractFile, bytes: Array[Byte]): Unit = {
    val output = file.bufferedOutput
    output.write(bytes)
    output.close()
  }

  def sourceFile(
      file: AbstractFile,
      base: java.nio.file.Path,
      codec: scala.io.Codec
  ): SourceFile =
    SourceFile(file, codec)

  def sourcePath(file: AbstractFile): String =
    file.absolutePath
}
