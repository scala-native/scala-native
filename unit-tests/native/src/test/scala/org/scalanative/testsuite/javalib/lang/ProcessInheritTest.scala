package org.scalanative.testsuite.javalib.lang

import java.io._
import java.nio.file.Files
import java.util.concurrent.TimeUnit

import scala.io.Source

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.{fcntl, unistd}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

class ProcessInheritTest {
  import ProcessUtils._

  @Test def inherit(): Unit = {
    def unixImpl() = {
      val f = Files.createTempFile("/tmp", "out")
      val flags = fcntl.O_RDWR | fcntl.O_TRUNC | fcntl.O_CREAT
      val fd = Zone.acquire { implicit z =>
        fcntl.open(toCString(f.toAbsolutePath.toString), flags, 0.toUInt)
      }

      val savedFD = unistd.dup(unistd.STDOUT_FILENO)
      fcntl.fcntl(savedFD, fcntl.F_SETFD, fcntl.FD_CLOEXEC)
      val proc =
        try {
          unistd.dup2(fd, unistd.STDOUT_FILENO)
          unistd.close(fd)
          new ProcessBuilder("ls", resourceDir).inheritIO().start()
        } finally {
          unistd.dup2(savedFD, unistd.STDOUT_FILENO)
          unistd.close(savedFD)
        }

      (proc, f)
    }

    def windowsImpl() = {
      import scala.scalanative.windows._
      import HandleApi._
      import HandleApiExt._
      import ConsoleApi._
      import FileApiExt._
      import winnt.AccessRights._
      val f = Files.createTempFile("tmp", "out")
      val readEnd, writeEnd, stdOutDup = stackalloc[Handle]()

      assertEquals(
        "createPipe",
        true,
        NamedPipeApi.CreatePipe(
          readPipePtr = readEnd,
          writePipePtr = writeEnd,
          securityAttributes = null,
          size = 0.toUInt
        )
      )

      val handle = Zone.acquire { implicit z =>
        FileApi.CreateFileW(
          toCWideStringUTF16LE(f.toAbsolutePath.toString),
          desiredAccess = FILE_GENERIC_WRITE | FILE_GENERIC_READ,
          shareMode = FILE_SHARE_ALL,
          securityAttributes = null,
          creationDisposition = CREATE_ALWAYS,
          flagsAndAttributes = FILE_ATTRIBUTE_NORMAL,
          templateFile = null
        )
      }
      assertNotEquals("Cannot create file", INVALID_HANDLE_VALUE, handle)

      val savedStdout = GetStdHandle(STD_OUTPUT_HANDLE)
      val proc =
        try {
          SetStdHandle(STD_OUTPUT_HANDLE, handle)
          processForCommand(Scripts.ls, "/b", resourceDir).inheritIO().start()
        } finally {
          SetStdHandle(STD_OUTPUT_HANDLE, savedStdout)
          CloseHandle(handle)
        }

      (proc, f)
    }

    val (proc, tmpFile) = if (isWindows) windowsImpl() else unixImpl()
    assertTrue(proc.waitFor(30, TimeUnit.SECONDS))
    val out = readInputStream(new FileInputStream(tmpFile.toFile))

    assertEquals(scripts, out.split(System.lineSeparator()).toSet)
  }

}
