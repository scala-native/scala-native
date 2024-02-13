package org.scalanative.testsuite.posixlib

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.BeforeClass

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.posix.stdlib

import scala.scalanative.meta.LinktimeInfo
import org.scalanative.testsuite.utils.Platform

object StdlibTest {

  @BeforeClass
  def beforeClass(): Unit = {
    assumeFalse(
      "posixlib stdlib.scala is not implemented on Windows",
      Platform.isWindows
    )
  }
}

class StdlibTest {
  /* Test some Open Group 2018 methods which have complicated argument
   * declarations. That is, the ones which keep me awake at night, wondering
   * if they will blow up on the first person who goes to use them.
   *
   * Also gives end users a working example of how to setup and use these
   * methods.
   */

  @Test def testGetsubopt(): Unit = {

    if (!LinktimeInfo.isWindows) Zone.acquire { implicit z =>
      val expectedNameValue = "SvantePääbo"
      val expectedAccessValue = "ro"

      val optionp = stackalloc[CString](2)

      // optionp string must be mutable.
      optionp(0) = toCString(
        s"doNotFind,name=${expectedNameValue},access=${expectedAccessValue}"
      )
      // Last option, optionp(1) is already null, keep it that way.

      val tokens = stackalloc[CString](4)

      // Specification describes these as 'const'
      tokens(0) = c"skip"
      tokens(1) = c"access"
      tokens(2) = c"name"
      // Last token, tokens(3) is already null, keep it that way.

      val valuep = stackalloc[CString]()

      // Options not in tokens are not found, even at index 0.
      val status_1 = stdlib.getsubopt(optionp, tokens, valuep)
      assertEquals("Should not have found first option", -1, status_1)

      // Options with tokens are found, even at an index offset > 0.
      val status_name = stdlib.getsubopt(optionp, tokens, valuep)
      assertEquals("failed to get 'name' option", 2, status_name)
      assertNotNull("'name' value is NULL", valuep)
      assertEquals(
        "Unexpected 'name' value",
        expectedNameValue,
        fromCString(valuep(0))
      )

      // Do it again, to make sure pointer offsets are working properly.
      val status_access = stdlib.getsubopt(optionp, tokens, valuep)
      assertEquals("failed to get 'access' option", 1, status_access)
      assertNotNull("'access' value is NULL", valuep)
      assertEquals(
        "Unexpected 'access' value",
        expectedAccessValue,
        fromCString(valuep(0))
      )
    }
  }

}
