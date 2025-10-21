package org.scalanative.testsuite.javalib.io

import java.io._

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scalanative.junit.utils.AssumesHelper._

class FileDescriptorTest {
  val in = FileDescriptor.in
  val out = FileDescriptor.out
  val err = FileDescriptor.err
  val fd = new FileDescriptor()

  @Test def validDescriptors(): Unit = {
    assertTrue(in.valid())
    assertTrue(out.valid())
    assertTrue(err.valid())
  }

  @Test def invalidDescriptors(): Unit = {
    assertFalse(fd.valid())
  }

  @Test def syncShouldThrowSyncFailedExceptionForStdin(): Unit = {
    assumeNotJVMCompliant()
    assertThrows(classOf[SyncFailedException], in.sync())
  }

  @Test def syncShouldThrowSyncFailedExceptionForStdout(): Unit = {
    assumeNotJVMCompliant()
    assertThrows(classOf[SyncFailedException], out.sync())
  }

  @Test def syncShouldThrowSyncFailedExceptionForStderr(): Unit = {
    assumeNotJVMCompliant()
    assertThrows(classOf[SyncFailedException], err.sync())
  }

  @Test def syncShouldThrowSyncFailedExceptionForNewFd(): Unit = {
    assertThrows(classOf[SyncFailedException], fd.sync())
  }

  @Test def validShouldVerifyThatFileDescriptorIsStillValid(): Unit = {
    val tmpFile = File.createTempFile("tmp", ".tmp")
    assertTrue(tmpFile.exists())
    val fis = new FileInputStream(tmpFile)
    assertTrue(fis.getFD().valid())
    fis.close()
    assertFalse(fis.getFD().valid())
  }
}
