package scala.scalanative.runtime

import scala.scalanative.native._

@link("z")
@extern
object zlib {
  type voidpf     = Ptr[Byte]
  type voidp      = Ptr[Byte]
  type voidpc     = Ptr[Byte]
  type uInt       = CUnsignedInt
  type uLong      = CUnsignedLong
  type uLongf     = CUnsignedLong
  type alloc_func = CFunctionPtr3[voidpf, uInt, uInt, voidpf]
  type free_func  = CFunctionPtr2[voidpf, voidpf, Void]
  type Bytef      = Byte
  type z_size_t   = CUnsignedLong
  type z_off_t    = CLong

  type z_stream = CStruct14[Ptr[Bytef], // next_in
                            uInt, // avail_in
                            uLong, // total_in,
                            Ptr[Bytef], // next_out
                            uInt, // avail_out
                            uLong, // total_out
                            CString, // msg
                            voidpf, // (internal) state
                            alloc_func, // zalloc
                            free_func, // zfree
                            voidpf, // opaque
                            CInt, // data_type
                            uLong, // adler
                            uLong] // future

  type z_streamp = Ptr[z_stream]

  type gz_header = CStruct13[CInt, // text
                             uLong, // time
                             CInt, // xflags
                             CInt, // os
                             Ptr[Bytef], // extra
                             uInt, // extra_len
                             uInt, // extra_max
                             Ptr[Bytef], // name
                             uInt, // name_max
                             Ptr[Bytef], // comment
                             uInt, // comm_max
                             CInt, // gcrc
                             CInt] // done

  type gz_headerp = Ptr[gz_header]

  type in_func =
    CFunctionPtr2[Ptr[Byte], Ptr[Ptr[CUnsignedChar]], CUnsignedInt]
  type out_func =
    CFunctionPtr3[Ptr[Byte], Ptr[CUnsignedChar], CUnsignedInt, CInt]
  type gzFile = Ptr[Byte]

  @name("scalanative_Z_NO_FLUSH")
  def Z_NO_FLUSH: CInt = extern

  @name("scalanative_Z_PARTIAL_FLUSH")
  def Z_PARTIAL_FLUSH: CInt = extern

  @name("scalanative_Z_SYNC_FLUSH")
  def Z_SYNC_FLUSH: CInt = extern

  @name("scalanative_Z_FULL_FLUSH")
  def Z_FULL_FLUSH: CInt = extern

  @name("scalanative_Z_FINISH")
  def Z_FINISH: CInt = extern

  @name("scalanative_Z_BLOCK")
  def Z_BLOCK: CInt = extern

  @name("scalanative_Z_TREES")
  def Z_TREES: CInt = extern

  @name("scalanative_Z_OK")
  def Z_OK: CInt = extern

  @name("scalanative_Z_STREAM_END")
  def Z_STREAM_END: CInt = extern

  @name("scalanative_Z_NEED_DICT")
  def Z_NEED_DICT: CInt = extern

  @name("scalanative_Z_ERRNO")
  def Z_ERRNO: CInt = extern

  @name("scalanative_Z_STREAM_ERROR")
  def Z_STREAM_ERROR: CInt = extern

  @name("scalanative_Z_DATA_ERROR")
  def Z_DATA_ERROR: CInt = extern

  @name("scalanative_Z_MEM_ERROR")
  def Z_MEM_ERROR: CInt = extern

  @name("scalanative_Z_BUF_ERROR")
  def Z_BUF_ERROR: CInt = extern

  @name("scalanative_Z_VERSION_ERROR")
  def Z_VERSION_ERROR: CInt = extern

  @name("scalanative_Z_NO_COMPRESSION")
  def Z_NO_COMPRESSION: CInt = extern

  @name("scalanative_Z_BEST_SPEED")
  def Z_BEST_SPEED: CInt = extern

  @name("scalanative_Z_BEST_COMPRESSION")
  def Z_BEST_COMPRESSION: CInt = extern

  @name("scalanative_Z_DEFAULT_COMPRESSION")
  def Z_DEFAULT_COMPRESSION: CInt = extern

  @name("scalanative_Z_FILTERED")
  def Z_FILTERED: CInt = extern

