package scala.scalanative.runtime

import scala.scalanative.runtime.dwarf.BinaryFile
import scala.scalanative.runtime.dwarf.MachO
import scala.scalanative.runtime.dwarf.DWARF
import scala.scalanative.runtime.dwarf.DWARF.DIE
import scala.scalanative.runtime.dwarf.DWARF.CompileUnit

import scala.scalanative.unsafe.CSize
import scala.scalanative.unsafe.Tag
import scala.scalanative.unsafe.Zone
import scala.scalanative.unsigned.UInt
import scalanative.unsigned._

import scala.annotation.tailrec

import java.io.File
import java.util.concurrent.ConcurrentHashMap

object Backtrace {
  private sealed trait Format
  private case object MACHO extends Format
  private case object ELF extends Format
  private case class DwarfInfo(
      subprograms: IndexedSeq[SubprogramDIE],
      strings: DWARF.Strings,
      /** ASLR offset (minus __PAGEZERO size for macho) */
      offset: Long,
      format: Format
  )

  private case class SubprogramDIE(
      lowPC: Long,
      highPC: Long,
      line: Int,
      filenameAt: Option[UInt]
  )

  private val MACHO_MAGIC = "cffaedfe"
  private val ELF_MAGIC = "7f454c46"

  private val cache = new ConcurrentHashMap[String, Option[DwarfInfo]]
  case class Position(filename: String, line: Int)
  object Position {
    final val empty = Position(null, 0)
  }

  def decodePosition(pc: Long): Position = {
    cache.get(filename) match {
      case None =>
        Position.empty // cached, there's no debug section
      case Some(info) =>
        impl(pc, info)
      case null =>
        processFile(filename, None) match {
          case None =>
            // there's no debug section, cache it so we don't parse the exec file any longer
            cache.put(filename, None)
            Position.empty
          case file @ Some(info) =>
            cache.put(filename, file)
            impl(pc, info)
        }
    }
  }

  private def impl(
      pc: Long,
      info: DwarfInfo
  ): Position = {
    // The address (DW_AT_(low|high)_address) in debug information has the file offset (the offset in the executable + __PAGEZERO in macho).
    // While the pc address retrieved from libunwind at runtime has the location of the memory into the virtual memory
    // at runtime. which has a random offset (called ASLR offset or slide) that is different for every run because of
    // Address Space Layout Randomization (ASLR) when the executable is built as PIE.
    // Subtract the offset to match the pc address from libunwind (runtime) and address in debug info (compile/link time).
    val address = pc - info.offset
    val position = for {
      subprogram <- search(info.subprograms, address)
      at <- subprogram.filenameAt
    } yield {
      val filename = info.strings.read(at)
      Position(filename, subprogram.line + 1) // line number in DWARF is 0-based
    }
    position.getOrElse(Position.empty)
  }

  private def search(
      dies: IndexedSeq[SubprogramDIE],
      address: Long
  ): Option[SubprogramDIE] = {
    val length = dies.length
    @tailrec
    def binarySearch(from: Int, to: Int): Option[SubprogramDIE] = {
      if (from < 0) binarySearch(0, to)
      else if (to > length) binarySearch(from, length)
      else if (to <= from) None
      else {
        val idx = from + (to - from - 1) / 2
        val die = dies(idx)
        if (die.lowPC <= address && address <= die.highPC) Some(die)
        else if (address < die.lowPC) binarySearch(from, idx)
        else // die.highPC < address
          binarySearch(idx + 1, to)
      }
    }
    binarySearch(0, length)
  }

  private def processMacho(
      macho: MachO
  )(implicit bf: BinaryFile): Option[(Vector[DIE], DWARF.Strings)] = {
    val sections = macho.segments.flatMap(_.sections)
    for {
      debug_info <- sections.find(_.sectname == "__debug_info")
      debug_abbrev <- sections.find(_.sectname == "__debug_abbrev")
      debug_str <- sections.find(_.sectname == "__debug_str")
      debug_line <- sections.find(_.sectname == "__debug_line")
    } yield {
      readDWARF(
        debug_info = DWARF.Section(debug_info.offset, debug_info.size),
        debug_abbrev = DWARF.Section(debug_abbrev.offset, debug_abbrev.size),
        debug_str = DWARF.Section(debug_str.offset, debug_str.size)
      )
    }
  }

  private def filterSubprograms(dies: Vector[CompileUnit]) = {
    var filenameAt: Option[UInt] = None
    dies
      .flatMap { die =>
        if (die.is(DWARF.Tag.DW_TAG_subprogram)) {
          for {
            line <- die.getLine
            low <- die.getLowPC
            high <- die.getHighPC(low)
          } yield SubprogramDIE(low, high, line, filenameAt)
        } else if (die.is(DWARF.Tag.DW_TAG_compile_unit)) {
          // Debug Information Entries (DIE) in DWARF has a tree structure, and
          // the DIEs after the Compile Unit DIE belongs to that compile unit (file in Scala)
          // TODO: Parse `.debug_line` section, and decode the filename using
          // `DW_AT_decl_file` attribute of the `subprogram` DIE.
          filenameAt = die.getName
          None
        } else None
      }
      .sortBy(_.lowPC)
      .toIndexedSeq
  }

  private def processFile(
      filename: String,
      matchUUID: Option[List[UInt]]
  ): Option[DwarfInfo] = {
    implicit val bf: BinaryFile = new BinaryFile(new File(filename))
    val head = bf.position()
    val magic = bf.readInt().toUInt.toHexString
    bf.seek(head)
    if (magic == MACHO_MAGIC) {
      val macho = MachO.parse(bf)
      val dwarfOpt: Option[(Vector[DIE], DWARF.Strings)] =
        processMacho(macho).orElse {
          val basename = new File(filename).getName()
          // dsymutil `foo` will assemble the debug information into `foo.dSYM/Contents/Resources/DWARF/foo`.
          // Coulnt't find the official source, but at least libbacktrace locate the dSYM file from this location.
          // https://github.com/ianlancetaylor/libbacktrace/blob/cdb64b688dda93bbbacbc2b1ccf50ce9329d4748/macho.c#L908
          val dSymPath =
            s"$filename.dSYM/Contents/Resources/DWARF/${basename}"
          if (new File(dSymPath).exists()) {
            val dSYMBin: BinaryFile = new BinaryFile(
              new File(dSymPath)
            )
            val dSYMMacho = MachO.parse(dSYMBin)
            if (dSYMMacho.uuid == macho.uuid) // Validate the macho in dSYM has the same build uuid.
              processMacho(dSYMMacho)(dSYMBin)
            else None
          } else None
        }

      for {
        dwarf <- dwarfOpt
        dies = dwarf._1.flatMap(_.units)
        subprograms = filterSubprograms(dies)
        offset = vmoffset.get_vmoffset()
      } yield {
        DwarfInfo(
          subprograms = subprograms,
          strings = dwarf._2,
          offset = offset,
          format = MACHO
        )
      }
    } else if (magic == ELF_MAGIC) {
      None
    } else { // COFF has various magic numbers
      None
    }

  }
  def readDWARF(
      debug_info: DWARF.Section,
      debug_abbrev: DWARF.Section,
      debug_str: DWARF.Section
  )(implicit bf: BinaryFile) = {
    DWARF.parse(
      debug_info = DWARF.Section(debug_info.offset, debug_info.size),
      debug_abbrev = DWARF.Section(debug_abbrev.offset, debug_abbrev.size)
    ) ->
      DWARF.Strings.parse(
        DWARF.Section(debug_str.offset, debug_str.size)
      )
  }
}
