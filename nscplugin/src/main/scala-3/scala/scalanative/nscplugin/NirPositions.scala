package scala.scalanative.nscplugin

import dotty.tools.dotc.core.*
import Contexts.*

import dotty.tools.dotc.util.{SourceFile, SourcePosition}
import dotty.tools.dotc.util.Spans.Span
import scalanative.nir
import scala.compiletime.uninitialized
import java.nio.file.{Path, Paths}

class NirPositions(positionRelativizationPaths: Seq[Path])(using Context) {
  given fromSourcePosition: Conversion[SourcePosition, nir.SourcePosition] = {
    sourcePos =>
      sourceAndSpanToNirPos(sourcePos.source, sourcePos.span)
  }

  given fromSpan: Conversion[Span, nir.SourcePosition] =
    sourceAndSpanToNirPos(ctx.compilationUnit.source, _)

  private def sourceAndSpanToNirPos(
      source: SourceFile,
      span: Span
  ): nir.SourcePosition = {
    def nirSource = conversionCache.toNIRSourceFile(source)
    if span.exists && source.exists then
      val point = span.point
      val line = source.offsetToLine(point)
      val column = source.column(point)
      nir.SourcePosition(nirSource, line, column)
    else nir.SourcePosition.NoPosition
  }

  private object conversionCache {
    import dotty.tools.dotc.util.*
    private var lastDotcSource: SourceFile = uninitialized
    private var lastNIRSource: nir.SourceFile = uninitialized

    def toNIRSourceFile(dotcSource: SourceFile): nir.SourceFile = {
      if dotcSource != lastDotcSource then {
        lastNIRSource = convert(dotcSource)
        lastDotcSource = dotcSource
      }
      lastNIRSource
    }

    private val sourceRoot = Paths
      .get(
        if !ctx.settings.sourcepath.isDefault
        then ctx.settings.sourcepath.value
        else ctx.settings.sourceroot.value
      )
      .toAbsolutePath()
    private def convert(dotcSource: SourceFile): nir.SourceFile = {
      if dotcSource.file.isVirtual
      then nir.SourceFile.Virtual
      else {
        val absSourcePath = dotcSource.file.absolute.jpath
        val relativeTo = positionRelativizationPaths
          .find(absSourcePath.startsWith(_))
          .getOrElse(sourceRoot)
          .toString()
        nir.SourceFile.Relative(SourceFile.relativePath(dotcSource, relativeTo))
      }
    }
  }
}
