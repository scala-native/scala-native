package java.util

// Ported from Scala.js, commit: 00915e8, dated: 2020-09-29

import java.io._
import java.lang.{
  Double => JDouble,
  Float => JFloat,
  Boolean => JBoolean,
  StringBuilder => JStringBuilder
}
import java.math.{BigDecimal, BigInteger, MathContext, RoundingMode}
import java.nio.CharBuffer
import java.nio.charset.Charset
import scala.annotation.{switch, tailrec}

final class Formatter private (private var dest: Appendable,
                               formatterLocaleInfo: Formatter.LocaleInfo)
    extends Closeable
    with Flushable {

  import Formatter._
  import Defaults._
  import Flags._

  if (dest == null) {
    dest = new JStringBuilder()
  }

  private[this] var closed: Boolean              = false
  private[this] var lastIOException: IOException = null

  def this() =
    this(new JStringBuilder(), Formatter.RootLocaleInfo)
  def this(a: Appendable) =
    this(a, Formatter.RootLocaleInfo)
  def this(l: Locale) =
    this(new JStringBuilder(), new Formatter.LocaleLocaleInfo(l))

  def this(a: Appendable, l: Locale) =
    this(a, new Formatter.LocaleLocaleInfo(l))

  private def this(os: OutputStream,
                   csn: String,
                   localeInfo: Formatter.LocaleInfo) =
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
    this({
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
    }, l)

  def this(file: File, csn: String, l: Locale) =
    this(file, csn, new Formatter.LocaleLocaleInfo(l))
  def this(file: File, csn: String) =
    this(file, csn, Formatter.RootLocaleInfo)

  def this(file: File) =
    this(new FileOutputStream(file))
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
  def this(fileName: String, csn: String) =
    this(new File(fileName), csn)
  def this(fileName: String) =
    this(new File(fileName))

  @inline
  private def trapIOExceptions(body: => Unit): Unit = {
    try {
      body
    } catch {
      case th: IOException =>
        lastIOException = th
    }
  }

  private def sendToDest(strings: String*): Unit = {
    trapIOExceptions {
      strings.foreach(dest.append(_))
    }
  }

  def close(): Unit = {
    if (!closed) {
      dest match {
        case cl: Closeable =>
          trapIOExceptions {
            cl.close()
          }
        case _ =>
      }
    }
    closed = true
  }

  def flush(): Unit = {
    checkNotClosed()
    dest match {
      case fl: Flushable =>
        trapIOExceptions {
          fl.flush()
        }
      case _ =>
    }
  }

  def format(format: String, args: Array[AnyRef]): Formatter =
    this.format(formatterLocaleInfo, format, args)

  def format(l: Locale, format: String, args: Array[AnyRef]): Formatter =
    this.format(new LocaleLocaleInfo(l), format, args)

  private def format(localeInfo: LocaleInfo,
                     format: String,
                     args: Array[AnyRef]): Formatter = {
    // scalastyle:off return
    checkNotClosed()

    val formatBuffer = CharBuffer.wrap(format)
    val parser       = new ParserStateMachine(formatBuffer)

    var lastImplicitArgIndex: Int = 0
    var lastArgIndex: Int         = 0 // required for < flag
    while (formatBuffer.hasRemaining) {
      val token      = parser.nextFormatToken()
      val flags      = token.getFlags()
      val conversion = token.conversion
      val plainText  = token.plainText

      // Process a portion without '%'
      if (conversion == FormatToken.Unset.toChar) {
        sendToDest(plainText)
        return this
      }

      sendToDest(plainText.substring(0, token.formatSpecifierIndex - 1))
      // Process one '%'
      val arg = if (!token.requireArgument()) {
        /* No argument. Make sure not to bump `lastImplicitArgIndex` nor to
         * affect `lastArgIndex`.
         */
        null
      } else {

        val argIndex = if (flags.useLastIndex) {
          // Explicitly use the last index
          lastArgIndex
        } else {
          val i = token.argIndex
          if (i == FormatToken.Unset || i == 0) {
            // Either there is no explicit index, or the explicit index is 0
            lastImplicitArgIndex += 1
            lastImplicitArgIndex
          } else if (i < 0) {
            // Cannot be parsed, same as useLastIndex
            lastArgIndex
          } else {
            // Could be parsed, this is the index
            i
          }
        }

        if (argIndex <= 0 || argIndex > args.length) {
          throw new MissingFormatArgumentException(plainText)
        }

        if (token.width < 0 &&
            (isNumericConversion(conversion) && flags.zeroPad ||
            flags.leftAlign)) {
          // Dropping head for JVM compliance
          throw new MissingFormatWidthException(
            plainText.substring(token.formatSpecifierIndex - 1))
        }

        lastArgIndex = argIndex
        args(argIndex - 1)
      }

      formatArg(localeInfo,
                arg,
                conversion,
                flags,
                token.width,
                token.precision)
    }
    this
    // scalastyle:on return
  }

  private def isNumericConversion(conversion: Char) =
    !"bBhHsScC%n".contains(conversion)

  private def formatArg(localeInfo: LocaleInfo,
                        arg: Any,
                        conversion: Char,
                        flags: Flags,
                        width: Int,
                        precision: Int): Unit = {
    @inline def rejectPrecision(): Unit = {
      if (precision >= 0)
        throw new IllegalFormatPrecisionException(precision)
    }

    def formatNullOrThrowIllegalFormatConversion(): Unit = {
      if (arg == null)
        formatNonNumericString(localeInfo, flags, width, precision, "null")
      else {
        throw new IllegalFormatConversionException(conversion, arg.getClass)
      }
    }

    @inline def precisionWithDefault =
      if (precision >= 0) precision
      else 6

    @inline def efgCommon(notation: (Number, Int, Boolean) => String): Unit = {
      def formatArg(arg: Number) = {
        /* The alternative format # of 'e', 'f' and 'g' is to force a
         * decimal separator.
         */
        val forceDecimalSep = flags.altFormat
        formatNumericString(
          localeInfo,
          flags,
          width,
          notation(arg, precisionWithDefault, forceDecimalSep))
      }

      arg match {
        case arg: Float =>
          if (JFloat.isNaN(arg) || JFloat.isInfinite(arg)) {
            formatNaNOrInfinite(flags, width, arg)
          } else formatArg(arg)

        case arg: Double =>
          if (JDouble.isNaN(arg) || JDouble.isInfinite(arg)) {
            formatNaNOrInfinite(flags, width, arg)
          } else formatArg(arg)

        case arg: BigDecimal =>
          formatArg(arg)

        case _ =>
          formatNullOrThrowIllegalFormatConversion()
      }
    }

    // On JVM list of invalid flags for 'o' and 'x' conversions is different for BigInteger and primitive types
    // In case of null we won't be able correctly distinguish underlying type, so we're using wider set of allowed flags.
    def invalidFlagsForOctalAndHex(arg: Any): Int =
      if (arg == null || arg.isInstanceOf[BigInteger]) UseGroupingSeps
      else InvalidFlagsForOctalAndHex

    (conversion: @switch) match {
      case 'b' | 'B' =>
        validateFlags(flags,
                      conversion,
                      invalidFlags = NumericOnlyFlags | AltFormat)
        val str = arg match {
          case arg: JBoolean => arg.toString
          case null          => "false"
          case _             => "true"
        }
        formatNonNumericString(RootLocaleInfo, flags, width, precision, str)

      case 'h' | 'H' =>
        validateFlags(flags,
                      conversion,
                      invalidFlags = NumericOnlyFlags | AltFormat)
        val str =
          if (arg == null) "null"
          else Integer.toHexString(arg.hashCode)
        formatNonNumericString(RootLocaleInfo, flags, width, precision, str)

      case 's' | 'S' =>
        arg match {
          case formattable: Formattable =>
            validateFlags(flags, conversion, invalidFlags = NumericOnlyFlags)
            val formattableFlags = {
              (if (flags.leftAlign) FormattableFlags.LEFT_JUSTIFY else 0) |
                (if (flags.altFormat) FormattableFlags.ALTERNATE else 0) |
                (if (flags.upperCase) FormattableFlags.UPPERCASE else 0)
            }
            formattable.formatTo(this, formattableFlags, width, precision)

          case _ =>
            validateFlags(flags,
                          conversion,
                          invalidFlags = NumericOnlyFlags | AltFormat)
            val str = String.valueOf(arg)
            formatNonNumericString(localeInfo, flags, width, precision, str)
        }

      case 'c' | 'C' =>
        validateFlags(flags,
                      conversion,
                      invalidFlags = NumericOnlyFlags | AltFormat)
        rejectPrecision()

        def checkValidCodePoint(arg: Int): Unit = {
          if (!Character.isValidCodePoint(arg))
            throw new IllegalFormatCodePointException(arg)
        }

        def formatCharString(charString: String) =
          formatNonNumericString(localeInfo, flags, width, -1, charString)

        arg match {
          case arg: Byte =>
            checkValidCodePoint(arg)
            formatCharString(arg.toChar.toString)

          case arg: Char =>
            checkValidCodePoint(arg)
            formatCharString(arg.toString)

          case arg: Short =>
            checkValidCodePoint(arg)
            formatCharString(arg.toChar.toString)

          case arg: Int =>
            checkValidCodePoint(arg)
            val str = if (arg < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
              new String(Array(arg.toChar))
            } else {
              new String(
                Array(
                  (0xd800 | ((arg >> 10) - (0x10000 >> 10))).toChar,
                  (0xdc00 | (arg & 0x3ff)).toChar
                ))
            }
            formatCharString(str)

          case _ => formatNullOrThrowIllegalFormatConversion()
        }

      case 'd' =>
        validateFlags(flags, conversion, invalidFlags = AltFormat)
        rejectPrecision()
        arg match {
          case _: Byte | _: Short | _: Char | _: Int | _: Long |
              _: BigInteger =>
            formatNumericString(localeInfo, flags, width, arg.toString())
          case _ =>
            formatNullOrThrowIllegalFormatConversion()
        }

      case 'o' =>
        // Octal formatting is not localized
        val prefix =
          if (flags.altFormat) zeroDigitString
          else ""

        def padAndSendWithOctalInt(arg: Int): Unit = padAndSendToDest(
          RootLocaleInfo,
          flags,
          width,
          prefix,
          java.lang.Integer.toOctalString(arg)
        )

        arg match {
          case arg: Byte  => padAndSendWithOctalInt(arg & 0xFF)
          case arg: Short => padAndSendWithOctalInt(arg & 0xFFFF)
          case arg: Int   => padAndSendWithOctalInt(arg)
          case arg: Long =>
            padAndSendToDest(RootLocaleInfo,
                             flags,
                             width,
                             prefix,
                             java.lang.Long.toOctalString(arg))
          case arg: BigInteger =>
            formatNumericString(RootLocaleInfo,
                                flags,
                                width,
                                arg.toString(8),
                                prefix)
          case _ =>
            rejectPrecision() // used here to respect order of throwing exceptions in the JVM
            formatNullOrThrowIllegalFormatConversion()
        }

        validateFlags(flags, conversion, invalidFlagsForOctalAndHex(arg))
        rejectPrecision()

      case 'x' | 'X' =>
        // Hex formatting is not localized
        rejectPrecision()

        val prefix = {
          if (!flags.altFormat) ""
          else if (flags.upperCase) "0X"
          else "0x"
        }

        def padAndSendWithHexInt(arg: Int): Unit = padAndSendToDest(
          RootLocaleInfo,
          flags,
          width,
          prefix,
          applyNumberUpperCase(flags, java.lang.Integer.toHexString(arg))
        )

        arg match {
          case arg: Byte  => padAndSendWithHexInt(arg & 0xFF)
          case arg: Short => padAndSendWithHexInt(arg & 0xFFFF)
          case arg: Int   => padAndSendWithHexInt(arg)
          case arg: Long =>
            padAndSendToDest(
              RootLocaleInfo,
              flags,
              width,
              prefix,
              applyNumberUpperCase(flags, java.lang.Long.toHexString(arg)))
          case arg: BigInteger =>
            formatNumericString(RootLocaleInfo,
                                flags,
                                width,
                                arg.toString(16),
                                prefix)
          case _ =>
            formatNullOrThrowIllegalFormatConversion()
        }

        validateFlags(flags, conversion, invalidFlagsForOctalAndHex(arg))

      case 'a' | 'A' =>
        validateFlags(flags,
                      conversion,
                      invalidFlags = NegativeParen | UseGroupingSeps)

        def formatHex(hex: String) = {
          val formatedHex =
            if (precision < 0) hex
            else {
              raw"\.(.*)p".r.replaceSomeIn(
                hex,
                res => {
                  val prev            = res.group(1)
                  val actualPrecision = precision.max(1)
                  val diff            = actualPrecision - prev.length
                  if (diff == 0) None
                  else {
                    val replacement =
                      if (diff > 0) prev + (zeroDigitString * diff)
                      else prev.take(actualPrecision)

                    Some(raw"\.${replacement}p")
                  }
                }
              )
            }

          formatNumericString(RootLocaleInfo,
                              flags,
                              width,
                              applyNumberUpperCase(flags, formatedHex))
        }

        arg match {
          case f: Float =>
            if (JFloat.isNaN(f) || JFloat.isInfinite(f)) {
              formatNaNOrInfinite(flags, width, f)
            } else formatHex(JFloat.toHexString(f))
          case d: Double =>
            if (JDouble.isNaN(d) || JDouble.isInfinite(d)) {
              formatNaNOrInfinite(flags, width, d)
            } else formatHex(JDouble.toHexString(d))
          case _ =>
            formatNullOrThrowIllegalFormatConversion()
        }

      case 'e' | 'E' =>
        validateFlags(flags, conversion, invalidFlags = UseGroupingSeps)
        efgCommon(computerizedScientificNotation _)

      case 'g' | 'G' =>
        validateFlags(flags, conversion, invalidFlags = AltFormat)
        efgCommon(generalScientificNotation _)

      case 'f' =>
        validateFlags(flags, conversion, invalidFlags = 0)
        efgCommon(decimalNotation _)

      case '%' =>
        validateFlagsForPercentAndNewline(flags,
                                          conversion,
                                          invalidFlags =
                                            AllWrittenFlags & ~LeftAlign)
        rejectPrecision()
        if (flags.leftAlign && width < 0)
          throw new MissingFormatWidthException("%-%")
        padAndSendToDestNoZeroPad(flags, width, "%")

      case 'n' =>
        rejectPrecision()
        if (width >= 0)
          throw new IllegalFormatWidthException(width)
        validateFlagsForPercentAndNewline(flags,
                                          conversion,
                                          invalidFlags = AllWrittenFlags)
        sendToDest(lineSeparatorString)

      // todo case 't' | 'T' => date/time
      case _ =>
        throw new UnknownFormatConversionException(conversion.toString)
    }
  }

  @inline
  private def validateFlags(flags: Flags,
                            conversion: Char,
                            invalidFlags: Int): Unit = {
    @noinline def flagsConversionMismatch(): Nothing = {
      throw new FormatFlagsConversionMismatchException(
        flagsToString(new Flags(flags.bits & invalidFlags)),
        conversion)
    }
    @noinline def illegalFlags(): Nothing =
      throw new IllegalFormatFlagsException(flagsToString(flags))

    val BadCombo1 = LeftAlign | ZeroPad
    val BadCombo2 = PositivePlus | PositiveSpace

    if (((flags.bits & BadCombo1) == BadCombo1) ||
        (flags.bits & BadCombo2) == BadCombo2) {
      illegalFlags()
    }

    if ((flags.bits & invalidFlags) != 0)
      flagsConversionMismatch()
  }

  @inline
  private def validateFlagsForPercentAndNewline(flags: Flags,
                                                conversion: Char,
                                                invalidFlags: Int): Unit = {
    @noinline def illegalFlags(): Nothing =
      throw new IllegalFormatFlagsException(flagsToString(flags))

    if ((flags.bits & invalidFlags) != 0)
      illegalFlags()
  }

  /* Should in theory be a method of `Flags`. See the comment on that class
   * about why we keep it here.
   */
  private def flagsToString(flags: Flags): String = {
    (if (flags.leftAlign) "-" else "") +
      (if (flags.altFormat) "#" else "") +
      (if (flags.positivePlus) "+" else "") +
      (if (flags.positiveSpace) " " else "") +
      (if (flags.zeroPad) "0" else "") +
      (if (flags.useGroupingSeps) "," else "") +
      (if (flags.negativeParen) "(" else "") +
      (if (flags.useLastIndex) "<" else "")
  }

  private def computerizedScientificNotation(
      num: Number,
      precision: Int,
      forceDecimalSep: Boolean): String = {

    val str = NumberFormatting
      .formatScientific(num, precision)
      .replace("E", "e+")
      .replace("e+-", "e-")

    // Finally, force the decimal separator, if requested
    if (!forceDecimalSep || str.indexOf(decimalSeparator) >= 0) {
      str
    } else {
      val pos = str.indexOf("e")
      str.substring(0, pos) + decimalSeparator + str.substring(pos)
    }
  }

  private def generalScientificNotation(num: Number,
                                        precision: Int,
                                        forceDecimalSep: Boolean): String = {
    val p =
      if (precision == 0) 1
      else precision

    /* Decisions about output format are based on BigDecimal rounding for compliance with JVM behaviour.
     * It handles overflow corner cases tested in `DefaultFormatterTest.formatForFloatDoubleConversionType_gG_Overflow`
     * eg. "%.0g", 0.000095 needs to be represented in decimal notation, but "%g", 0.00009 needs to use scientific notation
     */
    val bigDecimalAbs: BigDecimal = num match {
      case bd: BigDecimal => bd.abs(new MathContext(p))
      case _: JFloat | _: JDouble =>
        new BigDecimal(num.doubleValue().abs, new MathContext(p))
    }

    val shouldDisplayFixed: Boolean = {
      val abs = bigDecimalAbs.doubleValue()
      if (JDouble.isNaN(abs) || JDouble.isInfinite(abs)) false
      else abs >= 1e-4 && abs < Math.pow(10, p)
    }

    def calcSignificantDigits: Int = {
      /* First approximation of the smallest power of 10 that is >= m.
       * Due to rounding errors in the event of an imprecise `log10`
       * function, sig0 could actually be the smallest power of 10
       * that is > m.
       */
      val sig0 = bigDecimalAbs
        .round(new MathContext(1, RoundingMode.CEILING))
        .scale() * -1

      val isLessOrEqual = BigDecimal
        .valueOf(Math.pow(10, sig0))
        .compareTo(bigDecimalAbs) <= 0

      /* Increment sig0 so that it is always the first power of 10
       * that is > m.
       */
      if (isLessOrEqual) sig0 + 1
      else sig0
    }

    def isZero = bigDecimalAbs.doubleValue() == 0.0

    // between 1e-4 and 10e(p): display as fixed
    if (shouldDisplayFixed) {
      decimalNotation(num,
                      Math.max(p - calcSignificantDigits, 0),
                      forceDecimalSep)
    } else if (isZero) {
      // exact 0 should always be decimal
      decimalNotation(num, Math.max(precision - 1, 0), forceDecimalSep)
    } else {
      computerizedScientificNotation(num, p - 1, forceDecimalSep)
    }
  }

  private def decimalNotation(num: Number,
                              precision: Int,
                              forceDecimalSep: Boolean): String = {
    val str = NumberFormatting.formatDecimal(num, precision)
    // Finally, force the decimal separator, if requested
    if (forceDecimalSep && str.indexOf(decimalSeparator) < 0) {
      str + decimalSeparator
    } else str
  }

  private def formatNonNumericString(localeInfo: LocaleInfo,
                                     flags: Flags,
                                     width: Int,
                                     precision: Int,
                                     str: String): Unit = {
    val truncatedStr =
      if (precision < 0) str
      else str.substring(0, precision.min(str.length))
    padAndSendToDestNoZeroPad(flags,
                              width,
                              applyUpperCase(localeInfo, flags, truncatedStr))
  }

  private def formatNaNOrInfinite(flags: Flags, width: Int, x: Double): Unit = {
    // NaN and Infinite formatting are not localized

    val str = if (JDouble.isNaN(x)) {
      "NaN"
    } else if (x > 0.0) {
      if (flags.positivePlus) "+Infinity"
      else if (flags.positiveSpace) " Infinity"
      else "Infinity"
    } else {
      if (flags.negativeParen) "(Infinity)"
      else "-Infinity"
    }

    padAndSendToDestNoZeroPad(flags, width, applyNumberUpperCase(flags, str))
  }

  private def formatNumericString(localeInfo: LocaleInfo,
                                  flags: Flags,
                                  width: Int,
                                  str: String,
                                  basePrefix: String = ""): Unit = {
    /* Flags for which a numeric string needs to be decomposed and transformed,
     * not just padded and/or uppercased. We can write fast-paths in this
     * method if none of them are present.
     */
    val TransformativeFlags =
      PositivePlus | PositiveSpace | UseGroupingSeps | NegativeParen | AltFormat

    if (str.length >= width && !flags.hasAnyOf(TransformativeFlags)) {
      // Super-fast-path
      sendToDest(localeInfo.localizeNumber(applyNumberUpperCase(flags, str)))
    } else if (!flags.hasAnyOf(TransformativeFlags | ZeroPad)) {
      // Fast-path that does not need to inspect the string
      padAndSendToDestNoZeroPad(flags, width, applyNumberUpperCase(flags, str))
    } else {
      // Extract prefix and rest, based on flags and the presence of a sign
      val (numberPrefix, rest0) = if (str.charAt(0) != '-') {
        if (flags.positivePlus)
          ("+", str)
        else if (flags.positiveSpace)
          (" ", str)
        else
          ("", str)
      } else {
        if (flags.negativeParen)
          ("(", str.substring(1) + ")")
        else
          ("-", str.substring(1))
      }

      val prefix = numberPrefix + basePrefix

      // Insert grouping separators, if required
      val rest =
        if (flags.useGroupingSeps) insertGroupingCommas(localeInfo, rest0)
        else rest0

      // Apply uppercase, localization, pad and send
      padAndSendToDest(
        localeInfo,
        flags,
        width,
        prefix,
        localeInfo.localizeNumber(applyNumberUpperCase(flags, rest)))
    }
  }

  /** Inserts grouping commas at the right positions for the locale.
   *
   *  We already insert the ',' character, regardless of the locale. That is
   *  fixed later by `localeInfo.localizeNumber`. The only locale-sensitive
   *  behavior in this method is the grouping size.
   *
   *  The reason is that we do not want to insert a character that would
   *  collide with another meaning (such as '.') at this point.
   */
  private def insertGroupingCommas(localeInfo: LocaleInfo,
                                   s: String): String = {
    val groupingSize = localeInfo.groupingSize

    val len   = s.length
    var index = 0
    while (index != len && { val c = s.charAt(index); c >= '0' && c <= '9' }) {
      index += 1
    }

    index -= groupingSize

    if (index <= 0) {
      s
    } else {
      var result = s.substring(index)
      while (index > groupingSize) {
        val next = index - groupingSize
        result = s.substring(next, index) + "," + result
        index = next
      }
      s.substring(0, index) + "," + result
    }
  }

  private def applyNumberUpperCase(flags: Flags, str: String): String =
    if (flags.upperCase)
      str.toUpperCase() // uppercasing is not localized for numbers
    else str

  private def applyUpperCase(localeInfo: LocaleInfo,
                             flags: Flags,
                             str: String): String =
    if (flags.upperCase) localeInfo.toUpperCase(str)
    else str

  /** This method ignores `flags.zeroPad` and `flags.upperCase`. */
  private def padAndSendToDestNoZeroPad(flags: Flags,
                                        width: Int,
                                        str: String): Unit = {

    val len = str.length

    if (len >= width)
      sendToDest(str)
    else if (flags.leftAlign)
      sendToDest(str, strRepeat(" ", width - len))
    else
      sendToDest(strRepeat(" ", width - len), str)
  }

  /** This method ignores `flags.upperCase`. */
  private def padAndSendToDest(localeInfo: LocaleInfo,
                               flags: Flags,
                               width: Int,
                               prefix: String,
                               str: String): Unit = {

    val len = prefix.length + str.length
    if (len >= width)
      sendToDest(prefix, str)
    else if (flags.zeroPad)
      sendToDest(prefix,
                 strRepeat(localeInfo.zeroDigitString, width - len),
                 str)
    else if (flags.leftAlign)
      sendToDest(prefix, str, strRepeat(" ", width - len))
    else
      sendToDest(strRepeat(" ", width - len), prefix, str)
  }

  private def strRepeat(s: String, times: Int): String = {
    val result = new JStringBuilder()
    var i      = 0
    while (i != times) {
      result.append(s)
      i += 1
    }
    result.toString
  }

  def ioException(): IOException = lastIOException

  def locale(): Locale = {
    checkNotClosed()
    formatterLocaleInfo.locale
  }

  def out(): Appendable = {
    checkNotClosed()
    dest
  }

  override def toString(): String = {
    checkNotClosed()
    dest.toString()
  }

  @inline private def checkNotClosed(): Unit = {
    if (closed)
      throw new FormatterClosedException()
  }

}

