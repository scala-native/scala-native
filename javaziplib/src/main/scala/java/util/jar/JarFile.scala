package java.util.jar

// Ported from Apache Harmony

import java.io.{
  ByteArrayOutputStream,
  File,
  FilterInputStream,
  IOException,
  InputStream
}
import java.util.{Enumeration, List}
import java.util.zip.{ZipConstants, ZipEntry, ZipFile}

class JarFile(file: File, verify: Boolean, mode: Int)
    extends ZipFile(file, mode) {
  def this(file: File, verify: Boolean) = this(file, verify, ZipFile.OPEN_READ)
  def this(file: File) = this(file, true)
  def this(name: String, verify: Boolean) = this(new File(name), verify)
  def this(name: String) = this(new File(name))

  private var manifest: Manifest         = null
  private var manifestEntry: ZipEntry    = null
  private[jar] var verifier: JarVerifier = null

  private var closed: Boolean = false

  readMetaEntries()

  override def entries(): Enumeration[JarEntry] = {
    class JarFileEnumerator(ze: Enumeration[_ <: ZipEntry], jf: JarFile)
        extends Enumeration[JarEntry] {
      override def hasMoreElements(): Boolean =
        ze.hasMoreElements()

      override def nextElement(): JarEntry = {
        val je = new JarEntry(ze.nextElement())
        je.parentJar = jf
        je
      }
    }
    new JarFileEnumerator(super.entries(), this)
  }

  def getJarEntry(name: String) =
    getEntry(name).asInstanceOf[JarEntry]

  def getManifest(): Manifest =
    if (closed) {
      throw new IllegalStateException("JarFile has been closed")
    } else if (manifest != null) {
      manifest
    } else {
      try {
        var is = super.getInputStream(manifestEntry)
        if (verifier != null) {
          verifier.addMetaEntry(manifestEntry.getName(),
                                JarFile.readFullyAndClose(is))
          is = super.getInputStream(manifestEntry)
        }
        try manifest = new Manifest(is, verifier != null)
        finally is.close()
        manifestEntry = null // Can discard the entry now.
      } catch {
        case _: NullPointerException => manifestEntry = null
      }
      manifest
    }

  private def readMetaEntries(): Unit = {
    val metaEntries = getMetaEntriesImpl()
    if (metaEntries == null) {
      verifier = null
    } else {
      var signed = false
      var i      = 0
      var done   = false
      while (!done && i < metaEntries.length) {
        val entry     = metaEntries(i)
        val entryName = entry.getName()
        // Is this the entry for META-INF/MANIFEST.MF ?
        if (manifestEntry == null && JarFile.asciiEqualsIgnoreCase(
              JarFile.MANIFEST_NAME,
              entryName)) {
          manifestEntry = entry
          // If there is no verifier then we don't need to look any further,
          if (verifier == null) {
            done = true
          }
        } else {
          // Is this an entry that the verifier needs?
          if (verifier != null && (JarFile.asciiEndsWithIgnoreCase(
                entryName,
                ".SF") || JarFile.asciiEndsWithIgnoreCase(entryName, ".DSA") || JarFile
                .asciiEndsWithIgnoreCase(entryName, ".RSA"))) {
            signed = true
            val is  = super.getInputStream(entry)
            val buf = JarFile.readFullyAndClose(is)
            verifier.addMetaEntry(entryName, buf)
          }
        }
        i += 1
      }
      if (!signed) {
        verifier = null
      }
    }
  }

  override def getInputStream(ze: ZipEntry): InputStream = {
    if (manifestEntry != null) {
      getManifest()
    }
    if (verifier != null) {
      verifier.setManifest(getManifest())
      if (manifest != null) {
        verifier.mainAttributesEnd = manifest.getMainAttributesEnd()
      }
      if (verifier.readCertificates()) {
        verifier.removeMetaEntries()
        if (manifest != null) {
          manifest.removeChunks()
        }
        if (!verifier.isSignedJar()) {
          verifier = null
        }
      }
    }
    val in = super.getInputStream(ze)
    if (in == null) {
      null
    } else if (verifier == null || ze.getSize() == -1) {
      in
    } else {
      val entry = verifier.initEntry(ze.getName())
      if (entry == null) {
        in
      } else {
        new JarFile.JarFileInputStream(in, ze, entry)
      }
    }
  }

  override def getEntry(name: String): ZipEntry = {
    val ze = super.getEntry(name)
    if (ze == null) {
      ze
    } else {
      val je = new JarEntry(ze)
      je.parentJar = this
      je
    }
  }

  override def close(): Unit = {
    super.close()
    closed = true
  }

  private def getMetaEntriesImpl(): Array[ZipEntry] = {
    val list       = scala.collection.mutable.Buffer.empty[ZipEntry]
    val allEntries = entries()
    while (allEntries.hasMoreElements()) {
      val ze = allEntries.nextElement()
      if (ze.getName().startsWith(JarFile.META_DIR) && ze
            .getName()
            .length() > JarFile.META_DIR.length()) {
        list += ze
      }
    }
    list.toArray
  }
}

