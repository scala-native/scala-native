package java.nio.charset

import java.nio.charset.spi.CharsetProvider
import java.nio.{ByteBuffer, CharBuffer}
import java.util.{Arrays, Collections, HashSet, ServiceLoader}

import scala.collection.mutable

abstract class Charset protected (
    canonicalName: String,
    _aliases: Array[String]
) extends AnyRef
    with Comparable[Charset] {
  private lazy val aliasesSet =
    Collections.unmodifiableSet(new HashSet(Arrays.asList(_aliases)))

  final def name(): String = canonicalName

  final def aliases(): java.util.Set[String] = aliasesSet

  override final def equals(that: Any): Boolean = that match {
    case that: Charset => this.name() == that.name()
    case _             => false
  }

  override final def toString(): String = name()

  override final def hashCode(): Int = name().##

  override final def compareTo(that: Charset): Int =
    name().compareToIgnoreCase(that.name())

  def contains(cs: Charset): Boolean

  def newDecoder(): CharsetDecoder
  def newEncoder(): CharsetEncoder

  def canEncode(): Boolean = true

  private lazy val cachedDecoder = ThreadLocal.withInitial[CharsetDecoder](() =>
    this
      .newDecoder()
      .onMalformedInput(CodingErrorAction.REPLACE)
      .onUnmappableCharacter(CodingErrorAction.REPLACE)
  )

  private lazy val cachedEncoder = ThreadLocal.withInitial[CharsetEncoder](() =>
    this
      .newEncoder()
      .onMalformedInput(CodingErrorAction.REPLACE)
      .onUnmappableCharacter(CodingErrorAction.REPLACE)
  )

  final def decode(bb: ByteBuffer): CharBuffer =
    cachedDecoder.get().decode(bb)

  final def encode(cb: CharBuffer): ByteBuffer =
    cachedEncoder.get().encode(cb)

  final def encode(str: String): ByteBuffer =
    encode(CharBuffer.wrap(str))

  def displayName(): String = name()
}

object Charset {
  import StandardCharsets._

  def defaultCharset(): Charset = UTF_8

  def forName(charsetName: String): Charset = {
    val m = CharsetMap
    m.getOrElse(
      charsetName.toLowerCase,
      throw new UnsupportedCharsetException(charsetName)
    )
  }

  def isSupported(charsetName: String): Boolean =
    CharsetMap.contains(charsetName.toLowerCase)

  def availableCharsets(): java.util.SortedMap[String, Charset] =
    availableCharsetsResult

  private lazy val availableCharsetsResult = {
    val m =
      new java.util.TreeMap[String, Charset](String.CASE_INSENSITIVE_ORDER)
    allNativeCharsets.foreach { c =>
      m.put(c.name(), c)
    }
    customCharsetProviders.forEach { provider =>
      provider.charsets().forEachRemaining { c =>
        m.put(c.name(), c)
      }
    }
    Collections.unmodifiableSortedMap(m)
  }

  private lazy val CharsetMap = {
    val m =
      mutable.Map.empty[String, Charset] // TODO Check if a better map is needed

    // All these lists where obtained by experimentation on the JDK

    for (s <- Seq(
          "iso-8859-1",
          "iso8859-1",
          "iso_8859_1",
          "iso8859_1",
          "iso_8859-1",
          "8859_1",
          "iso_8859-1:1987",
          "latin1",
          "csisolatin1",
          "l1",
          "ibm-819",
          "ibm819",
          "cp819",
          "819",
          "iso-ir-100"
        )) m(s) = ISO_8859_1

    for (s <- Seq(
          "us-ascii",
          "ascii7",
          "ascii",
          "csascii",
          "default",
          "cp367",
          "ibm367",
          "iso646-us",
          "646",
          "iso_646.irv:1983",
          "iso_646.irv:1991",
          "ansi_x3.4-1986",
          "ansi_x3.4-1968",
          "iso-ir-6"
        )) m(s) = US_ASCII

    for (s <- Seq("utf-8", "utf8", "unicode-1-1-utf-8")) m(s) = UTF_8

    for (s <- Seq(
          "utf-16be",
          "utf_16be",
          "x-utf-16be",
          "iso-10646-ucs-2",
          "unicodebigunmarked"
        )) m(s) = UTF_16BE

    for (s <- Seq(
          "utf-16le",
          "utf_16le",
          "x-utf-16le",
          "unicodelittleunmarked"
        )) m(s) = UTF_16LE

    for (s <- Seq("utf-16", "utf_16", "unicode", "unicodebig")) m(s) = UTF_16

    customCharsetProviders.forEach { provider =>
      provider.charsets().forEachRemaining { charset =>
        charset.aliases().forEach { alias =>
          m(alias) = charset
        }
      }
    }
    m
  }

  private def customCharsetProviders =
    ServiceLoader.load(classOf[CharsetProvider])

  private def allNativeCharsets =
    Array(US_ASCII, ISO_8859_1, UTF_8, UTF_16BE, UTF_16LE, UTF_16)
}
