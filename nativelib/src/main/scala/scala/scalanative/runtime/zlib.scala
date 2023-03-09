package scala.scalanative.runtime

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo.isWindows

@link("z")
@extern
object zlib {
  import zlibExt._
  type voidpf = Ptr[Byte]
  type voidp = Ptr[Byte]
  type voidpc = Ptr[Byte]
  type uInt = CUnsignedInt
  type uLong = CUnsignedLong
  type uLongf = CUnsignedLong
  type alloc_func = CFuncPtr3[voidpf, uInt, uInt, voidpf]
  type free_func = CFuncPtr2[voidpf, voidpf, Unit]
  type Bytef = Byte
  type z_size_t = CUnsignedLong
  type z_off_t = CLong

  type in_func =
    CFuncPtr2[Ptr[Byte], Ptr[Ptr[CUnsignedChar]], CUnsignedInt]
  type out_func =
    CFuncPtr3[Ptr[Byte], Ptr[CUnsignedChar], CUnsignedInt, CInt]
  type gzFile = Ptr[Byte]
  type z_streamp = Ptr[z_stream[AnyVal, AnyVal]]
  type gz_headerp = Ptr[gz_header[AnyVal, AnyVal]]

  @name("scalanative_z_no_flush")
  def Z_NO_FLUSH: CInt = extern

  @name("scalanative_z_partial_flush")
  def Z_PARTIAL_FLUSH: CInt = extern

  @name("scalanative_z_sync_flush")
  def Z_SYNC_FLUSH: CInt = extern

  @name("scalanative_z_full_flush")
  def Z_FULL_FLUSH: CInt = extern

  @name("scalanative_z_finish")
  def Z_FINISH: CInt = extern

  @name("scalanative_z_block")
  def Z_BLOCK: CInt = extern

  @name("scalanative_z_trees")
  def Z_TREES: CInt = extern

  @name("scalanative_z_ok")
  def Z_OK: CInt = extern

  @name("scalanative_z_stream_end")
  def Z_STREAM_END: CInt = extern

  @name("scalanative_z_need_dict")
  def Z_NEED_DICT: CInt = extern

  @name("scalanative_z_errno")
  def Z_ERRNO: CInt = extern

  @name("scalanative_z_stream_error")
  def Z_STREAM_ERROR: CInt = extern

  @name("scalanative_z_data_error")
  def Z_DATA_ERROR: CInt = extern

  @name("scalanative_z_mem_error")
  def Z_MEM_ERROR: CInt = extern

  @name("scalanative_z_buf_error")
  def Z_BUF_ERROR: CInt = extern

  @name("scalanative_z_version_error")
  def Z_VERSION_ERROR: CInt = extern

  @name("scalanative_z_no_compression")
  def Z_NO_COMPRESSION: CInt = extern

  @name("scalanative_z_best_speed")
  def Z_BEST_SPEED: CInt = extern

  @name("scalanative_z_best_compression")
  def Z_BEST_COMPRESSION: CInt = extern

  @name("scalanative_z_default_compression")
  def Z_DEFAULT_COMPRESSION: CInt = extern

  @name("scalanative_z_filtered")
  def Z_FILTERED: CInt = extern

  @name("scalanative_z_huffman_only")
  def Z_HUFFMAN_ONLY: CInt = extern

  @name("scalanative_z_rle")
  def Z_RLE: CInt = extern

  @name("scalanative_z_fixed")
  def Z_FIXED: CInt = extern

  @name("scalanative_z_default_strategy")
  def Z_DEFAULT_STRATEGY: CInt = extern

  @name("scalanative_z_binary")
  def Z_BINARY: CInt = extern

  @name("scalanative_z_text")
  def Z_TEXT: CInt = extern

  @name("scalanative_z_ascii")
  def Z_ASCII: CInt = extern

  @name("scalanative_z_unknown")
  def Z_UNKNOWN: CInt = extern

  @name("scalanative_z_deflated")
  def Z_DEFLATED: CInt = extern

  @name("scalanative_z_null")
  def Z_NULL: CInt = extern

