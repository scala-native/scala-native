#ifdef SCALANATIVE_Z

#include <zlib.h>

int scalanative_z_no_flush() { return Z_NO_FLUSH; }

int scalanative_z_partial_flush() { return Z_PARTIAL_FLUSH; }

int scalanative_z_sync_flush() { return Z_SYNC_FLUSH; }

int scalanative_z_full_flush() { return Z_FULL_FLUSH; }

int scalanative_z_finish() { return Z_FINISH; }

int scalanative_z_block() { return Z_BLOCK; }

int scalanative_z_trees() { return Z_TREES; }

int scalanative_z_ok() { return Z_OK; }

int scalanative_z_stream_end() { return Z_STREAM_END; }

int scalanative_z_need_dict() { return Z_NEED_DICT; }

int scalanative_z_errno() { return Z_ERRNO; }

int scalanative_z_stream_error() { return Z_STREAM_ERROR; }

int scalanative_z_data_error() { return Z_DATA_ERROR; }

int scalanative_z_mem_error() { return Z_MEM_ERROR; }

int scalanative_z_buf_error() { return Z_BUF_ERROR; }

int scalanative_z_version_error() { return Z_VERSION_ERROR; }

int scalanative_z_no_compression() { return Z_NO_COMPRESSION; }

int scalanative_z_best_speed() { return Z_BEST_SPEED; }

int scalanative_z_best_compression() { return Z_BEST_COMPRESSION; }

int scalanative_z_default_compression() { return Z_DEFAULT_COMPRESSION; }

int scalanative_z_filtered() { return Z_FILTERED; }

int scalanative_z_huffman_only() { return Z_HUFFMAN_ONLY; }

int scalanative_z_rle() { return Z_RLE; }

int scalanative_z_fixed() { return Z_FIXED; }

int scalanative_z_default_strategy() { return Z_DEFAULT_STRATEGY; }

int scalanative_z_binary() { return Z_BINARY; }

int scalanative_z_text() { return Z_TEXT; }

int scalanative_z_ascii() { return Z_ASCII; }

int scalanative_z_unknown() { return Z_UNKNOWN; }

int scalanative_z_deflated() { return Z_DEFLATED; }

int scalanative_z_null() { return Z_NULL; }

const char *scalanative_zlibVersion() { return zlibVersion(); }

int scalanative_deflateInit(z_streamp strm, int level) {
    return deflateInit(strm, level);
}

int scalanative_deflate(z_streamp strm, int flush) {
    return deflate(strm, flush);
}

int scalanative_deflateEnd(z_streamp strm) { return deflateEnd(strm); }

int scalanative_inflateInit(z_streamp strm) { return inflateInit(strm); }

int scalanative_inflate(z_streamp strm, int flush) {
    return inflate(strm, flush);
}

int scalanative_inflateEnd(z_streamp strm) { return inflateEnd(strm); }

int scalanative_deflateInit2(z_streamp strm, int level, int method,
                             int windowBits, int memLevel, int strategy) {
    return deflateInit2(strm, level, method, windowBits, memLevel, strategy);
}

int scalanative_deflateSetDictionary(z_streamp strm, Bytef *dictionary,
                                     uInt dictLength) {
    return deflateSetDictionary(strm, dictionary, dictLength);
}

int scalanative_deflateCopy(z_streamp dest, z_streamp source) {
    return deflateCopy(dest, source);
}

int scalanative_deflateReset(z_streamp strm) { return deflateReset(strm); }

int scalanative_deflateParams(z_streamp strm, int level, int strategy) {
    return deflateParams(strm, level, strategy);
}

int scalanative_deflateTune(z_streamp strm, int good_length, int max_lazy,
                            int nice_length, int max_chain) {
    return deflateTune(strm, good_length, max_lazy, nice_length, max_chain);
}

uLong scalanative_deflateBound(z_streamp strm, uLong sourceLen) {
    return deflateBound(strm, sourceLen);
}

int scalanative_deflatePrime(z_streamp strm, int bits, int value) {
    return deflatePrime(strm, bits, value);
}

int scalanative_deflateSetHeader(z_streamp strm, gz_headerp head) {
    return deflateSetHeader(strm, head);
}

int scalanative_inflateInit2(z_streamp strm, int windowBits) {
    return inflateInit2(strm, windowBits);
}

int scalanative_inflateSetDictionary(z_streamp strm, Bytef *dictionary,
                                     uInt dictLength) {
    return inflateSetDictionary(strm, dictionary, dictLength);
}

