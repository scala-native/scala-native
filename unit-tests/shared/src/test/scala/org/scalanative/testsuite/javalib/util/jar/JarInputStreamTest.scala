package org.scalanative.testsuite.javalib.util.jar

// Ported from Apache Harmony

import java.util.jar._
import java.io.{ByteArrayInputStream, IOException}
import java.util.zip.{ZipEntry, ZipException}

import org.junit.Ignore
import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import JarBytes._

class JarInputStreamTest {

  private val entryName = "foo/bar/A.class"

  @Test def constructorInputStream(): Unit = {
    val is = new ByteArrayInputStream(hyts_patchBytes)
    var hasCorrectEntry = false
    val jis = new JarInputStream(is)
    assertTrue(jis.getManifest() != null)
    var je = jis.getNextJarEntry()
    while (je != null) {
      if (je.getName() == entryName) {
        hasCorrectEntry = true
      }
      je = jis.getNextJarEntry()
    }
    assertTrue(hasCorrectEntry)
  }

  @Test def closeAfterException(): Unit = {
    val is = new ByteArrayInputStream(brokenEntryBytes)
    val jis = new JarInputStream(is, false)
    jis.getNextEntry()
    assertThrows(classOf[ZipException], jis.getNextEntry())
    jis.close()
    assertThrows(classOf[IOException], jis.getNextEntry())
  }

  @Test def getNextJarEntryEx(): Unit = {
    val desired = Set("foo/", "foo/bar/", "foo/bar/A.class", "Blah.txt")
    val actual = scala.collection.mutable.Set.empty[String]
    var is = new ByteArrayInputStream(hyts_patchBytes)
    var jis = new JarInputStream(is)
    var je = jis.getNextJarEntry()
    while (je != null) {
      actual.add(je.toString())
      je = jis.getNextJarEntry()
    }
    assertTrue(actual == desired)
    jis.close()

    assertThrows(classOf[IOException], jis.getNextJarEntry())

    is = new ByteArrayInputStream(brokenEntryBytes)
    jis = new JarInputStream(is, false)
    jis.getNextJarEntry()
    assertThrows(classOf[ZipException], jis.getNextJarEntry())
  }

  @Test def getManifest(): Unit = {
    var is = new ByteArrayInputStream(hyts_patch2Bytes)
    var jis = new JarInputStream(is)
    var m = jis.getManifest()
    assertTrue(m == null)

    is = new ByteArrayInputStream(hyts_patchBytes)
    jis = new JarInputStream(is)
    m = jis.getManifest()
    assertTrue(m != null)
  }

  @Test def getNextJarEntry(): Unit = {
    val desired = Set("foo/", "foo/bar/", "foo/bar/A.class", "Blah.txt")
    val actual = scala.collection.mutable.Set.empty[String]
    val is = new ByteArrayInputStream(hyts_patchBytes)
    val jis = new JarInputStream(is)
    var je = jis.getNextJarEntry()
    while (je != null) {
      actual.add(je.toString())
      je = jis.getNextJarEntry()
    }
    assertTrue(actual == desired)
  }

  @Test def getNextEntryOnIntegrateJar(): Unit = {
    val is = new ByteArrayInputStream(integrateBytes)
    val jis = new JarInputStream(is, true)
    var entry: ZipEntry = null
    var count = 0
    while (count == 0 || entry != null) {
      count += 1
      entry = jis.getNextEntry()
    }
    assertTrue(count == 5)
    jis.close()
  }

  // @Ignore("#956")
  // @Test def getNextEntryOnModifiedClassJar(): Unit = {
  //   val is = new ByteArrayInputStream(modifiedClassBytes)
  //   val jis = new JarInputStream(is, true)
  //   var zipEntry: ZipEntry = null
  //   val indexOfTestClass = 4
  //   var count = 0
  //   while (count == 0 || zipEntry != null) {
  //     count += 1
  //     try {
  //       zipEntry = jis.getNextEntry()
  //       if (count == indexOfTestClass + 1) {
  //         assertTrue(false) // should have thrown Security Exception
  //       }
  //     } catch {
  //       case e: SecurityException if count == indexOfTestClass + 1 =>
  //       // expected
  //     }
  //   }
  //   assertTrue(count == 6)
  //   jis.close()
  // }

  // @Ignore("#956")
  // @Test def getNextEntryOnModifiedMainAttributesJar(): Unit = {
  //   val is = new ByteArrayInputStream(modifiedManifestMainAttributesBytes)
  //   val jis = new JarInputStream(is, true)
  //   assertTrue(jis.getNextEntry().getName() == "META-INF/TESTROOT.SF")
  //   assertTrue(jis.getNextEntry().getName() == "META-INF/TESTROOT.DSA")
  //   assertThrows(classOf[SecurityException], jis.getNextEntry())
  //   assertTrue(jis.getNextEntry().getName() == "META-INF/")
  //   assertTrue(jis.getNextEntry().getName() == "Test.class")
  //   jis.close()
  // }

