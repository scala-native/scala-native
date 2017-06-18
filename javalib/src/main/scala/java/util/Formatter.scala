package java.util

// Ported from Harmony

import java.io._
import java.lang.StringBuilder
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.security.AccessController
import java.security.PrivilegedAction
import java.text.DateFormatSymbols
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat

import scala.util.control.Breaks

class Formatter(
    private var _out: Appendable,
    private var _locale: Locale
) extends Closeable
    with Flushable {
  if (_out == null)
    _out = new StringBuilder()

  final class BigDecimalLayoutForm private (name: String, ordinal: Int)
      extends Enum[BigDecimalLayoutForm](name, ordinal)

  object BigDecimalLayoutForm {
    final val SCIENTIFIC    = new BigDecimalLayoutForm("SCIENTIFIC", 0)
    final val DECIMAL_FLOAT = new BigDecimalLayoutForm("DECIMAL_FLOAT", 1)

    def valueOf(name: String): BigDecimalLayoutForm =
      _values.find(_.name == name).getOrElse {
        throw new IllegalArgumentException(
          "No enum constant java.util.Formatter.BigDecimalLayoutForm." + name)
      }

    private val _values: Array[BigDecimalLayoutForm] =
      Array(SCIENTIFIC, DECIMAL_FLOAT)

    def values(): Array[BigDecimalLayoutForm] = _values.clone()
  }

  private var closed: Boolean = false

  private var lastIOException: IOException = _

  // Porting note: According to JDK Javadoc, Locale.getDefault() should be
  // Locale.getDefault(Locale.Category.FORMAT). However, the former is used because
  // Harmony does. The category doesn't exist yet in Scala Native, anyway.
  // Porting note #2: The test suite, which is also ported from Harmony,
  // assumes Locale.getDefault() as the default locale.
  def this() =
    this(new StringBuilder(), Locale.getDefault())
  def this(a: Appendable) =
    this(a, Locale.getDefault())
  def this(l: Locale) =
    this(new StringBuilder(), l)
  def this(os: OutputStream, csn: String, l: Locale) =
    this(
      new BufferedWriter(new OutputStreamWriter(os, csn)),
      l
    )
  def this(os: OutputStream, csn: String) =
    this(os, csn, Locale.getDefault())
  def this(os: OutputStream) =
    this(
      new BufferedWriter(new OutputStreamWriter(os, Charset.defaultCharset())),
      Locale.getDefault()
    )
  def this(file: File, csn: String, l: Locale) =
    this(
      {
        var fout: FileOutputStream = null
        try {
          fout = new FileOutputStream(file)
          val writer = new OutputStreamWriter(fout, csn)
          new BufferedWriter(writer)
        } catch {
          case e @ (_: RuntimeException | _: UnsupportedEncodingException) =>
            Formatter.closeOutputStream(fout)
            throw e
        }
      },
      l
    )
  def this(file: File, csn: String) =
    this(file, csn, Locale.getDefault())
  def this(file: File) =
    this(new FileOutputStream(file))
  def this(ps: PrintStream) =
    this(
      {
        if (null == ps)
          throw new NullPointerException()
        ps
      },
      Locale.getDefault()
    )
  def this(fileName: String, csn: String, l: Locale) =
    this(new File(fileName), csn, l)
  def this(fileName: String, csn: String) =
    this(new File(fileName), csn)
  def this(fileName: String) =
    this(new File(fileName))

  private def checkClosed(): Unit =
    if (closed)
      throw new FormatterClosedException()

  def locale(): Locale = {
    checkClosed()
    _locale
  }

  def out(): Appendable = {
    checkClosed()
    _out
  }

  override def toString: String = {
    checkClosed()
    _out.toString()
  }

  def flush(): Unit = {
    checkClosed()
    _out match {
      case f: Flushable =>
        try {
          f.flush()
        } catch {
          case e: IOException => lastIOException = e
        }
      case _ =>
    }
  }

  def close(): Unit = {
    closed = true
    try {
      _out match {
        case c: Closeable =>
          c.close()
        case _ =>
      }
    } catch {
      case e: IOException => lastIOException = e
    }
  }

  def ioException(): IOException = lastIOException

  def format(format: String, args: Array[Object]): Formatter =
    this.format(_locale, format, args)

  import Formatter._

  def format(l: Locale, format: String, args: Array[Object]): Formatter = {
    checkClosed()
    val formatBuffer = CharBuffer.wrap(format)
    val parser       = new ParserStateMachine(formatBuffer)
    val transformer  = new Transformer(this, l)

    var currentObjectIndex: Int     = 0
    var lastArgument: Object        = null
    var hasLastArgumentSet: Boolean = false
    while (formatBuffer.hasRemaining()) {
      parser.reset()
      val token          = parser.getNextFormatToken()
      var result: String = null
      var plainText      = token.getPlainText()
      if (token.getConversionType() == FormatToken.UNSET.asInstanceOf[Char]) {
        result = plainText
      } else {
        plainText = plainText.substring(0, plainText.indexOf('%'))
        var argument: Object = null
        if (token.requireArgument()) {
          val index =
            if (token.getArgIndex() == FormatToken.UNSET) {
              val idx = currentObjectIndex
              currentObjectIndex += 1
              idx
            } else
              token.getArgIndex()
          argument =
            getArgument(args, index, token, lastArgument, hasLastArgumentSet)
          lastArgument = argument
          hasLastArgumentSet = true
        }
        result = transformer.transform(token, argument)
        result = if (null == result) plainText else plainText + result
      }
      // if output is made by formattable callback
      if (null != result) {
        try {
          _out.append(result)
        } catch {
          case e: IOException => lastIOException = e
        }
      }
    }
    this
  }

  private def getArgument(args: Array[Object],
                          index: Int,
                          token: FormatToken,
                          lastArgument: Object,
                          hasLastArgumentSet: Boolean): Object = {
    if (index == FormatToken.LAST_ARGUMENT_INDEX && !hasLastArgumentSet)
      throw new MissingFormatArgumentException("<")
    else if (null == args)
      null
    else if (index >= args.length)
      throw new MissingFormatArgumentException(token.getPlainText())
    else if (index == FormatToken.LAST_ARGUMENT_INDEX)
      lastArgument
    else
      args(index)
  }
}

object Formatter {
  private def closeOutputStream(os: OutputStream): Unit = {
    if (null == os)
      return
    try {
      os.close()
    } catch {
      case _: IOException =>
      // silently
    }
  }

  private class FormatToken {
    import FormatToken._

