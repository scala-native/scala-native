package java.nio.charset

class IllegalCharsetNameException(charsetName: String)
    extends IllegalArgumentException(String.valueOf(charsetName)) {
  def getCharsetName(): String = charsetName
}
