package java.nio.charset.spi

import java.nio.charset.Charset
import java.util.Iterator

abstract class CharsetProvider protected() {
  def charsets(): Iterator[Charset]
  def charsetForName(charsetName: String): Charset
}