    private var formatStringStartIndex: Int = _

    private var plainText: String = _

    private var argIndex: Int = UNSET

    private var flags: Int = 0

    private var width: Int = UNSET

    private var precision: Int = UNSET

    private val strFlags = new StringBuilder(FLAGT_TYPE_COUNT)

    private var dateSuffix: Char = _ // will be used in new feature.

    private var conversionType: Char = UNSET.asInstanceOf[Char]

    def isPrecisionSet(): Boolean = precision != UNSET

    def isWidthSet(): Boolean = width != UNSET

    def isFlagSet(flag: Int): Boolean = 0 != (flags & flag)

    def getArgIndex(): Int = argIndex

    def setArgIndex(index: Int): Unit = argIndex = index

    def getPlainText(): String = plainText

    def setPlainText(plainText: String): Unit = this.plainText = plainText

    def getWidth(): Int = width

    def setWidth(width: Int): Unit = this.width = width

    def getPrecision(): Int = precision

    def setPrecision(precise: Int): Unit = this.precision = precise

    def getStrFlags(): String = strFlags.toString()

    def getFlags(): Int = flags

    def setFlags(flags: Int): Unit = this.flags = flags

    def setFlag(c: Char): Boolean = {
      var newFlag: Int = 0
      c match {
        case '-' => newFlag = FLAG_MINUS
        case '#' => newFlag = FLAG_SHARP
        case '+' => newFlag = FLAG_ADD
        case ' ' => newFlag = FLAG_SPACE
        case '0' => newFlag = FLAG_ZERO
        case ',' => newFlag = FLAG_COMMA
        case '(' => newFlag = FLAG_PARENTHESIS
        case _   => return false
      }
      if (0 != (flags & newFlag))
        throw new DuplicateFormatFlagsException(String.valueOf(c))
      flags = (flags | newFlag)
      strFlags.append(c)
      true
    }

    def getFormatStringStartIndex(): Int = formatStringStartIndex

    def setFormatStringStartIndex(index: Int): Unit =
      formatStringStartIndex = index

    def getConversionType(): Char = conversionType

    def setConversionType(c: Char): Unit = conversionType = c

    def getDateSuffix(): Char = dateSuffix

    def setDateSuffix(c: Char): Unit = dateSuffix = c

    def requireArgument(): Boolean =
      conversionType != '%' && conversionType != 'n'
  }

  private object FormatToken {
    val LAST_ARGUMENT_INDEX = -2

    val UNSET: Int = -1

    val FLAGS_UNSET: Int = 0

    val DEFAULT_PRECISION: Int = 6

    val FLAG_MINUS: Int = 1

    val FLAG_SHARP: Int = 1 << 1

    val FLAG_ADD: Int = 1 << 2

    val FLAG_SPACE: Int = 1 << 3

    val FLAG_ZERO: Int = 1 << 4

    val FLAG_COMMA: Int = 1 << 5

    val FLAG_PARENTHESIS: Int = 1 << 6

    private val FLAGT_TYPE_COUNT: Int = 6
  }

  private class Transformer(formatter: Formatter, locale_ : Locale) {
    import Transformer._

    private var formatToken: FormatToken = _

    private var arg: Object = _

    private val locale = if (null == locale_) Locale.US else locale_

    private var numberFormat: NumberFormat = _

    private var decimalFormatSymbols: DecimalFormatSymbols = _

    private var dateTimeUtil: DateTimeUtil = _

    private def getNumberFormat(): NumberFormat = {
      if (null == numberFormat)
        numberFormat = NumberFormat.getInstance(locale)
      numberFormat
    }

    private def getDecimalFormatSymbols(): DecimalFormatSymbols = {
      if (null == decimalFormatSymbols)
        decimalFormatSymbols = new DecimalFormatSymbols(locale)
      decimalFormatSymbols
    }

    def transform(token: FormatToken, argument: Object): String = {
      this.formatToken = token
      this.arg = argument

      var result =
        token.getConversionType() match {
          case 'B' | 'b' => transformFromBoolean()
          case 'H' | 'h' => transformFromHashCode()
          case 'S' | 's' => transformFromString()
          case 'C' | 'c' => transformFromCharacter()
          case 'd' | 'o' | 'x' | 'X' =>
            if (null == arg || arg.isInstanceOf[BigInteger])
              transformFromBigInteger()
            else
              transformFromInteger()
          case 'e' | 'E' | 'g' | 'G' | 'f' | 'a' | 'A' =>
            transformFromFloat()
          case '%' => transformFromPercent()
          case 'n' => transformFromLineSeparator()
          case 't' => transformFromDateTime()
          case unknown =>
            throw new UnknownFormatConversionException(String.valueOf(unknown))
        }

      if (Character.isUpperCase(token.getConversionType())) {
        if (null != result) {
          // Porting note: Harmony does this but this.locale should be respected
          result = result.toUpperCase(Locale.US)
        }
      }
      result
    }

    private def transformFromBoolean(): String = {
      val result     = new StringBuilder()
      val startIndex = 0
      val flags      = formatToken.getFlags()

      if (formatToken.isFlagSet(FormatToken.FLAG_MINUS) && !formatToken
            .isWidthSet())
        throw new MissingFormatWidthException(
          "-" + formatToken.getConversionType())

      if (FormatToken.FLAGS_UNSET != flags && FormatToken.FLAG_MINUS != flags)
        throw new FormatFlagsConversionMismatchException(
          formatToken.getStrFlags(),
          formatToken.getConversionType())

      if (null == arg)
        result.append("false")
      else if (arg.isInstanceOf[Boolean])
        result.append(arg)
      else
        result.append("true")
      padding(result, startIndex)
    }

    private def transformFromHashCode(): String = {
      val result = new StringBuilder()

      val startIndex = 0
      val flags      = formatToken.getFlags()

      if (formatToken.isFlagSet(FormatToken.FLAG_MINUS) && !formatToken
            .isWidthSet())
        throw new MissingFormatWidthException(
          "-" + formatToken.getConversionType())

      if (FormatToken.FLAGS_UNSET != flags && FormatToken.FLAG_MINUS != flags)
        throw new FormatFlagsConversionMismatchException(
          formatToken.getStrFlags(),
          formatToken.getConversionType())

      if (null == arg)
        result.append("null")
      else
        result.append(Integer.toHexString(arg.hashCode()))
      padding(result, startIndex)
    }

