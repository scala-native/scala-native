package scala.scalanative.windows

import scala.scalanative.unsafe.{Word as _, *}

@extern
object SysInfoApi {
  import MinWinBaseApi.*

  type SystemInfo = CStruct10[
    DWord, // oemId
    DWord, // pagesSize
    CVoidPtr, // minimum application address
    CVoidPtr, // max application address
    Ptr[DWord], // active processors mask
    DWord, // number of processors
    DWord, // processor type
    DWord, // allocation granularity
    Word, // processor level
    Word // processor revision
  ]

  def GetLocalTime(timeStruct: Ptr[SystemTime]): Unit = extern
  def GetSystemTime(timeStruct: Ptr[SystemTime]): Unit = extern

  def SetLocalTime(timeStruct: Ptr[SystemTime]): Boolean = extern
  def SetSystemTime(timeStruct: Ptr[SystemTime]): Boolean = extern

  def GetSystemInfo(info: Ptr[SystemInfo]): Unit = extern
}

object SysInfoApiOps {
  import SysInfoApi.*
  implicit class SystemInfoOps(val ref: Ptr[SystemInfo]) extends AnyVal {
    def oemId: DWord = ref._1
    def pagesSize: DWord = ref._2
    def minimumApplicationAddress: CVoidPtr = ref._3
    def maximalApplicationAddress: CVoidPtr = ref._4
    def activeProcessorsMask: Ptr[DWord] = ref._5
    def numberOfProcessors: DWord = ref._6
    def processorType: DWord = ref._7
    def allocationGranularity: DWord = ref._8
    def processorLevel: Word = ref._9
    def processorRevision: Word = ref._10
  }
}
