package java.text

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.util.Locale

class DecimalFormat extends NumberFormat {
  // basic implementation for java.util.Formatter with Locale.US only
  // TODO: add missing methods

  private[this] var dfsym = new DecimalFormatSymbols(Locale.US)

  private[this] var positivePrefix: String = ""

  private[this] var negativePrefix: String = dfsym.getMinusSign().toString

  private[this] var positiveSuffix: String = ""

  private[this] var negativeSuffix: String = ""

  // the Javadoc on setGroupingSize says that groupingSize is converted to a byte
  private[this] var groupingSize: Byte = 3

  private[this] var decimalSepShown: Boolean = false

  private[this] var maxIntDigits: Int = 0

  private[this] var minIntDigits: Int = 0

  private[this] var maxFracDigits: Int = 0

  private[this] var minFracDigits: Int = 0

  // if nonzero, Scientific Notation mode is enabled
  private[this] var sciExpoDigits: Int = 0

  private trait Formatting[A] {
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
          padLeft(afterDecimal, dfsym.getZeroDigit(), rounded.whole)
        paddedWhole.splitAt(paddedWhole.length - afterDecimal)
      }
      val rwhole =
        if (rwholeEmpty.isEmpty) Seq(dfsym.getZeroDigit()) else rwholeEmpty
      val rfrac =
        if (rfracZeros.forall(_ == dfsym.getZeroDigit())) Seq.empty
        else rfracZeros
      Digits(digits.negative, rwhole, rfrac)
    }

    def formatFixedPoint(number: A): String = {
      val ungroupedDigits = toDigits(number)
      val roundedDigits = {
        import ungroupedDigits._
        if (frac.length > getMaximumFractionDigits())
          roundAt(ungroupedDigits, getMaximumFractionDigits())
        else
          ungroupedDigits
      }
      val groupedDigits: Digits = {
        if (isGroupingUsed())
          roundedDigits.copy(whole = grouped(roundedDigits.whole))
        else
          roundedDigits
      }
      val negative  = groupedDigits.negative
      val wholePart = groupedDigits.whole
      val fracPart = groupedDigits.frac
        .padTo(getMinimumFractionDigits(), dfsym.getZeroDigit())
      val dotPart =
        if (fracPart.isEmpty && !isDecimalSeparatorAlwaysShown()) Seq.empty
        else Seq(dfsym.getDecimalSeparator())
      val signPart =
        if (negative) Seq(dfsym.getMinusSign())
        else Seq.empty

      (signPart ++ wholePart ++ dotPart ++ fracPart).mkString
    }

    def scaleDigits(unscaled: Digits): (Digits, Int) = {
      val Digits(negative, wholeDigits, fracDigits) = unscaled
      val allDigits                                 = (wholeDigits ++ fracDigits).dropWhile(_ == '0')
      val wholeDigitNum =
        if (getMaximumIntegerDigits() > getMinimumIntegerDigits() && getMaximumIntegerDigits() > 1)
          allDigits.length % getMaximumIntegerDigits()
        else
          getMinimumIntegerDigits() max 1
      val (wholePart, fracPartNotCutoff) = allDigits.splitAt(wholeDigitNum)
      val exp                            = fracPartNotCutoff.length - fracDigits.length
      (Digits(negative, wholePart, fracPartNotCutoff), exp)
    }

    def formatScientific(number: A): String = {
      val (roundedDigits, exp) = {
        val unscaledDigits                = toDigits(number)
        val (scaledDigits, exp)           = scaleDigits(unscaledDigits)
        val Digits(negative, whole, frac) = scaledDigits
        if (frac.length > getMaximumFractionDigits()) {
          val rounded = roundAt(scaledDigits, getMaximumFractionDigits())
          // check if carried
          if (scaledDigits.whole.length != rounded.whole.length) {
            val (newWhole, newPartZeros) =
              rounded.whole.splitAt(scaledDigits.whole.length)
            val newPart =
              if (newPartZeros.forall(_ == dfsym.getZeroDigit())) Seq.empty
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
        if (fracPart.isEmpty && !isDecimalSeparatorAlwaysShown()) Seq.empty
        else Seq(dfsym.getDecimalSeparator())
      val signPart =
        if (negative) Seq(dfsym.getMinusSign())
        else Seq.empty
      def padInt(len: Int, elem: Char, num: Int): Seq[Char] =
        if (num < 0)
          dfsym.getMinusSign() +: padLeft(len, elem, (-num).toString.toSeq)
        else
          padLeft(len, elem, num.toString.toSeq)
      (
        signPart ++
          wholePart.padTo(getMinimumIntegerDigits(), dfsym.getZeroDigit()) ++
          dotPart ++
          fracPart.padTo(getMinimumFractionDigits(), dfsym.getZeroDigit()) ++
          ('E' +: padInt(sciExpoDigits, dfsym.getZeroDigit(), exp))
      ).mkString
    }

    def format(number: A): String =
      if (sciExpoDigits == 0)
        formatFixedPoint(number)
      else
        formatScientific(number)
  }

  implicit private object LongFormatting extends Formatting[Long] {
    def toDigits(number: Long): Digits = {
      val numabs = number.abs
      def toBeTruncated =
        Iterator(numabs) ++
          Iterator.iterate(numabs / 10)(_ / 10).takeWhile(_ > 0)
      def whole =
        toBeTruncated
          .map { i =>
            (dfsym.getZeroDigit() + (i % 10)).toChar
          }
          .toList
          .reverse
      Digits(number < 0, whole, Seq.empty)
    }

    def roundToInteger(digits: Digits): Digits = {
      import digits._
      toDigits {
        val signum = if (negative) -1 else 1
        // assuming Long can be represented by Double
        val toBeRounded = (whole.mkString.toDouble + ("0." + frac.mkString).toDouble) * signum
        Math.round(toBeRounded)
      }
    }
  }

  implicit private object DoubleFormatting extends Formatting[Double] {
    def toDigits(number: Double): Digits = {
      val numabs = number.abs
      Digits(number < 0,
             nonNegativeIntegralDigits(numabs),
             nonNegativeFractionDigits(numabs))
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
              // TODO: consider other cases
              0.0
          }
        val toBeRounded = (whole.mkString.toDouble + fracd) * signum
        Math.round(toBeRounded)
      }
    }

    def nonNegativeIntegralDigits(number: Double): Seq[Char] = {
      def toBeTruncated =
        Iterator(number) ++
          Iterator.iterate(number / 10)(_ / 10).takeWhile(_ >= 1)
      toBeTruncated
        .map { d =>
          (d % 10).toInt
        }
        .map { i =>
          (dfsym.getZeroDigit() + i).toChar
        }
        .toList
        .reverse
    }

    def nonNegativeFractionDigits(number: Double): Seq[Char] = {
      val frac = number - Math.floor(number)
      Iterator
        .iterate(frac)(_ * 10)
        // workaround to avoid getting noises beyond java.lang.Double.MIN_VALUE == 4.9e-324
        .take(325)
        // take while the fractional part exists
        .takeWhile { f =>
          f != Math.floor(f)
        }
        // extract the first decimal place
        .map { f =>
          (f * 10 % 10).toInt
        }
        .mkString
    }
  }

  implicit private object BigIntegerFormatting extends Formatting[BigInteger] {
    def toDigits(number: BigInteger): Digits = {
      val numabs = number.abs
      Digits(number.signum < 0, numabs.toString.toSeq, Seq.empty)
    }

    def roundToInteger(digits: Digits): Digits = {
      import digits._
      toDigits {
        val sign = if (negative) "-" else ""
        val toBeRounded =
          new BigDecimal(sign + whole.mkString + "." + frac.mkString)
        toBeRounded.setScale(0, getRoundingMode()).toBigInteger
      }
    }
  }

  implicit private object BigDecimalFormatting extends Formatting[BigDecimal] {
    def toDigits(number: BigDecimal): Digits = {
      val numabs = number.abs
      val s      = numabs.toPlainString
      val (whole, frac) = s.indexOf('.') match {
        case -1 => (s, "")
        case dp =>
          val (whole, dotFrac) = s.splitAt(dp)
          (whole, dotFrac.tail)
      }
      Digits(number.signum < 0, whole.toSeq, frac.toSeq)
    }

    def roundToInteger(digits: Digits): Digits = {
      import digits._
      toDigits {
        val sign = if (negative) "-" else ""
        val toBeRounded =
          new BigDecimal(sign + whole.mkString + "." + frac.mkString)
        toBeRounded.setScale(0, getRoundingMode())
      }
    }
  }

  final override def format(obj: Object,
                            toAppendTo: StringBuffer,
                            pos: FieldPosition): StringBuffer =
    obj match {
      case bi: BigInteger =>
        formatImpl(bi, toAppendTo, pos)
      case bd: BigDecimal =>
        formatImpl(bd, toAppendTo, pos)
      case num: Number =>
        val l = num.longValue
        val d = num.doubleValue
        // type ascriptions are put to make sure the correct overload is called
        if (num == l)
          format(l: Long, toAppendTo, pos)
        else if (num == d)
          format(d: Double, toAppendTo, pos)
        else
          throw new UnsupportedOperationException(
            s"number ${num} cannot be represented by either of Long or Double, and class ${num.getClass.getName} is not supported"
          )
      case _ => throw new IllegalArgumentException
    }

  def format(number: Double,
             toAppendTo: StringBuffer,
             pos: FieldPosition): StringBuffer =
    formatImpl(number, toAppendTo, pos)

  private def grouped(cs: Seq[Char]): Seq[Char] =
    cs.reverseIterator
      .grouped(getGroupingSize())
      .flatMap { dfsym.getGroupingSeparator() +: _ }
      .drop(1)
      .toList
      .reverse

  def format(number: Long,
             toAppendTo: StringBuffer,
             pos: FieldPosition): StringBuffer =
    formatImpl(number, toAppendTo, pos)

  private def formatImpl[A](
      number: A,
      toAppendTo: StringBuffer,
      pos: FieldPosition)(implicit fmt: Formatting[A]): StringBuffer = {
    val formatted = fmt.format(number)
    pos.setBeginIndex(toAppendTo.length())
    pos.setEndIndex(toAppendTo.length() + formatted.length())
    toAppendTo.append(formatted)
    toAppendTo
  }

  def getDecimalFormatSymbols(): DecimalFormatSymbols = dfsym

  def setDecimalFormatSymbols(newSymbols: DecimalFormatSymbols): Unit =
    dfsym = newSymbols

  def getPositivePrefix(): String = positivePrefix

  def setPositivePrefix(newValue: String): Unit = positivePrefix = newValue

  def getNegativePrefix(): String = negativePrefix

  def setNegativePrefix(newValue: String): Unit = negativePrefix = newValue

  def getPositiveSuffix(): String = positiveSuffix

  def setPositiveSuffix(newValue: String): Unit = positiveSuffix = newValue

  def getNegativeSuffix(): String = negativeSuffix

  def setNegativeSuffix(newValue: String): Unit = negativeSuffix = newValue

  def getGroupingSize(): Int = groupingSize.toInt

  // the Javadoc on setGroupingSize says that groupingSize is converted to a byte
  def setGroupingSize(newValue: Int): Unit = groupingSize = newValue.toByte

  def isDecimalSeparatorAlwaysShown(): Boolean = decimalSepShown

  def applyPattern(pattern: String): Unit = {
    import DecimalFormat.PatternSyntax._
    pattern.toSeq match {
      case Pattern(PositivePattern(prefix, number, suffix), negative) =>
        prefix.map(_.value.mkString).foreach(setPositivePrefix)
        suffix.map(_.value.mkString).foreach(setPositiveSuffix)
        negative.map {
          case NegativePattern(prefix, _, suffix) =>
            prefix.map(_.value.mkString).foreach(setNegativePrefix)
            suffix.map(_.value.mkString).foreach(setNegativeSuffix)
        }
        number match {
          case Number(Integer(max, min, grp), frac, exp) =>
            setMaximumIntegerDigits(max)
            setMinimumIntegerDigits(min)
            grp match {
              case Some(groupSize) =>
                setGroupingSize(groupSize)
                setGroupingUsed(true)
              case None =>
                setGroupingUsed(false)
            }
            frac.foreach {
              case Fraction(max, min) =>
                setMaximumFractionDigits(max)
                setMinimumFractionDigits(min)
            }
            decimalSepShown = frac.isDefined
            sciExpoDigits = exp.map(_.exp).getOrElse(0)
        }
      case _ => throw new IllegalArgumentException
    }
  }

  def setMaximumIntegerDigits(newValue: Int): Unit =
    maxIntDigits = newValue max 0

  def setMinimumIntegerDigits(newValue: Int): Unit =
    minIntDigits = newValue max 0

  def setMaximumFractionDigits(newValue: Int): Unit =
    maxFracDigits = newValue max 0

  def setMinimumFractionDigits(newValue: Int): Unit =
    minFracDigits = newValue max 0

  def getMaximumIntegerDigits(): Int = maxIntDigits

  def getMinimumIntegerDigits(): Int = minIntDigits

  def getMaximumFractionDigits(): Int = maxFracDigits

  def getMinimumFractionDigits(): Int = minFracDigits

  def getRoundingMode(): RoundingMode =
    RoundingMode.HALF_EVEN // TODO: support others
}