    private def transformFromString(): String = {
      val result     = new StringBuilder()
      val startIndex = 0
      val flags      = formatToken.getFlags()

      if (formatToken.isFlagSet(FormatToken.FLAG_MINUS) && !formatToken
            .isWidthSet())
        throw new MissingFormatWidthException(
          "-" + formatToken.getConversionType())

      if (arg.isInstanceOf[Formattable]) {
        var flag: Int = 0
        if (FormatToken.FLAGS_UNSET != (flags & ~FormatToken.FLAG_MINUS & ~FormatToken.FLAG_SHARP))
          throw new IllegalFormatFlagsException(formatToken.getStrFlags())

        if (formatToken.isFlagSet(FormatToken.FLAG_MINUS))
          flag |= FormattableFlags.LEFT_JUSTIFY
        if (formatToken.isFlagSet(FormatToken.FLAG_SHARP))
          flag |= FormattableFlags.ALTERNATE
        if (Character.isUpperCase(formatToken.getConversionType()))
          flag |= FormattableFlags.UPPERCASE
        arg
          .asInstanceOf[Formattable]
          .formatTo(formatter,
                    flag,
                    formatToken.getWidth(),
                    formatToken.getPrecision())
        // all actions have been taken out in the
        // Formattable.formatTo, thus there is nothing to do, just
        // returns null, which tells the Parser to add nothing to the
        // output.
        return null
      }
      if (FormatToken.FLAGS_UNSET != flags && FormatToken.FLAG_MINUS != flags)
        throw new FormatFlagsConversionMismatchException(
          formatToken.getStrFlags(),
          formatToken.getConversionType())

      result.append(arg)
      padding(result, startIndex)
    }

    private def transformFromCharacter(): String = {
      val result = new StringBuilder()

      val startIndex = 0
      val flags      = formatToken.getFlags()

      if (formatToken.isFlagSet(FormatToken.FLAG_MINUS) && !formatToken
            .isWidthSet())
        throw new MissingFormatWidthException(
          "-" + formatToken.getConversionType())

      if (FormatToken.FLAGS_UNSET != flags && FormatToken.FLAG_MINUS != flags)
        throw new FormatFlagsConversionMismatchException(
          formatToken.getStrFlags(),
          formatToken.getConversionType())

      if (formatToken.isPrecisionSet())
        throw new IllegalFormatPrecisionException(formatToken.getPrecision())

      arg.asInstanceOf[Any] match {
        case null         => result.append("null")
        case c: Character => result.append(c)
        case b: Byte =>
          if (!Character.isValidCodePoint(b))
            throw new IllegalFormatCodePointException(b)
          result.append(b.asInstanceOf[Char])
        case s: Short =>
          if (!Character.isValidCodePoint(s))
            throw new IllegalFormatCodePointException(s)
          result.append(s.asInstanceOf[Char])
        case codePoint: Int =>
          if (!Character.isValidCodePoint(codePoint))
            throw new IllegalFormatCodePointException(codePoint)
          result.append(String.valueOf(Character.toChars(codePoint)))
        case _ =>
          throw new IllegalFormatConversionException(
            formatToken.getConversionType(),
            arg.getClass())
      }
      padding(result, startIndex)
    }

    private def transformFromPercent(): String = {
      val result = new StringBuilder("%")

      val startIndex = 0
      val flags      = formatToken.getFlags()

      if (formatToken.isFlagSet(FormatToken.FLAG_MINUS) && !formatToken
            .isWidthSet())
        throw new MissingFormatWidthException(
          "-" + formatToken.getConversionType())

      if (FormatToken.FLAGS_UNSET != flags && FormatToken.FLAG_MINUS != flags)
        throw new FormatFlagsConversionMismatchException(
          formatToken.getStrFlags(),
          formatToken.getConversionType())
      if (formatToken.isPrecisionSet())
        throw new IllegalFormatPrecisionException(formatToken.getPrecision())
      padding(result, startIndex)
    }

    private def transformFromLineSeparator(): String = {
      if (formatToken.isPrecisionSet())
        throw new IllegalFormatPrecisionException(formatToken.getPrecision())

      if (formatToken.isWidthSet())
        throw new IllegalFormatWidthException(formatToken.getWidth())

      val flags = formatToken.getFlags()
      if (FormatToken.FLAGS_UNSET != flags)
        throw new IllegalFormatFlagsException(formatToken.getStrFlags())

      if (null == lineSeparator) {
        lineSeparator =
          AccessController.doPrivileged(new PrivilegedAction[String]() {
            def run(): String = System.getProperty("line.separator")
          })
      }
      lineSeparator
    }

    private def padding(source: StringBuilder, startIndex: Int): String = {
      var start: Int        = startIndex
      val paddingRight      = formatToken.isFlagSet(FormatToken.FLAG_MINUS)
      var paddingChar: Char = '\u0020' // space as padding char.
      if (formatToken.isFlagSet(FormatToken.FLAG_ZERO)) {
        if ('d' == formatToken.getConversionType())
          paddingChar = getDecimalFormatSymbols().getZeroDigit()
        else
          paddingChar = '0'
      } else {
        start = 0
      }
      var width     = formatToken.getWidth()
      val precision = formatToken.getPrecision()

      var length = source.length()
      if (precision >= 0) {
        length = Math.min(length, precision)
        source.delete(length, source.length())
      }
      if (width > 0) {
        width = Math.max(source.length(), width)
      }
      if (length >= width) {
        return source.toString()
      }

      val paddings     = Array.fill[Char](width - length)(paddingChar)
      val insertString = new String(paddings)

      if (paddingRight)
        source.append(insertString)
      else
        source.insert(start, insertString)
      source.toString()
    }