object Formatter {

  object Defaults {
    final val minusSign           = '-'
    final val decimalSeparator    = '.'
    final val groupingSeparator   = ','
    final val zeroDigit           = '0'
    final val lineSeparator       = '\n'
    final val lineSeparatorString = lineSeparator.toString
    final val minusSignString     = minusSign.toString
    final val zeroDigitString     = zeroDigit.toString
    final val roundingMode        = RoundingMode.HALF_UP
  }

  final class BigDecimalLayoutForm private (name: String, ordinal: Int)
      extends Enum[BigDecimalLayoutForm](name, ordinal)

  object BigDecimalLayoutForm {

    final val SCIENTIFIC    = new BigDecimalLayoutForm("SCIENTIFIC", 0)
    final val DECIMAL_FLOAT = new BigDecimalLayoutForm("DECIMAL_FLOAT", 1)

    def valueOf(name: String): BigDecimalLayoutForm =
      _values.find(_.name() == name).getOrElse {
        throw new IllegalArgumentException(
          "No enum constant java.util.Formatter.BigDecimalLayoutForm." + name)
      }

    private val _values: Array[BigDecimalLayoutForm] =
      Array(SCIENTIFIC, DECIMAL_FLOAT)

    def values(): Array[BigDecimalLayoutForm] = _values.clone()
  }

