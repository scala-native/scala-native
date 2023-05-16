package org.scalanative.testsuite.posixlib.sys

import scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.sys.utsname._
import scala.scalanative.posix.sys.utsnameOps._
import scala.scalanative.unsafe._

import org.junit.Test
import org.junit.Assert._
import org.junit.Ignore

class UtsnameTest {
  @Test def utsnameOpsTest(): Unit = if (!isWindows) {

    val u: Ptr[utsname] = stackalloc[utsname]()

    val r = uname(u)
    assertEquals(
      s"uname failed, result is ${r}",
      r,
      0
    )

    val sysname = u._1.asInstanceOf[CArray[Byte, _256]]
    val nodename = u._2.asInstanceOf[CArray[Byte, _256]]
    val release = u._3.asInstanceOf[CArray[Byte, _256]]
    val version = u._4.asInstanceOf[CArray[Byte, _256]]
    val machine = u._5.asInstanceOf[CArray[Byte, _256]]

    assertEquals(
      s"sysname obtained from both utsname and utsnameOps should be equal",
      sysname,
      u.sysname
    )

    assertEquals(
      s"nodename obtained from both utsname and utsnameOps should be equal",
      nodename,
      u.nodename
    )

    assertEquals(
      s"release obtained from both utsname and utsnameOps should be equal",
      release,
      u.release
    )

    assertEquals(
      s"version obtained from both utsname and utsnameOps should be equal",
      version,
      u.version
    )

    assertEquals(
      s"machine obtained from both utsname and utsnameOps should be equal",
      machine,
      u.machine
    )

  }

  /* This is a visual test, hence ignored in CI.
   * It increases confidence in the results of the uname() call by
   * allowing developers to validate results in known environments.
   * CI has too many possibly valid values for this writer to automate.
   */
  @Ignore
  @Test def unameVisual(): Unit = if (!isWindows) {

    val u: Ptr[utsname] = stackalloc[utsname]()

    val r = uname(u)

    assertEquals(s"uname failed, result is ${r}", r, 0)

    val sysname = fromCString(u.sysname.at(0).asInstanceOf[CString])
    val nodename = fromCString(u.nodename.at(0).asInstanceOf[CString])
    val release = fromCString(u.release.at(0).asInstanceOf[CString])
    val version = fromCString(u.version.at(0).asInstanceOf[CString])
    val machine = fromCString(u.machine.at(0).asInstanceOf[CString])

    printf(s"\n\n")
    printf(s"uname.sysname: '${sysname}'\n")
    printf(s"uname.nodename: '${nodename}'\n")
    printf(s"uname.release: '${release}'\n")
    printf(s"uname.version: '${version}'\n")
    printf(s"uname.machine: '${machine}'\n")
    printf(s"\n\n")
  }
}