    private def transformFromInteger(): String = {
      var startIndex            = 0
      var isNegative            = false
      var result                = new StringBuilder()
      val currentConversionType = formatToken.getConversionType()
      var value: Long           = 0

      if (formatToken.isFlagSet(FormatToken.FLAG_MINUS) || formatToken
            .isFlagSet(FormatToken.FLAG_ZERO)) {
        if (!formatToken.isWidthSet())
          throw new MissingFormatWidthException(formatToken.getStrFlags())
      }
      if (formatToken.isFlagSet(FormatToken.FLAG_ADD) && formatToken.isFlagSet(
            FormatToken.FLAG_SPACE))
        throw new IllegalFormatFlagsException(formatToken.getStrFlags())
      if (formatToken.isPrecisionSet())
        throw new IllegalFormatPrecisionException(formatToken.getPrecision())
      arg.asInstanceOf[Any] match {
        case l: Long  => value = l
        case i: Int   => value = i.toLong
        case s: Short => value = s.toLong
        case b: Byte  => value = b.toLong
        case _ =>
          throw new IllegalFormatConversionException(
            formatToken.getConversionType(),
            arg.getClass())
      }
      if ('d' != currentConversionType) {
        if (formatToken.isFlagSet(FormatToken.FLAG_ADD) ||
            formatToken.isFlagSet(FormatToken.FLAG_SPACE) ||
            formatToken.isFlagSet(FormatToken.FLAG_COMMA) ||
            formatToken.isFlagSet(FormatToken.FLAG_PARENTHESIS)) {
          throw new FormatFlagsConversionMismatchException(
            formatToken.getStrFlags(),
            formatToken.getConversionType())
        }
      }

      if (formatToken.isFlagSet(FormatToken.FLAG_SHARP)) {
        if ('d' == currentConversionType)
          throw new FormatFlagsConversionMismatchException(
            formatToken.getStrFlags(),
            formatToken.getConversionType())
        else if ('o' == currentConversionType) {
          result.append("0")
          startIndex += 1
        } else {
          result.append("0x")
          startIndex += 2
        }
      }

      if (formatToken.isFlagSet(FormatToken.FLAG_MINUS) && formatToken
            .isFlagSet(FormatToken.FLAG_ZERO))
        throw new IllegalFormatFlagsException(formatToken.getStrFlags())

      if (value < 0)
        isNegative = true

      if ('d' == currentConversionType) {
        val numberFormat = getNumberFormat()
        if (formatToken.isFlagSet(FormatToken.FLAG_COMMA))
          numberFormat.setGroupingUsed(true)
        else
          numberFormat.setGroupingUsed(false)
        result.append(numberFormat.format(arg))
      } else {
        val BYTE_MASK: Long  = 0x00000000000000FFL;
        val SHORT_MASK: Long = 0x000000000000FFFFL;
        val INT_MASK: Long   = 0x00000000FFFFFFFFL;
        if (isNegative) {
          if (arg.isInstanceOf[Byte])
            value &= BYTE_MASK
          else if (arg.isInstanceOf[Short])
            value &= SHORT_MASK
          else if (arg.isInstanceOf[Int])
            value &= INT_MASK
        }
        if ('o' == currentConversionType)
          result.append(java.lang.Long.toOctalString(value))
        else
          result.append(java.lang.Long.toHexString(value))
        isNegative = false
      }

      if (!isNegative) {
        if (formatToken.isFlagSet(FormatToken.FLAG_ADD)) {
          result.insert(0, '+')
          startIndex += 1
        }
        if (formatToken.isFlagSet(FormatToken.FLAG_SPACE)) {
          result.insert(0, ' ')
          startIndex += 1
        }
      }

      if (isNegative && formatToken.isFlagSet(FormatToken.FLAG_PARENTHESIS)) {
        result = wrapParentheses(result)
        return result.toString()
      }
      if (isNegative && formatToken.isFlagSet(FormatToken.FLAG_ZERO))
        startIndex += 1
      return padding(result, startIndex)
    }

    private def wrapParentheses(result: StringBuilder): StringBuilder = {
      // delete the '-'
      result.deleteCharAt(0)
      result.insert(0, '(')
      if (formatToken.isFlagSet(FormatToken.FLAG_ZERO)) {
        formatToken.setWidth(formatToken.getWidth() - 1)
        padding(result, 1)
        result.append(')')
      } else {
        result.append(')')
        padding(result, 0)
      }
      result
    }

    private def transformFromSpecialNumber(): String = {
      var source: String = null

      if (!(arg.isInstanceOf[Number]) || arg.isInstanceOf[BigDecimal])
        return null

      val number = arg.asInstanceOf[Number]
      val d      = number.doubleValue()
      if (java.lang.Double.isNaN(d))
        source = "NaN"
      else if (java.lang.Double.isInfinite(d)) {
        if (d >= 0) {
          if (formatToken.isFlagSet(FormatToken.FLAG_ADD))
            source = "+Infinity"
          else if (formatToken.isFlagSet(FormatToken.FLAG_SPACE))
            source = " Infinity"
          else
            source = "Infinity"
        } else {
          if (formatToken.isFlagSet(FormatToken.FLAG_PARENTHESIS))
            source = "(Infinity)"
          else
            source = "-Infinity"
        }
      }

      if (null != source) {
        formatToken.setPrecision(FormatToken.UNSET)
        formatToken.setFlags(formatToken.getFlags() & (~FormatToken.FLAG_ZERO))
        source = padding(new StringBuilder(source), 0)
      }
      source
    }

    private def transformFromNull(): String = {
      formatToken.setFlags(formatToken.getFlags() & (~FormatToken.FLAG_ZERO))
      padding(new StringBuilder("null"), 0)
    }

    private def transformFromBigInteger(): String = {
      var startIndex            = 0
      var isNegative            = false
      var result                = new StringBuilder()
      val bigInt                = arg.asInstanceOf[BigInteger]
      val currentConversionType = formatToken.getConversionType()

      if (formatToken.isFlagSet(FormatToken.FLAG_MINUS) || formatToken
            .isFlagSet(FormatToken.FLAG_ZERO)) {
        if (!formatToken.isWidthSet())
          throw new MissingFormatWidthException(formatToken.getStrFlags())
      }

      if (formatToken.isFlagSet(FormatToken.FLAG_ADD) && formatToken.isFlagSet(
            FormatToken.FLAG_SPACE))
        throw new IllegalFormatFlagsException(formatToken.getStrFlags())

      if (formatToken.isFlagSet(FormatToken.FLAG_ZERO) && formatToken
            .isFlagSet(FormatToken.FLAG_MINUS))
        throw new IllegalFormatFlagsException(formatToken.getStrFlags())

      if (formatToken.isPrecisionSet())
        throw new IllegalFormatPrecisionException(formatToken.getPrecision())

      if ('d' != currentConversionType && formatToken.isFlagSet(
            FormatToken.FLAG_COMMA))
        throw new FormatFlagsConversionMismatchException(
          formatToken.getStrFlags(),
          currentConversionType)

      if (formatToken.isFlagSet(FormatToken.FLAG_SHARP) && 'd' == currentConversionType)
        throw new FormatFlagsConversionMismatchException(
          formatToken.getStrFlags(),
          currentConversionType)

      if (null == bigInt)
        return transformFromNull()

      isNegative = (bigInt.compareTo(BigInteger.ZERO) < 0)

      if ('d' == currentConversionType) {
        val numberFormat = getNumberFormat()
        val readableName = formatToken.isFlagSet(FormatToken.FLAG_COMMA)
        numberFormat.setGroupingUsed(readableName)
        result.append(numberFormat.format(bigInt))
      } else if ('o' == currentConversionType) {
        result.append(bigInt.toString(8))
      } else {
        result.append(bigInt.toString(16))
      }
      if (formatToken.isFlagSet(FormatToken.FLAG_SHARP)) {
        startIndex = if (isNegative) 1 else 0
        if ('o' == currentConversionType) {
          result.insert(startIndex, "0")
          startIndex += 1
        } else if ('x' == currentConversionType || 'X' == currentConversionType) {
          result.insert(startIndex, "0x")
          startIndex += 2
        }
      }

      if (!isNegative) {
        if (formatToken.isFlagSet(FormatToken.FLAG_ADD)) {
          result.insert(0, '+')
          startIndex += 1
        }
        if (formatToken.isFlagSet(FormatToken.FLAG_SPACE)) {
          result.insert(0, ' ')
          startIndex += 1
        }
      }

      if (isNegative && formatToken.isFlagSet(FormatToken.FLAG_PARENTHESIS)) {
        result = wrapParentheses(result)
        return result.toString()
      }
      if (isNegative && formatToken.isFlagSet(FormatToken.FLAG_ZERO)) {
        startIndex += 1
      }
      padding(result, startIndex)
    }

