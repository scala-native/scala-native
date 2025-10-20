package org.scalanative.testsuite.javalib.lang

import java.io.*
import java.nio.file.Files

import java.util.concurrent.TimeUnit
import scala.scalanative.unsigned.*
import scala.scalanative.unsafe.*
import scala.scalanative.posix.{fcntl, unistd}
import scala.scalanative.meta.LinktimeInfo.isWindows

import scala.io.Source

import org.junit.Test
import org.junit.Assert.*

class ProcessInheritTest {
  import ProcessUtils.*

  @Test def inherit(): Unit = {
    def unixImpl() = {
      val f = Files.createTempFile("/tmp", "out")
      val savedFD = unistd.dup(unistd.STDOUT_FILENO)
      val flags = fcntl.O_RDWR | fcntl.O_TRUNC | fcntl.O_CREAT
      val fd = Zone.acquire { implicit z =>
        fcntl.open(toCString(f.toAbsolutePath.toString), flags, 0.toUInt)
      }

      try {
        unistd.dup2(fd, unistd.STDOUT_FILENO)
        unistd.close(fd)
        val proc = new ProcessBuilder("ls", resourceDir).inheritIO().start()
        proc.waitFor(5, TimeUnit.SECONDS)
        readInputStream(new FileInputStream(f.toFile))
      } finally {
        unistd.dup2(savedFD, unistd.STDOUT_FILENO)
        unistd.close(savedFD)
      }
    }

    def windowsImpl() = {
      import scala.scalanative.windows.*
      import NamedPipeApi.*
      import HandleApi.*
      import HandleApiExt.*
      import ConsoleApi.*
      import ConsoleApiExt.*
      import FileApi.*
      import FileApiExt.*
      import winnt.AccessRights.*
      val f = Files.createTempFile("tmp", "out")
      val readEnd, writeEnd, stdOutDup = stackalloc[Handle]()

      assertEquals(
        "createPipe",
        true,
        CreatePipe(
          readPipePtr = readEnd,
          writePipePtr = writeEnd,
          securityAttributes = null,
          size = 0.toUInt
        )
      )

      Zone.acquire { implicit z =>
        val handle = CreateFileW(
          toCWideStringUTF16LE(f.toAbsolutePath.toString),
          desiredAccess = FILE_GENERIC_WRITE | FILE_GENERIC_READ,
          shareMode = FILE_SHARE_ALL,
          securityAttributes = null,
          creationDisposition = CREATE_ALWAYS,
          flagsAndAttributes = FILE_ATTRIBUTE_NORMAL,
          templateFile = null
        )
        assertNotEquals("Cannot create file", INVALID_HANDLE_VALUE, handle)

        val savedStdout = GetStdHandle(STD_OUTPUT_HANDLE)
        SetStdHandle(STD_OUTPUT_HANDLE, handle)

        try {
          val proc = processForCommand(Scripts.ls, "/b", resourceDir)
            .inheritIO()
            .start()
          proc.waitFor(5, TimeUnit.SECONDS)
          readInputStream(new FileInputStream(f.toFile))
        } finally {
          SetStdHandle(STD_OUTPUT_HANDLE, savedStdout)
          CloseHandle(handle)
        }
      }
    }

    val out =
      if (isWindows) windowsImpl()
      else unixImpl()

    assertEquals(scripts, out.split(System.lineSeparator()).toSet)
  }

}
