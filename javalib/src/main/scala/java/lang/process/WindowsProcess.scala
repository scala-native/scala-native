package java.lang.process

// Required only for cross-compilation with Scala 2
import scala.language.existentials

import java.io.FileDescriptor
import java.lang.ProcessBuilder._

import java.util.ArrayList
import java.util.ScalaOps._
import java.util.concurrent.TimeUnit
import java.nio.file.WindowsException
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows._

import HandleApi._
import HandleApiExt._
import ProcessThreadsApi._
import ProcessThreadsApiExt._
import ProcessThreadsApiOps._
import FileApiExt._
import NamedPipeApi._
import WinBaseApi._
import WinBaseApiOps._
import winnt.AccessRights._

private[process] class WindowsProcessHandle(
    handle: Handle,
    override val builder: ProcessBuilder
) extends GenericProcessHandle {
  private val _pid = ProcessThreadsApi.GetProcessId(handle)

  override final def pid(): Long = _pid.toLong

  override def supportsNormalTermination(): Boolean = false

  override protected def destroyImpl(force: Boolean): Boolean =
    ProcessThreadsApi.TerminateProcess(handle, 1.toUInt)

  override protected def close(): Unit = CloseHandle(handle)

  override protected def waitForImpl(): Boolean =
    osWaitForImpl(Constants.Infinite)

  override protected def waitForImpl(timeout: Long, unit: TimeUnit): Boolean =
    osWaitForImpl(unit.toMillis(timeout).toUInt)

  private def osWaitForImpl(timeoutMillis: DWord): Boolean =
    SynchApi.WaitForSingleObject(handle, timeoutMillis) match {
      case SynchApiExt.WAIT_TIMEOUT => false
      case SynchApiExt.WAIT_FAILED  =>
        throw WindowsException("Failed to wait on process handle")
      case _ => checkAndSetExitCode(); true
    }

  override protected def getExitCodeImpl: Option[Int] = {
    val exitCode: Ptr[DWord] = stackalloc[DWord]()
    if (ProcessThreadsApi.GetExitCodeProcess(handle, exitCode)) {
      val code = !exitCode
      if (code != ProcessThreadsApiExt.STILL_ACTIVE) Some(code.toInt) else None
    } else None
  }

}