  /* This class is never used in a place where it would box, so it will
   * completely disappear at link-time. Make sure to keep it that way.
   *
   * Also note that methods in this class are moved to the companion object, so
   * also take into account the comment on `object Flags`. In particular, do
   * not add non-inlineable methods in this class.
   */
  private final class Flags(val bits: Int) extends AnyVal {

    import Flags._

    @inline def leftAlign: Boolean = (bits & LeftAlign) != 0

    @inline def altFormat: Boolean = (bits & AltFormat) != 0

    @inline def positivePlus: Boolean = (bits & PositivePlus) != 0

    @inline def positiveSpace: Boolean = (bits & PositiveSpace) != 0

    @inline def zeroPad: Boolean = (bits & ZeroPad) != 0

    @inline def useGroupingSeps: Boolean = (bits & UseGroupingSeps) != 0

    @inline def negativeParen: Boolean = (bits & NegativeParen) != 0

    @inline def useLastIndex: Boolean = (bits & UseLastIndex) != 0

    @inline def upperCase: Boolean = (bits & UpperCase) != 0

    @inline def hasAnyOf(testBits: Int): Boolean = (bits & testBits) != 0
  }

  /* This object only contains `final val`s and (synthetic) `@inline`
   * methods. Therefore, it will completely disappear at link-time. Make sure
   * to keep it that way. In particular, do not add non-inlineable methods.
   */
  private object Flags {
    final val LeftAlign       = 0x001
    final val AltFormat       = 0x002
    final val PositivePlus    = 0x004
    final val PositiveSpace   = 0x008
    final val ZeroPad         = 0x010
    final val UseGroupingSeps = 0x020
    final val NegativeParen   = 0x040
    final val UseLastIndex    = 0x080
    final val UpperCase       = 0x100

