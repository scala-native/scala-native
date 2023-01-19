package org.scalanative.testsuite.javalib.util.jar

// Ported from Apache Harmony

import java.util.jar._
import java.io.{File, FileOutputStream, IOException}
import java.util.zip.ZipEntry

import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class JarOutputStreamTest {

  @Test def constructorOutputStreamManifest(): Unit = {
    val fooJar = File.createTempFile("hyts_", ".jar")
    val barZip = File.createTempFile("hyts_", ".zip")

    val fos = new FileOutputStream(fooJar)

    val man = new Manifest()
    val att = man.getMainAttributes()
    att.put(Attributes.Name.MANIFEST_VERSION, "1.0")
    att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo")
    att.put(Attributes.Name.CLASS_PATH, barZip.getName())

    fos.close()
    assertThrows(classOf[IOException], new JarOutputStream(fos, man))

    assertThrows(classOf[NullPointerException], new JarOutputStream(fos, null))
  }

  @Test def constructorOutputStream(): Unit = {
    val fooJar = File.createTempFile("hyts_", ".jar")
    val fos = new FileOutputStream(fooJar)
    val ze = new ZipEntry("Test")

    val joutFoo = new JarOutputStream(fos)
    joutFoo.putNextEntry(ze)
    joutFoo.write(33)

    fos.close()
    fooJar.delete()

    assertThrows(
      classOf[IOException], {
        val joutFoo = new JarOutputStream(fos)
        joutFoo.putNextEntry(ze)
      }
    )
  }

}
