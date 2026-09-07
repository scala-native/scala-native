package scala.scalanative.nscplugin

import java.nio.file.{Path, Paths}

import dotty.tools.dotc.core.Contexts._
import dotty.tools.dotc.util.SourceFile
import dotty.tools.io.AbstractFile

object CompilerCompat {
  val SymUtils = dotty.tools.dotc.core.Symbols
  val SymbolExtensions = dotty.tools.backend.jvm.SymbolUtils.symExtensions

  type ScalaPrimitives = dotty.tools.backend.ScalaPrimitives

  val LazyValHandleName = Option(
    dotty.tools.dotc.core.NameKinds.LazyVarHandleName
  )

  def abstractFileOutput(file: AbstractFile): java.io.OutputStream =
    file.bufferedOutput

  def sourceRootPath(using ctx: Context): Path = {
    val pathStr =
      if !ctx.settings.sourcepath.isDefault then ctx.settings.sourcepath.value
      else ctx.settings.sourceroot.value
    Paths.get(pathStr).toAbsolutePath.normalize()
  }

  def absoluteSourcePath(source: SourceFile): Path =
    source.file.absolute.jpath

  def relativeSourcePath(source: SourceFile, relativeTo: Path): String =
    SourceFile.relativePath(source, relativeTo.toString)
}
