package scala.scalanative.runtime.dwarf

import Endianness.LITTLE
import Endianness.BIG
import java.io.RandomAccessFile
import java.nio.channels.Channels
import scalanative.unsigned._

sealed trait Endianness extends Product with Serializable
object Endianness {
  case object LITTLE extends Endianness
  case object BIG extends Endianness
}

import MachO._

case class MachO private (header: Header, segments: List[Segment]) {}

object MachO {
  import CommonParsers._

  def parse(ds: BinaryFile): MachO = {
    implicit val stream = ds
    val magic = uint32()(Endianness.BIG, stream)
    val cputype = uint32()(Endianness.BIG, stream)

    implicit val endi = Endianness.LITTLE
    val header = new Header(
      magic = magic,
      cputype = cputype,
      cpusubtype = uint32(),
      filetype = uint32(),
      ncmds = uint32(),
      sizeofcmds = uint32(),
      flags = uint32()
    )

    val reserved = skipBytes(INT)

    val segments = List.newBuilder[Segment]

    // WARNING: Long truncated
    (0 until header.ncmds.toInt).foreach { cmdId =>
      val commandType = uint32()
      val commandSize = uint32()
      if (commandSize > 0.toUInt) {
        commandType.toInt match {
          case LoadCommand.LC_SEGMENT_64 =>
            segments += Segment.parse()
          case _ =>
            skipBytes((commandSize - 8.toUInt).toLong)
        }
      }

    }

    new MachO(header, segments.result())

  }

  final val MH_MAGIC = 0xfeedface
  final val MH_CIGAM = 0xcefaedfe

  final val MH_MAGIC_64 = 0xfeedfacf
  final val MH_CIGAM_64 = 0xcffaedfe

  type vm_prot_t = UInt

  case class Header(
      magic: UInt,
      cputype: UInt,
      cpusubtype: UInt,
      filetype: UInt,
      ncmds: UInt,
      sizeofcmds: UInt,
      flags: UInt
  ) {
    override def toString() =
      s"""
      |Header
      |  | magic: ${magic.toHexString}
      |  | cputype: ${cputype.toHexString}
      |  | cpusubtype: ${cpusubtype.toHexString}
      |  | filetype: ${filetype.toHexString}
      |  | ncmds: ${ncmds.toHexString}
      |  | sizeofcmds: ${sizeofcmds.toHexString}
      |  | flags: ${flags.toHexString}
      """.stripMargin.trim
  }

  case class Segment(
      segname: String,
      vmaddr: Long,
      vmsize: Long,
      fileoff: Long,
      filesize: Long,
      maxprot: vm_prot_t,
      initprot: vm_prot_t,
      nsects: UInt,
      flags: UInt,
      sections: List[Section]
  )
  object Segment {
    def parse()(implicit endi: Endianness, ds: BinaryFile): Segment = {
      val init = Segment(
        segname = string(16),
        vmaddr = uint64(),
        vmsize = uint64(),
        fileoff = uint64(),
        filesize = uint64(),
        maxprot = uint32(),
        initprot = uint32(),
        nsects = uint32(),
        flags = uint32(),
        sections = Nil
      )

      init.copy(sections = List.fill(init.nsects.toInt)(Section.parse()))
    }
  }

  case class Section(
      sectname: String,
      segname: String,
      addr: Long,
      size: Long,
      offset: UInt,
      align: UInt,
      reloff: UInt,
      nreloc: UInt,
      flags: UInt
  )

  object Section {
    def parse()(implicit endi: Endianness, ds: BinaryFile): Section = {
      val sect = Section(
        sectname = string(16),
        segname = string(16),
        addr = uint64(),
        size = uint64(),
        offset = uint32(),
        align = uint32(),
        reloff = uint32(),
        nreloc = uint32(),
        flags = uint32()
      )

      val reserved1 = uint32()
      val reserved2 = uint32()
      val reserved3 = uint32()
      sect
    }
  }
// struct section_64 { /* for 64-bit architectures */
// 	char		sectname[16];	/* name of this section */
// 	char		segname[16];	/* segment this section goes in */
// 	uint64_t	addr;		/* memory address of this section */
// 	uint64_t	size;		/* size in bytes of this section */
// 	uint32_t	offset;		/* file offset of this section */
// 	uint32_t	align;		/* section alignment (power of 2) */
// 	uint32_t	reloff;		/* file offset of relocation entries */
// 	uint32_t	nreloc;		/* number of relocation entries */
// 	uint32_t	flags;		/* flags (section type and attributes)*/
// 	uint32_t	reserved1;	/* reserved (for offset or index) */
// 	uint32_t	reserved2;	/* reserved (for count or sizeof) */
// 	uint32_t	reserved3;	/* reserved */
// };

  object LoadCommand {

    val LC_REQ_DYLD = 0x80000000