int scalanative_inflateSync(z_streamp strm) { return inflateSync(strm); }

int scalanative_inflateCopy(z_streamp dest, z_streamp source) {
    return inflateCopy(dest, source);
}

int scalanative_inflateReset(z_streamp strm) { return inflateReset(strm); }

int scalanative_inflateReset2(z_streamp strm, int windowBits) {
    return inflateReset2(strm, windowBits);
}

int scalanative_inflatePrime(z_streamp strm, int bits, int value) {
    return inflatePrime(strm, bits, value);
}

int scalanative_inflateMark(z_streamp strm) { return inflateMark(strm); }

int scalanative_inflateGetHeader(z_streamp strm, gz_headerp head) {
    return inflateGetHeader(strm, head);
}

int scalanative_inflateBackInit(z_streamp strm, int windowBits,
                                unsigned char *window) {
    return inflateBackInit(strm, windowBits, window);
}

int scalanative_inflateBack(z_streamp strm, in_func in, void *in_desc,
                            out_func out, void *out_desc) {
    return inflateBack(strm, in, in_desc, out, out_desc);
}

int scalanative_inflateBackEnd(z_streamp strm) { return inflateBackEnd(strm); }

uLong scalanative_zlibCompileFlags() { return zlibCompileFlags(); }

int scalanative_compress(Bytef *dest, uLongf *destLen, Bytef *source,
                         uLong sourceLength) {
    return compress(dest, destLen, source, sourceLength);
}

int scalanative_compress2(Bytef *dest, uLongf *destLen, void *source,
                          uLong sourceLength, int level) {
    return compress2(dest, destLen, source, sourceLength, level);
}

uLong scalanative_compressBound(uLong sourceLen) {
    return compressBound(sourceLen);
}

int scalanative_uncompress(Bytef *dest, uLongf *destLen, Bytef *source,
                           uLong sourceLen) {
    return uncompress(dest, destLen, source, sourceLen);
}

gzFile scalanative_gzopen(char *path, char *mode) { return gzopen(path, mode); }

gzFile scalanative_gzdopen(int fd, char *mode) { return gzdopen(fd, mode); }

int scalanative_gzsetparams(gzFile file, int level, int strategy) {
    return gzsetparams(file, level, strategy);
}

int scalanative_gzread(gzFile file, voidp buf, unsigned int len) {
    return gzread(file, buf, len);
}

int scalanative_gzwrite(gzFile file, voidpc buf, unsigned int len) {
    return gzwrite(file, buf, len);
}

int scalanative_gzprintf(gzFile file, char *format, ...) {
    return gzprintf(file, format);
}

int scalanative_gzputs(gzFile file, char *s) { return gzputs(file, s); }

char *scalanative_gzgets(gzFile file, char *buf, int len) {
    return gzgets(file, buf, len);
}

int scalanative_gzputc(gzFile file, int c) { return gzputc(file, c); }

int scalanative_gzgetc(gzFile file) { return gzgetc(file); }

int scalanative_gzungetc(int c, gzFile file) { return gzungetc(c, file); }

int scalanative_gzflush(gzFile file, int flush) { return gzflush(file, flush); }

z_off_t scalanative_gzseek(gzFile file, z_off_t offset, int whence) {
    return gzseek(file, offset, whence);
}

int scalanative_gzrewind(gzFile file) { return gzrewind(file); }

z_off_t scalanative_gztell(gzFile file) { return gztell(file); }

int scalanative_gzeof(gzFile file) { return gzeof(file); }

int scalanative_gzdirect(gzFile file) { return gzdirect(file); }

int scalanative_gzclose(gzFile file) { return gzclose(file); }

const char *scalanative_gzerror(gzFile file, int *errnum) {
    return gzerror(file, errnum);
}

void scalanative_gzclearerr(gzFile file) { return gzclearerr(file); }

uLong scalanative_adler32(uLong adler, Bytef *buf, uInt len) {
    return adler32(adler, buf, len);
}

uLong scalanative_adler32_combine(uLong adler1, uLong adler2, z_off_t len2) {
    return adler32_combine(adler1, adler2, len2);
}

uLong scalanative_crc32(uLong crc, Bytef *buf, uInt len) {
    return crc32(crc, buf, len);
}

uLong scalanative_crc32_combine(uLong crc1, uLong crc2, z_off_t len2) {
    return crc32_combine(crc1, crc2, len2);
}
#endif
