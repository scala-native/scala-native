package javalib.nio.file.attribute

import java.nio.file.attribute._
import java.nio.file.FileSystems

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows.assertThrows
import org.scalanative.testsuite

class UserPrincipalLookupServiceTest {

  val lookupService = FileSystems.getDefault.getUserPrincipalLookupService

  @Test def lookupPrincipalByNameSucceedsForNumeric(): Unit = {
    val expected = (Int.MaxValue / 2).toString // an arbitrary value

    assertEquals(
      expected,
      lookupService.lookupPrincipalByName(expected).getName
    )
  }

  @Test def lookupPrincipalByGroupNameSucceedsForNumeric(): Unit = {
    val expected = (Int.MaxValue / 3).toString // an arbitrary value

    assertEquals(
      expected,
      lookupService.lookupPrincipalByGroupName(expected).getName
    )
  }
}
