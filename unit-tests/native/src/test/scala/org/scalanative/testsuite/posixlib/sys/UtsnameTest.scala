package org.scalanative.testsuite.posixlib.sys

import scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.sys.uname._
import scala.scalanative.posix.sys.utsname._
import scala.scalanative.unsafe._

import org.junit.Test
import org.junit.Assert._

class UtsnameTest {
  @Test def utsnameOpsTest(): Unit = if (!isWindows) {

    val u: Ptr[utsname] = stackalloc[utsname]()

    val r = uname(u)
    assertEquals(
      s"uname failed, result is ${r}",
      r,
      0
    )

    val sysname = u.at1.asInstanceOf[Ptr[CChar]]
    val nodename = u.at2.asInstanceOf[Ptr[CChar]]
    val release = u.at3.asInstanceOf[Ptr[CChar]]
    val version = u.at4.asInstanceOf[Ptr[CChar]]
    val machine = u.at5.asInstanceOf[Ptr[CChar]]

    assertEquals(
      s"sysname obtained from both utsname and utsnameOps should be equal",
      fromCString(sysname),
      u.sysname
    )

    assertEquals(
      s"nodename obtained from both utsname and utsnameOps should be equal",
      fromCString(nodename),
      u.nodename
    )

    assertEquals(
      s"release obtained from both utsname and utsnameOps should be equal",
      fromCString(release),
      u.release
    )

    assertEquals(
      s"version obtained from both utsname and utsnameOps should be equal",
      fromCString(version),
      u.version
    )

    assertEquals(
      s"machine obtained from both utsname and utsnameOps should be equal",
      fromCString(machine),
      u.machine
    )

  }
}
