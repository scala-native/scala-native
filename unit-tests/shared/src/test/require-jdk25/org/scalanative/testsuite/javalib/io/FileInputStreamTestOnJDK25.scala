package org.scalanative.testsuite.javalib.io

import java.io._

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform.isWindows

class FileInputStreamTestOnJDK25 {

  // JDK 25 reads and discards bytes when skipping anything but regular files.

  @Test def skipDevNullSkipsNothing(): Unit = {
    assumeFalse("Unix device files", isWindows)
    val in = new FileInputStream("/dev/null")
    try {
      assertEquals("skip(1)", 0L, in.skip(1))
      assertEquals("skip(100)", 0L, in.skip(100))
      assertEquals("skip(-1)", 0L, in.skip(-1))
    } finally in.close()
  }

  @Test def skipDevZeroReadsBytes(): Unit = {
    assumeFalse("Unix device files", isWindows)
    val in = new FileInputStream("/dev/zero")
    try {
      assertEquals("skip(1)", 1L, in.skip(1))
      assertEquals("skip(100)", 100L, in.skip(100))
      assertEquals("skip(-1)", 0L, in.skip(-1))
    } finally in.close()
  }
}
