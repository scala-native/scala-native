package java.util.jar

// Ported from Apache Harmony

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}
import java.util.zip.{ZipConstants, ZipEntry, ZipInputStream}

class JarInputStream(in: InputStream, verify: Boolean)
    extends ZipInputStream(in) {
  def this(in: InputStream) = this(in, true)

  private var manifest: Manifest      = null
  private var eos: Boolean            = false
  private var mEntry: JarEntry        = null
  private var jarEntry: JarEntry      = null
  private var isMeta: Boolean         = false
  private var verifier: JarVerifier   = null
  private var verStream: OutputStream = null

  if (verify) {
    verifier = new JarVerifier("JarInputStream")
  }

  mEntry = getNextJarEntry()

  if (mEntry != null) {
    var name = mEntry.getName().map(JarFile.toASCIIUpperCase)
    if (name == JarFile.META_DIR) {
      mEntry = null // modifies behavior of getNextJarEntry()
      closeEntry()
      mEntry = getNextJarEntry()
      name = mEntry.getName().toUpperCase()
    }
    if (name == JarFile.MANIFEST_NAME) {
      mEntry = null
      manifest = new Manifest(this, verify)
      closeEntry()
      if (verify) {
        verifier.setManifest(manifest)
        if (manifest != null) {
          verifier.mainAttributesEnd = manifest.getMainAttributesEnd()
        }
      }
    } else {
      val temp = new Attributes(3)
      temp.getMap().put("hidden", null)
      mEntry.setAttributes(temp)
      /*
       * if not from the first entry, we will not get enough
       * information,so no verify will be taken out.
       */
      verifier = null
    }
  }

  def getManifest(): Manifest =
    manifest

  def getNextJarEntry(): JarEntry =
    getNextEntry().asInstanceOf[JarEntry]

  override def read(buffer: Array[Byte], offset: Int, length: Int): Int =
    if (mEntry != null) {
      -1
    } else {
      val r = super.read(buffer, offset, length)
      if (verStream != null && !eos) {
        if (r == -1) {
          eos = true
          if (verifier != null) {
            if (isMeta) {
              verifier.addMetaEntry(
                jarEntry.getName(),
                verStream.asInstanceOf[ByteArrayOutputStream].toByteArray())
              try verifier.readCertificates()
              catch { case e: SecurityException => verifier = null; throw e }
            } else {
              verStream.asInstanceOf[JarVerifier#VerifierEntry].verify()
            }
          }
        } else {
          verStream.write(buffer, offset, r)
        }
      }
      r
    }

  override def getNextEntry(): ZipEntry = {
    if (mEntry != null) {
      jarEntry = mEntry
      mEntry = null
      jarEntry.setAttributes(null)
      eos = false
      jarEntry
    } else {
      jarEntry = super.getNextEntry().asInstanceOf[JarEntry]
      if (jarEntry == null) {
        null
      } else {
        if (verifier != null) {
          isMeta = jarEntry
            .getName()
            .map(JarFile.toASCIIUpperCase)
            .startsWith(JarFile.META_DIR)
          if (isMeta) {
            verStream = new ByteArrayOutputStream()
          } else {
            verStream = verifier.initEntry(jarEntry.getName())
          }
        }
        eos = false
        jarEntry
      }
    }
  }

  override def createZipEntry(name: String): ZipEntry = {
    val entry = new JarEntry(name)
    if (manifest != null) {
      entry.setAttributes(manifest.getAttributes(name))
    }
    entry
  }

}

object JarInputStream extends ZipConstants
