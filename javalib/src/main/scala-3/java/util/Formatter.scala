package java.util
// Ported from Scala.js, commit: 0383e9f, dated: 2021-03-07

import java.io._
import java.lang.{
  Double => JDouble,
  Boolean => JBoolean,
  StringBuilder => JStringBuilder
}
import java.math.{BigDecimal, BigInteger}
import java.nio.CharBuffer
import java.nio.charset.Charset
import scala.annotation.{switch, tailrec}

final class Formatter private (
    dest: Appendable,
    formatterLocaleInfo: Formatter.LocaleInfo
) extends FormatterImpl(dest, formatterLocaleInfo) {
  import Formatter._

  def this() = this(new JStringBuilder(), Formatter.RootLocaleInfo)
  def this(a: Appendable) = this(a, Formatter.RootLocaleInfo)
  def this(l: Locale) =
    this(new JStringBuilder(), new Formatter.LocaleLocaleInfo(l))

  def this(a: Appendable, l: Locale) =
    this(a, new Formatter.LocaleLocaleInfo(l))

  private def this(
      os: OutputStream,
      csn: String,
      localeInfo: Formatter.LocaleInfo
  ) =
    this(
      new BufferedWriter(new OutputStreamWriter(os, csn)),
      localeInfo
    )
  def this(os: OutputStream, csn: String, l: Locale) =
    this(os, csn, new Formatter.LocaleLocaleInfo(l))
  def this(os: OutputStream, csn: String) =
    this(os, csn, Formatter.RootLocaleInfo)
  def this(os: OutputStream) =
    this(os, Charset.defaultCharset().name(), Formatter.RootLocaleInfo)

  private def this(file: File, csn: String, l: Formatter.LocaleInfo) =
    this(
      {
        var fout: FileOutputStream = null
        try {
          fout = new FileOutputStream(file)
          val writer = new OutputStreamWriter(fout, csn)
          new BufferedWriter(writer)
        } catch {
          case e @ (_: RuntimeException | _: UnsupportedEncodingException) =>
            if (fout != null) {
              try { fout.close() }
              catch {
                case _: IOException => () // silently
              }
            }
            throw e
        }
      },
      l
    )

  def this(file: File, csn: String, l: Locale) =
    this(file, csn, new Formatter.LocaleLocaleInfo(l))
  def this(file: File, csn: String) = this(file, csn, Formatter.RootLocaleInfo)

  def this(file: File) = this(new FileOutputStream(file))
  def this(ps: PrintStream) =
    this(
      {
        if (null == ps)
          throw new NullPointerException()
        ps
      },
      Formatter.RootLocaleInfo
    )

  def this(fileName: String, csn: String, l: Locale) =
    this(new File(fileName), csn, l)
  def this(fileName: String, csn: String) = this(new File(fileName), csn)
  def this(fileName: String) = this(new File(fileName))
}

object Formatter extends FormatterCompanionImpl {
  enum BigDecimalLayoutForm extends Enum[BigDecimalLayoutForm]() {
    case SCIENTIFIC extends BigDecimalLayoutForm
    case DECIMAL_FLOAT extends BigDecimalLayoutForm
  }
}
