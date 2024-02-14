package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.WinBaseApi._
import HandleApi.Handle

@extern
object SynchApi {
  type CriticalSection = CVoidPtr
  type ConditionVariable = CVoidPtr

  @name("scalanative_sizeof_CriticalSection")
  def SizeOfCriticalSection: CSize = extern

  @name("scalanative_sizeof_ConditionVariable")
  def SizeOfConditionVariable: CSize = extern

  def InitializeConditionVariable(conditionVariable: ConditionVariable): Unit =
    extern
  def InitializeCriticalSection(criticalSection: CriticalSection): Unit =
    extern
  def InitializeCriticalSectionAndSpinCount(
      criticalSection: CriticalSection,
      spinCount: DWord
  ): Boolean = extern
  def InitializeCriticalEx(
      criticalSection: CriticalSection,
      spinCount: DWord,
      flags: DWord
  ): Boolean = extern
  def DeleteCriticalSection(criticalSection: CriticalSection): Unit = extern

  def CreateEventA(
      eventAttributes: Ptr[SecurityAttributes],
      name: CString,
      flags: DWord,
      desiredAccess: DWord
  ): Handle = extern
  def CreateEventExA(
      eventAttributes: Ptr[SecurityAttributes],
      manualReset: Boolean,
      initialState: Boolean,
      name: CString
  ): Handle = extern
  def CreateEventExW(
      eventAttributes: Ptr[SecurityAttributes],
      manualReset: Boolean,
      initialState: Boolean,
      name: CWString
  ): Handle = extern
  def CreateEventW(
      eventAttributes: Ptr[SecurityAttributes],
      manualReset: Boolean,
      initialState: Boolean,
      name: CWString
  ): Handle = extern
  def ResetEvent(event: Handle): Boolean = extern
  def SetEvent(event: Handle): Boolean = extern

  def SetCriticalSectionSpinCount(
      criticalSection: CriticalSection,
      spinCount: DWord
  ): DWord = extern

  def TryEnterCriticalSection(criticalSection: CriticalSection): Boolean =
    extern
  @blocking
  def EnterCriticalSection(criticalSection: CriticalSection): Unit = extern
  def LeaveCriticalSection(criticalSection: CriticalSection): Unit = extern

  @blocking def Sleep(milliseconds: DWord): Unit = extern
  @blocking def SleepConditionVariableCS(
      conditionVariable: ConditionVariable,
      criticalSection: CriticalSection,
      milliseconds: DWord
  ): Boolean = extern
  def WakeAllConditionVariable(conditionVariable: ConditionVariable): Unit =
    extern
  def WakeConditionVariable(conditionVariable: ConditionVariable): Unit = extern
  @blocking def WaitForSingleObject(
      ref: Handle,
      miliseconds: DWord
  ): DWord = extern

}

object SynchApiExt {
  final val WAIT_ABANDONED = 0x00000080L.toUInt
  final val WAIT_OBJECT_0 = 0x00000000L.toUInt
  final val WAIT_TIMEOUT = 0x00000102L.toUInt
  final val WAIT_FAILED = 0xffffffff.toUInt
}
