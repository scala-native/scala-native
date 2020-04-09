package java.util.jar

// Ported from Apache Harmony

import JarBytes._

object JarEntrySuite extends tests.Suite {

  test("Constructor(JarEntry)") {
    val jarFile     = getJarFile()
    val newJarEntry = new JarEntry(jarFile.getJarEntry(entryName))
    assert(newJarEntry != null)
    jarFile.close()
  }

  test("Constructor(ZipEntry)") {
    val jarFile = getJarFile()
    assert(jarFile != null)
    val zipEntry = jarFile.getEntry(entryName)
    assert(zipEntry != null)
    val jarEntry = new JarEntry(zipEntry)
    assert(jarEntry != null)
    assert(jarEntry.getName() == entryName)
    assert(jarEntry.getSize() == 311)
    jarFile.close()
  }

  test("getAttributes()") {
    val attrJar       = getAttJarFile()
    val attrsJarEntry = attrJar.getJarEntry(attEntryName)
    assert(attrsJarEntry.getAttributes() != null)

    val noAttrsJarEntry = attrJar.getJarEntry(attEntryName2)
    assert(noAttrsJarEntry.getAttributes() == null)
    attrJar.close()
  }

  testFails("getCertificates()", issue = 956) {
    val jarFile  = getJarFile()
    val zipEntry = jarFile.getEntry(entryName2)
    val jarEntry = new JarEntry(zipEntry)
    assert(jarEntry.getCertificates() == null)
    jarFile.close()

    val signedJar = getSignedJarFile()
    val jarEntry1 = signedJar.getJarEntry("Test.class")
    val jarEntry2 = signedJar.getJarEntry("Test.class")
    val in        = jarFile.getInputStream(jarEntry1)
    val buffer    = new Array[Byte](1024)
    while (in.available() > 0) {
      assert(jarEntry1.getCertificates() == null)
      assert(jarEntry2.getCertificates() == null)
      in.read(buffer)
    }
    assert(in.read() == -1)
    assert(jarEntry1.getCertificates() != null)
    assert(jarEntry2.getCertificates() != null)
    in.close()
    signedJar.close()
  }

  testFails("getCodeSigners()", issue = 956) {
    val signedJar = getSignedJarFile()
    val jarEntry  = signedJar.getJarEntry("Test.class")
    val in        = signedJar.getInputStream(jarEntry)
    val buffer    = new Array[Byte](1024)
    while (in.available > 0) {
      assert(jarEntry.getCodeSigners() == null)
      in.read(buffer)
    }
    assert(in.read() == -1)
    val codeSigners = jarEntry.getCodeSigners()
    assert(codeSigners != null && codeSigners.length == 2)
    var certs_bob   = codeSigners(0).getSignerCertPath().getCertificates()
    var certs_alice = codeSigners(1).getSignerCertPath().getCertificates()
    if (1 == certs_bob.size()) {
      val temp = certs_bob
      certs_bob = certs_alice
      certs_alice = temp
    }
    assert(certs_bob.size() == 2)
    assert(certs_alice.size() == 1)
    assert(new JarEntry("aaa").getCodeSigners() == null)
    signedJar.close()
  }

  private def getJarFile(): JarFile =
    JarBytes.getJarFile(jarBytes)

  private def getAttJarFile(): JarFile =
    JarBytes.getJarFile(attJarBytes)

  private def getSignedJarFile(): JarFile =
    JarBytes.getJarFile(signedJarBytes)

  private val entryName     = "foo/bar/A.class"
  private val entryName2    = "Blah.txt"
  private val attEntryName  = "HasAttributes.txt"
  private val attEntryName2 = "NoAttributes.txt"

}
