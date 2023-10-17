package scala.scalanative.nio.charset

import java.nio.charset.spi.CharsetProvider

class DefaultCharsetProvider() extends CharsetProvider(){
  def charsetForName(charsetName: String): java.nio.charset.Charset = ???
  def charsets(): java.util.Iterator[java.nio.charset.Charset] = ???
}