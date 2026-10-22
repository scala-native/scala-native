package scala.scalanative.nscplugin

import java.nio.file.{Path, Paths}

import dotty.tools.dotc.ast.tpd._
import dotty.tools.dotc.core.Contexts._
import dotty.tools.dotc.core.StdNames._
import dotty.tools.dotc.core.Symbols._
import dotty.tools.dotc.core.Types._
import dotty.tools.dotc.util.SourceFile
import dotty.tools.io.AbstractFile
import scala.annotation.unused

object CompilerCompat {
  val Inlines = dotty.tools.dotc.inlines.Inlines
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

  // Scala 3.10.1 replaced `getPrimitive(Apply, Type): Int` and `isPrimitive(Tree)`
  // with `getPrimitive(Tree): Option[Int]` and a final `isPrimitive(Symbol)`.
  abstract class NirPrimitivesBase(using ctx: Context)
      extends ScalaPrimitives(using ctx) {
    protected def nirPrimitiveCode(sym: Symbol): Option[Int]

    override def getPrimitive(sym: Symbol)(using Context): Option[Int] =
      nirPrimitiveCode(sym).orElse(super.getPrimitive(sym))

    // `tpe` stays in the signature for callers compiled against pre-3.10.1.
    // Array ops are now generic APPLY/UPDATE/LENGTH.
    def getPrimitive(app: Apply, @unused tpe: Type): Int =
      super.getPrimitive(app).get

    def isPrimitive(tree: Tree): Boolean = {
      val sym = tree.symbol
      if (sym == NoSymbol)
        tree match {
          case Select(_, nme.clone_) => false
          case _                     => true
        }
      else super.isPrimitive(sym)
    }
  }
}
