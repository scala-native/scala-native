package java.niocharset

import java.nio._
import java.nio.charset._

import scala.util._

// Ported from Scala.js

class BaseCharset(val charset: Charset) extends tests.Suite {
  import BaseCharset._

  protected val AllErrorActions = Seq(
    CodingErrorAction.IGNORE,
    CodingErrorAction.REPLACE,
    CodingErrorAction.REPORT)

  protected val ReportActions = Seq(
    CodingErrorAction.REPORT)

  protected def testDecode(in: ByteBuffer)(
    outParts: OutPart[CharBuffer]*): Unit = {

    def testOneConfig(malformedAction: CodingErrorAction,
                      unmappableAction: CodingErrorAction, readOnly: Boolean): Unit = {

      val decoder = charset.newDecoder()
      decoder.onMalformedInput(malformedAction)
      decoder.onUnmappableCharacter(unmappableAction)

      val inBuf =
        if (readOnly) in.asReadOnlyBuffer()
        else in.duplicate()
      assert(inBuf.isReadOnly == readOnly)
      assert(inBuf.hasArray != readOnly)

      val actualTry = Try {
        val buf = decoder.decode(inBuf)
        val actualChars = new Array[Char](buf.remaining())
        buf.get(actualChars)
        actualChars
      }

      val expectedTry = Try {
        val expectedChars = Array.newBuilder[Char]
        outParts foreach {
          case BufferPart(buf) =>
            val bufArray = new Array[Char](buf.remaining)
            buf.mark()
            try buf.get(bufArray)
            finally buf.reset()
            expectedChars ++= bufArray
          case Malformed(len) =>
            malformedAction match {
              case CodingErrorAction.IGNORE  =>
              case CodingErrorAction.REPLACE =>
                expectedChars ++= decoder.replacement()
              case CodingErrorAction.REPORT  =>
                throw new MalformedInputException(len)
            }
          case Unmappable(len) =>
            unmappableAction match {
              case CodingErrorAction.IGNORE  =>
              case CodingErrorAction.REPLACE =>
                expectedChars ++= decoder.replacement()
              case CodingErrorAction.REPORT  =>
                throw new UnmappableCharacterException(len)
            }
        }
        expectedChars.result()
      }

      (actualTry, expectedTry) match {
        case (Failure(actualEx: MalformedInputException),
        Failure(expectedEx: MalformedInputException)) =>
          assertEquals(expectedEx.getInputLength(), actualEx.getInputLength())

        case (Failure(actualEx: UnmappableCharacterException),
        Failure(expectedEx: UnmappableCharacterException)) =>
          assertEquals(expectedEx.getInputLength(), actualEx.getInputLength())

        case (Success(actualChars), Success(expectedChars)) =>
          assert(java.util.Arrays.equals(expectedChars.map(_.toInt), actualChars.map(_.toInt)))

        case _ =>
          // For the error message
          assertEquals(expectedTry, actualTry)
      }
    }

    val hasAnyMalformed = outParts.exists(_.isInstanceOf[Malformed])
    val hasAnyUnmappable = outParts.exists(_.isInstanceOf[Unmappable])

    for {
      malformedAction  <- if (hasAnyMalformed)  AllErrorActions else ReportActions
      unmappableAction <- if (hasAnyUnmappable) AllErrorActions else ReportActions
      readOnly         <- List(false, true)
    } {
      testOneConfig(malformedAction, unmappableAction, readOnly)
    }
  }