  // @Ignore("#956")
  // @Test def getNextEntryOnModifiedManifestEntryAttributesJar(): Unit = {
  //   val is = new ByteArrayInputStream(modifiedManifestEntryAttributesBytes)
  //   val jis = new JarInputStream(is, true)
  //   var zipEntry: ZipEntry = null
  //   var count = 0
  //   val indexofDSA = 2
  //   while (count == 0 || zipEntry != null) {
  //     count += 1
  //     try {
  //       zipEntry = jis.getNextEntry()
  //       if (count == indexofDSA + 1) {
  //         assertTrue(false) // Should have throws Security Exception
  //       }
  //     } catch {
  //       case _: SecurityException if count == indexofDSA + 1 =>
  //       // expected
  //     }
  //   }
  //   assertTrue(count == 5)
  //   jis.close()
  // }

  // @Ignore("#956")
  // @Test def getNextEntryOnModifiedSfEntryAttributesJar(): Unit = {
  //   val is = new ByteArrayInputStream(modifiedSFEntryAttributesBytes)
  //   val jis = new JarInputStream(is, true)
  //   var zipEntry: ZipEntry = null
  //   var count = 0
  //   val indexofDSA = 2
  //   while (count == 0 || zipEntry != null) {
  //     count += 1
  //     try {
  //       zipEntry = jis.getNextEntry()
  //       if (count == indexofDSA + 1) {
  //         assertTrue(false) // Should have throws Security Exception
  //       }
  //     } catch {
  //       case _: SecurityException if count == indexofDSA + 1 =>
  //       // expected
  //     }
  //   }
  //   assertTrue(count == 5)
  //   jis.close()
  // }

  // @Ignore("#956")
  // @Test def readModifiedClassJar(): Unit = {
  //   val is = new ByteArrayInputStream(modifiedClassBytes)
  //   val jis = new JarInputStream(is, true)
  //   val indexOfTestClass = 4
  //   var count = 0
  //   var zipEntry: ZipEntry = null
  //   while (count == 0 || zipEntry != null) {
  //     count += 1
  //     zipEntry = jis.getNextEntry()
  //     val buffer = new Array[Byte](1024)
  //     try {
  //       var length = 0
  //       while (length >= 0) {
  //         length = jis.read(buffer)
  //       }
  //       if (count == indexOfTestClass) {
  //         assertTrue(false) // should have thrown Security Exception
  //       }
  //     } catch {
  //       case _: SecurityException if count == indexOfTestClass =>
  //       // expected
  //     }
  //   }
  //   assertTrue(count == 5)
  //   jis.close()
  // }

  @Test def readIntegrateJar(): Unit = {
    val is = new ByteArrayInputStream(integrateBytes)
    val jis = new JarInputStream(is)
    var count = 0
    var zipEntry: ZipEntry = null
    while (count == 0 || zipEntry != null) {
      count += 1
      zipEntry = jis.getNextEntry()
      val buffer = new Array[Byte](1024)
      var length = 0
      while (length >= 0) {
        length = jis.read(buffer)
      }
    }
    assertTrue(count == 5)
    jis.close()
  }

  // @Ignore("#956")
  // @Test def readModifiedManifestMainAttributesJar(): Unit = {
  //   val is = new ByteArrayInputStream(modifiedManifestMainAttributesBytes)
  //   val jis = new JarInputStream(is)
  //   val indexofDSA = 2
  //   var count = 0
  //   var zipEntry: ZipEntry = null
  //   while (count == 0 || zipEntry != null) {
  //     count += 1
  //     zipEntry = jis.getNextEntry()
  //     val buffer = new Array[Byte](1024)
  //     try {
  //       var length = 0
  //       while (length >= 0) {
  //         length = jis.read(buffer)
  //       }
  //       if (count == indexofDSA) {
  //         assertTrue(false) // should have throws Security Exception
  //       }
  //     } catch {
  //       case _: SecurityException if count == indexofDSA =>
  //       // expected
  //     }
  //   }
  //   assertTrue(count == 5)
  //   jis.close()
  // }

  // @Ignore("#956")
  // @Test def readModifiedSfEntryAttributesJar(): Unit = {
  //   val is = new ByteArrayInputStream(modifiedSFEntryAttributesBytes)
  //   val jis = new JarInputStream(is)
  //   val indexofDSA = 2
  //   var count = 0
  //   var zipEntry: ZipEntry = null
  //   while (count == 0 || zipEntry != null) {
  //     count += 1
  //     zipEntry = jis.getNextEntry()
  //     val buffer = new Array[Byte](1024)
  //     try {
  //       var length = 0
  //       while (length >= 0) {
  //         length = jis.read(buffer)
  //       }
  //       if (count == indexofDSA) {
  //         assertTrue(false) // should have thrown Security Exception
  //       }
  //     } catch {
  //       case _: SecurityException if count == indexofDSA =>
  //       // expected
  //     }
  //   }
  //   assertTrue(count == 5)
  //   jis.close()
  // }

  @Test def getNextEntryOnBrokenEntryJar(): Unit = {
    val is = new ByteArrayInputStream(brokenEntryBytes)
    val jis = new JarInputStream(is)
    jis.getNextEntry()
    assertThrows(classOf[ZipException], jis.getNextEntry())

    assertThrows(
      classOf[IOException], {
        jis.close() // Android throws exception here, already!
        jis.getNextEntry() // But RI here, only!
      }
    )
  }

}