  // Basic Functions
  @name("scalanative_zlibVersion")
  def zlibVersion(): CString = extern

  @name("scalanative_deflateInit")
  def deflateInit(strm: z_streamp, level: CInt): CInt = extern;

  @name("scalanative_deflate")
  def deflate(strm: z_streamp, flush: CInt): CInt = extern

  @name("scalanative_deflateEnd")
  def deflateEnd(strm: z_streamp): CInt = extern

  @name("scalanative_inflateInit")
  def inflateInit(strm: z_streamp): CInt = extern

  @name("scalanative_inflate")
  def inflate(strm: z_streamp, flush: CInt): CInt = extern

  @name("scalanative_inflateEnd")
  def inflateEnd(strm: z_streamp): CInt = extern

  // Advanced Functions
  @name("scalanative_deflateInit2")
  def deflateInit2(
      strm: z_streamp,
      level: CInt,
      method: CInt,
      windowBits: CInt,
      memLevel: CInt,
      strategy: CInt
  ): CInt = extern

  @name("scalanative_deflateSetDictionary")
  def deflateSetDictionary(
      strm: z_streamp,
      dictionary: Ptr[Bytef],
      dictLength: uInt
  ): CInt = extern

  @name("scalanative_deflateCopy")
  def deflateCopy(dest: z_streamp, source: z_streamp): CInt = extern

  @name("scalanative_deflateReset")
  def deflateReset(strm: z_streamp): CInt = extern

  @name("scalanative_deflateParams")
  def deflateParams(strm: z_streamp, level: CInt, strategy: CInt): CInt =
    extern

  @name("scalanative_deflateTune")
  def deflateTune(
      strm: z_streamp,
      good_length: CInt,
      max_lazy: CInt,
      nice_length: CInt,
      max_chain: CInt
  ): CInt = extern

  @name("scalanative_deflateBound")
  def deflateBound(strm: z_streamp, sourceLen: uLong): uLong = extern

  @name("scalanative_deflatePrime")
  def deflatePrime(strm: z_streamp, bits: CInt, value: CInt): CInt = extern

  @name("scalanative_deflateSetHeader")
  def deflateSetHeader(strm: z_streamp, head: gz_headerp): CInt = extern

  @name("scalanative_inflateInit2")
  def inflateInit2(strm: z_streamp, windowBits: CInt): CInt = extern

  @name("scalanative_inflateSetDictionary")
  def inflateSetDictionary(
      strm: z_streamp,
      dictionary: Ptr[Bytef],
      dictLength: uInt
  ): CInt = extern

  @name("scalanative_inflateSync")
  def inflateSync(strm: z_streamp): CInt = extern

  @name("scalanative_inflateCopy")
  def inflateCopy(dest: z_streamp, source: z_streamp): CInt = extern

  @name("scalanative_inflateReset")
  def inflateReset(strm: z_streamp): CInt = extern

  @name("scalanative_inflateReset2")
  def inflateReset2(strm: z_streamp, windowBits: CInt): CInt = extern

  @name("scalanative_inflatePrime")
  def inflatePrime(strm: z_streamp, bits: CInt, value: CInt): CInt = extern

  @name("scalanative_inflateMark")
  def inflateMark(strm: z_streamp): CInt = extern

  @name("scalanative_inflateGetHeader")
  def inflateGetHeader(strm: z_streamp, head: gz_headerp): CInt = extern

  @name("scalanative_inflateBackInit")
  def inflateBackInit(
      strm: z_streamp,
      windowBits: CInt,
      window: Ptr[CUnsignedChar]
  ): CInt = extern

  @name("scalanative_inflateBack")
  def inflateBack(
      strm: z_streamp,
      in: in_func,
      in_desc: Ptr[Byte],
      out: out_func,
      out_desc: Ptr[Byte]
  ): CInt = extern

  @name("scalanative_inflateBackEnd")
  def inflateBackEnd(strm: z_streamp): CInt = extern

  @name("scalanative_zlibCompileFlags")
  def zlibCompileFlags(): uLong = extern

