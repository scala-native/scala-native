package java.util.jar

// Ported from Apache Harmony

import java.io.{ByteArrayOutputStream, FileOutputStream, InputStream}
import java.nio.file.Files
import java.util.zip.ZipEntry
import JarBytes._

object JarFileSuite extends tests.Suite {

  private def getJAR1() = getJarFile(hyts_patchBytes)
  private def getJAR2() = getJarFile(hyts_patch2Bytes)
  private def getJAR3() = getJarFile(hyts_manifest1Bytes)
  private def getJAR4() = getJarFile(hyts_signedBytes)
  private def getJAR5() = getJarFile(integrateBytes)

  private final val JAR1_ENTRY1       = "foo/bar/A.class"
  private final val JAR5_SIGNED_ENTRY = "Test.class"
  private final val JAR4_SIGNED_ENTRY = "coucou/FileAccess.class"
  private final val emptyEntry1       = "subfolder/internalSubset01.js";
  private final val emptyEntry2       = "svgtest.js";
  private final val emptyEntry3       = "svgunit.js";

  test("Constructor()") {
    assert(getJAR1().getEntry(JAR1_ENTRY1).getName() == JAR1_ENTRY1)
  }

  test("entries()") {
    val jarFile = getJAR1()
    val e       = jarFile.entries()
    var i       = 0
    while (e.hasMoreElements()) {
      e.nextElement()
      i += 1
    }
    assert(jarFile.size() == i)
    jarFile.close()
    assert(i == 6)
  }

  test("entriesIterator") {
    var jarFile     = getJAR1()
    var enumeration = jarFile.entries()
    jarFile.close()
    assertThrows[IllegalStateException] {
      enumeration.hasMoreElements()
    }

    jarFile = getJAR1()
    enumeration = jarFile.entries()
    jarFile.close()
    assertThrows[IllegalStateException] {
      enumeration.nextElement()
    }
  }

  test("getEntry(String)") {
    val jarFile = getJAR1()
    assert(jarFile.getEntry(JAR1_ENTRY1).getSize() == 311)

    var enumeration = jarFile.entries()
    assert(enumeration.hasMoreElements())
    while (enumeration.hasMoreElements()) {
      val je = enumeration.nextElement()
      jarFile.getEntry(je.getName())
    }

    enumeration = jarFile.entries()
    assert(enumeration.hasMoreElements())
    val je = enumeration.nextElement()
    jarFile.close()
    assertThrows[IllegalStateException] {
      jarFile.getEntry(je.getName)
    }
  }

  test("getJarEntry(String)") {
    val jarFile = getJAR1()
    assert(jarFile.getJarEntry(JAR1_ENTRY1).getSize() == 311)

    var enumeration = jarFile.entries()
    assert(enumeration.hasMoreElements())
    while (enumeration.hasMoreElements()) {
      val je = enumeration.nextElement()
      jarFile.getJarEntry(je.getName())
    }

    enumeration = jarFile.entries()
    assert(enumeration.hasMoreElements())
    val je = enumeration.nextElement()
    jarFile.close()
    assertThrows[IllegalStateException] {
      jarFile.getJarEntry(je.getName)
    }
  }

  // TODO: Enable this test once we support `JarOutputStream`:
  // test("getManifest()") {
  //   var jarFile = getJAR1()
  //   val is      = jarFile.getInputStream(jarFile.getEntry(JAR1_ENTRY1))
  //   assert(is.available() > 0)
  //   assert(jarFile.getManifest() != null)
  //   jarFile.close()
  //
  //   jarFile = getJAR2()
  //   assert(jarFile.getManifest() == null)
  //   jarFile.close()
  //
  //   jarFile = getJAR3()
  //   assert(jarFile.getManifest() != null)
  //   jarFile.close()
  //
  //   val manifest   = new Manifest()
  //   val attributes = manifest.getMainAttributes()
  //   attributes.put(new Attributes.Name("Manifest-Version"), "1.0")
  //   val manOut = new ByteArrayOutputStream()
  //   manifest.write(manOut)
  //   val manBytes = manOut.toByteArray()
  //   val file     = Files.createTempFile("hyts_manifest1", ".jar")
  //   val jarOut =
  //     new JarOutputStream(new FileOutputStream(file.toFile.getAbsolutePath()))
  //   var entry = new ZipEntry("META-INF/")
  //   entry.setSize(0)
  //   jarOut.putNextEntry(entry)
  //   entry = new ZipEntry(JarFile.MANIFEST_NAME)
  //   entry.setSize(manBytes.length)
  //   jarOut.putNextEntry(entry)
  //   jarOut.write(manBytes)
  //   entry = new ZipEntry("myfile")
  //   entry.setSize(1)
  //   jarOut.putNextEntry(entry)
  //   jarOut.write(65)
  //   jarOut.close()
  //   val jar = new JarFile(file.toFile.getAbsolutePath(), false)
  //   assert(jar.getManifest() != null)
  //   jar.close()
  //   Files.delete(file)
  //
  //   val jF = getJAR2()
  //   jF.close()
  //   assertThrows[IllegalStateException] {
  //     jF.getManifest()
  //   }
  // }