    final val InvalidFlagsForOctalAndHex =
      PositivePlus | PositiveSpace | UseGroupingSeps | NegativeParen

    final val NumericOnlyFlags =
      PositivePlus | PositiveSpace | ZeroPad | UseGroupingSeps | NegativeParen

    final val AllWrittenFlags =
      LeftAlign | AltFormat | NumericOnlyFlags | UseLastIndex
  }

  private trait NumberFormatting[A] {

    import Defaults._

    // whole: "0" or [1-9]+[0-9]* (i.e. empty seqs and leading zeros are NOT allowed)
    // frac: [0-9]* (i.e. empty seqs and leading zeros are allowed)
    case class Digits(negative: Boolean, whole: Seq[Char], frac: Seq[Char])

    def toDigits(number: A): Digits

    def roundToInteger(digits: Digits): Digits

    def padLeft(len: Int, elem: Char, str: Seq[Char]): Seq[Char] = {
      val padnum = len - str.length
      if (padnum > 0)
        Seq.fill(padnum)(elem) ++ str
      else
        str
    }

    def roundAt(digits: Digits, afterDecimal: Int): Digits = {
      val shifted = {
        import digits._
        val (newWhole, newFrac) = frac.splitAt(afterDecimal)
        Digits(negative, whole ++ newWhole, newFrac)
      }
      val rounded = roundToInteger(shifted)
      // rounded.frac should be empty
      val (rwholeEmpty, rfracZeros) = {
        val paddedWhole =
          padLeft(afterDecimal, zeroDigit, rounded.whole)
        paddedWhole.splitAt(paddedWhole.length - afterDecimal)
      }
      val rwhole =
        if (rwholeEmpty.isEmpty) Seq(zeroDigit) else rwholeEmpty
      val rfrac =
        if (rfracZeros.forall(_ == zeroDigit)) Seq.empty
        else rfracZeros
      Digits(digits.negative, rwhole, rfrac)
    }