object JarFile extends ZipConstants {
  final val MANIFEST_NAME         = "META-INF/MANIFEST.MF"
  private[jar] final val META_DIR = "META-INF/"

  private def readFullyAndClose(is: InputStream): Array[Byte] =
    try {
      // Initial read
      val buffer   = new Array[Byte](1024)
      val count    = is.read(buffer)
      val nextByte = is.read()

      // Did we get it all in one read?
      if (nextByte == -1) {
        val dest = new Array[Byte](count)
        System.arraycopy(buffer, 0, dest, 0, count)
        dest
      } else {
        // Requires additional reads
        val baos = new ByteArrayOutputStream(count * 2)
        baos.write(buffer, 0, count)
        baos.write(nextByte)
        var done = false
        while (!done) {
          val count = is.read(buffer)
          if (count == -1) {
            done = true
          } else {
            baos.write(buffer, 0, count)
          }
        }
        baos.toByteArray()
      }
    } finally is.close()

  private def asciiEndsWithIgnoreCase(source: String,
                                      suffix: String): Boolean = {
    val length = suffix.length()
    if (length > source.length()) {
      false
    } else {
      val offset = source.length() - length
      var i      = 0
      var result = true
      while (result && i < length) {
        val c1 = source.charAt(i + offset)
        val c2 = suffix.charAt(i)
        if (c1 != c2 && toASCIIUpperCase(c1) != toASCIIUpperCase(c2)) {
          result = false
        }
        i += 1
      }
      result
    }

  }

  private[jar] def asciiEqualsIgnoreCase(s1: String, s2: String): Boolean =
    if (s1 == null || s2 == null) {
      false
    } else if (s1 == s2) {
      true
    } else if (s1.length != s2.length) {
      false
    } else {
      var i      = 0
      var result = true
      while (result && i < s1.length) {
        val b1 = s1.charAt(i)
        val b2 = s2.charAt(i)
        if (b1 != b2 && toASCIIUpperCase(b1) != toASCIIUpperCase(b2)) {
          result = false
        }
        i += 1
      }
      result
    }

  def toASCIIUpperCase(c: Char): Char =
    if ('a' <= c && c <= 'z') {
      (c - ('a' - 'A')).toChar
    } else {
      c
    }

  private[jar] final class JarFileInputStream(is: InputStream,
                                              zipEntry: ZipEntry,
                                              entry: JarVerifier#VerifierEntry)
      extends FilterInputStream(is) {
    private var count: Long   = zipEntry.getSize()
    private var done: Boolean = false

    override def read(): Int =
      if (done) {
        -1
      } else if (count > 0) {
        val r = super.read()
        if (r != -1) {
          entry.write(r)
          count -= 1
        } else {
          count = 0
        }
        if (count == 0) {
          done = true
          entry.verify()
        }
        r
      } else {
        done = true
        entry.verify()
        -1
      }

    override def read(buf: Array[Byte], off: Int, nbytes: Int): Int =
      if (done) {
        -1
      } else {
        if (count > 0) {
          val r = super.read(buf, off, nbytes)
          if (r != -1) {
            var size = r
            if (count < size) {
              size = count.toInt
            }
            entry.write(buf, off, size)
            count -= size
          } else {
            count = 0
          }
          if (count == 0) {
            done = true
            entry.verify()
          }
          r
        } else {
          done = true
          entry.verify()
          -1
        }
      }

    override def available(): Int =
      if (done) 0
      else super.available()

    override def skip(nbytes: Long): Long = {
      var cnt  = 0L
      var rem  = 0L
      var done = false
      val buf  = new Array[Byte](Math.min(nbytes, 2048L).toInt)
      while (!done && cnt < nbytes) {
        val x = read(buf, 0, {
          rem = nbytes - cnt; if (rem > buf.length) buf.length else rem.toInt
        })
        if (x == -1) {
          done = true
        } else {
          cnt += x
        }
      }
      cnt
    }
  }
}
