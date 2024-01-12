package scala.scalanative.nscplugin

import dotty.tools.dotc.core._
import Contexts._

import dotty.tools.dotc.util.{SourceFile, SourcePosition}
import dotty.tools.dotc.util.Spans.Span
import scalanative.nir
import scala.compiletime.uninitialized

class NirPositions()(using Context) {
  given fromSourcePosition: Conversion[SourcePosition, nir.Position] = {
    sourcePos =>
      sourceAndSpanToNirPos(sourcePos.source, sourcePos.span)
  }

  given fromSpan: Conversion[Span, nir.Position] =
    sourceAndSpanToNirPos(ctx.compilationUnit.source, _)

  private def sourceAndSpanToNirPos(
      source: SourceFile,
      span: Span
  ): nir.Position = {
    def nirSource = conversionCache.toNIRSourceFile(source)
    if (span.exists && source.exists)
      val point = span.point
      val line = source.offsetToLine(point)
      val column = source.column(point)
      nir.Position(nirSource, line, column)
    else if (source.exists) nir.Position(nirSource, 0, 0)
    else nir.Position.NoPosition
  }

  private object conversionCache {
    import dotty.tools.dotc.util._
    private var lastDotcSource: SourceFile = uninitialized
    private var lastNIRSource: nir.SourceFile = uninitialized

    def toNIRSourceFile(dotcSource: SourceFile): nir.SourceFile = {
      if (dotcSource != lastDotcSource) {
        lastNIRSource = convert(dotcSource)
        lastDotcSource = dotcSource
      }
      lastNIRSource
    }

    private val sourceRoot =
      if !ctx.settings.sourcepath.isDefault
      then ctx.settings.sourcepath.value
      else ctx.settings.sourceroot.value
    private def convert(dotcSource: SourceFile): nir.SourceFile = {
      if dotcSource.file.isVirtual
      then nir.SourceFile.Virtual
      else
        val sourceRelativePath = SourceFile.relativePath(dotcSource, sourceRoot)
        nir.SourceFile.SourceRootRelative(sourceRelativePath)
    }
  }
}
