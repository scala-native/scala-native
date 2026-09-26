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

  // `AbstractFile.bufferedOutput` is gone since 3.10.0, buffer it ourselves
  def abstractFileOutput(file: AbstractFile): java.io.OutputStream =
    new java.io.BufferedOutputStream(file.output)

  def sourceRootPath(using ctx: Context): Path = {
    val path =
      if !ctx.settings.sourcepath.isDefault then
        Paths.get(ctx.settings.sourcepath.value)
      else ctx.settings.sourceroot.value.jpath
    path.toAbsolutePath.normalize()
  }

  def absoluteSourcePath(source: SourceFile): Path =
    source.file.jpath.toAbsolutePath.normalize()

  def relativeSourcePath(source: SourceFile, relativeTo: Path): String = {
    val absSourcePath = absoluteSourcePath(source)
    val refPath = relativeTo.toAbsolutePath.normalize()
    if absSourcePath.startsWith(refPath) then
      refPath
        .relativize(absSourcePath)
        .toString
        .replace(java.io.File.separatorChar, '/')
    else source.file.path
  }
}
