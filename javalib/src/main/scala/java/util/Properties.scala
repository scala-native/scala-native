package java.util

import java.io._
import java.{lang => jl}
import java.{util => ju}
import java.nio.charset.StandardCharsets

import scala.annotation.switch
import scala.annotation.tailrec
import scala.collection.JavaConverters._

import ScalaOps._

class Properties(protected val defaults: Properties)
    extends ju.Hashtable[AnyRef, AnyRef] {

  def this() = this(null)

  def setProperty(key: String, value: String): AnyRef =
    put(key, value)

  def load(inStream: InputStream): Unit = {
    val stream = new BufferedInputStream(inStream)
    loadImpl(new InputStreamReader(stream, StandardCharsets.ISO_8859_1))
  }

  def load(reader: Reader): Unit =
    loadImpl(reader)

  def getProperty(key: String): String =
    getProperty(key, defaultValue = null)

  def getProperty(key: String, defaultValue: String): String = {
    get(key) match {
      case value: String => value

      case _ =>
        if (defaults != null) defaults.getProperty(key, defaultValue)
        else defaultValue
    }
  }

  def propertyNames(): ju.Enumeration[_] = {
    val propNames = new ju.HashSet[String]
    foreachAncestor { ancestor =>
      ancestor.keySet().scalaOps.foreach { key =>
        // Explicitly use asInstanceOf, to trigger the ClassCastException mandated by the spec
        propNames.add(key.asInstanceOf[String])
      }
    }
    Collections.enumeration(propNames)
  }

  def stringPropertyNames(): ju.Set[String] = {
    val set = new ju.HashSet[String]
    foreachAncestor { ancestor =>
      ancestor.entrySet().scalaOps.foreach { entry =>
        (entry.getKey(), entry.getValue()) match {
          case (key: String, _: String) => set.add(key)
          case _                        => // Ignore key
        }
      }
    }
    set
  }

  @inline @tailrec
  private final def foreachAncestor(f: Properties => Unit): Unit = {
    f(this)
    if (defaults ne null)
      defaults.foreachAncestor(f)
  }

  private def format(entry: ju.Map.Entry[AnyRef, AnyRef]): String = {
    val key: String   = entry.getKey.asInstanceOf[String]
    val value: String = entry.getValue.asInstanceOf[String]
    if (key.length > 40)
      s"${key.substring(0, 37)}...=$value"
    else
      s"$key=$value"
  }

  private final val listStr = "-- listing properties --"

  def list(out: PrintStream): Unit = {
    out.println(listStr)
    entrySet().asScala.foreach { entry => out.println(format(entry)) }
  }

  def list(out: PrintWriter): Unit = {
    out.println(listStr)
    entrySet().asScala.foreach { entry => out.println(format(entry)) }
  }

  def store(out: OutputStream, comments: String): Unit = {
    val writer = new OutputStreamWriter(out, StandardCharsets.ISO_8859_1)
    storeImpl(writer, comments, toHex = true)
  }

  def store(writer: Writer, comments: String): Unit =
    storeImpl(writer, comments, toHex = false)

  private def storeImpl(writer: Writer,
                        comments: String,
                        toHex: Boolean): Unit = {
    if (comments != null) {
      writeComments(writer, comments, toHex)
    }

    writer.write('#')
    writer.write(new Date().toString)
    writer.write(System.lineSeparator)

    entrySet().asScala.foreach { entry =>
      writer.write(
        encodeString(entry.getKey.asInstanceOf[String], isKey = true, toHex))
      writer.write('=')
      writer.write(
        encodeString(entry.getValue.asInstanceOf[String], isKey = false, toHex))
      writer.write(System.lineSeparator)
    }
    writer.flush()
  }

  @deprecated("", "")
  def save(out: OutputStream, comments: String): Unit =
    store(out, comments)

  private def loadImpl(reader: Reader): Unit = {
    import java.util.regex._
    val trailingBackspace = Pattern.compile("""(\\)+$""")
    val br                = new BufferedReader(reader)
    val valBuf            = new jl.StringBuilder()
    var prevValueContinue = false
    var isKeyParsed       = false
    var key: String       = null
    var line: String      = null

    while ({ line = br.readLine(); line != null }) {
      var i: Int   = -1
      var ch: Char = Char.MinValue

      def getNextChar(): Char = {
        i += 1
        // avoid out of bounds if value is empty
        if (i < line.length())
          line.charAt(i)
        else
          ch
      }

      def parseUnicodeEscape(): Char = {
        val sb = new jl.StringBuilder()
        var j  = 0
        while (j < 4) {
          sb.append(line.charAt(i))
          if (j < 3) {
            // don't advance past the last char used
            i += 1
          }
          j += 1
        }
        val ch = Integer.parseInt(sb.toString(), 16).toChar
        ch
      }

      def isWhitespace(char: Char): Boolean =
        char == ' ' || char == '\t' || char == '\f'

      def isTokenKeySeparator(char: Char): Boolean =
        char == '=' || char == ':'

      def isKeySeparator(char: Char): Boolean =
        isTokenKeySeparator(char) || isWhitespace(char)

      def isEmpty(): Boolean =
        line.isEmpty()

      def isComment(): Boolean =
        line.startsWith("#") || line.startsWith("!")

      def oddBackslash(): Boolean = {
        val m = trailingBackspace.matcher(line)
        if (m.find()) {
          val num   = m.end(1) - m.start
          val isOdd = num % 2 != 0
          isOdd
        } else {
          false
        }
      }

      def valueContinues(): Boolean = oddBackslash()

      def processChar(buf: jl.StringBuilder): Unit =
        if (ch == '\\') {
          ch = getNextChar()
          ch match {
            case 'u' =>
              getNextChar() // advance
              val uch = parseUnicodeEscape()
              buf.append(uch)
            case 't' => buf.append('\t')
            case 'f' => buf.append('\f')
            case 'r' => buf.append('\r')
            case 'n' => buf.append('\n')
            case _   => buf.append(ch)
          }
        } else {
          buf.append(ch)
        }

      def parseKey(): String = {
        val buf = new jl.StringBuilder()
        // ignore leading whitespace
        while (i < line.length && isWhitespace(ch)) {
          ch = getNextChar()
        }
        // key sep or empty value
        while (!isKeySeparator(ch) && i < line.length()) {
          processChar(buf)
          ch = getNextChar()
        }
        // ignore trailing whitespace
        while (i < line.length && isWhitespace(ch)) {
          ch = getNextChar()
        }
        // ignore non-space key separator
        if (i < line.length && isTokenKeySeparator(ch)) {
          ch = getNextChar()
        }
        isKeyParsed = true
        buf.toString()
      }

      def parseValue(): String = {
        // ignore leading whitespace
        while (i < line.length && isWhitespace(ch)) {
          ch = getNextChar()
        }

        // nothing but line continuation
        if (valueContinues() && i == line.length() - 1) {
          // ignore the final backslash
          ch = getNextChar()
        }

        while (i < line.length) {
          if (valueContinues() && i == line.length() - 1) {
            // ignore the final backslash
            ch = getNextChar()
          } else {
            processChar(valBuf)
            ch = getNextChar()
          }
        }
        valBuf.toString()
      }

      // run the parsing
      if (!(isComment() || isEmpty())) {
        ch = getNextChar()
        if (!isKeyParsed) {
          valBuf.setLength(0)
          key = parseKey()
          val value = parseValue()
          prevValueContinue = valueContinues()
          if (!prevValueContinue) {
            setProperty(key, value)
            isKeyParsed = false
          }
        } else if (prevValueContinue && valueContinues()) {
          val value = parseValue()
          prevValueContinue = valueContinues()
        } else {
          val value = parseValue()
          setProperty(key, value)
          isKeyParsed = false
          prevValueContinue = false
        }
      }
    }
  }

  private def writeComments(writer: Writer,
                            comments: String,
                            toHex: Boolean): Unit = {
    writer.write('#')
    val chars = comments.toCharArray
    var index = 0
    while (index < chars.length) {
      val ch = chars(index)
      if (ch <= 0xff) {
        if (ch == '\r' || ch == '\n') {
          // "\r\n"
          if (ch == '\r'
              && index + 1 < chars.length
              && chars(index + 1) == '\n') {
            index += 1
          }
          writer.write(System.lineSeparator)
          // add '#' if next char doesn't start with a comment
          if (index + 1 < chars.length
              && (chars(index + 1) != '#' && chars(index + 1) != '!')) {
            writer.write('#')
          }
        } else {
          writer.write(ch)
        }
      } else {
        if (toHex) {
          writer.write(unicodeToHexaDecimal(ch))
        } else {
          writer.write(ch)
        }
      }
      index += 1
    }
    writer.write(System.lineSeparator)
  }

  private def encodeString(string: String,
                           isKey: Boolean,
                           toHex: Boolean): String = {
    val buffer = new jl.StringBuilder(200)
    var index  = 0
    val length = string.length
    // leading element (value) spaces are escaped
    if (!isKey) {
      while (index < length && string.charAt(index) == ' ') {
        buffer.append("\\ ")
        index += 1
      }
    }

    while (index < length) {
      val ch = string.charAt(index)
      (ch: @switch) match {
        case '\t' =>
          buffer.append("\\t")
        case '\n' =>
          buffer.append("\\n")
        case '\f' =>
          buffer.append("\\f")
        case '\r' =>
          buffer.append("\\r")
        case '\\' | '#' | '!' | '=' | ':' =>
          buffer.append('\\')
          buffer.append(ch)
        case ' ' if isKey =>
          buffer.append("\\ ")
        case _ =>
          if (toHex && (ch < 0x20 || ch > 0x7e)) {
            buffer.append(unicodeToHexaDecimal(ch))
          } else {
            buffer.append(ch)
          }
      }
      index += 1
    }
    buffer.toString()
  }

  private def unicodeToHexaDecimal(ch: Int): Array[Char] = {
    def hexChar(x: Int): Char =
      if (x > 9) (x - 10 + 'A').toChar
      else (x + '0').toChar

    Array('\\',
          'u',
          hexChar((ch >>> 12) & 15),
          hexChar((ch >>> 8) & 15),
          hexChar((ch >>> 4) & 15),
          hexChar(ch & 15))
  }

  // TODO:
  // def loadFromXML(in: InputStream): Unit
  // def storeToXML(os: OutputStream, comment: String): Unit
  // def storeToXML(os: OutputStream, comment: String, encoding: String): Unit
}