private[process] object WindowsProcess {
  type PipeHandles = CArray[Handle, Nat._2]
  private final val readEnd = 0
  private final val writeEnd = 1

  def apply(
      builder: ProcessBuilder
  ): GenericProcess = Zone.acquire { implicit z =>
    val (inRead, inWrite) =
      createPipeOrThrow(
        builder.redirectInput(),
        ConsoleApiExt.stdIn,
        isStdIn = true,
        "Couldn't create std input pipe."
      )
    val (outRead, outWrite) =
      createPipeOrThrow(
        builder.redirectOutput(),
        ConsoleApiExt.stdOut,
        isStdIn = false,
        "Couldn't create std output pipe."
      )
    val (errRead, errWrite) = {
      if (builder.redirectErrorStream()) (INVALID_HANDLE_VALUE, outWrite)
      else
        createPipeOrThrow(
          builder.redirectError(),
          ConsoleApiExt.stdErr,
          isStdIn = false,
          "Couldn't create std error pipe."
        )
    }

    val cmd = builder.command()
    val dir = toCWideStringUTF16LE(builder.directory().getAbsolutePath())
    val argv = toCWideStringUTF16LE(cmd.scalaOps.mkString("", " ", ""))
    val envp = nullTerminatedBlock {
      val list = new ArrayList[String]
      builder
        .environment()
        .entrySet()
        .iterator()
        .scalaOps
        .foreach(e => list.add(s"${e.getKey()}=${e.getValue()}"))
      list
    }.asInstanceOf[Ptr[Byte]]

    // stackalloc is documented as returning zeroed memory
    val processInfo = stackalloc[ProcessInformation]()
    val startupInfo = stackalloc[StartupInfoW]()
    startupInfo.cb = sizeof[StartupInfoW].toUInt
    startupInfo.stdInput = inRead
    startupInfo.stdOutput = outWrite
    startupInfo.stdError = errWrite
    startupInfo.flags = STARTF_USESTDHANDLES

    val created = CreateProcessW(
      applicationName = null,
      commandLine = argv,
      processAttributres = null,
      threadAttributes = null,
      inheritHandle = true,
      creationFlags = CREATE_UNICODE_ENVIRONMENT | CREATE_NO_WINDOW,
      environment = envp,
      currentDirectory = dir,
      startupInfo = startupInfo,
      processInformation = processInfo
    )

    if (created) {
      def toFileDescriptor(handle: Handle, readOnly: Boolean) =
        new FileDescriptor(
          FileDescriptor.FileHandle(handle),
          readOnly = readOnly
        )

      CloseHandle(inRead)
      CloseHandle(outWrite)
      CloseHandle(errWrite)
      CloseHandle(processInfo.thread)

      val handle = new WindowsProcessHandle(processInfo.process, builder)
      new GenericProcess(handle) {
        override protected def fdIn =
          toFileDescriptor(inWrite, readOnly = false)
        override protected def fdOut =
          toFileDescriptor(outRead, readOnly = true)
        override protected def fdErr =
          toFileDescriptor(errRead, readOnly = true)
      }
    } else {
      throw WindowsException(s"Failed to create process for command: $cmd")
    }
  }

  private def createPipeOrThrow(
      redirect: Redirect,
      stdHandle: Handle,
      isStdIn: Boolean,
      msg: => String
  ): (Handle, Handle) = {

    val securityAttributes = stackalloc[SecurityAttributes]()
    securityAttributes.length = sizeof[SecurityAttributes].toUInt
    securityAttributes.inheritHandle = true
    securityAttributes.securityDescriptor = null

    val pipe: PipeHandles = !stackalloc[PipeHandles]()
    val pipeEnds @ (pipeRead, pipeWrite) = (pipe.at(readEnd), pipe.at(writeEnd))
    val pipeCreated =
      CreatePipe(pipeRead, pipeWrite, null, 0.toUInt)
    if (!pipeCreated)
      throw WindowsException(msg)

    val (childEnd, parentEnd) =
      if (isStdIn) pipeEnds
      else pipeEnds.swap

    setupRedirect(redirect, childEnd, stdHandle)

    SetHandleInformation(!childEnd, HANDLE_FLAG_INHERIT, 1.toUInt)
    SetHandleInformation(!parentEnd, HANDLE_FLAG_INHERIT, 0.toUInt)

    (!pipeRead, !pipeWrite)
  }

  @inline private def setupRedirect(
      redirect: ProcessBuilder.Redirect,
      childHandle: Ptr[Handle],
      stdHandle: Handle
  ): Unit = {

    @inline def openRedirectFd(
        access: DWord,
        disposition: DWord,
        flagsAndAttributes: DWord = FILE_ATTRIBUTE_NORMAL,
        sharing: DWord = FILE_SHARE_ALL
    ) = Zone.acquire { implicit z =>
      val handle = FileApi.CreateFileW(
        filename = toCWideStringUTF16LE(redirect.file().getAbsolutePath()),
        desiredAccess = access,
        shareMode = sharing,
        securityAttributes = null,
        creationDisposition = disposition,
        flagsAndAttributes = flagsAndAttributes,
        templateFile = null
      )
      if (handle == INVALID_HANDLE_VALUE) {
        throw WindowsException.onPath(redirect.file().toString())
      }
      handle
    }

    def duplicateOrThrow(handle: Handle, kind: String): Unit = {
      val hasSucceded = DuplicateHandle(
        sourceProcess = GetCurrentProcess(),
        source = handle,
        targetProcess = GetCurrentProcess(),
        target = childHandle,
        desiredAccess = 0.toUInt,
        inheritHandle = true,
        options = DUPLICATE_SAME_ACCESS
      )

      if (!hasSucceded) {
        throw WindowsException(s"Couldn't duplicate $kind file descriptor")
      }
    }

    redirect.`type`() match {
      case ProcessBuilder.Redirect.Type.PIPE => ()

      case ProcessBuilder.Redirect.Type.INHERIT =>
        duplicateOrThrow(stdHandle, "inherit")

      case ProcessBuilder.Redirect.Type.READ =>
        val fd = openRedirectFd(
          access = FILE_GENERIC_READ,
          disposition = OPEN_ALWAYS
        )
        duplicateOrThrow(fd, "read")

      case ProcessBuilder.Redirect.Type.WRITE =>
        val fd = openRedirectFd(
          access = FILE_GENERIC_WRITE,
          disposition = CREATE_ALWAYS
        )
        duplicateOrThrow(fd, "write")

      case ProcessBuilder.Redirect.Type.APPEND =>
        val fd = openRedirectFd(
          access = FILE_GENERIC_WRITE | FILE_APPEND_DATA,
          disposition = OPEN_ALWAYS
        )
        duplicateOrThrow(fd, "append")
    }
  }

  @inline private def nullTerminatedBlock(
      list: java.util.List[String]
  )(implicit z: Zone): CWString = {
    val NUL = 0.toChar.toString
    val block = toCWideStringUTF16LE(list.scalaOps.mkString("", NUL, NUL))

    list.add("")
    val totalSize = list.scalaOps.foldLeft(0)(_ + _.size + 1) - 1
    val blockEnd = block + totalSize
    assert(!blockEnd == 0, s"not null terminated got ${!blockEnd}")
    assert(
      !(blockEnd - 1) == 0,
      s"not null terminated -1, got ${!(blockEnd - 1)}"
    )

    block
  }
}