    def formatFixedPoint(number: A, fractionDigits: Int): String = {
      val digits = toDigits(number)

      val Digits(negative, wholePart, fracPart0) = {
        import digits._
        if (frac.length > fractionDigits)
          roundAt(digits, fractionDigits)
        else
          digits
      }

      val fracPart = fracPart0.padTo(fractionDigits, zeroDigit)
      val dotPart =
        if (fracPart.isEmpty && fractionDigits <= 0) Seq.empty
        else Seq(decimalSeparator)
      val signPart =
        if (negative) Seq(minusSign)
        else Seq.empty

      (signPart ++ wholePart ++ dotPart ++ fracPart).mkString
    }

    def scaleDigits(unscaled: Digits): (Digits, Int) = {
      val Digits(negative, wholeDigits, fracDigits) = unscaled
      val allDigits                                 = (wholeDigits ++ fracDigits).dropWhile(_ == '0')
      val wholeDigitNum                             = 1
      val (wholePart, fracPartNotCutoff)            = allDigits.splitAt(wholeDigitNum)
      val exp                                       = fracPartNotCutoff.length - fracDigits.length
      (Digits(negative, wholePart, fracPartNotCutoff), exp)
    }

    def formatScientific(number: A, fractionDigits: Int): String = {
      val (roundedDigits, exp) = {
        val unscaledDigits                = toDigits(number)
        val (scaledDigits, exp)           = scaleDigits(unscaledDigits)
        val Digits(negative, whole, frac) = scaledDigits

        if (frac.length > fractionDigits) {
          val rounded = roundAt(scaledDigits, fractionDigits)
          // check if carried
          if (scaledDigits.whole.length != rounded.whole.length) {
            val (newWhole, newPartZeros) =
              rounded.whole.splitAt(scaledDigits.whole.length)
            val newPart =
              if (newPartZeros.forall(_ == zeroDigit)) Seq.empty
              else newPartZeros
            (Digits(rounded.negative, newWhole, newPart ++ rounded.frac),
             exp + newPartZeros.length)
          } else
            (rounded, exp)
        } else
          (scaledDigits, exp)
      }
      val Digits(negative, wholePart, fracPart) = roundedDigits

      val dotPart =
        if (fracPart.isEmpty && fractionDigits <= 0) Seq.empty
        else Seq(decimalSeparator)
      val signPart =
        if (negative) Seq(minusSign)
        else Seq.empty

      def padInt(len: Int, elem: Char, num: Int): Seq[Char] =
        if (num < 0)
          minusSign +: padLeft(len, elem, (-num).toString.toSeq)
        else
          padLeft(len, elem, num.toString.toSeq)

      (
        signPart ++
          wholePart.padTo(1, zeroDigit) ++
          dotPart ++
          fracPart.padTo(fractionDigits, zeroDigit) ++
          ('E' +: padInt(2, zeroDigit, exp))
      ).mkString
    }
  }

