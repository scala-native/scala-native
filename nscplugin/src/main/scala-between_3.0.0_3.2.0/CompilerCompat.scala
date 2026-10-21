package scala.scalanative.nscplugin

import java.nio.file.{Path, Paths}

import dotty.tools.dotc.core.Contexts._
import dotty.tools.dotc.core.Symbols.Symbol
import dotty.tools.dotc.util.SourceFile
import dotty.tools.io.AbstractFile

object CompilerCompat {
  val Inlines = dotty.tools.dotc.typer.Inliner
  val SymUtils = dotty.tools.dotc.transform.SymUtils
  val SymbolExtensions =
    dotty.tools.backend.jvm.DottyBackendInterface.symExtensions

  type ScalaPrimitives = dotty.tools.backend.jvm.DottyPrimitives

  val LazyValHandleName = None

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

  abstract class NirPrimitivesBase(using ctx: Context)
      extends ScalaPrimitives(ctx) {
    protected def nirPrimitiveCode(sym: Symbol): Option[Int]

    override def getPrimitive(sym: Symbol): Int =
      nirPrimitiveCode(sym).getOrElse(super.getPrimitive(sym))

    override def isPrimitive(sym: Symbol): Boolean =
      nirPrimitiveCode(sym).isDefined || super.isPrimitive(sym)
  }
}