  protected def testEncode(in: CharBuffer)(
    outParts: OutPart[ByteBuffer]*): Unit = {

    def testOneConfig(malformedAction: CodingErrorAction,
                      unmappableAction: CodingErrorAction, readOnly: Boolean): Unit = {

      val encoder = charset.newEncoder()
      encoder.onMalformedInput(malformedAction)
      encoder.onUnmappableCharacter(unmappableAction)

      val inBuf =
        if (readOnly) in.asReadOnlyBuffer()
        else in.duplicate()
      assert(inBuf.isReadOnly == readOnly)
      assert(inBuf.hasArray != readOnly)

      val actualTry = Try {
        val buf = encoder.encode(inBuf)
        val actualBytes = new Array[Byte](buf.remaining())
        buf.get(actualBytes)
        actualBytes
      }

      val expectedTry = Try {
        val expectedBytes = Array.newBuilder[Byte]
        outParts foreach {
          case BufferPart(buf) =>
            val bufArray = new Array[Byte](buf.remaining)
            buf.mark()
            try buf.get(bufArray)
            finally buf.reset()
            expectedBytes ++= bufArray
          case Malformed(len) =>
            malformedAction match {
              case CodingErrorAction.IGNORE  =>
              case CodingErrorAction.REPLACE =>
                expectedBytes ++= encoder.replacement()
              case CodingErrorAction.REPORT  =>
                throw new MalformedInputException(len)
            }
          case Unmappable(len) =>
            unmappableAction match {
              case CodingErrorAction.IGNORE  =>
              case CodingErrorAction.REPLACE =>
                expectedBytes ++= encoder.replacement()
              case CodingErrorAction.REPORT  =>
                throw new UnmappableCharacterException(len)
            }
        }
        expectedBytes.result()
      }

      (actualTry, expectedTry) match {
        case (Failure(actualEx: MalformedInputException),
        Failure(expectedEx: MalformedInputException)) =>
          assertEquals(expectedEx.getInputLength(), actualEx.getInputLength())

        case (Failure(actualEx: UnmappableCharacterException),
        Failure(expectedEx: UnmappableCharacterException)) =>
          assertEquals(expectedEx.getInputLength(), actualEx.getInputLength())

        case (Success(actualBytes), Success(expectedBytes)) =>
          assert(java.util.Arrays.equals(expectedBytes, actualBytes))

        case _ =>
          // For the error message
          assertEquals(expectedTry, actualTry)
      }
    }

    val hasAnyMalformed = outParts.exists(_.isInstanceOf[Malformed])
    val hasAnyUnmappable = outParts.exists(_.isInstanceOf[Unmappable])

    for {
      malformedAction  <- if (hasAnyMalformed)  AllErrorActions else ReportActions
      unmappableAction <- if (hasAnyUnmappable) AllErrorActions else ReportActions
      readOnly         <- List(false, true)
    } {
      testOneConfig(malformedAction, unmappableAction, readOnly)
    }
  }
}

object BaseCharset {
  sealed abstract class OutPart[+BufferType <: Buffer]
  final case class BufferPart[BufferType <: Buffer](buf: BufferType) extends OutPart[BufferType]
  final case class Malformed(length: Int) extends OutPart[Nothing]
  final case class Unmappable(length: Int) extends OutPart[Nothing]

  object OutPart {
    implicit def fromBuffer[BufferType <: Buffer](buf: BufferType): BufferPart[BufferType] =
      BufferPart(buf)
  }

  implicit class Interpolators(val sc: StringContext) extends AnyVal {
    def bb(args: Any*): ByteBuffer = {
      val strings = sc.parts.iterator
      val expressions = args.iterator
      val buf = Array.newBuilder[Byte]

      def appendStr(s: String): Unit = {
        val s1 = s.replace(" ", "")
        require(s1.length % 2 == 0)
        for (i <- 0 until s1.length by 2)
          buf += java.lang.Integer.parseInt(s1.substring(i, i+2), 16).toByte
      }

      appendStr(strings.next())
      while (strings.hasNext) {
        expressions.next() match {
          case b: Byte            => buf += b
          case bytes: Array[Byte] => buf ++= bytes
          case bytes: Seq[_]      =>
            buf ++= bytes.map(_.asInstanceOf[Number].byteValue())
        }
        appendStr(strings.next())
      }

      ByteBuffer.wrap(buf.result())
    }

    def cb(args: Any*): CharBuffer =
      CharBuffer.wrap(sc.s(args: _*).toCharArray)
  }
}