package scala.scalanative.runtime.dwarf

import scala.collection.mutable

import scalanative.unsigned._
import scalanative.unsafe._

private[runtime] object DWARF {
  implicit val endi: Endianness = Endianness.LITTLE
  import CommonParsers._

  case class DIE(
      header: DWARF.Header,
      units: scala.Array[DWARF.CompileUnit]
  )

  case class Header(
      version: Int,
      is64: Boolean,
      unit_length: Long,
      unit_type: UByte,
      debug_abbrev_offset: Long,
      address_size: Long,
      unit_offset: Long,
      header_offset: Long
  )
  object Header {
    def parse()(implicit bf: BinaryFile): Header = {
      val header_offset = bf.position()
      val unit_length_s = uint32()

      val (dwarf64, unit_length) = if (unit_length_s == -1) {
        (true, uint64())
      } else (false, unit_length_s.toLong)

      val unit_offset = bf.position()

      val version = uint16()
      assert(
        version >= 2.toUInt && version <= 4.toUInt,
        s"Expected DWARF version 2-4, got $version instead"
      )

      def read_ulong: Long =
        if (dwarf64) uint64() else uint32().toLong

      val (unit_type, address_size, debug_abbrev_offset): (UByte, UByte, Long) =
        if (version >= 5.toUInt) {
          (
            uint8(),
            uint8(),
            uint64()
          )
        } else {
          val dao = read_ulong
          (
            0.toUByte,
            uint8(),
            dao
          )
        }
      Header(
        version = version.toInt,
        is64 = dwarf64,
        unit_length = unit_length,
        unit_type = unit_type,
        debug_abbrev_offset = debug_abbrev_offset,
        address_size = address_size.toInt,
        unit_offset = unit_offset,
        header_offset = header_offset
      )

    }
  }

  case class Abbrev(
      code: Int,
      tag: Tag,
      children: Boolean,
      attributes: scala.Array[Attr]
  )
  case class Attr(at: Attribute, form: Form, value: Int)

  object Abbrev {
    def parse(implicit ds: BinaryFile): collection.Map[Int, Abbrev] = {
      def readAttribute(): Option[Attr] = {
        val at = read_unsigned_leb128()
        val form = read_unsigned_leb128()
        if (at == 0 && form == 0) None
        else
          Some(
            Attr(
              at,
              form,
              value = 0
            )
          )
      }
      def readAbbrev(): Option[Abbrev] = {
        val code = read_unsigned_leb128()
        if (code == 0) None
        else {
          val tag = read_unsigned_leb128()
          val children = uint8() == 1

          val attrs = scala.Array.newBuilder[Attr]

          var stop = false

          while (!stop) {
            val attr = readAttribute()

            attr.foreach(attrs += _)

            stop = attr.isEmpty
          }

          Some(Abbrev(code, tag, children, attrs.result()))
        }
      }

      val abbrevs = mutable.Map.empty[Int, Abbrev]

      var stop = false
      while (!stop) {
        val abbrev = readAbbrev()
        abbrev.foreach(v => abbrevs(v.code) = v)
        stop = abbrev.isEmpty
      }

      abbrevs
    }
  }

  case class CompileUnit(
      tag: Option[DWARF.Tag],
      name: Option[UInt],
      linkageName: Option[UInt],
      line: Option[Int],
      lowPC: Option[Long],
      highPC: Option[Long]
  ) {
    def is(tag: Tag) = this.tag.contains(tag)
  }
  object CompileUnit {
    final val empty = CompileUnit(
      None,
      None,
      None,
      None,
      None,
      None
    )
  }

  case class Section(offset: UInt, size: Long)
  case class Strings(buf: Array[Byte]) {
    def read(at: UInt): String = {

      // WARNING: lots of precision loss
      assert(at < buf.length.toUInt)
      val until = buf.indexWhere(_ == 0, at.toInt)

      new String(buf.slice(at.toInt, until))
    }
  }
  object Strings {
    lazy val empty = Strings(Array.empty)
    def parse(debug_str: Section)(implicit bf: BinaryFile): Strings = {
      val pos = bf.position()
      bf.seek(debug_str.offset.toLong)

      val buf = Array.ofDim[Byte](debug_str.size.toInt)
      bf.readFully(buf)
      bf.seek(pos)

      Strings(buf)
    }
  }

  def parse(
      debug_info: Section,
      debug_abbrev: Section
  )(implicit bf: BinaryFile): scala.Array[DIE] = {
    bf.seek(debug_info.offset.toLong)
    val end_offset = debug_info.offset.toLong + debug_info.size
    def stop = bf.position() >= end_offset
    val dies = scala.Array.newBuilder[DIE]
    while (!stop) {
      val die = DIE.parse(debug_info, debug_abbrev)
      dies += die
    }
    dies.result()
  }

  object DIE {
    private val abbrevCache =
      mutable.Map.empty[Long, collection.Map[Int, Abbrev]]
    def parse(
        debug_info: Section,
        debug_abbrev: Section
    )(implicit bf: BinaryFile) = {

      val header = Header.parse()

      val abbrevOffset = debug_abbrev.offset.toLong + header.debug_abbrev_offset
      val idx = abbrevCache.get(abbrevOffset) match {
        case Some(abbrev) => abbrev
        case None =>
          val pos = bf.position()
          bf.seek(abbrevOffset)
          val abbrev = Abbrev.parse(bf)
          abbrevCache.put(abbrevOffset, abbrev)
          bf.seek(pos)
          abbrev
      }
      val units = readUnits(header.unit_offset, header, idx)
      DIE(header, units)
    }
  }

  def readUnits(
      offset: Long,
      header: Header,
      idx: collection.Map[Int, Abbrev]
  )(implicit ds: BinaryFile): scala.Array[CompileUnit] = {

    val end_offset = offset + header.unit_length

    def stop = ds.position() >= end_offset
    val units = scala.Array.newBuilder[CompileUnit]

    while (!stop) {
      val code = read_unsigned_leb128()
      idx.get(code) match {
        case None =>
          units += CompileUnit.empty
        case Some(abbrev) =>
          var name = Option.empty[UInt]
          var linkageName = Option.empty[UInt]
          var line = Option.empty[Int]
          var lowPC = Option.empty[Long]
          var highPC = Option.empty[Long]
          abbrev.attributes.foreach { attr =>
            if (attr.at == DWARF.Attribute.DW_AT_name && attr.form == DWARF.Form.DW_FORM_strp) {
              name = Some(uint32())
            } else if (attr.at == DWARF.Attribute.DW_AT_linkage_name) {
              linkageName = Some(uint32())
            } else if (attr.at == DWARF.Attribute.DW_AT_decl_line) {
              attr.form match {
                case DWARF.Form.DW_FORM_data1 =>
                  // skipping `.toUInt` adds boxing
                  line = Some(uint8().toUInt.toInt)
                case DWARF.Form.DW_FORM_data2 =>
                  // skipping `.toUInt` adds boxing
                  line = Some(uint16().toUInt.toInt)
                case DWARF.Form.DW_FORM_data4 =>
                  line = Some(uint32().toInt)
                case other => line = Some(0)
              }
            } else if (attr.at == DWARF.Attribute.DW_AT_low_pc) {
              lowPC = Some(uint64())
            } else if (attr.at == DWARF.Attribute.DW_AT_high_pc &&
                DWARF.Form.isConstantClass(attr.form)) {
              val value = uint32()
              val lowPCValue = lowPC
                .getOrElse(
                  throw new RuntimeException(
                    "BUG: expected lowPc to be defined"
                  )
                )

              highPC = Some(lowPCValue + value.toLong)
            } else if (attr.at == DWARF.Attribute.DW_AT_high_pc &&
                DWARF.Form.isAddressClass(attr.form)) {
              highPC = Some(uint64())
            } else {
              AttributeValue.skip(header, attr.form)
            }
          }

          units += CompileUnit(
            Some(abbrev.tag),
            name,
            linkageName,
            line,
            lowPC,
            highPC
          )
      }

    }
    units.result()
  }

  object AttributeValue {

    /** Consumes the attribute bytes. We don't want to pay the parsing price for
     *  values we are not interested in keep in sync with `parse`
     */
    def skip(header: Header, form: Form)(implicit ds: BinaryFile): Unit = {
      import Form._
      form match {
        case DW_FORM_strp =>
          if (header.is64) skipBytes(8)
          else skipBytes(4)
        case DW_FORM_data1 =>
          skipBytes(1)
        case DW_FORM_data2 =>
          skipBytes(2)
        case DW_FORM_data4 =>
          skipBytes(4)
        case DW_FORM_addr =>
          if (header.address_size == 4)
            skipBytes(4)
          else if (header.address_size == 8)
            skipBytes(8)
          else
            throw new RuntimeException(
              s"Uknown header size: ${header.address_size}"
            )
        case DW_FORM_flag =>
          skipBytes(1)
        case DW_FORM_ref_addr =>
          if (header.is64) skipBytes(8)
          else skipBytes(4)
        case DW_FORM_sec_offset =>
          if (header.is64) skipBytes(4)
          else skipBytes(4)
        case DW_FORM_flag_present =>
        case DW_FORM_udata =>
          skip_leb128()
        case DW_FORM_sdata =>
          skip_leb128()
        case DW_FORM_ref8 =>
          skipBytes(8)
        case DW_FORM_ref4 =>
          skipBytes(4)
        case DW_FORM_ref2 =>
          skipBytes(2)
        case DW_FORM_ref1 =>
          skipBytes(1)
        case DW_FORM_exprloc =>
          val len = read_unsigned_leb128()
          skipBytes(len)

        case DW_FORM_block1 =>
          val len = uint8()
          skipBytes(len.toLong)
        case DW_FORM_string =>
          while (ds.readByte() != 0) {}
        case _ =>
          throw new Exception(s"Unsupported form: $form")
      }

    }
  }

  def skip_leb128()(implicit ds: BinaryFile): Unit = {
    while ({
      val byte = ds.readByte()
      (byte & 0x80.toByte) != 0
    }) {}
  }

  def read_unsigned_leb128()(implicit ds: BinaryFile): Int = {
    var result = 0
    var shift = 0
    var stop = false
    while (!stop) {
      val byte = ds.readByte().toInt
      result |= (byte & 0x7f) << shift
      stop = (byte & 0x80) == 0
      shift += 7
    }

    result
  }

  def read_signed_leb128()(implicit ds: BinaryFile): Int = {
    var result = 0
    var shift = 0
    var stop = false
    val size = 32
    var byte: Byte = 0
    while (!stop) {
      byte = ds.readByte()
      result |= (byte & 0x7f) << shift
      stop = (byte & 0x80) == 0
      shift += 7
    }

    if ((shift < 32) && ((byte & 0x40) != 0)) {
      result |= -(1 << shift)
    }

    result
  }

  type Attribute = Int
  object Attribute {
    final val DW_AT_sibling = 0x01
    final val DW_AT_location = 0x02
    final val DW_AT_name = 0x03
    final val DW_AT_ordering = 0x09
    final val DW_AT_byte_size = 0x0b
    final val DW_AT_bit_offset = 0x0c
    final val DW_AT_bit_size = 0x0d
    final val DW_AT_stmt_list = 0x10
    final val DW_AT_low_pc = 0x11
    final val DW_AT_high_pc = 0x12
    final val DW_AT_language = 0x13
    final val DW_AT_discr_value = 0x15
    final val DW_AT_visibility = 0x16
    final val DW_AT_import = 0x17
    final val DW_AT_string_length = 0x19
    final val DW_AT_common_reference = 0x1a
    final val DW_AT_comp_dir = 0x1b
    final val DW_AT_const_value = 0x1c
    final val DW_AT_containing_type = 0x1d
    final val DW_AT_default_value = 0x1e
    final val DW_AT_inline = 0x20
    final val DW_AT_is_optional = 0x21
    final val DW_AT_lower_bound = 0x22
    final val DW_AT_producer = 0x25
    final val DW_AT_prototyped = 0x27
    final val DW_AT_return_addr = 0x2a
    final val DW_AT_start_scope = 0x2c
    final val DW_AT_stride_size = 0x2e
    final val DW_AT_upper_bound = 0x2f
    final val DW_AT_abstract_origin = 0x31
    final val DW_AT_accessibility = 0x32
    final val DW_AT_address_class = 0x33
    final val DW_AT_artificial = 0x34
    final val DW_AT_base_types = 0x35
    final val DW_AT_calling_convention = 0x36
    final val DW_AT_count = 0x37
    final val DW_AT_data_member_location = 0x38
    final val DW_AT_decl_column = 0x39
    final val DW_AT_decl_file = 0x3a
    final val DW_AT_decl_line = 0x3b
    final val DW_AT_declaration = 0x3c
    final val DW_AT_ranges = 0x55
    final val DW_AT_linkage_name = 0x6e
  }

  // DWARF v4 specification 7.5.4 describes

  type Form = Int
  object Form {
    final val DW_FORM_addr = 0x01
    final val DW_FORM_block2 = 0x03
    final val DW_FORM_block4 = 0x04
    final val DW_FORM_data2 = 0x05
    final val DW_FORM_data4 = 0x06
    final val DW_FORM_data8 = 0x07
    final val DW_FORM_string = 0x08
    final val DW_FORM_block = 0x09
    final val DW_FORM_block1 = 0x0a
    final val DW_FORM_data1 = 0x0b
    final val DW_FORM_flag = 0x0c
    final val DW_FORM_sdata = 0x0d
    final val DW_FORM_strp = 0x0e
    final val DW_FORM_udata = 0x0f
    final val DW_FORM_ref_addr = 0x10
    final val DW_FORM_ref1 = 0x11
    final val DW_FORM_ref2 = 0x12
    final val DW_FORM_ref4 = 0x13
    final val DW_FORM_ref8 = 0x14
    final val DW_FORM_ref_udata = 0x15
    final val DW_FORM_indirect = 0x16
    final val DW_FORM_sec_offset = 0x17
    final val DW_FORM_exprloc = 0x18
    final val DW_FORM_flag_present = 0x19
    final val DW_FORM_ref_sig8 = 0x20

    // DWARF v4 7.5.4 describes which form belongs to which classes
    def isConstantClass(form: Form): Boolean =
      form match {
        case DW_FORM_data2 | DW_FORM_data4 | DW_FORM_data8 | DW_FORM_sdata |
            DW_FORM_udata =>
          true
        case _ => false
      }

    def isAddressClass(form: Form): Boolean =
      form match {
        case DW_FORM_addr => true
        case _            => false
      }

  }

  type Tag = Int
  object Tag {
    final val DW_TAG_array_type = 0x01
    final val DW_TAG_class_type = 0x02
    final val DW_TAG_entry_point = 0x03
    final val DW_TAG_enumeration_type = 0x04
    final val DW_TAG_formal_parameter = 0x05
    final val DW_TAG_imported_declaration = 0x08
    final val DW_TAG_label = 0x0a
    final val DW_TAG_lexical_block = 0x0b
    final val DW_TAG_member = 0x0d
    final val DW_TAG_pointer_type = 0x0f
    final val DW_TAG_reference_type = 0x10
    final val DW_TAG_compile_unit = 0x11
    final val DW_TAG_string_type = 0x12
    final val DW_TAG_structure_type = 0x13
    final val DW_TAG_subroutine_type = 0x15
    final val DW_TAG_typedef = 0x16
    final val DW_TAG_union_type = 0x17
    final val DW_TAG_unspecified_parameters = 0x18
    final val DW_TAG_variant = 0x19
    final val DW_TAG_common_block = 0x1a
    final val DW_TAG_common_inclusion = 0x1b
    final val DW_TAG_inheritance = 0x1c
    final val DW_TAG_inlined_subroutine = 0x1d
    final val DW_TAG_module = 0x1e
    final val DW_TAG_ptr_to_member_type = 0x1f
    final val DW_TAG_set_type = 0x20
    final val DW_TAG_subrange_type = 0x21
    final val DW_TAG_with_stmt = 0x22
    final val DW_TAG_access_declaration = 0x23
    final val DW_TAG_base_type = 0x24
    final val DW_TAG_catch_block = 0x25
    final val DW_TAG_const_type = 0x26
    final val DW_TAG_constant = 0x27
    final val DW_TAG_enumerator = 0x28
    final val DW_TAG_file_type = 0x29
    final val DW_TAG_friend = 0x2a
    final val DW_TAG_namelist = 0x2b
    final val DW_TAG_namelist_item = 0x2c
    final val DW_TAG_packed_type = 0x2d
    final val DW_TAG_subprogram = 0x2e
    final val DW_TAG_template_type_param = 0x2f
  }

  object Lines {

    def parse(section: Section)(implicit bf: BinaryFile) = {
      bf.seek(section.offset.toLong)
      val header = Header.parse()
    }
    case class Header(
        unit_length: Int,
        version: Short,
        header_length: Int,
        minimum_instruction_length: Byte,
        maximum_operations_per_instruction: Byte,
        default_is_stmt: Byte,
        line_base: Byte,
        line_range: Byte,
        opcode_base: Byte,
        standard_opcode_lengths: Array[Byte],
        include_directories: Seq[String],
        file_names: Seq[String]
    )
    object Header {
      def parse()(implicit ds: BinaryFile) = {
        val unit_length = uint32()
        val version = uint16()
        val header_length = uint32()
        val minimum_instruction_length = uint8()
        val maximum_operations_per_instruction = uint8()
        val default_is_stmt = uint8() == 1
        val line_base = uint8()
        val line_range = uint8()
        val opcode_base = uint8()
      }
    }
    class Registers private (
        var address: Long,
        var op_index: Int,
        var file: Int,
        var line: Int,
        var column: Int,
        var is_stmt: Boolean,
        var basic_block: Boolean,
        var end_sequence: Boolean,
        var prologue_end: Boolean,
        var epilogue_begin: Boolean,
        var isa: Int,
        var descriminator: Int
    )
    object Registers {
      def apply(default_is_stmt: Boolean) =
        new Registers(
          address = 0L,
          op_index = 0,
          file = 1,
          line = 1,
          column = 0,
          is_stmt = default_is_stmt,
          basic_block = false,
          end_sequence = false,
          prologue_end = false,
          epilogue_begin = false,
          isa = 0,
          descriminator = 0
        )
    }
  }

}
