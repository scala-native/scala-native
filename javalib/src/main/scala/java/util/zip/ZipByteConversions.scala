package java.util.zip

import java.nio.charset.{Charset, StandardCharsets}

private[zip] object ZipByteConversions {

  /* This is an attempt consolidate and describe zip Charset conversion
   * complexity in one place.
   *
   * One can not simplify the underlying frothing sea of zip complexity,
   * especially as practiced in the wild, but _can_ try to reduce the
   * Scala Native complexity riding loosely on top. The former are
   * 'zip features'; the latter are bugs.
   *
   * See URL:
   *     https://en.wikipedia.org/wiki/ZIP_(file_format)#History
   *
   * The original Harmony code base comment:
   *   The actual character set is "IBM Code Page 437".  As of
   *   Sep 2006, the Zip spec (APPNOTE.TXT) supports UTF-8.  When
   *   bit 11 of the GP flags field is set, the file name and
   *   comment fields are UTF-8.
   *
   * "IBM Code Page 437" is also known as "Code Page 437" and/or
   * "MS-DOS CP437".
   *
   * CP437 is not one of the Java StandardCharsets, so
   * StandardCharsets.ISO_8859_1 (a.k.a Latin_1) is often used instead in
   * order to convert all 8 bits of single bytes to Java UTF-16 Strings.
   *
   * CP437 is described as the "specified" (i.e. it may not actually be
   * described in the spec) code page. Its limitations lead people to
   * use either its later relative CP1252 (Latin-1 for Windows) or
   * the local character set used by the operating system.
   * Wild West, East, North, South, and probably Outer Space.
   *
   *
   * The convention here is that the caller passes in Zip general purpose
   * flag bits and a Charset to use if Bit 11 is clear/not_set. If that
   * bit is set, then StandardCharsets.UTF_8 is used.
   *
   * The Charset passed in is probably, not required to be, the Charset
   * constructor argument of the caller.
   *
   * Some remaining complexity (non-exhaustive):
   *
   *   *) The author has seen one report that macOS uses UTF-8 for the name,
   *      archive comment, and entry comment coding but DOES NOT set
   *      the UTF-8 bit.
   *
   *      Of true, that is an Apple "feature" and a future evolution of these
   *      methods need be changed to accommodate that feature.
   *
   *   *) Where is my emoji?
   *
   *      Not all recent Unicode codepoints, such as the latest emoji,
   *      may be available.
   *
   *      Scala Native currently (2024-03) uses Unicode version 13.0.
   *      Unicode 15.1 was released on September, 2023.
   *
   *      In theory, attempting to convert codepoints defined after
   *      Unicode 13.0 should throw an Exception. How strict is the
   *      Scala Native conversion code?
   */

  final val UTF8_ENABLED_MASK = 0x800 // Bit 11, Decimal 2048

  def getCharset(flagBits: Short, defaultCharset: Charset): Charset = {
    if ((flagBits & UTF8_ENABLED_MASK) == UTF8_ENABLED_MASK)
      StandardCharsets.UTF_8
    else defaultCharset
  }

  /* zipGPBitFlag arguments contain the zip general purpose bit flag bits
   * at both (decimal) offset:
   *    6 bytes in the Local file header (LOCSIG "PK\3\4")
   *    8 bytes in the Central directory header (CENSIG "PK\1\2")
   */

  def bytesToString(
      rawBytes: Array[Byte],
      zipGpBitFlag: Short,
      defaultCharset: Charset
  ): String = {
    if ((rawBytes == null) || (rawBytes.length <= 0)) ""
    else new String(rawBytes, getCharset(zipGpBitFlag, defaultCharset))
  }

  def bytesFromString(
      str: String,
      zipGpBitFlag: Short,
      defaultCharset: Charset
  ): Array[Byte] = {
    if (str == null) new Array[Byte](0)
    else str.getBytes(getCharset(zipGpBitFlag, defaultCharset))
  }
}
