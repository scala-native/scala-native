package scala.scalanative.windows

import scala.scalanative.unsafe.{Word => _, _}
import scalanative.unsigned._
import HandleApi.Handle
import WinBaseApi.SecurityAttributes

@link("Advapi32")
@link("Kernel32")
@extern()
object ProcessThreadsApi {
  type StartupInfoW = CStruct18[
    DWord,
    CWString,
    CWString,
    CWString,
    DWord,
    DWord,
    DWord,
    DWord,
    DWord,
    DWord,
    DWord,
    DWord,
    Word,
    Word,
    Ptr[Byte],
    Handle,
    Handle,
    Handle
  ]
  type ProcessInformation = CStruct4[Handle, Handle, DWord, DWord]

  def CreateProcessW(
      applicationName: CWString,
      commandLine: CWString,
      processAttributres: Ptr[SecurityAttributes],
      threadAttributes: Ptr[SecurityAttributes],
      inheritHandle: Boolean,
      creationFlags: DWord,
      environment: Ptr[Byte],
      currentDirectory: CWString,
      startupInfo: Ptr[StartupInfoW],
      processInformation: Ptr[ProcessInformation]
  ): Boolean =
    extern

  def ExitProcess(exitCode: UInt): Unit = extern
  def ExitThread(exitCode: DWord): Unit = extern
  @blocking
  def FlushProcessWriteBuffers(): Unit = extern
  def GetCurrentProcess(): Handle = extern
  def GetCurrentProcessToken(): Handle = extern
  def GetCurrentThread(): Handle = extern
  def GetExitCodeProcess(handle: Handle, exitCodePtr: Ptr[DWord]): Boolean =
    extern

  def GetExitCodeThread(handle: Handle, exitCodePtr: Ptr[DWord]): Boolean =
    extern

  def GetProcessId(handle: Handle): DWord = extern
  def OpenThreadToken(
      thread: Handle,
      desiredAccess: DWord,
      openAsSelf: Boolean,
      tokenHandle: Ptr[Handle]
  ): Boolean = extern
  def OpenProcessToken(
      process: Handle,
      desiredAccess: DWord,
      tokenHandle: Ptr[Handle]
  ): Boolean = extern

  def ResumeThread(thread: Handle): DWord = extern
  @blocking def SwitchToThread(): Boolean = extern
  @blocking def SuspendThread(thread: Handle): DWord = extern

  def SetThreadPriority(thread: Handle, priority: Int): Boolean = extern

  def TerminateProcess(handle: Handle, exitCode: UInt): Boolean = extern
  def TerminateThread(handle: Handle, exitCode: DWord): Boolean = extern

}

object ProcessThreadsApiExt {
  // Exit codes
  final val SUCCESSFUL = 0.toUInt
  final val STILL_ACTIVE = 259.toUInt

  // StartupInfo flags
  final val STARTF_FORCEONFEEDBACK = 0x00000040.toUInt
  final val STARTF_FORCEOFFFEEDBACK = 0x00000080.toUInt
  final val STARTF_PREVENTPINNING = 0x00002000.toUInt
  final val STARTF_RUNFULLSCREEN = 0x00000020.toUInt
  final val STARTF_TITLEISAPPID = 0x00001000.toUInt
  final val STARTF_TITLEISLINKNAME = 0x00000800.toUInt
  final val STARTF_UNTRUSTEDSOURCE = 0x00008000.toUInt
  final val STARTF_USECOUNTCHARS = 0x00000008.toUInt
  final val STARTF_USEFILLATTRIBUTE = 0x00000010.toUInt
  final val STARTF_USEHOTKEY = 0x00000200.toUInt
  final val STARTF_USEPOSITION = 0x00000004.toUInt
  final val STARTF_USESHOWWINDOW = 0x00000001.toUInt
  final val STARTF_USESIZE = 0x00000002.toUInt
  final val STARTF_USESTDHANDLES = 0x00000100.toUInt