object DecimalFormat {
  private object PatternSyntax {
    case class Pattern(positive: PositivePattern,
                       negative: Option[NegativePattern])
    object Pattern {
      object ++ {
        def unapply(input: Seq[Char]): Option[(PositivePattern, Seq[Char])] =
          (0 to input.length).view
            .map { input splitAt _ }
            .collect {
              case (PositivePattern(prefix, number, suffix), rest) =>
                (PositivePattern(prefix, number, suffix), rest)
            }
            .lastOption
      }

      def unapply(input: Seq[Char])
        : Option[(PositivePattern, Option[NegativePattern])] =
        input match {
          case pp ++ rest =>
            rest match {
              case Seq() => Some((pp, None))
              case ';' +: NegativePattern(prefix, number, suffix) =>
                Some((pp, Some(NegativePattern(prefix, number, suffix))))
              case _ =>
                None
            }
          case _ => None
        }
    }

    case class SignedPattern(prefix: Option[Prefix],
                             number: Number,
                             suffix: Option[Suffix])
    object SignedPattern {
      object Prefix_++ {
        def unapply(input: Seq[Char]): Option[(Prefix, Seq[Char])] =
          (0 to input.length).view
            .map { input splitAt _ }
            .collect { case (Prefix(value), rest) => (Prefix(value), rest) }
            .lastOption
      }

