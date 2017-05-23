package java.util.jar

// Ported from Apache Harmony

import java.io.{ByteArrayInputStream, IOException}
import java.util.zip.{ZipEntry, ZipException}
import JarBytes._

object JarInputStreamSuite extends tests.Suite {

  private val entryName = "foo/bar/A.class"

  test("Constructor(InputStream)") {
    val is              = new ByteArrayInputStream(hyts_patchBytes)
    var hasCorrectEntry = false
    val jis             = new JarInputStream(is)
    assert(jis.getManifest() != null)
    var je = jis.getNextJarEntry()
    while (je != null) {
      if (je.getName() == entryName) {
        hasCorrectEntry = true
      }
      je = jis.getNextJarEntry()
    }
    assert(hasCorrectEntry)
  }

  test("Close after exception") {
    val is  = new ByteArrayInputStream(brokenEntryBytes)
    val jis = new JarInputStream(is, false)
    jis.getNextEntry()
    assertThrows[ZipException] {
      jis.getNextEntry()
    }
    jis.close()
    assertThrows[IOException] {
      jis.getNextEntry()
    }
  }

  test("getNextJarEntry_Ex") {
    val desired = Set("foo/", "foo/bar/", "foo/bar/A.class", "Blah.txt")
    val actual  = scala.collection.mutable.Set.empty[String]
    var is      = new ByteArrayInputStream(hyts_patchBytes)
    var jis     = new JarInputStream(is)
    var je      = jis.getNextJarEntry()
    while (je != null) {
      actual.add(je.toString())
      je = jis.getNextJarEntry()
    }
    assert(actual == desired)
    jis.close()

    assertThrows[IOException] {
      jis.getNextJarEntry()
    }

    is = new ByteArrayInputStream(brokenEntryBytes)
    jis = new JarInputStream(is, false)
    jis.getNextJarEntry()
    assertThrows[ZipException] {
      jis.getNextJarEntry()
    }
  }

  test("getManifest()") {
    var is  = new ByteArrayInputStream(hyts_patch2Bytes)
    var jis = new JarInputStream(is)
    var m   = jis.getManifest()
    assert(m == null)

    is = new ByteArrayInputStream(hyts_patchBytes)
    jis = new JarInputStream(is)
    m = jis.getManifest()
    assert(m != null)
  }

  test("getNextJarEntry()") {
    val desired = Set("foo/", "foo/bar/", "foo/bar/A.class", "Blah.txt")
    val actual  = scala.collection.mutable.Set.empty[String]
    val is      = new ByteArrayInputStream(hyts_patchBytes)
    val jis     = new JarInputStream(is)
    var je      = jis.getNextJarEntry()
    while (je != null) {
      actual.add(je.toString())
      je = jis.getNextJarEntry()
    }
    assert(actual == desired)
  }

  test("getNextEntry on Integrate.jar") {
    val is              = new ByteArrayInputStream(integrateBytes)
    val jis             = new JarInputStream(is, true)
    var entry: ZipEntry = null
    var count           = 0
    while (count == 0 || entry != null) {
      count += 1
      entry = jis.getNextEntry()
    }
    assert(count == 5)
    jis.close()
  }

  // TODO: Uncomment once we support signed jars
  // test("getNextEntry on Modified_Class.jar") {
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
  //         assert(false) // should have thrown Security Exception
  //       }
  //     } catch {
  //       case e: SecurityException if count == indexOfTestClass + 1 =>
  //         // expected
  //     }
  //   }
  //   assert(count == 6)
  //   jis.close()
  // }

  // TODO: Uncomment once we support signed jars
  // test("getNextEntry on Modified_MainAttributes.jar") {
  //   val is = new ByteArrayInputStream(modifiedManifestMainAttributesBytes)
  //   val jis = new JarInputStream(is, true)
  //   assert(jis.getNextEntry().getName() == "META-INF/TESTROOT.SF")
  //   assert(jis.getNextEntry().getName() == "META-INF/TESTROOT.DSA")
  //   assertThrows[SecurityException] {
  //     jis.getNextEntry()
  //   }
  //   assert(jis.getNextEntry().getName() == "META-INF/")
  //   assert(jis.getNextEntry().getName() == "Test.class")
  //   jis.close()
  // }

  // TODO: Uncomment once we support signed jars
  // test("getNextEntry on Modified_Manifest_EntryAttributes.jar") {
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
  //         assert(false) // Should have throws Security Exception
  //       }
  //     } catch {
  //       case _: SecurityException if count == indexofDSA + 1 =>
  //         // expected
  //     }
  //   }
  //   assert(count == 5)
  //   jis.close()
  // }

  // TODO: Uncomment once we support signed jars
  // test("getNextEntry on Modified_SF_EntryAttributes.jar") {
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
  //         // assert(false) // Should have throws Security Exception
  //       }
  //     } catch {
  //       case _: SecurityException if count == indexofDSA + 1 =>
  //         // expected
  //     }
  //   }
  //   println(count == 5)
  //   jis.close()
  // }

  // TODO: Uncomment once we support signed jars
  // test("read Modified_Class.jar") {
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
  //         assert(false) // should have thrown Security Exception
  //       }
  //     } catch {
  //       case _: SecurityException if count == indexOfTestClass =>
  //         // expected
  //     }
  //   }
  //   assert(count == 5)
  //   jis.close()
  // }

  test("read Integrate.jar") {
    val is                 = new ByteArrayInputStream(integrateBytes)
    val jis                = new JarInputStream(is)
    var count              = 0
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
    assert(count == 5)
    jis.close()
  }

  // TODO: Uncomment once we support signed jars
  // test("read Modified_Manifest_MainAttributes.jar") {
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
  //         assert(false) // should have throws Security Exception
  //       }
  //     } catch {
  //       case _: SecurityException if count == indexofDSA =>
  //         // expected
  //     }
  //   }
  //   assert(count == 5)
  //   jis.close()
  // }

  // TODO: Uncomment once we suport signed jars
  // test("read Modified_SF_EntryAttributes.jar") {
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
  //         assert(false) // should have thrown Security Exception
  //       }
  //     } catch {
  //       case _: SecurityException if count == indexofDSA =>
  //         // expected
  //     }
  //   }
  //   assert(count == 5)
  //   jis.close()
  // }

  test("getNextEntry() on Broken_entry.jar") {
    val is  = new ByteArrayInputStream(brokenEntryBytes)
    val jis = new JarInputStream(is)
    jis.getNextEntry()
    assertThrows[ZipException] {
      jis.getNextEntry()
    }

    assertThrows[IOException] {
      jis.close()        // Android throws exception here, already!
      jis.getNextEntry() // But RI here, only!
    }
  }

}