  test("getInputStream(ZipEntry)") {
    val jf = getJAR1()
    var is = jf.getInputStream(new JarEntry("invalid"))
    assert(is == null)

    is = jf.getInputStream(jf.getEntry(JAR1_ENTRY1))
    assert(is.available() > 0)

    // try to read class file header
    val b = new Array[Byte](1024)
    is.read(b, 0, 1024)
    jf.close()
    assert(b(0) == 0xCA.toByte)
    assert(b(1) == 0xFE.toByte)
    assert(b(2) == 0xBA.toByte)
    assert(b(3) == 0xBE.toByte)
  }

  testFails("input stream operations with signed files", issue = 956) {
    var jar   = getJAR4()
    var entry = new JarEntry(JAR4_SIGNED_ENTRY)
    var in    = jar.getInputStream(entry)
    in.read()

    // RI verifies only entries which appear via getJarEntry method
    jar = getJAR4()
    entry = jar.getJarEntry(JAR4_SIGNED_ENTRY)
    in = jar.getInputStream(entry)
    readExactly(in, entry.getSize().toInt - 1)
    assert(entry.getCertificates() == null)
    in.read()
    assert(entry.getCertificates() != null)
    assert(-1 == in.read())

    jar = getJAR4()
    entry = jar.getJarEntry(JAR4_SIGNED_ENTRY)
    entry.setSize(entry.getSize() - 1)
    in = jar.getInputStream(entry)
    readExactly(in, entry.getSize().toInt - 1)
    assert(entry.getCertificates() == null)
    assertThrows[SecurityException] {
      in.read()
    }
    assert(in.read() == -1)
  }

  test("JAR created with 1.4") {
    val jarFile = getJarFile(createdBy14Bytes)
    val entries = jarFile.entries()
    while (entries.hasMoreElements()) {
      val zipEntry = entries.nextElement()
      jarFile.getInputStream(zipEntry)
    }
  }

  test("jar verification") {
    // The jar is intact, then everything is alright
    val jarFile = getJAR5()
    val entries = jarFile.entries()
    while (entries.hasMoreElements()) {
      val zipEntry = entries.nextElement()
      jarFile.getInputStream(zipEntry)
    }
  }

  testFails("jar verification modified entry", issue = 956) {
    // The jar is instact, but the entry object is modified.
    var jarFile  = getJAR5()
    var zipEntry = jarFile.getJarEntry(JAR5_SIGNED_ENTRY)
    zipEntry.setSize(zipEntry.getSize() + 1)
    jarFile.getInputStream(zipEntry).skip(Long.MaxValue)

    jarFile = getJAR5()
    zipEntry = jarFile.getJarEntry(JAR5_SIGNED_ENTRY)
    zipEntry.setSize(zipEntry.getSize() - 1)

    assertThrows[SecurityException] {
      jarFile.getInputStream(zipEntry).read(new Array[Byte](5000), 0, 5000)
    }
  }

  test("jar file insert entry in manifest jar") {
    // If another entry is inserted into Manifest, no security exception will be
    // thrown out.
    val jarFile = getJarFile(insertedEntryManifestBytes)
    val entries = jarFile.entries()
    var count   = 0
    while (entries.hasMoreElements()) {
      val zipEntry = entries.nextElement()
      jarFile.getInputStream(zipEntry)
      count += 1
    }
    assert(count == 5)
  }

  testFails("jar file modified class", issue = 956) {
    // The content of Test.class is modified, jarFile.getInputStream will not
    // throw security Exception, but it will anytime before the inputStream got
    // from getInputStream method has been read to end.
    val path = Files.createTempFile("jarfile", ".jar")
    Files.write(path, modifiedClassBytes)
    val jarFile = new JarFile(path.toFile, true)
    val entries = jarFile.entries()
    while (entries.hasMoreElements()) {
      val zipEntry = entries.nextElement()
      jarFile.getInputStream(zipEntry)
    }
    // The content of Test.class has been tampered.
    val zipEntry = jarFile.getEntry("Test.class")
    val in       = jarFile.getInputStream(zipEntry)
    val buffer   = new Array[Byte](1024)
    assertThrows[SecurityException] {
      while (in.available() > 0) {
        in.read(buffer)
      }
    }
  }