  object NumberFormatting {

    import Defaults._

    implicit private object LongFormatting extends NumberFormatting[Long] {
      def toDigits(number: Long): Digits = {
        val numabs = number.abs

        def toBeTruncated =
          scala.Iterator(numabs) ++
            scala.Iterator.iterate(numabs / 10)(_ / 10).takeWhile(_ > 0)

        def whole =
          toBeTruncated
            .map { i => (zeroDigit + (i % 10)).toChar }
            .toList
            .reverse

        Digits(number < 0, whole, Seq.empty)
      }

      def roundToInteger(digits: Digits): Digits = {
        import digits._
        toDigits {
          val signum = if (negative) -1 else 1
          // assuming Long can be represented by Double
          val toBeRounded =
            (whole.mkString.toDouble + ("0." + frac.mkString).toDouble) * signum
          Math.round(toBeRounded)
        }
      }
    }

    implicit private object DoubleFormatting extends NumberFormatting[Double] {

      import Defaults._

      case class DoubleDigits(whole: Seq[Char], frac: Seq[Char])

      private[this] def getDoubleDigits(number: Double): DoubleDigits = {
        // regular expressions (regex) are a good Computer Science candidate
        // here but are not used.
        // This is essential low level code. I doubt the current
        // implementation of regex is either robust or perfomant enough.
        // I do not have time to benchmark alternate implementations, so
        // I go for an implementation I believe I can both do quickly
        // and get correct.

        val (wdPrefix, suffix) = number.toString.span(_ != '.')

        val (fracDigits, expDigits) = suffix.tail.span(_ != 'E')

        if (expDigits.isEmpty()) {
          DoubleDigits(wdPrefix.toSeq, fracDigits.toSeq)
        } else {
          val exponentB10 = java.lang.Integer.parseInt(expDigits.tail)

          if (exponentB10 > 0) {
            val (wdSuffix, fd) = fracDigits.splitAt(Math.min(exponentB10, 16))
            val wd =
              (wdPrefix + wdSuffix).padTo(exponentB10 + 1, zeroDigit)

            DoubleDigits(wd.toSeq, fd.toSeq)

          } else {
            val fdSeq = padLeft(exponentB10.abs + fracDigits.length,
                                zeroDigit,
                                (wdPrefix + fracDigits).toSeq)

            DoubleDigits(Seq(zeroDigit), fdSeq)
          }
        }
      }

      def toDigits(number: Double): Digits = {
        val numabs                    = number.abs
        val DoubleDigits(whole, frac) = getDoubleDigits(numabs)
        val neg                       = number < 0 || number == -0.0
        Digits(neg, whole, frac)
      }

      def roundToInteger(digits: Digits): Digits = {
        import digits._
        toDigits {
          val signum = if (negative) -1 else 1
          val fracd =
            try {
              ("0." + frac.mkString).toDouble
            } catch {
              case e: NumberFormatException =>
                // typically frac contains too many zeros
                // consider other cases?
                0.0
            }
          val toBeRounded = (whole.mkString.toDouble + fracd) * signum
          // Conversion to BigDecimal was used for JVM half-up rounding compliance
          BigDecimal
            .valueOf(toBeRounded)
            .setScale(0, roundingMode)
            .doubleValue()
        }
      }

    }

