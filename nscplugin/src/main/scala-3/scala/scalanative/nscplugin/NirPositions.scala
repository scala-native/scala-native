package scala.scalanative.nscplugin

import dotty.tools.dotc.core._
import Contexts._

import dotty.tools.dotc.util.{SourceFile, SourcePosition}
import dotty.tools.dotc.util.Spans.Span
import scalanative.nir

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
    def nirSource = conversionCache.toNIRSource(source)
    if (span.exists && source.exists)
      // dotty positions are 1-based but NIR positions are 0-based
      val point = span.point
      val line = source.offsetToLine(point) - 1
      val column = source.column(point) - 1
      nir.Position(nirSource, line, column)
    else if (source.exists) nir.Position(nirSource, 0, 0)
    else nir.Position.NoPosition
  }

  private object conversionCache {
    import dotty.tools.dotc.util._
    private var lastDotcSource: SourceFile = _
    private var lastNIRSource: nir.Position.SourceFile = _

    def toNIRSource(dotcSource: SourceFile): nir.Position.SourceFile = {
      if (dotcSource != lastDotcSource) {
        lastNIRSource = convert(dotcSource)
        lastDotcSource = dotcSource
      }
      lastNIRSource
    }

    private def convert(
        dotcSource: SourceFile
    ): nir.Position.SourceFile = {
      dotcSource.file.file match {
        case null =>
          new java.net.URI(
            "virtualfile", // Pseudo-Scheme
            dotcSource.file.path, // Scheme specific part
            null // Fragment
          )
        case file => file.toURI
      }
    }
  }
}
