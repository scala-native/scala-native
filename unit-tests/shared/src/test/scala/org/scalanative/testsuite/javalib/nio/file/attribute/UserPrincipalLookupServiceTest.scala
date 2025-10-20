package org.scalanative.testsuite.javalib.nio.file.attribute

import java.nio.file.attribute.*
import java.nio.file.FileSystems

import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.isWindows
import org.scalanative.testsuite

class UserPrincipalLookupServiceTest {

  val lookupService = FileSystems.getDefault.getUserPrincipalLookupService

  @Test def lookupPrincipalByNameSucceedsForNumeric(): Unit = {
    assumeFalse("Numeric user names not supported on Windows", isWindows)
    val expected = (Int.MaxValue / 2).toString // an arbitrary value

    assertEquals(
      expected,
      lookupService.lookupPrincipalByName(expected).getName
    )
  }

  @Test def lookupPrincipalByGroupNameSucceedsForNumeric(): Unit = {
    assumeFalse("Numeric group names not supported on Windows", isWindows)
    val expected = (Int.MaxValue / 3).toString // an arbitrary value

    assertEquals(
      expected,
      lookupService.lookupPrincipalByGroupName(expected).getName
    )
  }

  @Test def lookupPrincipalByNameSucceedsForCurrentDirOwner(): Unit = {
    import java.nio.file.*
    val currentDirOwner = Files.getOwner(Paths.get("."))
    assertEquals(
      currentDirOwner,
      lookupService.lookupPrincipalByName(currentDirOwner.getName())
    )
  }
}