      object Number_++ {
        def unapply(input: Seq[Char]): Option[(Number, Seq[Char])] =
          (0 to input.length).view
            .map { input splitAt _ }
            .collect {
              case (Number(int, frac, exp), rest) =>
                (Number(int, frac, exp), rest)
            }
            .lastOption
      }

      def unapply(
          input: Seq[Char]): Option[(Option[Prefix], Number, Option[Suffix])] =
        input match {
          case Number(int, frac, exp) =>
            Some((None, Number(int, frac, exp), None))
          case pf Prefix_++ Number(int, frac, exp) =>
            Some((Some(pf), Number(int, frac, exp), None))
          case pf Prefix_++ (n Number_++ Suffix(suff)) =>
            Some((Some(pf), n, Some(Suffix(suff))))
          case _ => None
        }
    }

    type PositivePattern = SignedPattern
    val PositivePattern = SignedPattern
    type NegativePattern = SignedPattern
    val NegativePattern = SignedPattern

    case class Affix(value: Seq[Char])
    object Affix {
      val specials = Set('\uFFFE', '\uFFFF', '0', '#', '.', ',', 'E', ';', '%',
        '\u2030', '\u00A4', '\'')

      def unapply(input: Seq[Char]): Option[Seq[Char]] = {
        val (matched, rest) = input.span(c => !(specials contains c))
        rest match {
          case Seq() => Some(matched)
          case '\'' +: '\'' +: more =>
            unapply(more).map { m =>
              matched ++ ('\'' +: m)
            }
          case '\'' +: literalPlus =>
            literalPlus.indexOf('\'') match {
              case -1 => None
              case apos =>
                val (literal, plus) = literalPlus.splitAt(apos)
                unapply(plus.tail).map { matched ++ literal ++ _ }
            }
          case _ => None
        }
      }
    }

