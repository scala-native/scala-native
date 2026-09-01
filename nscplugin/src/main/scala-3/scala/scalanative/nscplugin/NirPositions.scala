package scala.scalanative.nscplugin

import java.nio.file.Path

import dotty.tools.dotc.core._
import dotty.tools.dotc.util.Spans.Span
import dotty.tools.dotc.util.{SourceFile, SourcePosition}
import scala.compiletime.uninitialized

import scalanative.nir

import Contexts._

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
    if (span.exists && source.exists)
      val point = span.point
      val line = source.offsetToLine(point)
      val column = source.column(point)
      nir.SourcePosition(nirSource, line, column)
    else nir.SourcePosition.NoPosition
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

    private val sourceRoot = CompilerCompat.sourceRootPath
    private def convert(dotcSource: SourceFile): nir.SourceFile = {
      if dotcSource.file.isVirtual
      then nir.SourceFile.Virtual
      else {
        val absSourcePath = CompilerCompat.absoluteSourcePath(dotcSource)
        val relativeTo = positionRelativizationPaths
          .find(absSourcePath.startsWith(_))
          .getOrElse(sourceRoot)
        nir.SourceFile.Relative(
          CompilerCompat.relativeSourcePath(dotcSource, relativeTo)
        )
      }
    }
  }
}
