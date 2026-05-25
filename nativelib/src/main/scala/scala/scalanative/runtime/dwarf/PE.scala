package scala.scalanative.runtime.dwarf

import scalanative.unsigned._

private[runtime] case class PE(
    sectionHeaders: Array[PE.SectionHeader],
    isPositionIndependentBinary: Boolean,
    preferredImageBase: Long
)

private[runtime] object PE {
  import CommonParsers._

  private final val MZ_MAGIC = 0x5a4d.toUShort
  private final val PE_MAGIC = 0x00004550.toUInt
  private final val PE32_PLUS_MAGIC = 0x020b.toUShort
  private final val IMAGE_DLL_CHARACTERISTICS_DYNAMIC_BASE = 0x0040.toUShort
  private final val SECTION_HEADER_SIZE = 40

  def parse(ds: BinaryFile): PE = {
    implicit val stream: BinaryFile = ds
    implicit val endi: Endianness = Endianness.LITTLE

    ds.seek(0)
    val mz = uint16()
    if (mz != MZ_MAGIC) {
      throw new IllegalArgumentException(
        s"Not a PE file, expected MZ magic, got 0x${Integer.toHexString(mz.toInt)}"
      )
    }

    ds.seek(0x3c)
    val peOffset = uint32().toLong

    ds.seek(peOffset)
    val signature = uint32()
    if (signature != PE_MAGIC) {
      throw new IllegalArgumentException(
        s"Not a PE file, expected PE signature, got 0x${Integer.toHexString(signature.toInt)}"
      )
    }

    // COFF file header
    val _machine = uint16()
    val numberOfSections = uint16()
    stream.skipNBytes(4L) // TimeDateStamp
    val pointerToSymbolTable = uint32()
    val numberOfSymbols = uint32()
    val sizeOfOptionalHeader = uint16()
    val _characteristics = uint16()

    val optionalHeaderOffset = peOffset + 4 + 20
    ds.seek(optionalHeaderOffset)
    val optionalMagic = uint16()
    if (optionalMagic != PE32_PLUS_MAGIC) {
      throw new IllegalArgumentException(
        s"Unsupported PE optional header magic 0x${Integer.toHexString(optionalMagic.toInt)}"
      )
    }

    // PE32+ IMAGE_OPTIONAL_HEADER64.ImageBase and DllCharacteristics
    ds.seek(optionalHeaderOffset + 0x18)
    val preferredImageBase = uint64()
    ds.seek(optionalHeaderOffset + 0x46)
    val dllCharacteristics = uint16()
    val isPIE = (dllCharacteristics & IMAGE_DLL_CHARACTERISTICS_DYNAMIC_BASE) != 0

    val sectionTableOffset = optionalHeaderOffset + sizeOfOptionalHeader.toLong
    // COFF string table follows the symbol table (18 bytes per symbol).
    val coffStringTableOffset =
      pointerToSymbolTable.toLong + numberOfSymbols.toLong * 18L

    val sectionHeaders =
      readSectionHeaders(numberOfSections, sectionTableOffset, coffStringTableOffset)

    PE(sectionHeaders.toArray, isPIE, preferredImageBase)
  }

  private def readSectionHeaders(
      numberOfSections: UShort,
      sectionTableOffset: Long,
      coffStringTableOffset: Long
  )(implicit
      stream: BinaryFile,
      endi: Endianness
  ) = {
    val headers = new collection.mutable.ListBuffer[SectionHeader]
    var i = 0
    while (i < numberOfSections.toInt) {
      stream.seek(sectionTableOffset + i.toLong * SECTION_HEADER_SIZE)
      val nameBytes = stream.readNBytes(8)
      val name = resolveSectionName(nameBytes, coffStringTableOffset)
      stream.seek(sectionTableOffset + i.toLong * SECTION_HEADER_SIZE + 8)
      val virtualSize = uint32()
      val virtualAddress = uint32()
      val sizeOfRawData = uint32()
      val pointerToRawData = uint32()

      headers.append(
        SectionHeader(
          name = name,
          virtualAddress = virtualAddress.toLong,
          offset = pointerToRawData.toLong,
          size = sizeOfRawData.toLong
        )
      )
      i += 1
    }
    headers.toList
  }

  private def resolveSectionName(
      nameBytes: Array[Byte],
      coffStringTableOffset: Long
  )(implicit stream: BinaryFile, endi: Endianness): String = {
    val shortName = new String(nameBytes.takeWhile(_ != 0), "ASCII")
    if (shortName.startsWith("/")) {
      val index = scala.util.Try(shortName.drop(1).toInt).getOrElse(0)
      stream.seek(coffStringTableOffset + index)
      new String(stream.readWhile(_ != 0), "ASCII")
    } else {
      shortName
    }
  }

  case class SectionHeader(
      name: String,
      virtualAddress: Long,
      offset: Long,
      size: Long
  )
}
