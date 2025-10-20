package org.scalanative.testsuite.windowslib

import org.junit.Test
import org.junit.Assert.*

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.unsafe.*
import scala.scalanative.windows.MinWinBaseApi.FileTimeStruct
import scala.scalanative.windows.MinWinBaseApiOps.*
import scala.scalanative.windows.ProcessThreadsApi

class ProcessThreadsApiTest {

  @Test def testGetProcessTimes(): Unit = if (isWindows) {
    val creationTime = stackalloc[FileTimeStruct]()
    val exitTime = stackalloc[FileTimeStruct]()
    val kernelTime = stackalloc[FileTimeStruct]()
    val userTime = stackalloc[FileTimeStruct]()

    val result = ProcessThreadsApi.GetProcessTimes(
      ProcessThreadsApi.GetCurrentProcess(),
      creationTime,
      exitTime,
      kernelTime,
      userTime
    )

    assertTrue("result is false", result)

    assertTrue(
      s"kernelTime [${kernelTime.fileTime}] < 0",
      kernelTime.fileTime.toLong >= 0L
    )

    assertTrue(
      s"userTime [${userTime.fileTime}] < 0",
      userTime.fileTime.toLong >= 0L
    )
  }

}