  // Utility functions
  @name("scalanative_compress")
  def compress(
      dest: Ptr[Bytef],
      destLen: Ptr[uLongf],
      source: Ptr[Bytef],
      sourceLength: uLong
  ): CInt = extern

  @name("scalanative_compress2")
  def compress2(
      dest: Ptr[Bytef],
      destLen: Ptr[uLongf],
      source: Ptr[Byte],
      sourceLength: uLong,
      level: CInt
  ): CInt = extern

  @name("scalanative_compressBound")
  def compressBound(sourceLen: uLong): uLong = extern

  @name("scalanative_uncompress")
  def uncompress(
      dest: Ptr[Bytef],
      destLen: Ptr[uLongf],
      source: Ptr[Bytef],
      sourceLen: uLong
  ): CInt = extern

  // gzip File Access Functions
  @name("scalanative_gzopen")
  def gzopen(path: CString, mode: CString): gzFile = extern

  @name("scalanative_gzdopen")
  def gzdopen(fd: CInt, mode: CString): gzFile = extern

  @name("scalanative_gzsetparams")
  def gzsetparams(file: gzFile, level: CInt, strategy: CInt): CInt = extern

  @name("scalanative_gzread")
  def gzread(file: gzFile, buf: voidp, len: CUnsignedInt): CInt = extern

  @name("scalanative_gzwrite")
  def gzwrite(file: gzFile, buf: voidpc, len: CUnsignedInt): CInt = extern

  @name("scalanative_gzputs")
  def gzputs(file: gzFile, s: CString): CInt = extern

  @name("scalanative_gzgets")
  def gzgets(file: gzFile, buf: CString, len: CInt): CString = extern

  @name("scalanative_gzputc")
  def gzputc(file: gzFile, c: CInt): CInt = extern

  @name("scalanative_gzgetc")
  def gzgetc(file: gzFile): CInt = extern

  @name("scalanative_gzungetc")
  def gzungetc(c: CInt, file: gzFile): CInt = extern

  @name("scalanative_gzflush")
  def gzflush(file: gzFile, flush: CInt): CInt = extern

  @name("scalanative_gzseek")
  def gzseek(file: gzFile, offset: z_off_t, whence: CInt): z_off_t = extern

  @name("scalanative_gzrewind")
  def gzrewind(file: gzFile): CInt = extern

  @name("scalanative_gztell")
  def gztell(file: gzFile): z_off_t = extern

  @name("scalanative_gzeof")
  def gzeof(file: gzFile): CInt = extern

  @name("scalanative_gzdirect")
  def gzdirect(file: gzFile): CInt = extern

  @name("scalanative_gzclose")
  def gzclose(file: gzFile): CInt = extern

  @name("scalanative_gzerror")
  def gzerror(file: gzFile, errnum: Ptr[Int]): CString = extern

  @name("scalanative_gzclearerr")
  def gzclearerr(file: gzFile): Unit = extern

  // Checksum Functions
  @name("scalanative_adler32")
  def adler32(adler: uLong, buf: Ptr[Bytef], len: uInt): uLong = extern

  @name("scalanative_adler32_combine")
  def adler32_combine(adler1: uLong, adler2: uLong, len2: z_off_t): uLong =
    extern

  @name("scalanative_crc32")
  def crc32(crc: uLong, buf: Ptr[Bytef], len: uInt): uLong = extern

  @name("scalanative_crc32_combine")
  def crc32_combine(crc1: uLong, crc2: uLong, len2: z_off_t): uLong = extern
}

object zlibExt {
  import zlib._

  object z_stream {
    // Depending on the OS zlib can use different types inside z_stream
    // We can distinguish to layouts using different size of integers:
    // 64-bit: using uint32 and uint64, it can be found on Unix
    // 32-bit  using uint15 and uint32, which is present on Windows
    def size: CSize = fromRawUSize(
      if (isWindows) Intrinsics.sizeOf[z_stream_32]
      else Intrinsics.sizeOf[z_stream_64]
    )
  }