    type Prefix = Affix
    val Prefix = Affix
    type Suffix = Affix
    val Suffix = Affix

    case class Number(int: Integer,
                      frac: Option[Fraction],
                      exp: Option[Exponent])
    object Number {
      object Integer_++ {
        def unapply(input: Seq[Char]): Option[(Integer, Seq[Char])] =
          (0 to input.length).view
            .map { input splitAt _ }
            .collect {
              case (Integer(max, min, grp), rest) =>
                (Integer(max, min, grp), rest)
            }
            .lastOption
      }

      object Fraction_++ {
        def unapply(input: Seq[Char]): Option[(Fraction, Seq[Char])] =
          (0 to input.length).view
            .map { input splitAt _ }
            .collect {
              case (Fraction(max, min), rest) => (Fraction(max, min), rest)
            }
            .lastOption
      }

      def unapply(input: Seq[Char])
        : Option[(Integer, Option[Fraction], Option[Exponent])] =
        input match {
          case Integer(max, min, grp) =>
            Some((Integer(max, min, grp), None, None))
          case i Integer_++ Exponent(exp) =>
            Some((i, None, Some(Exponent(exp))))
          case i Integer_++ '.' +: rest =>
            rest match {
              case Fraction(max, min) =>
                Some((i, Some(Fraction(max, min)), None))
              case f Fraction_++ Exponent(exp) =>
                Some((i, Some(f), Some(Exponent(exp))))
              case _ => None
            }
          case _ => None
        }
    }