    implicit private object BigIntegerFormatting
        extends NumberFormatting[BigInteger] {
      def toDigits(number: BigInteger): Digits = {
        val numabs = number.abs()
        Digits(number.signum() < 0, numabs.toString.toSeq, Seq.empty)
      }

      def roundToInteger(digits: Digits): Digits = {
        import digits._
        toDigits {
          val sign = if (negative) minusSignString else ""
          val toBeRounded =
            new BigDecimal(
              sign + whole.mkString + decimalSeparator + frac.mkString)
          toBeRounded
            .setScale(0, roundingMode)
            .toBigInteger()
        }
      }
    }

    implicit private object BigDecimalFormatting
        extends NumberFormatting[BigDecimal] {
      def toDigits(number: BigDecimal): Digits = {
        val numabs = number.abs()
        val s      = numabs.toPlainString()
        val (whole, frac) = s.indexOf(decimalSeparator) match {
          case -1 => (s, "")
          case dp =>
            val (whole, dotFrac) = s.splitAt(dp)
            (whole, dotFrac.tail)
        }
        Digits(number.signum() < 0, whole.toSeq, frac.toSeq)
      }

      def roundToInteger(digits: Digits): Digits = {
        import digits._
        toDigits {
          val sign = if (negative) minusSignString else ""
          val toBeRounded =
            new BigDecimal(
              sign + whole.mkString + decimalSeparator + frac.mkString)
          toBeRounded.setScale(0, roundingMode)
        }
      }
    }

    def formatDecimal(arg: Number, precision: Int): String =
      format(f => f.formatFixedPoint(_, precision))(arg)

    def formatScientific(arg: Number, precision: Int): String =
      format(f => f.formatScientific(_, precision))(arg)

    private def format(
        formatWithNotation: NumberFormatting[Any] => Any => String)(
        arg: Number): String = {

      def formatImpl[A](arg: A)(implicit fmt: NumberFormatting[A]): String = {
        formatWithNotation(fmt.asInstanceOf[NumberFormatting[Any]])(arg)
      }

      arg match {
        case bi: BigInteger => formatImpl(bi)
        case bd: BigDecimal => formatImpl(bd)
        case num: Number =>
          val l = num.longValue()
          val d = num.doubleValue()
          // type ascriptions are put to make sure the correct overload is called
          if (num == l)
            formatImpl(l: Long)
          // special case, num.doubleValue would erase sign
          else if (num == d)
            formatImpl(d: Double)
          else
            throw new UnsupportedOperationException(
              s"number $num cannot be represented by either of Long or Double, and class ${num.getClass.getName} is not supported"
            )
        case _ => throw new IllegalArgumentException
      }
    }
  }

  /* A proxy for a `java.util.Locale` or for the root locale that provides
   * the info required by `Formatter`.
   *
   * The purpose of this abstraction is to allow `java.util.Formatter` to link
   * when `java.util.Locale` and `java.text.*` are not on the classpath, as
   * long as only methods that do not take an explicit `Locale` are used.
   *
   * While the `LocaleLocaleInfo` subclass actually delegates to a `Locale`
   * (and hence cannot link without `Locale`), the object `RootLocaleInfo`
   * hard-codes the required information about the Root locale.
   *
   * We use object-oriented method calls so that the reachability analysis
   * never reaches the `Locale`-dependent code if `LocaleLocaleInfo` is never
   * instantiated, which is the case as long the methods and constructors
   * taking an explicit `Locale` are not called.
   *
   * When `LocaleLocaleInfo` can be dead-code-eliminated, the optimizer can
   * even inline and constant-fold all the methods of `RootLocaleInfo`,
   * resulting in top efficiency.
   */
  private sealed abstract class LocaleInfo {
    def locale: Locale

    def groupingSize: Int

    def localizeNumber(str: String): String

    def toUpperCase(str: String): String

    def zeroDigit: Char

    def zeroDigitString: String = zeroDigit.toString
  }

  private object RootLocaleInfo extends LocaleInfo {
    def locale: Locale = Locale.ROOT

    def groupingSize: Int = 3

    def zeroDigit: Char = Defaults.zeroDigit

    def localizeNumber(str: String): String = str

    def toUpperCase(str: String): String = str.toUpperCase()
  }

  private final class LocaleLocaleInfo(val locale: Locale) extends LocaleInfo {

    import java.text._

    private def actualLocale: Locale =
      if (locale == null) Locale.ROOT
      else locale

    private lazy val decimalFormatSymbols: DecimalFormatSymbols =
      DecimalFormatSymbols.getInstance(actualLocale)

    lazy val groupingSize: Int = getGroupingSize

    private def getGroupingSize =
      NumberFormat.getNumberInstance(actualLocale) match {
        case decimalFormat: DecimalFormat => decimalFormat.getGroupingSize()
        case _                            => 3
      }

    def zeroDigit: Char = decimalFormatSymbols.getZeroDigit()

    def localizeNumber(str: String): String = {
      val formatSymbols = decimalFormatSymbols
      val digitOffset   = formatSymbols.getZeroDigit() - Defaults.zeroDigit
      var result        = ""
      val len           = str.length()
      var i             = 0
      import Defaults._
      while (i != len) {
        result += (str.charAt(i) match {
          case c if c >= '0' && c <= '9' => (c + digitOffset).toChar
          case `decimalSeparator`        => formatSymbols.getDecimalSeparator()
          case `groupingSeparator`       => formatSymbols.getGroupingSeparator()
          case `lineSeparator`           => System.lineSeparator()
          case c                         => c
        })
        i += 1
      }
      result
    }

    def toUpperCase(str: String): String = str.toUpperCase(actualLocale)
  }

  /* ParserStateMachine ported from Apache Harmony,
   * Used instead of regexp due to performance issues
   */
  private class ParserStateMachine(format: CharBuffer) {

    import ParserStateMachine._

