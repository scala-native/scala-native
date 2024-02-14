package scala.scalanative.windows

import scala.scalanative.unsafe.{Word => _, _}

@extern
object SysInfoApi {
  import MinWinBaseApi._

  type SystemInfo = CStruct10[
    DWord, // oemId
    DWord, // pagesSize
    Ptr[_], // minimum application address
    Ptr[_], // max application address
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
  import SysInfoApi._
  implicit class SystemInfoOps(val ref: Ptr[SystemInfo]) extends AnyVal {
    def oemId: DWord = ref._1
    def pagesSize: DWord = ref._2
    def minimumApplicationAddress: Ptr[_] = ref._3
    def maximalApplicationAddress: Ptr[_] = ref._4
    def activeProcessorsMask: Ptr[DWord] = ref._5
    def numberOfProcessors: DWord = ref._6
    def processorType: DWord = ref._7
    def allocationGranularity: DWord = ref._8
    def processorLevel: Word = ref._9
    def processorRevision: Word = ref._10
  }
}
