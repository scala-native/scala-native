package org.scalanative.testsuite.javalib.nio.file.attribute

import java.nio.file.attribute.{
  PosixFileAttributeView, PosixFileAttributes, PosixFilePermission
}
import java.nio.file.{Files, Path}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{BeforeClass, Test}

import org.scalanative.testsuite.utils.Platform

/* The code under test should work on Java 8 through latest (currently 23).
 * It is tested on JDK11 because that version introduces "Path.of()".
 * In some later JVM versions, "Paths.get()" becomes deprecated. No sense
 * writing new code which uses it.
 */

object PosixFileAttributeViewTestOnJDK11 {

  @BeforeClass def posixOnly(): Unit = {
    assumeTrue("Not implemented in Windows", !Platform.isWindows)
  }
}

class PosixFileAttributeViewTestOnJDK11 {

  // Issue 4067
  @Test def readPosixFileAttributes(): Unit = {
    val path = Path.of(".")

    val posixAttrView =
      Files.getFileAttributeView(path, classOf[PosixFileAttributeView])

    val posixAttrs = posixAttrView.readAttributes()

    assertNotNull("unexpected Null POSIX file attributes", posixAttrs)

    // Permissions is a POSIX attribute;
    val permissions = posixAttrs.permissions()
    assertTrue("expected number of permissons > 0", permissions.size() > 0)

    // quick consistency check.
    assertTrue(
      "dot should have at least owner:read permission",
      permissions.contains(PosixFilePermission.OWNER_READ)
    )
  }

}