    /* Constants for the cmd field of all load commands, the type */
    val LC_SEGMENT = 0x1 /* segment of this file to be mapped */
    val LC_SYMTAB = 0x2 /* link-edit stab symbol table info */
    val LC_SYMSEG = 0x3 /* link-edit gdb symbol table info (obsolete) */
    val LC_THREAD = 0x4 /* thread */
    val LC_UNIXTHREAD = 0x5 /* unix thread (includes a stack) */
    val LC_LOADFVMLIB = 0x6 /* load a specified fixed VM shared library */
    val LC_IDFVMLIB = 0x7 /* fixed VM shared library identification */
    val LC_IDENT = 0x8 /* object identification info (obsolete) */
    val LC_FVMFILE = 0x9 /* fixed VM file inclusion (internal use) */
    val LC_PREPAGE = 0xa /* prepage command (internal use) */
    val LC_DYSYMTAB = 0xb /* dynamic link-edit symbol table info */
    val LC_LOAD_DYLIB = 0xc /* load a dynamically linked shared library */
    val LC_ID_DYLIB = 0xd /* dynamically linked shared lib ident */
    val LC_LOAD_DYLINKER = 0xe /* load a dynamic linker */
    val LC_ID_DYLINKER = 0xf /* dynamic linker identification */
    val LC_PREBOUND_DYLIB = 0x10 /* modules prebound for a dynamically */
    /*  linked shared library */
    val LC_ROUTINES = 0x11 /* image routines */
    val LC_SUB_FRAMEWORK = 0x12 /* sub framework */
    val LC_SUB_UMBRELLA = 0x13 /* sub umbrella */
    val LC_SUB_CLIENT = 0x14 /* sub client */
    val LC_SUB_LIBRARY = 0x15 /* sub library */
    val LC_TWOLEVEL_HINTS = 0x16 /* two-level namespace lookup hints */
    val LC_PREBIND_CKSUM = 0x17 /* prebind checksum */

    /*
     * load a dynamically linked shared library that is allowed to be missing
     * (all symbols are weak imported).
     */
    val LC_LOAD_WEAK_DYLIB = (0x18 | LC_REQ_DYLD)

    val LC_SEGMENT_64 = 0x19 /* 64-bit segment of this file to be
 			   mapped */
    val LC_ROUTINES_64 = 0x1a /* 64-bit image routines */
    val LC_UUID = 0x1b /* the uuid */
    val LC_RPATH = (0x1c | LC_REQ_DYLD) /* runpath additions */
    val LC_CODE_SIGNATURE = 0x1d /* local of code signature */
    val LC_SEGMENT_SPLIT_INFO = 0x1e /* local of info to split segments */
    val LC_REEXPORT_DYLIB = (0x1f | LC_REQ_DYLD) /* load and re-export dylib */
    val LC_LAZY_LOAD_DYLIB = 0x20 /* delay load of dylib until first use */
    val LC_ENCRYPTION_INFO = 0x21 /* encrypted segment information */
    val LC_DYLD_INFO = 0x22 /* compressed dyld information */
    val LC_DYLD_INFO_ONLY =
      (0x22 | LC_REQ_DYLD) /* compressed dyld information only */
    val LC_LOAD_UPWARD_DYLIB = (0x23 | LC_REQ_DYLD) /* load upward dylib */
    val LC_VERSION_MIN_MACOSX = 0x24 /* build for MacOSX min OS version */
    val LC_VERSION_MIN_IPHONEOS = 0x25 /* build for iPhoneOS min OS version */
    val LC_FUNCTION_STARTS =
      0x26 /* compressed table of function start addresses */
    val LC_DYLD_ENVIRONMENT = 0x27 /* string for dyld to treat
 			    like environment variable */
    val LC_MAIN = (0x28 | LC_REQ_DYLD) /* replacement for LC_UNIXTHREAD */
    val LC_DATA_IN_CODE = 0x29 /* table of non-instructions in __text */
    val LC_SOURCE_VERSION = 0x2a /* source version used to build binary */
    val LC_DYLIB_CODE_SIGN_DRS =
      0x2b /* Code signing DRs copied from linked dylibs */
    val LC_ENCRYPTION_INFO_64 = 0x2c /* 64-bit encrypted segment information */
    val LC_LINKER_OPTION = 0x2d /* linker options in MH_OBJECT files */
    val LC_LINKER_OPTIMIZATION_HINT =
      0x2e /* optimization hints in MH_OBJECT files */
    val LC_VERSION_MIN_TVOS = 0x2f /* build for AppleTV min OS version */
    val LC_VERSION_MIN_WATCHOS = 0x30 /* build for Watch min OS version */
    val LC_NOTE = 0x31 /* arbitrary data included within a Mach-O file */
    val LC_BUILD_VERSION = 0x32 /* build for platform min OS version */
    val LC_DYLD_EXPORTS_TRIE =
      (0x33 | LC_REQ_DYLD) /* used with linkedit_data_command, payload is trie */
    val LC_DYLD_CHAINED_FIXUPS =
      (0x34 | LC_REQ_DYLD) /* used with linkedit_data_command */
    val LC_FILESET_ENTRY =
      (0x35 | LC_REQ_DYLD) /* used with fileset_entry_command */
  }
}
