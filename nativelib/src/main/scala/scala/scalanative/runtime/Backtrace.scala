package scala.scalanative.runtime

import java.io.File
import scala.scalanative.runtime.dwarf.BinaryFile
import java.io.RandomAccessFile
import scala.scalanative.runtime.dwarf.MachO
import scala.scalanative.runtime.dwarf.DWARF
import scala.collection.mutable
import scala.scalanative.unsafe.CSize
import scala.scalanative.unsafe.Tag
import scala.scalanative.unsigned.UInt
import scala.collection.concurrent.TrieMap
import scala.scalanative.unsafe.Zone
import scalanative.unsigned._
import scala.io.Source
import scala.scalanative.runtime.dwarf.DWARF.CompileUnit
import scala.annotation.tailrec
import scala.scalanative.runtime.dwarf.DWARF.DIE

object Backtrace {
  private sealed trait Format
  private case object MACHO extends Format
  private case object ELF extends Format
  private case class DwarfInfo(
      subprograms: IndexedSeq[SubprogramDIE],
      strings: DWARF.Strings,
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

  private val cache = TrieMap.empty[String, Option[DwarfInfo]]

  def decodeFileline(pc: Long)(implicit zone: Zone): Option[(String, Int)] = {
    cache.get(filename) match {
      case Some(None) =>
        None // cached, there's no debug section
      case Some(Some(info)) =>
        impl(pc, info)
      case None =>
        processFile(filename, None) match {
          case None =>
            // there's no debug section, cache it so we don't parse the exec file any longer
            println("DIE not found, cache")
            cache.put(filename, None)
            None
          case file @ Some(info) =>
            println("DIE found ")
            cache.put(filename, file)
            impl(pc, info)
        }
    }
  }

  private def impl(
      pc: Long,
      info: DwarfInfo
  ): Option[(String, Int)] = {
    val address = pc - info.offset
    for {
      subprogram <- search(info.subprograms, address)
      at <- subprogram.filenameAt
    } yield {
      val filename = info.strings.read(at)
      (filename, subprogram.line + 1)
    }
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

  private def offsetOSX(macho: MachO): Long = {
    // calculate offset
    val pid = libc.getpid().intValue()
    val proc = new ProcessBuilder("vmmap", "-summary", s"$pid").start()
    // TODO: close input stream
    val vmmap = Source.fromInputStream(proc.getInputStream())
    val offset = (for {
      loadAddress <- vmmap
        .getLines()
        .find(_.startsWith("Load Address"))
        .map { line =>
          val addressStr = line.split(":").last.trim.drop(2)
          java.lang.Long.parseLong(addressStr, 16)
        }
      pageZeroSize <- macho.segments
        .find(_.segname == "__PAGEZERO")
        .map(_.vmsize)
    } yield loadAddress - pageZeroSize).getOrElse(0L)
    offset
  }

  private def filterSubprograms(dies: Vector[CompileUnit], strings: DWARF.Strings) = {
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
    implicit val bf: BinaryFile = new BinaryFile(
      new RandomAccessFile(filename, "r")
    )

    val head = bf.position()
    val magic = bf.readInt().toUInt.toHexString
    bf.seek(head)
    if (magic == MACHO_MAGIC) {
      val macho = MachO.parse(bf)
      val dwarfOpt: Option[(Vector[DIE], DWARF.Strings)] =
        processMacho(macho).orElse {
          val basename = new File(filename).getName()
          val dSymPath =
            s"$filename.dSYM/Contents/Resources/DWARF/${basename}"
          if (new File(dSymPath).exists()) {
            val dSYMBin: BinaryFile = new BinaryFile(
              new RandomAccessFile(dSymPath, "r")
            )
            val dSYMMacho = MachO.parse(dSYMBin)
            if (dSYMMacho.uuid == macho.uuid)
              processMacho(dSYMMacho)(dSYMBin)
            else None
          } else None
        }

      for {
        dwarf <- dwarfOpt
        dies = dwarf._1.flatMap(_.units)
        subprograms = filterSubprograms(dies, dwarf._2)
        offset = offsetOSX(macho)
      } yield {
        DwarfInfo(
          subprograms = subprograms,
          strings = dwarf._2,
          offset = offset,
          format = MACHO
        )
      }
    } else if (magic == ELF_MAGIC) {
      sys.error(
        "ELF is not supported yet, will someone please write an ELF parser"
      )
    } else { // COFF has various magic numbers
      sys.error(
        "Windows is not supported yet, will someone please write a COFF parser, or whatever windows uses"
      )
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