  private[scalanative] type z_stream[UINT, ULONG] =
    CStruct14[
      Ptr[Bytef], // next_in
      UINT, // avail_in
      ULONG, // total_in,
      Ptr[Bytef], // next_out
      UINT, // avail_out
      ULONG, // total_out
      CString, // msg
      voidpf, // (internal) state
      alloc_func, // zalloc
      free_func, // zfree
      voidpf, // opaque
      CInt, // data_type
      ULONG, // adler
      ULONG // future
    ]

  private[scalanative] type z_stream_32 =
    z_stream[CUnsignedShort, CUnsignedInt]
  private[scalanative] type z_stream_64 =
    z_stream[CUnsignedInt, CUnsignedLong]

  object gz_header {
    // Depending on the OS zlib can use different types inside gz_header
    // For details see comment in z_stream

    def size: CSize = fromRawUSize(
      if (isWindows) Intrinsics.sizeOf[gz_header_32]
      else Intrinsics.sizeOf[gz_header_64]
    )
  }

  private[scalanative] type gz_header[UINT, ULONG] =
    CStruct13[
      CInt, // text
      ULONG, // time
      CInt, // xflags
      CInt, // os
      Ptr[Bytef], // extra
      UINT, // extra_len
      UINT, // extra_max
      Ptr[Bytef], // name
      UINT, // name_max
      Ptr[Bytef], // comment
      UINT, // comm_max
      CInt, // gcrc
      CInt // done
    ]
  private[scalanative] type gz_header_32 =
    gz_header[CUnsignedShort, CUnsignedInt]
  private[scalanative] type gz_header_64 =
    gz_header[CUnsignedInt, CUnsignedLong]
}

object zlibOps {
  import zlib._
  import zlibExt._
  implicit class ZStreamOps(val ref: z_streamp) extends AnyVal {
    import z_stream._
    @alwaysinline private def asZStream32 = ref.asInstanceOf[Ptr[z_stream_32]]
    @alwaysinline private def asZStream64 = ref.asInstanceOf[Ptr[z_stream_64]]

    def nextIn: Ptr[Bytef] = asZStream32._1
    def availableIn: uInt = if (isWindows) asZStream32._2 else asZStream64._2
    def totalIn: uLong = if (isWindows) asZStream32._3 else asZStream64._3
    def nextOut: Ptr[Bytef] =
      if (isWindows) asZStream32._4 else asZStream64._4
    def availableOut: uInt =
      if (isWindows) asZStream32._5 else asZStream64._5
    def totalOut: uLong = if (isWindows) asZStream32._6 else asZStream64._6
    def msg: CString = if (isWindows) asZStream32._7 else asZStream64._7
    def state: voidpf = if (isWindows) asZStream32._8 else asZStream64._8
    def zalloc: alloc_func =
      if (isWindows) asZStream32._9 else asZStream64._9
    def zfree: free_func =
      if (isWindows) asZStream32._10 else asZStream64._10
    def opaque: voidpf = if (isWindows) asZStream32._11 else asZStream64._11
    def data_type: CInt = if (isWindows) asZStream32._12 else asZStream64._12
    def adler: uLong = if (isWindows) asZStream32._13 else asZStream64._13
    def future: uLong = if (isWindows) asZStream32._14 else asZStream64._14

    def nextIn_=(v: Ptr[Bytef]): Unit = asZStream32._1 = v
    def availableIn_=(v: uInt): Unit =
      if (isWindows) asZStream32._2 = v.toUShort else asZStream64._2 = v
    def totalIn_=(v: uLong): Unit =
      if (isWindows) asZStream32._3 = v.toUInt else asZStream64._3 = v
    def nextOut_=(v: Ptr[Bytef]): Unit =
      if (isWindows) asZStream32._4 = v else asZStream64._4 = v
    def availableOut_=(v: uInt): Unit =
      if (isWindows) asZStream32._5 = v.toUShort else asZStream64._5 = v
    def totalOut_=(v: uLong): Unit =
      if (isWindows) asZStream32._6 = v.toUInt else asZStream64._6 = v
    def msg_=(v: CString): Unit =
      if (isWindows) asZStream32._7 = v else asZStream64._7 = v
    def state_=(v: voidpf): Unit =
      if (isWindows) asZStream32._8 = v else asZStream64._8 = v
    def zalloc_=(v: alloc_func): Unit =
      if (isWindows) asZStream32._9 = v else asZStream64._9 = v
    def zfree_=(v: free_func): Unit =
      if (isWindows) asZStream32._10 = v else asZStream64._10 = v
    def opaque_=(v: voidpf): Unit =
      if (isWindows) asZStream32._11 = v else asZStream64._11 = v
    def data_type_=(v: CInt): Unit =
      if (isWindows) asZStream32._12 = v else asZStream64._12 = v
    def adler_=(v: uLong): Unit =
      if (isWindows) asZStream32._13 = v.toUInt else asZStream64._13 = v
    def future_=(v: uLong): Unit =
      if (isWindows) asZStream32._14 = v.toUInt else asZStream64._14 = v
  }

