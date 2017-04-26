package java.util

import java.io._

import scala.util.matching.Regex
import java.lang.StringBuilder

class Formatter(private val dest: StringBuilder) extends Closeable with Flushable {
  var closed = false

  def this() = this(new StringBuilder())
  def this(appendable: Appendable, locale: Locale) = this()
  def this(appendable: Appendable) = this()
  def this(file: File, charsetName: String, locale: Locale) = this()
  def this(file: File, charsetName: String) = this()
  def this(file: File) = this()
  def this(locale: Locale) = this()
  def this(out: OutputStream, charsetName: String, locale: Locale) = this()
  def this(out: OutputStream, charsetName: String) = this()
  def this(out: OutputStream) = this()
  def this(out: PrintStream) = this()
  def this(fileName: String, charsetName: String, locale: Locale) = this()
  def this(fileName: String, charsetName: String) = this()
  def this(fileName: String) = this()

  def close(): Unit = ???
  def flush(): Unit = ???
  def format(locale: Locale, format: String, args: Array[Object]): Formatter =
    ???
  def format(format: String, args: Array[Object]): Formatter = ???
  def ioException(): IOException                             = ???
  def locale(): Locale                                       = ???
  def out(): Appendable                                      = ???
  override def toString: String                              = ???
}

object Formatter {

  private class RegExpExtractor(val regexp: Regex) {
    def unapply(str: String): Option[Regex.Match] = {
      regexp.findFirstMatchIn(str)
    }
  }

  private val RegularChunk = new RegExpExtractor("""^[^\x25]+""".r)
  private val DoublePercent = new RegExpExtractor("""^\x25{2}""".r)
  private val EOLChunk = new RegExpExtractor("""^\x25n""".r)
  private val FormattedChunk = new RegExpExtractor(
    """^\x25(?:([1-9]\d*)\$)?([-#+ 0,\(<]*)(\d*)(?:\.(\d+))?([A-Za-z])""".r)

}
