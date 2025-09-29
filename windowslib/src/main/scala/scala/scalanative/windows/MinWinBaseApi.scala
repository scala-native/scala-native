package scala.scalanative.windows

import scala.language.implicitConversions

import scala.scalanative.unsafe.{Word => _, _}
import scala.scalanative.unsigned._
import scala.scalanative.windows.util.Conversion

import HandleApi._

object MinWinBaseApi {
  type FileTime = ULargeInteger
  type FileTimeStruct = CStruct2[DWord, DWord]
  type SystemTime = CStruct8[Word, Word, Word, Word, Word, Word, Word, Word]
  type DUMMYSTRUCTNAME = CStruct2[DWord, DWord]
  type OVERLAPPED = CStruct4[ULong, ULong, DUMMYSTRUCTNAME, Handle]
}

object MinWinBaseApiOps {
  import MinWinBaseApi._
  implicit class FileTimeStructOps(ref: Ptr[FileTimeStruct]) {
    def lowFileTime: DWord = ref._1
    def highFileTime: DWord = ref._2
    def fileTime: FileTime = {
      Conversion.dwordPairToULargeInteger(
        high = highFileTime,
        low = lowFileTime
      )
    }
    def lowFileTime_=(v: DWord): Unit = ref._1
    def highFileTime_=(v: DWord): Unit = ref._2
    def fileTime_=(v: FileTime): Unit = {
      Conversion.uLargeIntegerToDWordPair(v, high = ref.at1, low = ref.at2)
    }
  }

  object FileTimeOps {
    private final val EpochPerSecond = 10000000L.toULong
    private final val EpochPerMilis = 10000L.toULong
    final val EpochInterval = 100L // ns
    final val UnixEpochDifference = 116444736000000000L
    final val UnixEpochDifferenceMillis = 11644473600000L
    final val UnixEpochDifferenceSeconds = 11644473600L

    def toUnixEpochSeconds(filetime: FileTime): Long = {
      val windowsSeconds = filetime / EpochPerSecond
      windowsSeconds.toLong - UnixEpochDifferenceSeconds
    }

    def toUnixEpochMillis(filetime: FileTime): Long = {
      val windowsMillis = filetime / EpochPerMilis
      windowsMillis.toLong - UnixEpochDifferenceMillis
    }

    def fromUnixEpoch(epoch: Long): FileTime = {
      val windowsSeconds = (epoch + UnixEpochDifferenceSeconds).toULong
      windowsSeconds * EpochPerSecond
    }

    def fromUnixEpochMillis(epochMillis: Long): FileTime = {
      val windowsMillis = (epochMillis + UnixEpochDifferenceMillis).toULong
      windowsMillis * EpochPerMilis
    }
  }

  implicit class SystemTimeOps(val ref: Ptr[SystemTime]) extends AnyVal {
    def year: Word = ref._1
    def month: Word = ref._2
    def dayOfWeek: Word = ref._3
    def day: Word = ref._4
    def hour: Word = ref._5
    def minute: Word = ref._6
    def second: Word = ref._7
    def milliseconds: Word = ref._8

    def year_=(v: Word): Unit = ref._1
    def month_=(v: Word): Unit = ref._2
    def dayOfWeek_=(v: Word): Unit = ref._3
    def day_=(v: Word): Unit = ref._4
    def hour_=(v: Word): Unit = ref._5
    def minute_=(v: Word): Unit = ref._6
    def second_=(v: Word): Unit = ref._7
    def milliseconds_=(v: Word): Unit = ref._8
  }

  implicit class OverlappedOps(val ref: Ptr[OVERLAPPED]) extends AnyVal {
    def Internal: ULong = ref._1
    def InternalHigh: ULong = ref._2
    def DUMMYSTRUCTNAME: DUMMYSTRUCTNAME = ref._3
    def hEvent: Handle = ref._4

    def Internal_=(v: ULong): Unit = ref._1 = v
    def InternalHigh_=(v: ULong): Unit = ref._2 = v
    def DUMMYSTRUCTNAME_=(v: DUMMYSTRUCTNAME): Unit = ref._3 = v
    def hEvent_=(v: Handle): Unit = ref._4 = v
  }

  implicit class DUMMYSTRUCTNAMEOps(val ref: Ptr[DUMMYSTRUCTNAME])
      extends AnyVal {
    def Offset: DWord = ref._1
    def OffsetHigh: DWord = ref._2

    def Offset_=(v: DWord) = ref._1 = v
    def OffsetHigh_=(v: DWord) = ref._2 = v
  }
}