  implicit class GZHeaderOps(val ref: gz_headerp) extends AnyVal {
    import gz_header._

    @alwaysinline private def asZStream32 = ref.asInstanceOf[Ptr[gz_header_32]]
    @alwaysinline private def asZStream64 = ref.asInstanceOf[Ptr[gz_header_64]]

    def text: CInt = if (isWindows) asZStream32._1 else asZStream64._1
    def time: uLong = if (isWindows) asZStream32._2 else asZStream64._2
    def xflags: CInt = if (isWindows) asZStream32._3 else asZStream64._3
    def os: CInt = if (isWindows) asZStream32._4 else asZStream64._4
    def extra: Ptr[Bytef] = if (isWindows) asZStream32._5 else asZStream64._5
    def extra_len: uInt = if (isWindows) asZStream32._6 else asZStream64._6
    def extra_max: uInt = if (isWindows) asZStream32._7 else asZStream64._7
    def name: Ptr[Bytef] = if (isWindows) asZStream32._8 else asZStream64._8
    def name_max: uInt = if (isWindows) asZStream32._9 else asZStream64._9
    def comment: Ptr[Bytef] =
      if (isWindows) asZStream32._10 else asZStream64._10
    def comm_max: uInt = if (isWindows) asZStream32._11 else asZStream64._11
    def gcrc: CInt = if (isWindows) asZStream32._12 else asZStream64._12
    def done: CInt = if (isWindows) asZStream32._13 else asZStream64._13

    def text_=(v: CInt): Unit =
      if (isWindows) asZStream32._1 = v
      else asZStream64._1 = v
    def time_=(v: uLong): Unit =
      if (isWindows) asZStream32._2 = v.toUInt
      else asZStream64._2 = v
    def xflags_=(v: CInt): Unit =
      if (isWindows) asZStream32._3 = v
      else asZStream64._3 = v
    def os_=(v: CInt): Unit =
      if (isWindows) asZStream32._4 = v
      else asZStream64._4 = v
    def extra_=(v: Ptr[Bytef]): Unit =
      if (isWindows) asZStream32._5 = v
      else asZStream64._5 = v
    def extra_len_=(v: uInt): Unit =
      if (isWindows) asZStream32._6 = v.toUShort
      else asZStream64._6 = v
    def extra_max_=(v: uInt): Unit =
      if (isWindows) asZStream32._7 = v.toUShort
      else asZStream64._7 = v
    def name_=(v: Ptr[Bytef]): Unit =
      if (isWindows) asZStream32._8 = v
      else asZStream64._8 = v
    def name_max_=(v: uInt): Unit =
      if (isWindows) asZStream32._9 = v.toUShort
      else asZStream64._9 = v
    def comment_=(v: Ptr[Bytef]): Unit =
      if (isWindows) asZStream32._10 = v
      else asZStream64._10 = v
    def comm_max_=(v: uInt): Unit =
      if (isWindows) asZStream32._11 = v.toUShort
      else asZStream64._11 = v
    def gcrc_=(v: CInt): Unit =
      if (isWindows) asZStream32._12 = v
      else asZStream64._12 = v
    def done_=(v: CInt): Unit =
      if (isWindows) asZStream32._13 = v
      else asZStream64._13 = v
  }
}
