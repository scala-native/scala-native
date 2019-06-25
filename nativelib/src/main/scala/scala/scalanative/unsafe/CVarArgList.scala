package scala.scalanative
package unsafe

import scala.language.implicitConversions
import scalanative.unsigned._
import scalanative.runtime.{intrinsic, RawPtr, toRawPtr, libc, LongArray}

/** Type of a C-style vararg list (va_list in C). */
final class CVarArgList private[scalanative] (
    private[scalanative] val rawptr: RawPtr)

object CVarArgList {
  // Implementation below is based on VarArgs.swift from apple/swift repo.
  // Currently we only support X86_64, we'll need to revisit the code below
  // if and when we add any more officially supported architectures.

  private type Header =
    CStruct4[CUnsignedInt, CUnsignedInt, Ptr[Long], Ptr[Long]]
  private implicit class HeaderOps(val ptr: Ptr[Header]) extends AnyVal {
    def gpOffset: CUnsignedInt     = ptr._1
    def fpOffset: CUnsignedInt     = ptr._2
    def overflowArgArea: Ptr[Long] = ptr._3
    def regSaveArea: Ptr[Long]     = ptr._4

    def gpOffset_=(value: CUnsignedInt): Unit     = ptr._1 = value
    def fpOffset_=(value: CUnsignedInt): Unit     = ptr._2 = value
    def overflowArgArea_=(value: Ptr[Long]): Unit = ptr._3 = value
    def regSaveArea_=(value: Ptr[Long]): Unit     = ptr._4 = value
  }

  private final val countGPRegisters  = 6
  private final val countFPRegisters  = 8
  private final val fpRegisterWords   = 2
  private final val registerSaveWords = countGPRegisters + countFPRegisters * fpRegisterWords

  /** Construct C-style vararg list from Scala sequence. */
  private[scalanative] def fromSeq(varargs: Seq[CVarArg])(
      implicit z: Zone): CVarArgList = {
    var storage         = new Array[Long](registerSaveWords)
    var wordsUsed       = storage.size
    var gpRegistersUsed = 0
    var fpRegistersUsed = 0

    def isPassedAsDouble(vararg: CVarArg): Boolean =
      vararg.tag == Tag.Float || vararg.tag == Tag.Double

    def encode[T](value: T)(implicit tag: Tag[T]): Array[Long] = value match {
      case value: Byte =>
        encode(value.toLong)
      case value: Short =>
        encode(value.toLong)
      case value: Int =>
        encode(value.toLong)
      case value: UByte =>
        encode(value.toULong)
      case value: UShort =>
        encode(value.toULong)
      case value: UInt =>
        encode(value.toULong)
      case value: Float =>
        encode(value.toDouble)
      case _ =>
        val count = ((sizeof(tag) + sizeof[Long] - 1) / sizeof[Long]).toInt
        val words = new Array[Long](count)
        val start = words.asInstanceOf[LongArray].at(0).asInstanceOf[Ptr[T]]
        tag.store(start, value)
        words
    }

    def append(word: Long): Unit = {
      if (wordsUsed == storage.size) {
        val newstorage = new Array[Long](storage.size * 2)
        System.arraycopy(storage, 0, newstorage, 0, storage.size)
        storage = newstorage
      }
      storage(wordsUsed) = word
      wordsUsed += 1
    }

    varargs.foreach { vararg =>
      val encoded  = encode(vararg.value)(vararg.tag)
      val isDouble = isPassedAsDouble(vararg)

      if (isDouble && fpRegistersUsed < countFPRegisters) {
        var startIndex = countGPRegisters + (fpRegistersUsed * fpRegisterWords)
        encoded.foreach { w =>
          storage(startIndex) = w
          startIndex += 1
        }
        fpRegistersUsed += 1
      } else if (encoded.size == 1 && !isDouble && gpRegistersUsed < countGPRegisters) {
        val startIndex = gpRegistersUsed
        storage(startIndex) = encoded(0)
        gpRegistersUsed += 1
      } else {
        encoded.foreach(append)
      }
    }

    val resultStorage =
      z.alloc(sizeof[Long] * storage.size).asInstanceOf[Ptr[Long]]
    val storageStart = storage.asInstanceOf[LongArray].at(0)
    libc.memcpy(toRawPtr(resultStorage),
                toRawPtr(storageStart),
                wordsUsed * sizeof[Long])

    val resultHeader = z.alloc(sizeof[Header]).asInstanceOf[Ptr[Header]]
    resultHeader.gpOffset = 0.toUInt
    resultHeader.fpOffset = (countGPRegisters * sizeof[Long]).toUInt
    resultHeader.regSaveArea = resultStorage
    resultHeader.overflowArgArea = resultStorage + registerSaveWords

    new CVarArgList(toRawPtr(resultHeader))
  }
}
