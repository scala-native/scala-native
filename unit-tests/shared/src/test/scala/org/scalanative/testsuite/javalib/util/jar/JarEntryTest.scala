package org.scalanative.testsuite.javalib.util.jar

import java.util.jar._

// Ported from Apache Harmony

import org.junit.Ignore
import org.junit.Test
import org.junit.Assert._

import JarBytes._

class JarEntryTest {

  @Test def constructorJarEntry(): Unit = {
    val jarFile = getJarFile()
    val newJarEntry = new JarEntry(jarFile.getJarEntry(entryName))
    assertTrue(newJarEntry != null)
    jarFile.close()
  }

  @Test def constructorZipEntry(): Unit = {
    val jarFile = getJarFile()
    assertTrue(jarFile != null)
    val zipEntry = jarFile.getEntry(entryName)
    assertTrue(zipEntry != null)
    val jarEntry = new JarEntry(zipEntry)
    assertTrue(jarEntry != null)
    assertTrue(jarEntry.getName() == entryName)
    assertTrue(jarEntry.getSize() == 311)
    jarFile.close()
  }

  @Test def getAttributes(): Unit = {
    val attrJar = getAttJarFile()
    val attrsJarEntry = attrJar.getJarEntry(attEntryName)
    assertTrue(attrsJarEntry.getAttributes() != null)

    val noAttrsJarEntry = attrJar.getJarEntry(attEntryName2)
    assertTrue(noAttrsJarEntry.getAttributes() == null)
    attrJar.close()
  }

  @Ignore("#956")
  @Test def getCertificates(): Unit = {
    val jarFile = getJarFile()
    val zipEntry = jarFile.getEntry(entryName2)
    val jarEntry = new JarEntry(zipEntry)
    assertTrue(jarEntry.getCertificates() == null)
    jarFile.close()

    val signedJar = getSignedJarFile()
    val jarEntry1 = signedJar.getJarEntry("Test.class")
    val jarEntry2 = signedJar.getJarEntry("Test.class")
    val in = jarFile.getInputStream(jarEntry1)
    val buffer = new Array[Byte](1024)
    while (in.available() > 0) {
      assertTrue(jarEntry1.getCertificates() == null)
      assertTrue(jarEntry2.getCertificates() == null)
      in.read(buffer)
    }
    assertTrue(in.read() == -1)
    assertTrue(jarEntry1.getCertificates() != null)
    assertTrue(jarEntry2.getCertificates() != null)
    in.close()
    signedJar.close()
  }

  @Ignore("#956")
  @Test def getCodeSigners(): Unit = {
    val signedJar = getSignedJarFile()
    val jarEntry = signedJar.getJarEntry("Test.class")
    val in = signedJar.getInputStream(jarEntry)
    val buffer = new Array[Byte](1024)
    while (in.available > 0) {
      assertTrue(jarEntry.getCodeSigners() == null)
      in.read(buffer)
    }
    assertTrue(in.read() == -1)
    val codeSigners = jarEntry.getCodeSigners()
    assertTrue(codeSigners != null && codeSigners.length == 2)
    var certs_bob = codeSigners(0).getSignerCertPath().getCertificates()
    var certs_alice = codeSigners(1).getSignerCertPath().getCertificates()
    if (1 == certs_bob.size()) {
      val temp = certs_bob
      certs_bob = certs_alice
      certs_alice = temp
    }
    assertTrue(certs_bob.size() == 2)
    assertTrue(certs_alice.size() == 1)
    assertTrue(new JarEntry("aaa").getCodeSigners() == null)
    signedJar.close()
  }

  private def getJarFile(): JarFile =
    JarBytes.getJarFile(jarBytes)

  private def getAttJarFile(): JarFile =
    JarBytes.getJarFile(attJarBytes)

  private def getSignedJarFile(): JarFile =
    JarBytes.getJarFile(signedJarBytes)

  private val entryName = "foo/bar/A.class"
  private val entryName2 = "Blah.txt"
  private val attEntryName = "HasAttributes.txt"
  private val attEntryName2 = "NoAttributes.txt"

}
