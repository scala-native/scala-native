/**
 * Ported from Harmony
 */
package java.util

import java.io._
import java.{util => ju}

import scala.annotation.{switch, tailrec}
import scala.collection.immutable.{Map => SMap}
import scala.collection.JavaConverters._

class Properties(protected val defaults: Properties)
    extends ju.Hashtable[AnyRef, AnyRef] {

  def this() = this(null)

  def setProperty(key: String, value: String): AnyRef =
    put(key, value)

  def load(inStream: InputStream): Unit = {
    val stream = new BufferedInputStream(inStream)
    loadImpl(new InputStreamReader(stream, "ISO8859-1"))
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
    val thisSet = keySet().asScala.map(_.asInstanceOf[String])
    val defaultsIterator =
      if (defaults != null) defaults.propertyNames().asScala.toIterator
      else scala.collection.Iterator.empty
    val filteredDefaults = defaultsIterator.collect {
      case k: String if !thisSet(k) => k
    }
    (thisSet.iterator ++ filteredDefaults).asJavaEnumeration
  }

  def stringPropertyNames(): ju.Set[String] = {
    val set = new ju.HashSet[String]
    entrySet().asScala.foreach { entry =>
      (entry.getKey, entry.getValue) match {
        case (key: String, _: String) => set.add(key)
        case _                        => // Ignore key
      }
    }
    if (defaults != null)
      set.addAll(defaults.stringPropertyNames())
    set
  }

  private def format(entry: ju.Map.Entry[AnyRef, AnyRef]): String = {
    val key: String   = entry.getKey.asInstanceOf[String]
    val value: String = entry.getValue.asInstanceOf[String]
    if (key.length > 40)
      s"${key.substring(0, 37)}...=$value"
    else
      s"$key=$value"
  }

  private val listStr = "-- listing properties --"

  def list(out: PrintStream): Unit = {
    out.println(listStr)
    entrySet().asScala.foreach { entry => out.println(format(entry)) }
  }

  def list(out: PrintWriter): Unit = {
    out.println(listStr)
    entrySet().asScala.foreach { entry => out.println(format(entry)) }
  }

  def store(out: OutputStream, comments: String): Unit = {
    val writer = new OutputStreamWriter(out, "ISO8859_1")
    storeImpl(writer, comments, true)
  }

  def store(writer: Writer, comments: String): Unit =
    storeImpl(writer, comments, false)

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
      writer.write(encodeString(entry.getKey.asInstanceOf[String], true, toHex))
      writer.write('=')
      writer.write(
        encodeString(entry.getValue.asInstanceOf[String], false, toHex))
      writer.write(System.lineSeparator)
    }
    writer.flush()
  }

  @deprecated("", "")
  def save(out: OutputStream, comments: String): Unit =
    store(out, comments)

  private def loadImpl2(reader: Reader): Unit = {
    val br           = new BufferedReader(reader)
    var line: String = null
    while ({ line = br.readLine().trim(); line != null }) {
      var i: Int = 0
      println(line.length())

      def parseUnicodeEscape(): Char = ???

      def parseKey(): String = {
        val buf = new StringBuilder()
        while (!(line.charAt(i) == '=' || line.charAt(i) == ':')) {
          buf.append(line.charAt(i))
          i += 1
        }
        buf.toString
      }

      def parseValue(): String = {
        val buf = new StringBuilder()
        while (i < line.length) {
          buf.append(line.charAt(i))
          i += 1
        }
        buf.toString
      }

      def isComment(): Boolean =
        line.startsWith("#") || line.startsWith("!")

      def valueContinues(): Boolean =
        line.endsWith("\\")

      if (!isComment()) {
        println(s"value continues: $valueContinues")
        val key = parseKey()
        i += 1 // the '='
        val value = parseValue()
        setProperty(key, value)
      }
    }
    br.close()
  }

  private val NONE     = 0
  private val SLASH    = 1
  private val UNICODE  = 2
  private val CONTINUE = 3 // when \r is encountered looks for next \n
  private val KEY_DONE = 4
  private val IGNORE   = 5
  private lazy val nextCharMap =
    SMap('b' -> '\b', 'f' -> '\f', 'n' -> '\n', 'r' -> '\r', 't' -> '\t')

  private def loadImpl(reader: Reader): Unit = {
    var mode           = NONE
    var unicode        = 0
    var count          = 0
    var nextChar: Char = 0
    var buf            = new Array[Char](80)
    var offset         = 0
    var keyLength      = -1
    val br             = new BufferedReader(reader)

    @tailrec def processNext(isFirstChar: Boolean): Unit = {
      val intVal = br.read()
      if (intVal == -1) {
        if (mode == UNICODE && count <= 4) {
          throw new IllegalArgumentException(
            "Invalid Unicode sequence: expected format")
        }
        if (keyLength == -1 && offset > 0)
          keyLength = offset
        if (keyLength >= 0) {
          val key   = new String(buf, 0, keyLength)
          val value = new String(buf, keyLength, offset - keyLength)
          put(key, if (mode == SLASH) value + '\u0000' else value)
        }
      } else {
        nextChar = intVal.toChar
        if (offset == buf.length) {
          val newBuf = new Array[Char](buf.length << 1)
          System.arraycopy(buf, 0, newBuf, 0, offset)
          buf = newBuf
        }
        val _bool = if (mode == SLASH) {
          mode = NONE
          (nextChar: @switch) match {
            case '\r' =>
              mode = CONTINUE // Look for a following \n
              isFirstChar
            case '\u0085' | '\n' =>
              mode = IGNORE // Ignore whitespace on the next line
              isFirstChar
            case c @ ('b' | 'f' | 'n' | 'r' | 't') =>
              nextChar = nextCharMap(c)
              buf(offset) = nextChar
              offset += 1
              false
            case 'u' =>
              mode = UNICODE
              unicode = 0
              count = 0
              isFirstChar
            case _ =>
              buf(offset) = nextChar
              offset += 1
              false
          }
        } else {
          def fn(_nextChar: Char): Boolean = (_nextChar: @switch) match {
            case '#' | '!' if isFirstChar =>
              @tailrec def ignoreCharsTillEOL(tempVal: Char): Unit = {
                if (tempVal != 0xFFFF) { // -1.toChar
                  nextChar = tempVal.toChar
                  // not required
                  if (nextChar != '\r' && nextChar != '\n' && nextChar != '\u0085') {
                    ignoreCharsTillEOL(br.read().toChar)
                  }
                }
              }

              ignoreCharsTillEOL(br.read().toChar)
              isFirstChar
            case c @ ('\n' | '\u0085' | '\r') =>
              if (c == '\n' && mode == CONTINUE) { // Part of a \r\n sequence
                mode = IGNORE
                isFirstChar
              } else {
                mode = NONE
                if (offset > 0 || (offset == 0 && keyLength == 0)) {
                  if (keyLength == -1) keyLength = offset
                  val key   = new String(buf, 0, keyLength)
                  val value = new String(buf, keyLength, offset - keyLength)
                  put(key, value)
                }
                keyLength = -1
                offset = 0
                true
              }
            case '\\' =>
              if (mode == KEY_DONE) keyLength = offset
              mode = SLASH
              isFirstChar
            case ':' | '=' if keyLength == -1 =>
              // if parsing the key
              mode = NONE
              keyLength = offset
              isFirstChar
            case _ =>
              if (nextChar < 256 && Character.isWhitespace(nextChar)) {
                if (mode == CONTINUE) mode = IGNORE
                // if key length == 0 or value length == 0
                if (offset == 0 || offset == keyLength || mode == IGNORE)
                  isFirstChar
                else if (keyLength == -1) {
                  mode = KEY_DONE
                  isFirstChar
                } else {
                  if (mode == IGNORE || mode == CONTINUE) mode = NONE
                  else if (mode == KEY_DONE) {
                    keyLength = offset
                    mode = NONE
                  }
                  buf(offset) = nextChar
                  offset += 1
                  false
                }
              } else {
                if (mode == IGNORE || mode == CONTINUE) mode = NONE
                else if (mode == KEY_DONE) {
                  keyLength = offset
                  mode = NONE
                }
                buf(offset) = nextChar
                offset += 1
                false
              }
          }
          if (mode == UNICODE) {
            val digit = Character.digit(nextChar, 16)
            if (digit >= 0) {
              unicode = (unicode << 4) + digit
              count += 1
            } else if (count <= 4) {
              throw new IllegalArgumentException(
                "Invalid Unicode sequence: illegal character")
            }
            if (digit >= 0 && count < 4) {
              isFirstChar
            } else {
              mode = NONE
              buf(offset) = unicode.toChar
              offset += 1
              if (nextChar != '\n' && nextChar != '\u0085')
                isFirstChar
              else
                fn(nextChar)
            }
          } else {
            fn(nextChar)
          }
        }
        processNext(_bool)
      }
    }

    processNext(true)

  }

  private def writeComments(writer: Writer,
                            comments: String,
                            toHex: Boolean): Unit = {
    writer.write('#')
    val chars = comments.toCharArray
    var index = 0
    while (index < chars.length) {
      if (chars(index) < 256) {
        if (chars(index) == '\r' || chars(index) == '\n') {
          def indexPlusOne = index + 1
          // "\r\n"
          if (chars(index) == '\r'
              && indexPlusOne < chars.length
              && chars(indexPlusOne) == '\n') {
            index += 1
          }
          writer.write(System.lineSeparator)
          // return char with either '#' or '!' afterward
          if (indexPlusOne < chars.length
              && (chars(indexPlusOne) == '#'
              || chars(indexPlusOne) == '!')) {
            writer.write(chars(indexPlusOne))
            index += 1
          } else {
            writer.write('#')
          }

        } else {
          writer.write(chars(index))
        }
      } else {
        if (toHex) {
          writer.write(unicodeToHexaDecimal(chars(index)))
        } else {
          writer.write(chars(index))
        }
      }
      index += 1
    }
    writer.write(System.lineSeparator)
  }

  private def encodeString(string: String,
                           isKey: Boolean,
                           toHex: Boolean): String = {
    val buffer = new StringBuilder(200)
    var index  = 0
    val length = string.length
    // leading element (value) spaces are escaped
    while (!isKey && index < length && string.charAt(index) == ' ') {
      buffer.append("\\ ")
      index += 1
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
          if (toHex && (ch < ' ' || ch > '~')) {
            buffer.appendAll(unicodeToHexaDecimal(ch))
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