    private def transformFromFloat(): String = {
      var result                = new StringBuilder()
      var startIndex            = 0
      val currentConversionType = formatToken.getConversionType()

      if (formatToken.isFlagSet(
            FormatToken.FLAG_MINUS | FormatToken.FLAG_ZERO)) {
        if (!formatToken.isWidthSet())
          throw new MissingFormatWidthException(formatToken.getStrFlags())
      }

      if (formatToken.isFlagSet(FormatToken.FLAG_ADD) && formatToken.isFlagSet(
            FormatToken.FLAG_SPACE))
        throw new IllegalFormatFlagsException(formatToken.getStrFlags())

      if (formatToken.isFlagSet(FormatToken.FLAG_MINUS) && formatToken
            .isFlagSet(FormatToken.FLAG_ZERO))
        throw new IllegalFormatFlagsException(formatToken.getStrFlags())

      if ('e' == Character.toLowerCase(currentConversionType)) {
        if (formatToken.isFlagSet(FormatToken.FLAG_COMMA))
          throw new FormatFlagsConversionMismatchException(
            formatToken.getStrFlags(),
            currentConversionType)
      }

      if ('g' == Character.toLowerCase(currentConversionType)) {
        if (formatToken.isFlagSet(FormatToken.FLAG_SHARP))
          throw new FormatFlagsConversionMismatchException(
            formatToken.getStrFlags(),
            currentConversionType)
      }

      if ('a' == Character.toLowerCase(currentConversionType)) {
        if (formatToken.isFlagSet(FormatToken.FLAG_COMMA) || formatToken
              .isFlagSet(FormatToken.FLAG_PARENTHESIS))
          throw new FormatFlagsConversionMismatchException(
            formatToken.getStrFlags(),
            currentConversionType)
      }

      if (null == arg)
        return transformFromNull()

      if (!(arg.isInstanceOf[Float] || arg.isInstanceOf[Double] || arg
            .isInstanceOf[BigDecimal]))
        throw new IllegalFormatConversionException(currentConversionType,
                                                   arg.getClass())

      val specialNumberResult = transformFromSpecialNumber()

      if (null != specialNumberResult)
        return specialNumberResult

      if ('a' != Character.toLowerCase(currentConversionType)) {
        formatToken.setPrecision {
          if (formatToken.isPrecisionSet())
            formatToken.getPrecision()
          else
            FormatToken.DEFAULT_PRECISION
        }
      }

      val floatUtil = new FloatUtil(
        result,
        formatToken,
        NumberFormat.getInstance(locale).asInstanceOf[DecimalFormat],
        arg)
      floatUtil.transform(formatToken, result)

      formatToken.setPrecision(FormatToken.UNSET)

      if (getDecimalFormatSymbols().getMinusSign() == result.charAt(0)) {
        if (formatToken.isFlagSet(FormatToken.FLAG_PARENTHESIS)) {
          result = wrapParentheses(result)
          return result.toString()
        }
      } else {
        if (formatToken.isFlagSet(FormatToken.FLAG_SPACE)) {
          result.insert(0, ' ')
          startIndex += 1
        }
        if (formatToken.isFlagSet(FormatToken.FLAG_ADD)) {
          result.insert(0, floatUtil.getAddSign())
          startIndex += 1
        }
      }

      val firstChar = result.charAt(0)
      if (formatToken.isFlagSet(FormatToken.FLAG_ZERO) && (firstChar == floatUtil
            .getAddSign() || firstChar == floatUtil.getMinusSign()))
        startIndex = 1

      if ('a' == Character.toLowerCase(currentConversionType))
        startIndex += 2
      padding(result, startIndex)
    }

    private def transformFromDateTime(): String = {
      val startIndex            = 0
      val currentConversionType = formatToken.getConversionType()

      if (formatToken.isPrecisionSet())
        throw new IllegalFormatPrecisionException(formatToken.getPrecision())

      if (formatToken.isFlagSet(FormatToken.FLAG_SHARP))
        throw new FormatFlagsConversionMismatchException(
          formatToken.getStrFlags(),
          currentConversionType)

      if (formatToken.isFlagSet(FormatToken.FLAG_MINUS) && FormatToken.UNSET == formatToken
            .getWidth())
        throw new MissingFormatWidthException("-" + currentConversionType)

      if (null == arg)
        return transformFromNull()

      val calendar =
        arg match {
          case cal: Calendar => cal
          case _ =>
            val date =
              arg.asInstanceOf[Any] match {
                case l: Long => new Date(l)
                case d: Date => d
                case _ =>
                  throw new IllegalFormatConversionException(
                    currentConversionType,
                    arg.getClass())
              }
            val calendar = Calendar.getInstance(locale)
            calendar.setTime(date)
            calendar
        }

      if (null == dateTimeUtil)
        dateTimeUtil = new DateTimeUtil(locale)
      val result = new StringBuilder()
      dateTimeUtil.transform(formatToken, calendar, result)
      padding(result, startIndex)
    }
  }

  private object Transformer {
    private var lineSeparator: String = _
  }

