package java.util.jar

// Ported from Apache Harmony

import java.io.IOException
import java.nio.{ByteBuffer, CharBuffer}
import java.nio.charset.{CoderResult, StandardCharsets}
import java.util.Map

class InitManifest private[jar] (buf: Array[Byte],
                                 main: Attributes,
                                 ver: Attributes.Name) {

  private var pos: Int                   = 0
  private[jar] var linebreak             = 0
  private[jar] var name: Attributes.Name = null
  private[jar] var value: String         = null
  private[jar] val decoder               = StandardCharsets.UTF_8.newDecoder()
  private[jar] var cBuf                  = CharBuffer.allocate(4096)

  if (!readHeader() || (ver != null && !name.equals(ver))) {
    throw new IOException(s"Missing version attribute: $ver")
  }
  main.put(name, value)
  while (readHeader()) {
    main.put(name, value)
  }

  private[jar] def initEntries(entries: Map[String, Attributes],
                               chunks: Map[String, Manifest.Chunk]): Unit = {
    var mark = pos
    while (readHeader()) {
      if (!Attributes.Name.NAME.equals(name)) {
        throw new IOException("Entry is not named")
      }
      val entryNameValue = value
      var entry          = entries.get(entryNameValue)
      if (entry == null) {
        entry = new Attributes(12)
      }
      while (readHeader()) {
        entry.put(name, value)
      }

      if (chunks != null) {
        if (chunks.get(entryNameValue) != null) {
          // TODO A buf: There might be several verification chunks for
          // the same name. I believe they should be used to update
          // signature in order of appearance; there are two ways to fix
          // this: either use a list of chunks, or decide on used
          // signature algorithm in advance and reread the chunks while
          // updating the signature; for now a defensive error is thrown
          throw new IOException(
            "A jar verifier does not support more than one entry with the same name")
        }
        chunks.put(entryNameValue, new Manifest.Chunk(mark, pos))
        mark = pos
      }
      entries.put(entryNameValue, entry)
    }
  }

  private[jar] def getPos(): Int =
    pos

  private def readHeader(): Boolean =
    if (linebreak > 1) {
      // break a section on an empty line
      linebreak = 0
      false
    } else {
      readName()
      linebreak = 0
      readValue()
      linebreak > 0
    }

  private def wrap(mark: Int, pos: Int): Array[Byte] = {
    val buffer = new Array[Byte](pos - mark)
    System.arraycopy(buf, mark, buffer, 0, pos - mark)
    buffer
  }

  private def readName(): Unit = {
    var i    = 0
    var mark = pos

    while (pos < buf.length) {
      val b = buf(pos)
      pos += 1

      if (b == ':') {
        val nameBuffer = wrap(mark, pos - 1)

        pos += 1
        if (buf(pos - 1) != ' ') {
          throw new IOException(s"Invalid attribute $nameBuffer")
        }

        name = new Attributes.Name(nameBuffer)
        return
      }

      if (!((b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z') || b == '_' || b == '-' || (b >= '0' && b <= '9'))) {
        throw new IOException(s"Invalid attribute $b")
      }

    }
    if (i > 0) {
      throw new IOException(s"Invalid attribute ${wrap(mark, buf.length)}")
    }
  }

  private def readValue(): Unit = {
    var next: Byte = 0
    var lastCr     = false
    var mark       = pos
    var last       = pos
    var done       = false

    decoder.reset()
    cBuf.clear()

    while (!done && pos < buf.length) {
      next = buf(pos)
      pos += 1

      next match {
        case 0 =>
          throw new IOException("NUL character in a manifest")
        case '\n' =>
          if (lastCr) {
            lastCr = false
          } else {
            linebreak += 1
          }
        case '\r' =>
          lastCr = true
          linebreak += 1
        case ' ' if linebreak == 1 =>
          decode(mark, last, false)
          mark = pos
          last = mark
          linebreak = 0
        case _ =>
          if (linebreak >= 1) {
            pos -= 1
            done = true
          } else {
            last = pos
          }
      }

    }

    decode(mark, last, true)
    while (CoderResult.OVERFLOW == decoder.flush(cBuf)) {
      enlargeBuffer()
    }
    value = new String(cBuf.array(), cBuf.arrayOffset(), cBuf.position())
  }

  private def decode(mark: Int, pos: Int, endOfInput: Boolean): Unit = {
    val bBuf = ByteBuffer.wrap(buf, mark, pos - mark)
    while (CoderResult.OVERFLOW == decoder.decode(bBuf, cBuf, endOfInput)) {
      enlargeBuffer()
    }
  }

  private def enlargeBuffer(): Unit = {
    val newBuf = CharBuffer.allocate(cBuf.capacity() * 2)
    newBuf.put(cBuf.array(), cBuf.arrayOffset(), cBuf.position())
    cBuf = newBuf
  }
}