  // Creation flags
  final val CREATE_BREAKAWAY_FROM_JOB = 0x01000000.toUInt
  final val CREATE_DEFAULT_ERROR_MODE = 0x04000000.toUInt
  final val CREATE_NEW_CONSOLE = 0x00000010.toUInt
  final val CREATE_NEW_PROCESS_GROUP = 0x00000200.toUInt
  final val CREATE_NO_WINDOW = 0x08000000.toUInt
  final val CREATE_PROTECTED_PROCESS = 0x00040000.toUInt
  final val CREATE_PRESERVE_CODE_AUTHZ_LEVEL = 0x02000000.toUInt
  final val CREATE_SECURE_PROCESS = 0x00400000.toUInt
  final val CREATE_SEPARATE_WOW_VDM = 0x00000800.toUInt
  final val CREATE_SHARED_WOW_VDM = 0x00001000.toUInt
  final val CREATE_SUSPENDED = 0x00000004.toUInt
  final val CREATE_UNICODE_ENVIRONMENT = 0x00000400.toUInt
  final val DEBUG_ONLY_THIS_PROCESS = 0x00000002.toUInt
  final val DEBUG_PROCESS = 0x00000001.toUInt
  final val DETACHED_PROCESS = 0x00000008.toUInt
  final val EXTENDED_STARTUPINFO_PRESENT = 0x00080000.toUInt
  final val INHERIT_PARENT_AFFINITY = 0x00010000.toUInt

  // Thread creation flags
  // final val CREATE_SUSPENDED = 0x00000004.toUInt // duplicated with process flag
  final val STACK_SIZE_PARAM_IS_A_RESERVATION = 0x00010000.toUInt

  // Thread Priority
  final val THREAD_MODE_BACKGROUND_BEGIN = 0x00010000
  final val THREAD_MODE_BACKGROUND_END = 0x00020000
  final val THREAD_PRIORITY_IDLE = -15
  final val THREAD_PRIORITY_LOWEST = -2
  final val THREAD_PRIORITY_BELOW_NORMAL = -1
  final val THREAD_PRIORITY_NORMAL = 0
  final val THREAD_PRIORITY_ABOVE_NORMAL = 1
  final val THREAD_PRIORITY_HIGHEST = 2
  final val THREAD_PRIORITY_TIME_CRITICAL = 15
}
object ProcessThreadsApiOps {
  import ProcessThreadsApi._
  implicit class StartupInfoWOps(ref: Ptr[StartupInfoW])(implicit
      tag: Tag[StartupInfoW]
  ) {
    def cb: DWord = ref._1
    def reserved: CWString = ref._2
    def desktop: CWString = ref._3
    def title: CWString = ref._4
    def x: DWord = ref._5
    def y: DWord = ref._6
    def xSize: DWord = ref._7
    def ySize: DWord = ref._8
    def xCountChars: DWord = ref._9
    def yCountChars: DWord = ref._10
    def fillAtribute: DWord = ref._11
    def flags: DWord = ref._12
    def showWindow: Word = ref._13
    def cbReserved2: Word = ref._14
    def lpReserved2: Ptr[Byte] = ref._15
    def stdInput: Handle = ref._16
    def stdOutput: Handle = ref._17
    def stdError: Handle = ref._18

    def cb_=(v: DWord): Unit = ref._1 = v
    def reserved_=(v: CWString): Unit = ref._2 = v
    def desktop_=(v: CWString): Unit = ref._3 = v
    def title_=(v: CWString): Unit = ref._4 = v
    def x_=(v: DWord): Unit = ref._5 = v
    def y_=(v: DWord): Unit = ref._6 = v
    def xSize_=(v: DWord): Unit = ref._7 = v
    def ySize_=(v: DWord): Unit = ref._8 = v
    def xCountChars_=(v: DWord): Unit = ref._9 = v
    def yCountChars_=(v: DWord): Unit = ref._10 = v
    def fillAtribute_=(v: DWord): Unit = ref._11 = v
    def flags_=(v: DWord): Unit = ref._12 = v
    def showWindow_=(v: Word): Unit = ref._13 = v
    def cbReserved2_=(v: Word): Unit = ref._14 = v
    def lpReserved2_=(v: Ptr[Byte]): Unit = ref._15 = v
    def stdInput_=(v: Handle): Unit = ref._16 = v
    def stdOutput_=(v: Handle): Unit = ref._17 = v
    def stdError_=(v: Handle): Unit = ref._18 = v
  }

  implicit class ProcessInformationOps(ref: Ptr[ProcessInformation])(implicit
      tag: Tag[ProcessInformation]
  ) {
    def process: Handle = ref._1
    def thread: Handle = ref._2
    def processId: DWord = ref._3
    def threadId: DWord = ref._4

    def process_=(v: Handle): Unit = ref._1 = v
    def thread_=(v: Handle): Unit = ref._2 = v
    def processId_=(v: DWord): Unit = ref._3 = v
    def threadId_=(v: DWord): Unit = ref._4 = v
  }
}
