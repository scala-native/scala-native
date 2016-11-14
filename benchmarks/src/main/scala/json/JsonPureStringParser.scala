/*******************************************************************************
 * Copyright (c) 2015 Stefan Marr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package json

final class JsonPureStringParser(input: String) {
  var index           = -1
  var line            = 1
  var column          = 0
  var current: String = null
  var captureBuffer   = ""
  var captureStart    = -1

  def parse() = {
    read()
    skipWhiteSpace()
    val result = readValue()
    skipWhiteSpace()
    if (!isEndOfText()) {
      throw error("Unexpected character")
    }
    result
  }

  def readValue() = {
    current match {
      case "n" =>
        readNull()
      case "t" =>
        readTrue()
      case "f" =>
        readFalse()
      case "\"" =>
        readString()
      case "[" =>
        readArray()
      case "{" =>
        readObject()
      case "-" | "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" =>
        readNumber()
      case _ =>
        throw expected("value")
    }
  }

  def readArray(): JsonArray = {
    read()
    val array = new JsonArray()
    skipWhiteSpace()
    if (readChar("]")) {
      return array
    }
    do {
      skipWhiteSpace()
      array.add(readValue())
      skipWhiteSpace()
    } while (readChar(","))
    if (!readChar("]")) {
      throw expected("',' or ']'")
    }
    array
  }

  def readObject(): JsonObject = {
    read()
    val object_ = new JsonObject()
    skipWhiteSpace()
    if (readChar("}")) {
      return object_
    }
    do {
      skipWhiteSpace()
      val name = readName()
      skipWhiteSpace()
      if (!readChar(":")) {
        throw expected("':'")
      }
      skipWhiteSpace()
      object_.add(name, readValue())
      skipWhiteSpace()
    } while (readChar(","))

    if (!readChar("}")) {
      throw expected("',' or '}'")
    }
    return object_
  }

  def readName() = {
    if (!current.equals("\"")) {
      throw expected("name")
    }
    readStringInternal()
  }

  def readNull() = {
    read()
    readRequiredChar("u")
    readRequiredChar("l")
    readRequiredChar("l")
    JsonLiteral.NULL
  }

  def readTrue() = {
    read()
    readRequiredChar("r")
    readRequiredChar("u")
    readRequiredChar("e")
    JsonLiteral.TRUE
  }

  def readFalse() = {
    read()
    readRequiredChar("a")
    readRequiredChar("l")
    readRequiredChar("s")
    readRequiredChar("e")
    JsonLiteral.FALSE
  }

  def readRequiredChar(ch: String) = {
    if (!readChar(ch)) {
      throw expected("'" + ch + "'")
    }
  }

  def readString() = {
    new JsonString(readStringInternal())
  }

  def readStringInternal() = {
    read()
    startCapture()
    while (!current.equals("\"")) {
      if (current.equals("\\")) {
        pauseCapture()
        readEscape()
        startCapture()
      } else {
        read()
      }
    }
    val string = endCapture()
    read()
    string
  }

  def readEscape() = {
    read()
    current match {
      case "\"" | "/" | "\\" =>
        captureBuffer += current
      case "b" =>
        captureBuffer += "\b"
      case "f" =>
        captureBuffer += "\f"
      case "n" =>
        captureBuffer += "\n"
      case "r" =>
        captureBuffer += "\r"
      case "t" =>
        captureBuffer += "\t"
      case _ =>
        throw expected("valid escape sequence")
    }
    read()
  }

  def readNumber() = {
    startCapture()
    readChar("-")
    val firstDigit = current
    if (!readDigit()) {
      throw expected("digit")
    }
    if (!firstDigit.equals("0")) {
      while (readDigit()) {}
    }
    readFraction()
    readExponent()
    new JsonNumber(endCapture())
  }

  def readFraction(): Boolean = {
    if (!readChar(".")) {
      return false
    }
    if (!readDigit()) {
      throw expected("digit")
    }
    while (readDigit()) {}
    return true
  }

  def readExponent(): Boolean = {
    if (!readChar("e") && !readChar("E")) {
      return false
    }
    if (!readChar("+")) {
      readChar("-")
    }
    if (!readDigit()) {
      throw expected("digit")
    }

    while (readDigit()) {}

    return true
  }

  def readChar(ch: String): Boolean = {
    if (!current.equals(ch)) {
      return false
    }
    read()
    return true
  }

  def readDigit(): Boolean = {
    if (!isDigit()) {
      return false
    }
    read()
    return true
  }

  def skipWhiteSpace() = {
    while (isWhiteSpace()) {
      read()
    }
  }

  def read() = {
    if ("\n".equals(current)) {
      line += 1
      column = 0
    }
    index += 1
    if (index < input.length()) {
      current = input.substring(index, index + 1)
    } else {
      current = null
    }
  }

  def startCapture() = {
    captureStart = index
  }

  def pauseCapture() = {
    val end = if (current == null) index else index - 1
    captureBuffer += input.substring(captureStart, end + 1)
    captureStart = -1
  }

  def endCapture() = {
    val end              = if (current == null) index else index - 1
    var captured: String = null
    if ("".equals(captureBuffer)) {
      captured = input.substring(captureStart, end + 1)
    } else {
      captureBuffer += input.substring(captureStart, end + 1)
      captured = captureBuffer
      captureBuffer = ""
    }
    captureStart = -1
    captured
  }

  def expected(expected: String) = {
    if (isEndOfText()) {
      error("Unexpected end of input")
    } else {
      error("Expected " + expected)
    }
  }

  def error(message: String) = {
    new ParseException(message, index, line, column - 1)
  }

  def isWhiteSpace() = {
    " ".equals(current) || "\t".equals(current) || "\n".equals(current) || "\r"
      .equals(current)
  }

  def isDigit() = {
    "0".equals(current) ||
    "1".equals(current) ||
    "2".equals(current) ||
    "3".equals(current) ||
    "4".equals(current) ||
    "5".equals(current) ||
    "6".equals(current) ||
    "7".equals(current) ||
    "8".equals(current) ||
    "9".equals(current)
  }

  def isEndOfText() = {
    current == null
  }
}
