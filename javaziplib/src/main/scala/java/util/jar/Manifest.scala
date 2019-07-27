package java.util.jar

// Ported from Apache Harmony

import java.io.{ByteArrayOutputStream, IOException, InputStream, OutputStream}
import java.nio.{ByteBuffer, CharBuffer}
import java.nio.charset.{CharsetEncoder, CoderResult, StandardCharsets}
import java.util.{HashMap, Map}

class Manifest() extends Cloneable {

  private var mainAttributes: Attributes = new Attributes()
  private var entries: Map[String, Attributes] =
    new HashMap[String, Attributes]
  private var chunks: Map[String, Manifest.Chunk] = null
  private var im: InitManifest                    = null
  private var mainEnd: Int                        = 0

  def this(is: InputStream) = {
    this()
    read(is)
  }

  def this(man: Manifest) = {
    this()
    mainAttributes = man.mainAttributes.clone().asInstanceOf[Attributes]
    entries = man
      .getEntries()
      .asInstanceOf[HashMap[String, Attributes]]
      .clone()
      .asInstanceOf[HashMap[String, Attributes]]
  }

  private[jar] def this(is: InputStream, readChunks: Boolean) = {
    this()
    if (readChunks) {
      chunks = new HashMap[String, Manifest.Chunk]
    }
    read(is)
  }

  def clear(): Unit = {
    im = null
    entries.clear()
    mainAttributes.clear()
  }

  def getAttributes(name: String): Attributes =
    getEntries().get(name)

  def getEntries(): Map[String, Attributes] =
    entries

  def getMainAttributes(): Attributes =
    mainAttributes

  override def clone(): Object =
    new Manifest(this)

  def write(os: OutputStream): Unit =
    Manifest.write(this, os)

  def read(is: InputStream): Unit = {
    val buf: Array[Byte] = Manifest.readFully(is)
    if (buf.length != 0) {
      // replace EOF and NUL with another new line
      val b = buf(buf.length - 1)
      if (0 == b || 26 == b) {
        buf(buf.length - 1) = '\n'
      }

      // Attributes.Name.MANIFEST_VERSION is not used for
      // the second parameter for RI compatibility
      im = new InitManifest(buf, mainAttributes, null)
      mainEnd = im.getPos()
      // FIXME (from Apache Harmony, no more details)
      im.initEntries(entries, chunks)
      im = null
    }
  }

  override def hashCode(): Int =
    mainAttributes.hashCode() ^ getEntries().hashCode()

  override def equals(o: Any): Boolean =
    o match {
      case other: Manifest =>
        mainAttributes == other.mainAttributes && getEntries() == other
          .getEntries()
      case _ =>
        false
    }

  private[jar] def getChunk(name: String): Manifest.Chunk =
    chunks.get(name)

  private[jar] def removeChunks(): Unit =
    chunks = null

  private[jar] def getMainAttributesEnd(): Int =
    mainEnd
}

object Manifest {
  private[jar] class Chunk(val start: Int, val end: Int)

  private[jar] final val LINE_LENGTH_LIMIT: Int  = 72
  private final val LINE_SEPARATOR: Array[Byte]  = Array[Byte]('\r', '\n')
  private final val VALUE_SEPARATOR: Array[Byte] = Array[Byte](':', ' ')
  private final val NAME_ATTRIBUTE: Attributes.Name =
    new Attributes.Name("Name")

  private[jar] def write(manifest: Manifest, out: OutputStream): Unit = {
    val encoder = StandardCharsets.UTF_8.newEncoder()
    val buffer  = ByteBuffer.allocate(4096)

    val version =
      manifest.mainAttributes.getValue(Attributes.Name.MANIFEST_VERSION)
    if (version != null) {
      writeEntry(out,
                 Attributes.Name.MANIFEST_VERSION,
                 version,
                 encoder,
                 buffer)
      val entries = manifest.mainAttributes.keySet().iterator()
      while (entries.hasNext()) {
        val name = entries.next().asInstanceOf[Attributes.Name]
        if (name != Attributes.Name.MANIFEST_VERSION) {
          writeEntry(out,
                     name,
                     manifest.mainAttributes.getValue(name),
                     encoder,
                     buffer)
        }
      }
    }
    out.write(LINE_SEPARATOR)
    val i = manifest.getEntries.keySet.iterator
    while (i.hasNext) {
      val key = i.next()
      writeEntry(out, NAME_ATTRIBUTE, key, encoder, buffer)
      val attrib  = manifest.entries.get(key)
      val entries = attrib.keySet().iterator()
      while (entries.hasNext()) {
        val name = entries.next().asInstanceOf[Attributes.Name]
        writeEntry(out, name, attrib.getValue(name), encoder, buffer)
      }
      out.write(LINE_SEPARATOR)
    }
  }

  private def writeEntry(os: OutputStream,
                         name: Attributes.Name,
                         value: String,
                         encoder: CharsetEncoder,
                         bBuf: ByteBuffer): Unit = {
    val out = name.getBytes()
    if (out.length > LINE_LENGTH_LIMIT) {
      throw new IOException(
        s"""A length of the encoded header name "$name" exceeded maximum length $LINE_LENGTH_LIMIT""")
    } else {
      os.write(out)
      os.write(VALUE_SEPARATOR)

      encoder.reset()
      bBuf.clear().limit(LINE_LENGTH_LIMIT - out.length - 2)

      val cBuf = CharBuffer.wrap(value)
      var done = false
      while (!done) {
        var r = encoder.encode(cBuf, bBuf, true)
        if (CoderResult.UNDERFLOW == r) {
          r = encoder.flush(bBuf)
        }
        os.write(bBuf.array(), bBuf.arrayOffset(), bBuf.position())
        os.write(LINE_SEPARATOR)
        if (CoderResult.UNDERFLOW == r) {
          done = true
        } else {
          os.write(' ')
          bBuf.clear().limit(LINE_LENGTH_LIMIT - 1)
        }
      }
    }
  }

  private def readFully(is: InputStream): Array[Byte] = {
    // Initial read
    val buffer   = new Array[Byte](4096)
    var count    = is.read(buffer)
    val nextByte = is.read()

    // Did we get it all in one read?
    if (nextByte == -1) {
      val dest = new Array[Byte](count)
      System.arraycopy(buffer, 0, dest, 0, count)
      dest
    } else {
      // Does it look like a manifest?
      if (!containsLine(buffer, count)) {
        throw new IOException("Manifest is too long")
      } else {
        val baos = new ByteArrayOutputStream(count * 2)
        var done = false
        baos.write(buffer, 0, count)
        baos.write(nextByte)
        while (!done) {
          count = is.read(buffer)
          if (count == -1) {
            done = true
          } else {
            baos.write(buffer, 0, count)
          }
        }
        baos.toByteArray()
      }
    }
  }

  private def containsLine(buffer: Array[Byte], length: Int) = {
    var i      = 0
    var result = false
    while (!result && i < length) {
      if (buffer(i) == 0x0A || buffer(i) == 0x0D) {
        result = true
      }
      i += 1
    }
    result
  }

}