    case class Integer(max: Int, min: Int, groupSize: Option[Int])
    object Integer {
      def unapply(input: Seq[Char]): Option[(Int, Int, Option[Int])] =
        input match {
          case MinimumInteger(min, grp)        => Some((min, min, grp))
          case '#' +: Nil                      => Some((1, 0, None))
          case '#' +: Integer((max, min, grp)) => Some((max + 1, min, grp))
          case '#' +: ',' +: Integer((max, min, grp)) =>
            Some((max + 1, min, grp.orElse(Some(max))))
          case ',' +: Integer((max, min, grp)) =>
            // Note: according to the Javadoc, a pattern starting with a comma is ill-formed.
            // However, OpenJDK 8 accepts it, and Formatter ported from Harmony uses it.
            Some((max, min, grp.orElse(Some(max))))
          case _ => None
        }
    }

    case class MinimumInteger(min: Int, groupSize: Option[Int])
    object MinimumInteger {
      def unapply(input: Seq[Char]): Option[(Int, Option[Int])] =
        input match {
          case '0' +: Nil                        => Some((1, None))
          case '0' +: MinimumInteger((min, grp)) => Some((min + 1, grp))
          case '0' +: ',' +: MinimumInteger((min, grp)) =>
            Some((min + 1, grp.orElse(Some(min))))
          case ',' +: MinimumInteger((min, grp)) =>
            // Note: according to the Javadoc, a pattern starting with a comma is ill-formed.
            // However, OpenJDK 8 accepts it, and Formatter ported from Harmony uses it.
            Some((min, grp.orElse(Some(min))))
          case _ => None
        }
    }

    case class Fraction(max: Int, min: Int)
    object Fraction {
      object ++ {
        def unapply(input: Seq[Char]): Option[(MinimumFraction, Seq[Char])] =
          (0 to input.length).view
            .map { input splitAt _ }
            .collect {
              case (MinimumFraction(min), of) => (MinimumFraction(min), of)
            }
            .lastOption
      }

      def unapply(input: Seq[Char]): Option[(Int, Int)] = // max, min
        input match {
          case Seq()                 => Some((0, 0))
          case MinimumFraction(min)  => Some((min, min))
          case OptionalFraction(max) => Some((max, 0))
          case MinimumFraction(min) ++ OptionalFraction(opt) =>
            Some((opt + min, min))
          case _ => None
        }
    }

    case class MinimumFraction(min: Int)
    object MinimumFraction {
      def unapply(input: Seq[Char]): Option[Int] =
        input match {
          case '0' +: Nil                  => Some(1)
          case '0' +: MinimumFraction(min) => Some(min + 1)
          case _                           => None
        }
    }

    case class OptionalFraction(opt: Int)
    object OptionalFraction {
      def unapply(input: Seq[Char]): Option[Int] =
        input match {
          case '#' +: Nil                   => Some(1)
          case '#' +: OptionalFraction(max) => Some(max + 1)
          case _                            => None
        }
    }

    case class Exponent(exp: Int)
    object Exponent {
      def unapply(input: Seq[Char]): Option[Int] =
        input match {
          case 'E' +: MinimumExponent(exp) => Some(exp)
          case _                           => None
        }
    }

    case class MinimumExponent(exp: Int)
    object MinimumExponent {
      def unapply(input: Seq[Char]): Option[Int] =
        input match {
          case '0' +: Nil                  => Some(1)
          case '0' +: MinimumExponent(exp) => Some(exp + 1)
          case _                           => None
        }
    }
  }
}
