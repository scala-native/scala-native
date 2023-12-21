package scala.scalanative.nscplugin

import dotty.tools.dotc.core._
import Contexts._

import dotty.tools.dotc.util.{SourceFile, SourcePosition}
import dotty.tools.dotc.util.Spans.Span
import GenNIR.URIMap
import scalanative.nir
import scala.compiletime.uninitialized

class NirPositions(sourceURIMaps: List[GenNIR.URIMap])(using Context) {
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
        case file =>
          val srcURI = file.toURI
          def matches(pat: java.net.URI) = pat.relativize(srcURI) != srcURI

          sourceURIMaps
            .collectFirst {
              case URIMap(from, to) if matches(from) =>
                val relURI = from.relativize(srcURI)
                to.fold(relURI)(_.resolve(relURI))
            }
            .getOrElse(srcURI)
      }
    }
  }
}
