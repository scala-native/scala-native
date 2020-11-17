package java.util.jar

// Ported from Apache Harmony

import java.io.OutputStream
import java.util.zip.{ZipEntry, ZipOutputStream}

class JarOutputStream(out: OutputStream) extends ZipOutputStream(out) {

  private var manifest: Manifest = null

  def this(out: OutputStream, mf: Manifest) = {
    this(out)
    if (mf == null) {
      throw new NullPointerException()
    } else {
      manifest = mf
      val ze = new ZipEntry(JarFile.MANIFEST_NAME)
      putNextEntry(ze)
      manifest.write(this)
      closeEntry()
    }
  }

  override def putNextEntry(ze: ZipEntry): Unit =
    super.putNextEntry(ze)
}