  private class FloatUtil(private var result: StringBuilder,
                          private var formatToken: FormatToken,
                          private val decimalFormat: DecimalFormat,
                          private val argument: Object) {
    private var minusSign =
      decimalFormat.getDecimalFormatSymbols().getMinusSign()

    def transform(aFormatToken: FormatToken, aResult: StringBuilder): Unit = {
      this.result = aResult
      this.formatToken = aFormatToken
      formatToken.getConversionType() match {
        case 'e' | 'E' => transform_e()
        case 'f'       => transform_f()
        case 'g' | 'G' => transform_g()
        case 'a' | 'A' => transform_a()
        case _ =>
          throw new UnknownFormatConversionException(
            String.valueOf(formatToken.getConversionType()))
      }
    }

    def getMinusSign(): Char = minusSign

    def getAddSign(): Char = '+'

    def transform_e(): Unit = {
      val pattern = new StringBuilder()
      pattern.append('0')
      if (formatToken.getPrecision() > 0) {
        pattern.append('.')
        val zeros = Array.fill[Char](formatToken.getPrecision())('0')
        pattern.append(zeros)
      }
      pattern.append('E')
      // Porting note: Harmony appends "+00" here, but it is nonstandard; applyPattern throws IllegalArgumentException on OpenJDK 8
      pattern.append("00")
      decimalFormat.applyPattern(pattern.toString())
      val formattedString = decimalFormat.format(argument)
      result.append(formattedString.replace("E", "e+").replace("e+-", "e-"))

      if (formatToken.isFlagSet(FormatToken.FLAG_SHARP) && 0 == formatToken
            .getPrecision()) {
        val indexOfE = result.indexOf("e")
        val dot      = decimalFormat.getDecimalFormatSymbols().getDecimalSeparator()
        result.insert(indexOfE, dot)
      }
    }

    def transform_g(): Unit = {
      var precision = formatToken.getPrecision
      precision = if (0 == precision) 1 else precision
      formatToken.setPrecision(precision)

      if (0.0 == argument.asInstanceOf[Number].doubleValue()) {
        precision -= 1
        formatToken.setPrecision(precision)
        transform_f()
        return
      }

      var requireScientificRepresentation = true
      var d                               = argument.asInstanceOf[Number].doubleValue()
      d = Math.abs(d)
      if (java.lang.Double.isInfinite(d)) {
        precision = formatToken.getPrecision()
        precision -= 1
        formatToken.setPrecision(precision)
        transform_e()
        return
      }
      val b = new BigDecimal(d, new MathContext(precision))
      d = b.doubleValue()
      var l = b.longValue()

      if (d >= 1 && d < Math.pow(10, precision)) {
        if (l < Math.pow(10, precision)) {
          requireScientificRepresentation = false
          precision -= String.valueOf(l).length()
          precision = if (precision < 0) 0 else precision
          l = Math.round(d * Math.pow(10, precision + 1))
          if (String.valueOf(l).length() <= formatToken.getPrecision())
            precision += 1
          formatToken.setPrecision(precision)
        }
      } else {
        l = b.movePointRight(4).longValue()
        if (d >= Math.pow(10, -4) && d < 1) {
          requireScientificRepresentation = false
          precision += 4 - String.valueOf(l).length()
          l = b.movePointRight(precision + 1).longValue()
          if (String.valueOf(l).length() <= formatToken.getPrecision())
            precision += 1
          l = b.movePointRight(precision).longValue()
          if (l >= Math.pow(10, precision - 4))
            formatToken.setPrecision(precision)
        }
      }
      if (requireScientificRepresentation) {
        precision = formatToken.getPrecision()
        precision -= 1
        formatToken.setPrecision(precision)
        transform_e()
      } else {
        transform_f()
      }
    }

    def transform_f(): Unit = {
      val pattern = new StringBuilder()
      if (formatToken.isFlagSet(FormatToken.FLAG_COMMA)) {
        pattern.append(',')
        val groupingSize = decimalFormat.getGroupingSize()
        if (groupingSize > 1) {
          val sharps = Array.fill[Char](groupingSize - 1)('#')
          pattern.append(sharps)
        }
      }

      pattern.append(0)

      if (formatToken.getPrecision() > 0) {
        pattern.append('.')
        val zeros = Array.fill[Char](formatToken.getPrecision())('0')
        pattern.append(zeros)
      }
      decimalFormat.applyPattern(pattern.toString())
      result.append(decimalFormat.format(argument))
      if (formatToken.isFlagSet(FormatToken.FLAG_SHARP) && 0 == formatToken
            .getPrecision()) {
        val dot = decimalFormat.getDecimalFormatSymbols().getDecimalSeparator()
        result.append(dot)
      }
    }

    def transform_a(): Unit = {
      val currentConversionType = formatToken.getConversionType()

      argument.asInstanceOf[Any] match {
        case f: Float =>
          result.append(java.lang.Float.toHexString(f.floatValue()))
        case d: Double =>
          result.append(java.lang.Double.toHexString(d.doubleValue()))
        case _ =>
          // BigInteger is not supported.
          throw new IllegalFormatConversionException(currentConversionType,
                                                     argument.getClass())
      }

      if (!formatToken.isPrecisionSet())
        return

      var precision = formatToken.getPrecision()
      precision = if (0 == precision) 1 else precision
      val indexOfFirstFracitoanlDigit = result.indexOf(".") + 1
      val indexOfP                    = result.indexOf("p")
      val fractionalLength            = indexOfP - indexOfFirstFracitoanlDigit

      if (fractionalLength == precision)
        return

      if (fractionalLength < precision) {
        val zeros = Array.fill[Char](precision - fractionalLength)('0')
        result.insert(indexOfP, zeros)
        return
      }
      result.delete(indexOfFirstFracitoanlDigit + precision, indexOfP)
    }
  }

  private class DateTimeUtil(locale: Locale) {
    import DateTimeUtil._

    private var calendar: Calendar = _

    private var result: StringBuilder = _

    private var dateFormatSymbols: DateFormatSymbols = _