  @name("scalanative_Z_HUFFMAN_ONLY")
  def Z_HUFFMAN_ONLY: CInt = extern

  @name("scalanative_Z_RLE")
  def Z_RLE: CInt = extern

  @name("scalanative_Z_FIXED")
  def Z_FIXED: CInt = extern

  @name("scalanative_Z_DEFAULT_STRATEGY")
  def Z_DEFAULT_STRATEGY: CInt = extern

  @name("scalanative_Z_BINARY")
  def Z_BINARY: CInt = extern

  @name("scalanative_Z_TEXT")
  def Z_TEXT: CInt = extern

  @name("scalanative_Z_ASCII")
  def Z_ASCII: CInt = extern

  @name("scalanative_Z_UNKNOWN")
  def Z_UNKNOWN: CInt = extern

  @name("scalanative_Z_DEFLATED")
  def Z_DEFLATED: CInt = extern

  @name("scalanative_Z_NULL")
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
  def deflateInit2(strm: z_streamp,
                   level: CInt,
                   method: CInt,
                   windowBits: CInt,
                   memLevel: CInt,
                   strategy: CInt): CInt = extern

  @name("scalanative_deflateSetDictionary")
  def deflateSetDictionary(strm: z_streamp,
                           dictionary: Ptr[Bytef],
                           dictLength: uInt): CInt = extern

  @name("scalanative_deflateCopy")
  def deflateCopy(dest: z_streamp, source: z_streamp): CInt = extern

  @name("scalanative_deflateReset")
  def deflateReset(strm: z_streamp): CInt = extern

  @name("scalanative_deflateParams")
  def deflateParams(strm: z_streamp, level: CInt, strategy: CInt): CInt =
    extern

  @name("scalanative_deflateTune")
  def deflateTune(strm: z_streamp,
                  good_length: CInt,
                  max_lazy: CInt,
                  nice_length: CInt,
                  max_chain: CInt): CInt = extern

  @name("scalanative_deflateBound")
  def deflateBound(strm: z_streamp, sourceLen: uLong): uLong = extern

  @name("scalanative_deflatePrime")
  def deflatePrime(strm: z_streamp, bits: CInt, value: CInt): CInt = extern

  @name("scalanative_deflateSetHeader")
  def deflateSetHeader(strm: z_streamp, head: gz_headerp): CInt = extern

  @name("scalanative_inflateInit2")
  def inflateInit2(strm: z_streamp, windowBits: CInt): CInt = extern

  @name("scalanative_inflateSetDictionary")
  def inflateSetDictionary(strm: z_streamp,
                           dictionary: Ptr[Bytef],
                           dictLength: uInt): CInt = extern

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
  def inflateBackInit(strm: z_streamp,
                      windowBits: CInt,
                      window: Ptr[CUnsignedChar]): CInt = extern

  @name("scalanative_inflateBack")
  def inflateBack(strm: z_streamp,
                  in: in_func,
                  in_desc: Ptr[Byte],
                  out: out_func,
                  out_desc: Ptr[Byte]): CInt = extern

  @name("scalanative_inflateBackEnd")
  def inflateBackEnd(strm: z_streamp): CInt = extern

  @name("scalanative_zlibCompileFlags")
  def zlibCompileFlags(): uLong = extern

  // Utility functions
  @name("scalanative_compress")
  def compress(dest: Ptr[Bytef],
               destLen: Ptr[uLongf],
               source: Ptr[Bytef],
               sourceLength: uLong): CInt = extern

  @name("scalanative_compress2")
  def compress2(dest: Ptr[Bytef],
                destLen: Ptr[uLongf],
                source: Ptr[Byte],
                sourceLength: uLong,
                level: CInt): CInt = extern

  @name("scalanative_compressBound")
  def compressBound(sourceLen: uLong): uLong = extern

  @name("scalanative_uncompress")
  def uncompress(dest: Ptr[Bytef],
                 destLen: Ptr[uLongf],
                 source: Ptr[Bytef],
                 sourceLen: uLong): CInt = extern

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

  @name("scalanative_gzprintf")
  def gzprintf(file: gzFile, format: CString, args: CVararg*): CInt = extern

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
