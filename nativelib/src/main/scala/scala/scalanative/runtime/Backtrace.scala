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

object Backtrace {
  private val MACHO_MAGIC = "cffaedfe"
  private val ELF_MAGIC = "7f454c46"

  private val cache = TrieMap.empty[String, (Vector[DWARF.DIE], DWARF.Strings)]

  def decodeFileline(pc: Long)(implicit zone: Zone): Option[(String, Int)] = {
    println(s"decodeFileline: $pc")
    synchronized {
      cache.get(filename) match {
        case Some((dies, strings)) if dies.isEmpty =>
          None // cached, there's no debug section
        case Some((dies, strings)) =>
          impl(pc, dies, strings)
        case None =>
          processFile match {
            case None =>
              // there's no debug section, cache it so we don't parse the exec file any longer
              println("DIE not found, cache")
              cache.put(filename, (Vector.empty, DWARF.Strings.empty))
              None
            case Some((dies, strings)) =>
              println("DIE found ")
              cache.put(filename, (dies, strings))
              impl(pc, dies, strings)
          }
      }
    }
  }

  private def impl(
      pc: Long,
      dies: Vector[DWARF.DIE],
      strings: DWARF.Strings
  ): Option[(String, Int)] = {
    def decode(dwarf: DWARF.DIE): Option[(String, Int)] = {
      dwarf.units.collectFirst {
        case cu if check(pc, cu) =>
          for {
            file <- extractFilename(dwarf.units, cu, strings)
            line <- extractLine(cu)
          } yield (file, line)
      }.flatten
    }

    dies
      .to(LazyList)
      .map(d => decode(d))
      .collectFirst {
        case Some(value) => value
      }
  }

  private def extractLine(die: DWARF.CompileUnit): Option[Int] = {
    die.values.collectFirst {
      case v if v._1.at == DWARF.Attribute.DW_AT_decl_line =>
        v._2.asInstanceOf[UByte].toInt
    }
  }

  private def extractFilename(
      dies: Vector[DWARF.CompileUnit],
      subprogram: DWARF.CompileUnit,
      strings: DWARF.Strings
  ): Option[String] = {
    val index = dies.indexOf(subprogram)
    val prev = dies.splitAt(index)._1
    for {
      cu <- prev.findLast(cu =>
        cu.abbrev.exists(_.tag == DWARF.Tag.DW_TAG_compile_unit)
      )
      name <- cu.values.collectFirst {
        case v if v._1.at == DWARF.Attribute.DW_AT_name =>
          strings.read(v._2.asInstanceOf[UInt])
      }
    } yield name
  }

  private def check(address: Long, die: DWARF.CompileUnit): Boolean = {
    def getLowPC: Option[Long] = die.values.collectFirst {
      case v if v._1.at == DWARF.Attribute.DW_AT_low_pc =>
        println(v._2.asInstanceOf[Long])
        v._2.asInstanceOf[Long]
    }
    def getHighPC(lowPC: Long): Option[Long] =
      die.values.collectFirst {
        case v if v._1.at == DWARF.Attribute.DW_AT_high_pc =>
          if (DWARF.Form.isConstantClass(v._1.form)) {
            val value = v._2.asInstanceOf[UInt]
            lowPC + value.toLong
          } else if (DWARF.Form.isAddressClass(v._1.form)) {
            val value = v._2.asInstanceOf[Long]
            value
          } else {
            println(s"Invalid DW_AT_high_pc class: ${v._1.form}")
            0L
          }
      }

    if (die.abbrev.exists(_.tag == DWARF.Tag.DW_TAG_subprogram)) {
      (for {
        lowPC <- getLowPC
        highPC <- getHighPC(lowPC)
      } yield {
        val res = lowPC <= address// && address < highPC
        res
      }).getOrElse(false)
    } else false
  }

  private def processFile: Option[(Vector[DWARF.DIE], DWARF.Strings)] = {
    val file = new File(s"$filename.dSYM/Contents/Resources/DWARF/sandbox")
    implicit val bf: BinaryFile = new BinaryFile(
      new RandomAccessFile(s"$filename.dSYM/Contents/Resources/DWARF/sandbox", "r")
    )

    val head = bf.position()
    val magic = bf.readInt().toUInt.toHexString
    bf.seek(head)
    if (magic == MACHO_MAGIC) {
      val macho = MachO.parse(bf)
      val sections = macho.segments.flatMap(_.sections)

      for {
        debug_info <- sections.find(_.sectname == "__debug_info")
        debug_abbrev <- sections.find(_.sectname == "__debug_abbrev")
        debug_str <- sections.find(_.sectname == "__debug_str")
        debug_line <- sections.find(_.sectname == "__debug_line")

      } yield {
        println("read DWARF")
        readDWARF(
          debug_info = DWARF.Section(debug_info.offset, debug_info.size),
          debug_abbrev = DWARF.Section(debug_abbrev.offset, debug_abbrev.size),
          debug_str = DWARF.Section(debug_str.offset, debug_str.size)
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

// val file = new File(filename)
// implicit val bf: BinaryFile = new BinaryFile(
//   new RandomAccessFile(filename, "r")
// )