    def transform(formatToken: FormatToken,
                  aCalendar: Calendar,
                  aResult: StringBuilder): Unit = {
      this.result = aResult
      this.calendar = aCalendar
      val suffix = formatToken.getDateSuffix()

      suffix match {
        case 'H'       => transform_H()
        case 'I'       => transform_I()
        case 'M'       => transform_M()
        case 'S'       => transform_S()
        case 'L'       => transform_L()
        case 'N'       => transform_N()
        case 'k'       => transform_k()
        case 'l'       => transform_l()
        case 'p'       => transform_p(true)
        case 's'       => transform_s()
        case 'z'       => transform_z()
        case 'Z'       => transform_Z()
        case 'Q'       => transform_Q()
        case 'B'       => transform_B()
        case 'b' | 'h' => transform_b()
        case 'A'       => transform_A()
        case 'a'       => transform_a()
        case 'C'       => transform_C()
        case 'Y'       => transform_Y()
        case 'y'       => transform_y()
        case 'j'       => transform_j()
        case 'm'       => transform_m()
        case 'd'       => transform_d()
        case 'e'       => transform_e()
        case 'R'       => transform_R()
        case 'T'       => transform_T()
        case 'r'       => transform_r()
        case 'D'       => transform_D()
        case 'F'       => transform_F()
        case 'c'       => transform_c()
        case _ =>
          throw new UnknownFormatConversionException(
            String.valueOf(formatToken.getConversionType()) + formatToken
              .getDateSuffix())
      }
    }

    private def transform_e(): Unit = {
      val day = calendar.get(Calendar.DAY_OF_MONTH)
      result.append(day)
    }

    private def transform_d(): Unit = {
      val day = calendar.get(Calendar.DAY_OF_MONTH)
      result.append(paddingZeros(day, 2))
    }

    private def transform_m(): Unit = {
      val month = calendar.get(Calendar.MONTH) + 1
      result.append(paddingZeros(month, 2))
    }

    private def transform_j(): Unit = {
      val day = calendar.get(Calendar.DAY_OF_YEAR)
      result.append(paddingZeros(day, 3))
    }

    private def transform_y(): Unit = {
      val year = calendar.get(Calendar.YEAR) % 100
      result.append(paddingZeros(year, 2))
    }

    private def transform_Y(): Unit = {
      val year = calendar.get(Calendar.YEAR)
      result.append(paddingZeros(year, 4))
    }

    private def transform_C(): Unit = {
      val year = calendar.get(Calendar.YEAR) / 100
      result.append(paddingZeros(year, 2))
    }

    private def transform_a(): Unit = {
      val day = calendar.get(Calendar.DAY_OF_WEEK)
      result.append(getDateFormatSymbols().getShortWeekdays()(day))
    }

    private def transform_A(): Unit = {
      val day = calendar.get(Calendar.DAY_OF_WEEK)
      result.append(getDateFormatSymbols().getWeekdays()(day))
    }

    private def transform_b(): Unit = {
      val month = calendar.get(Calendar.MONTH)
      result.append(getDateFormatSymbols().getShortMonths()(month))
    }

    private def transform_B(): Unit = {
      val month = calendar.get(Calendar.MONTH)
      result.append(getDateFormatSymbols().getMonths()(month))
    }

    private def transform_Q(): Unit = {
      val milliSeconds = calendar.getTimeInMillis()
      result.append(milliSeconds)
    }

    private def transform_s(): Unit = {
      val milliSeconds = calendar.getTimeInMillis() / 1000
      result.append(milliSeconds)
    }

    private def transform_Z(): Unit = {
      val timeZone = calendar.getTimeZone()
      result.append(
        timeZone.getDisplayName(timeZone.inDaylightTime(calendar.getTime()),
                                TimeZone.SHORT,
                                locale))
    }

    private def transform_z(): Unit = {
      val zoneOffset = calendar.get(Calendar.ZONE_OFFSET) / 3600000 * 100
      if (zoneOffset >= 0)
        result.append('+')
      result.append(paddingZeros(zoneOffset, 4))
    }

    private def transform_p(isLowerCase: Boolean): Unit = {
      val i = calendar.get(Calendar.AM_PM)
      var s = getDateFormatSymbols().getAmPmStrings()(i)
      if (isLowerCase)
        s = s.toLowerCase(locale)
      result.append(s)
    }

    private def transform_N(): Unit = {
      // TODO: System.nanoTime()
      val nanosecond = calendar.get(Calendar.MILLISECOND) * 1000000L
      result.append(paddingZeros(nanosecond, 9))
    }

    private def transform_L(): Unit = {
      val millisecond = calendar.get(Calendar.MILLISECOND)
      result.append(paddingZeros(millisecond, 3))
    }

    private def transform_S(): Unit = {
      val second = calendar.get(Calendar.SECOND)
      result.append(paddingZeros(second, 2))
    }

    private def transform_M(): Unit = {
      val minute = calendar.get(Calendar.MINUTE)
      result.append(paddingZeros(minute, 2))
    }

    private def transform_l(): Unit = {
      var hour = calendar.get(Calendar.HOUR)
      if (0 == hour)
        hour = 12
      result.append(hour)
    }

    private def transform_k(): Unit = {
      val hour = calendar.get(Calendar.HOUR_OF_DAY)
      result.append(hour)
    }

    private def transform_I(): Unit = {
      var hour = calendar.get(Calendar.HOUR)
      if (0 == hour)
        hour = 12
      result.append(paddingZeros(hour, 2))
    }

    private def transform_H(): Unit = {
      val hour = calendar.get(Calendar.HOUR_OF_DAY)
      result.append(paddingZeros(hour, 2))
    }

    private def transform_R(): Unit = {
      transform_H()
      result.append(':')
      transform_M()
    }

    private def transform_T(): Unit = {
      transform_H()
      result.append(':')
      transform_M()
      result.append(':')
      transform_S()
    }

    private def transform_r(): Unit = {
      transform_I()
      result.append(':')
      transform_M()
      result.append(':')
      transform_S()
      result.append(' ')
      transform_p(false)
    }

    private def transform_D(): Unit = {
      transform_m()
      result.append('/')
      transform_d()
      result.append('/')
      transform_y()
    }

    private def transform_F(): Unit = {
      transform_Y()
      result.append('-')
      transform_m()
      result.append('-')
      transform_d()
    }

    private def transform_c(): Unit = {
      transform_a()
      result.append(' ')
      transform_b()
      result.append(' ')
      transform_d()
      result.append(' ')
      transform_T()
      result.append(' ')
      transform_Z()
      result.append(' ')
      transform_Y()
    }

    private def getDateFormatSymbols(): DateFormatSymbols = {
      if (null == dateFormatSymbols) {
        dateFormatSymbols = new DateFormatSymbols(locale)
      }
      dateFormatSymbols
    }
  }

  private object DateTimeUtil {
    private def paddingZeros(number: Long, length: Int): String = {
      var len    = length
      val result = new StringBuilder()
      result.append(number)
      var startIndex = 0
      if (number < 0) {
        len += 1
        startIndex = 1
      }
      len -= result.length()
      if (len > 0) {
        val zeros = Array.fill[Char](len)('0')
        result.insert(startIndex, zeros)
      }
      result.toString()
    }
  }