    def nextFormatToken(): FormatToken = {
      var currentChar = FormatToken.Unset.asInstanceOf[Char]
      val token       = new FormatToken(format.position())

      @tailrec
      def loop(state: Int): FormatToken = {
        // FINITE STATE MACHINE
        val prevChar = currentChar
        if (ParserStateMachine.Exit != state) {
          // exit state does not need to get next char
          currentChar = getNextFormatChar()
          if (EOS == currentChar && ParserStateMachine.Entry != state) {
            throw new UnknownFormatConversionException("%")
          }
        }

        (state: @switch) match {
          case ParserStateMachine.Exit =>
            token.plainText = getFormatString(token)
            token

          case ParserStateMachine.Entry =>
            loop {
              if (EOS == currentChar) ParserStateMachine.Exit
              else if ('%' == currentChar) {
                token.formatSpecifierIndex =
                  format.position() - token.formatStringStartIndex
                StartConversion
              } else state
            }

          case ParserStateMachine.StartConversion =>
            loop {
              if (Character.isDigit(currentChar)) {
                val position       = format.position() - 1
                val number         = getPositiveInt(format)
                var nextChar: Char = 0
                if (format.hasRemaining) {
                  nextChar = format.get()
                }

                val newState = if ('$' == nextChar) {
                  token.argIndex = number
                  ApplyFlags
                } else {
                  // the digital zero stands for one format flag.
                  if ('0' == currentChar) {
                    format.position(position)
                    ApplyFlags
                  } else {
                    // the digital sequence stands for the width.
                    // do not get the next char.
                    format.position(format.position() - 1)
                    token.width = number
                    ApplyWidth
                  }
                }
                currentChar = nextChar
                newState
              } else {
                if ('<' == currentChar) {
                  token.argIndex = FormatToken.LastArgumentIndex
                } else {
                  // do not get the next char.
                  format.position(format.position() - 1)
                }
                ApplyFlags
              }
            }

          case ParserStateMachine.ApplyFlags =>
            loop {
              if (token.setFlag(currentChar)) state
              else if (Character.isDigit(currentChar)) {
                token.width = getPositiveInt(format)
                ApplyWidth
              } else if ('.' == currentChar) {
                ApplyPrecision
              } else {
                // do not get the next char.
                format.position(format.position() - 1)
                ApplyConversionType
              }
            }

          case ParserStateMachine.ApplyWidth =>
            loop {
              if ('.' == currentChar) ApplyPrecision
              else {
                format.position(format.position() - 1)
                ApplyConversionType
              }
            }

          case ParserStateMachine.ApplyPrecision =>
            if (Character.isDigit(currentChar)) {
              token.precision = getPositiveInt(format)
            } else {
              // the precision is required but not given by the format string.
              throw new UnknownFormatConversionException(getFormatString(token))
            }
            loop(ApplyConversionType)

          case ParserStateMachine.ApplyConversionType =>
            token.conversion = currentChar
            if ("bBhHsScCdoxXaAeEgGfn%".indexOf(currentChar) < 0) {
              val hasArgIdx = token.argIndex != FormatToken.Unset
              val noWidthOrPrecision = token.width == FormatToken.Unset ||
                token.precision == FormatToken.Unset
              /* In Scala.js we used regex matching to determinate if format specifier
               * is valid, but it's not possible when using current parser.
               * We provide estimation about the root of problem which should be compliant
               * with JVM. Checking for EndOfString is done in parser loop.
               * Based on test cases of `FormatterTest.unknownFormatConversion`
               */
              val causedBy = {
                if (hasArgIdx && noWidthOrPrecision) token.argIndex.toString
                else token.conversion.toString
              }

              throw new UnknownFormatConversionException(causedBy)
            }
            if (currentChar <= 'Z') {
              token.setFlag(Flags.UpperCase)
            }
            loop(Exit)

          case ParserStateMachine.ApplySuffix =>
            // Todo `t` | `T` support
            loop(Exit)
        }
      }

      loop(Entry)
    }

    private def getNextFormatChar(): Char = {
      if (format.hasRemaining) format.get()
      else EOS.toChar
    }

    private def getFormatString(token: FormatToken): String = {
      val end = format.position()
      format.rewind()
      val formatString =
        format.subSequence(token.formatStringStartIndex, end).toString
      format.position(end)
      formatString
    }

    private def getPositiveInt(buffer: CharBuffer): Int = {
      def loop(): Int = {
        if (buffer.hasRemaining && Character.isDigit(buffer.get())) {
          loop()
        } else buffer.position() - 1
      }

      val start = buffer.position() - 1
      val end   = loop().min(buffer.limit())
      buffer.position(0)
      val intStr = buffer.subSequence(start, end).toString
      buffer.position(end)
      try {
        val value = java.lang.Long.parseLong(intStr)
        if (value <= Int.MaxValue) value.toInt
        else FormatToken.ParsedValueTooLarge
      } catch {
        case _: NumberFormatException =>
          FormatToken.Unset
      }
    }
  }

  private object ParserStateMachine {
    final val EOS                 = -1.asInstanceOf[Char]
    final val Exit                = 0
    final val Entry               = 1
    final val StartConversion     = 2
    final val ApplyFlags          = 3
    final val ApplyWidth          = 4
    final val ApplyPrecision      = 5
    final val ApplyConversionType = 6
    final val ApplySuffix         = 7
  }

  private class FormatToken(val formatStringStartIndex: Int) {
    import FormatToken._

    var formatSpecifierIndex: Int = _
    var plainText: String         = _
    private var flags: Int        = 0
    var argIndex: Int             = Unset
    var width: Int                = Unset
    var precision: Int            = Unset
    var conversion: Char          = Unset.asInstanceOf[Char]

    def getFlags(): Flags = new Flags(flags)

    def setFlag(flag: Int): Unit = flags |= flag

    def setFlag(c: Char): Boolean = {
      import Flags._
      val flag = (c: @switch) match {
        case '-' => LeftAlign
        case '#' => AltFormat
        case '+' => PositivePlus
        case ' ' => PositiveSpace
        case '0' => ZeroPad
        case ',' => UseGroupingSeps
        case '(' => NegativeParen
        case '<' => UseLastIndex
        case _   => return false
      }

      if ((flags & flag) != 0)
        throw new DuplicateFormatFlagsException(c.toString)

      flags |= flag
      true
    }

    @inline
    def requireArgument(): Boolean =
      conversion != '%' && conversion != 'n'
  }

  private object FormatToken {
    final val Unset               = -1
    final val LastArgumentIndex   = -2
    final val ParsedValueTooLarge = -3
  }
}
