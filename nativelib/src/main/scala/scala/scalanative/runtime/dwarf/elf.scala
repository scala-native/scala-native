package scala.scalanative.runtime.dwarf

import scalanative.unsigned._

private[runtime] case class ELF private (
    header: ELF.Header,
    sectionHeaders: Array[ELF.SectionHeader]
) {}

object ELF {
  import CommonParsers._

  def parse(ds: BinaryFile): ELF = {
    implicit val stream = ds

    val magic = uint32()(Endianness.BIG, stream)
    val cls = uint8()(Endianness.BIG, stream)
    val endianness = uint8()(Endianness.BIG, stream)
    val version = uint8()(Endianness.BIG, stream)
    val abi = uint8()(Endianness.BIG, stream)
    val abi_version = uint8()(Endianness.BIG, stream)
    val padding = stream.skipNBytes(7L)

    implicit val endi: Endianness =
      if (endianness == 1) Endianness.LITTLE else Endianness.BIG
    implicit val bits: Bits = if (cls == 1) Bits.X32 else Bits.X64

    val header = Header(
      magic = magic,
      cls = cls.toByte,
      endianness = endianness.toByte,
      version = version.toByte,
      abi = abi.toByte,
      abiVersion = abi_version.toByte,
      fileType = uint16(),
      machine = uint16(),
      versionAgain = uint32(),
      entryPointAddress = readVariableSize(),
      programHeaderStart = readVariableSize(),
      sectionsHeaderStart = readVariableSize(),
      flags = uint32(),
      headerSize = uint16(),
      programHeaderSize = uint16(),
      programHeaderEntries = uint16(),
      sectionsHeaderSize = uint16(),
      sectionsHeaderEntries = uint16(),
      sectionNamesEntryIndex = uint16()
    )

    ds.seek(header.sectionsHeaderStart)
    val sectionHeaders =
      readSectionHeaders(header.sectionsHeaderEntries)

    ELF(header, sectionHeaders.toArray)
  }

  private def readSectionHeaders(
      entries: UShort
  )(implicit ds: BinaryFile, bits: Bits, endi: Endianness) = {
    val headers = new collection.mutable.ListBuffer[SectionHeader]
    for (_ <- 0 until entries.toInt) {
      val header = SectionHeader(
        sectionNameAddress = uint32(),
        sectionType = uint32(),
        flags = readVariableSize(),
        virtualAddressInMemory = readVariableSize(),
        offset = readVariableSize(),
        size = readVariableSize(),
        sectionIndex = uint32(),
        sectionInfo = uint32(),
        sectionAlignment = readVariableSize(),
        entrySize = readVariableSize()
      )
      headers.append(header)
    }
    headers.toList
  }

  case class Header(
      magic: UInt,
      cls: Byte,
      endianness: Byte,
      version: Byte,
      abi: Byte,
      abiVersion: Byte,
      fileType: UShort,
      machine: UShort,
      versionAgain: UInt,
      entryPointAddress: Long,
      programHeaderStart: Long,
      sectionsHeaderStart: Long,
      flags: UInt,
      headerSize: UShort,
      programHeaderSize: UShort,
      programHeaderEntries: UShort,
      sectionsHeaderSize: UShort,
      sectionsHeaderEntries: UShort,
      sectionNamesEntryIndex: UShort
  )

  case class SectionHeader(
      sectionNameAddress: UInt,
      sectionType: UInt,
      flags: Long,
      virtualAddressInMemory: Long,
      offset: Long,
      size: Long,
      sectionIndex: UInt,
      sectionInfo: UInt,
      sectionAlignment: Long,
      entrySize: Long
  ) {
    private var sectionName: Option[String] = None
    def getName(sectionNamesOffset: Long)(implicit bf: BinaryFile): String = {
      sectionName.getOrElse {
        bf.seek(sectionNamesOffset)
        bf.skipNBytes(sectionNameAddress.toLong)
        val name = new String(bf.readWhile(_ != 0))
        sectionName = Some(name)
        name
      }
    }
  }

  private def readVariableSize()(implicit
      ds: BinaryFile,
      bits: Bits,
      endi: Endianness
  ) =
    bits match {
      case Bits.X32 => uint32().toLong
      case Bits.X64 => uint64()
    }

  private sealed abstract class Bits
  private object Bits {
    case object X32 extends Bits
    case object X64 extends Bits
  }

}