  testFails("jar file modified manifest main attributes", issue = 956) {
    // In the Modified.jar, the main attributes of META-INF/MANIFEST.MF is
    // tampered manually. Hence the RI 5.0 JarFile.getInputStram of any
    // JarEntry will throw security exception.
    val path = Files.createTempFile("jarfile", ".jar")
    Files.write(path, modifiedManifestMainAttributesBytes)
    val jarFile = new JarFile(path.toFile, true)
    val entries = jarFile.entries()
    while (entries.hasMoreElements()) {
      val zipEntry = entries.nextElement()
      jarFile.getInputStream(zipEntry)
    }
    // The content of Test.class has been tampered.
    val zipEntry = jarFile.getEntry("Test.class")
    val in       = jarFile.getInputStream(zipEntry)
    val buffer   = new Array[Byte](1024)
    assertThrows[SecurityException] {
      while (in.available() > 0) {
        in.read(buffer)
      }
    }
  }

  testFails("jar file modified manifest entry attributes", issue = 956) {
    // It is all right in our origian lJarFile. If the Entry Attributes, for
    // example Test.class in our jar, the jarFile.getInputStream will throw
    // Security Exception.
    val path = Files.createTempFile("jarfile", ".jar")
    Files.write(path, modifiedManifestEntryAttributesBytes)
    val jarFile = new JarFile(path.toFile, true)
    val entries = jarFile.entries()
    while (entries.hasMoreElements()) {
      val zipEntry = entries.nextElement()
      assertThrows[SecurityException] {
        jarFile.getInputStream(zipEntry)
      }
    }
  }

  testFails("jar file modified sf entry attributes", issue = 956) {
    // If the content of the .SA file is modified, no matter what it resides,
    // JarFile.getInputStream of any JarEntry will trop SecurityException()
    val path = Files.createTempFile("jarfile", ".jar")
    Files.write(path, modifiedSFEntryAttributesBytes)
    val jarFile = new JarFile(path.toFile, true)
    val entries = jarFile.entries()
    while (entries.hasMoreElements()) {
      val zipEntry = entries.nextElement()
      assertThrows[SecurityException] {
        jarFile.getInputStream(zipEntry)
      }
    }
  }

  test("getInputStream(JarEntry)") {
    var jf = getJAR1()
    var is = jf.getInputStream(jf.getEntry(JAR1_ENTRY1))
    assert(is.available() > 0)

    val buffer = new Array[Byte](1024)
    val r      = is.read(buffer, 0, 1024)
    jf.close()
    is.close()

    val sb = new StringBuilder()
    var i  = 0
    while (i < r) {
      sb.append((buffer(i) & 0xFF).toChar)
      i += 1
    }
    val contents = sb.toString()
    assert(contents.indexOf("foo") > 0)
    assert(contents.indexOf("bar") > 0)

    assertThrows[IllegalStateException] {
      jf.getInputStream(jf.getEntry(JAR1_ENTRY1))
    }

    jf = getJAR1()
    is = jf.getInputStream(new JarEntry("invalid"))
    assert(is == null)
    jf.close()
  }

  test("jar verification empty entry") {
    val path = Files.createTempFile("jarfile", ".jar")
    Files.write(path, emptyEntriesSignedBytes)
    val jarFile = new JarFile(path.toFile)

    var zipEntry = jarFile.getJarEntry(emptyEntry1)
    var res =
      jarFile.getInputStream(zipEntry).read(new Array[Byte](100), 0, 100)
    assert(res == -1)

    zipEntry = jarFile.getJarEntry(emptyEntry2)
    res = jarFile.getInputStream(zipEntry).read(new Array[Byte](100), 0, 100)
    assert(res == -1)

    zipEntry = jarFile.getJarEntry(emptyEntry3)
    res = jarFile.getInputStream(zipEntry).read()
    assert(res == -1)
  }

  test("jar written with flush") {
    val path = Files.createTempFile("jarfile", ".jar")
    Files.write(path, hyts_flushedBytes)

    // Used to crash with ZipException: Central Directory Entry not found
    try new JarFile(path.toFile)
    catch { case e: Exception => println(e); e.printStackTrace }
  }

  private def readExactly(in: InputStream, _numBytes: Int): Unit = {
    var numBytes = _numBytes
    val buffer   = new Array[Byte](1024)
    while (numBytes > 0) {
      val read = in.read(buffer, 0, Math.min(numBytes, 1024))
      assert(read != -1)
      numBytes -= read
    }
  }
}