  private class ParserStateMachine(format: CharBuffer) {
    import ParserStateMachine._

    private var token: FormatToken = _

    private var state: Int = ENTRY_STATE

    private var currentChar: Char = 0

    def reset(): Unit = {
      this.currentChar = FormatToken.UNSET.asInstanceOf[Char]
      this.state = ENTRY_STATE
      this.token = null
    }

    def getNextFormatToken(): FormatToken = {
      token = new FormatToken()
      token.setFormatStringStartIndex(format.position())

      // FINITE AUTOMATIC MACHINE
      val b = new Breaks
      import b.{breakable, break}
      breakable {
        while (true) {

          if (ParserStateMachine.EXIT_STATE != state) {
            // exit state does not need to get next char
            currentChar = getNextFormatChar()
            if (EOS == currentChar && ParserStateMachine.ENTRY_STATE != state)
              throw new UnknownFormatConversionException(getFormatString())
          }

          state match {
            // exit state
            case ParserStateMachine.EXIT_STATE =>
              process_EXIT_STATE()
              break()
            // plain text state, not yet applied converter
            case ParserStateMachine.ENTRY_STATE =>
              process_ENTRY_STATE()
            // begins converted string
            case ParserStateMachine.START_CONVERSION_STATE =>
              process_START_CONVERSION_STATE()
            case ParserStateMachine.FLAGS_STATE =>
              process_FLAGS_STATE()
            case ParserStateMachine.WIDTH_STATE =>
              process_WIDTH_STATE()
            case ParserStateMachine.PRECISION_STATE =>
              process_PRECISION_STATE()
            case ParserStateMachine.CONVERSION_TYPE_STATE =>
              process_CONVERSION_TYPE_STATE()
            case ParserStateMachine.SUFFIX_STATE =>
              process_SUFFIX_STATE()
          }
        }
      }

      token
    }

    private def getNextFormatChar(): Char = {
      if (format.hasRemaining())
        format.get()
      else
        EOS
    }

    private def getFormatString(): String = {
      val end = format.position()
      format.rewind()
      val formatString =
        format.subSequence(token.getFormatStringStartIndex(), end).toString()
      format.position(end)
      formatString
    }

    private def process_ENTRY_STATE(): Unit = {
      if (EOS == currentChar)
        state = ParserStateMachine.EXIT_STATE
      else if ('%' == currentChar) {
        // change to conversion type state
        state = START_CONVERSION_STATE
      }
      // else remains in ENTRY_STATE
    }

    private def process_START_CONVERSION_STATE(): Unit = {
      if (Character.isDigit(currentChar)) {
        val position       = format.position() - 1
        val number         = parseInt(format)
        var nextChar: Char = 0
        if (format.hasRemaining()) {
          nextChar = format.get()
        }
        if ('$' == nextChar) {
          // the digital sequence stands for the argument index.
          val argIndex = number
          // k$ stands for the argument whose index is k-1 except that
          // 0$ and 1$ both stands for the first element.
          if (argIndex > 0) {
            token.setArgIndex(argIndex - 1)
          } else if (argIndex == FormatToken.UNSET) {
            throw new MissingFormatArgumentException(getFormatString())
          }
          state = FLAGS_STATE
        } else {
          // the digital zero stands for one format flag.
          if ('0' == currentChar) {
            state = FLAGS_STATE
            format.position(position)
          } else {
            // the digital sequence stands for the width.
            state = WIDTH_STATE
            // do not get the next char.
            format.position(format.position() - 1)
            token.setWidth(number)
          }
        }
        currentChar = nextChar
      } else if ('<' == currentChar) {
        state = FLAGS_STATE
        token.setArgIndex(FormatToken.LAST_ARGUMENT_INDEX)
      } else {
        state = FLAGS_STATE
        // do not get the next char.
        format.position(format.position() - 1)
      }
    }

    private def process_FLAGS_STATE(): Unit = {
      if (token.setFlag(currentChar)) {
        // remains in FLAGS_STATE
      } else if (Character.isDigit(currentChar)) {
        token.setWidth(parseInt(format))
        state = WIDTH_STATE
      } else if ('.' == currentChar) {
        state = PRECISION_STATE
      } else {
        state = CONVERSION_TYPE_STATE
        // do not get the next char.
        format.position(format.position() - 1)
      }
    }

    private def process_WIDTH_STATE(): Unit = {
      if ('.' == currentChar) {
        state = PRECISION_STATE
      } else {
        state = CONVERSION_TYPE_STATE
        // do not get the next char.
        format.position(format.position() - 1)
      }
    }

    private def process_PRECISION_STATE(): Unit = {
      if (Character.isDigit(currentChar)) {
        token.setPrecision(parseInt(format))
      } else {
        // the precision is required but not given by the format string.
        throw new UnknownFormatConversionException(getFormatString())
      }
      state = CONVERSION_TYPE_STATE
    }

    private def process_CONVERSION_TYPE_STATE(): Unit = {
      token.setConversionType(currentChar)
      if ('t' == currentChar || 'T' == currentChar) {
        state = SUFFIX_STATE
      } else {
        state = EXIT_STATE
      }
    }

    private def process_SUFFIX_STATE(): Unit = {
      token.setDateSuffix(currentChar)
      state = EXIT_STATE
    }

    private def process_EXIT_STATE(): Unit =
      token.setPlainText(getFormatString())

    private def parseInt(buffer: CharBuffer): Int = {
      val start = buffer.position() - 1
      var end   = buffer.limit()
      val b     = new Breaks
      import b.{breakable, break}
      breakable {
        while (buffer.hasRemaining()) {
          if (!Character.isDigit(buffer.get())) {
            end = buffer.position() - 1
            break()
          }
        }
      }
      buffer.position(0)
      val intStr = buffer.subSequence(start, end).toString()
      buffer.position(end)
      try {
        Integer.parseInt(intStr)
      } catch {
        case _: NumberFormatException =>
          FormatToken.UNSET
      }
    }
  }

  private object ParserStateMachine {
    private val EOS: Char = -1.asInstanceOf[Char]

    private val EXIT_STATE: Int = 0

    private val ENTRY_STATE: Int = 1

    private val START_CONVERSION_STATE: Int = 2

    private val FLAGS_STATE: Int = 3

    private val WIDTH_STATE: Int = 4

    private val PRECISION_STATE: Int = 5

    private val CONVERSION_TYPE_STATE: Int = 6

    private val SUFFIX_STATE: Int = 7
  }
}
