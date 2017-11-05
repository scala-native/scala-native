#include <zlib.h>

#ifdef _WIN32
typedef unsigned __int64 scalanative_uLong;
struct internal_state;
typedef struct scalanative_z_stream_s {
    z_const Bytef *next_in;     /* next input byte */
    uInt avail_in;              /* number of bytes available at next_in */
    scalanative_uLong total_in; /* total number of input bytes read so far */

    Bytef *next_out;             /* next output byte will go here */
    uInt avail_out;              /* remaining free space at next_out */
    scalanative_uLong total_out; /* total number of bytes output so far */

    z_const char *msg;                /* last error message, NULL if no error */
    struct internal_state FAR *state; /* not visible by applications */

    alloc_func zalloc; /* used to allocate the internal state */
    free_func zfree;   /* used to free the internal state */
    voidpf opaque;     /* private data object passed to zalloc and zfree */

    int data_type; /* best guess about the data type: binary or text
                      for deflate, or the decoding state for inflate */
    scalanative_uLong
        adler; /* Adler-32 or CRC-32 value of the uncompressed data */
    scalanative_uLong reserved; /* reserved for future use */
} scalanative_z_stream;
typedef scalanative_z_stream FAR *scalanative_z_streamp;
z_streamp convertToZStream(scalanative_z_streamp strm, z_streamp temp) {
    temp->next_in = strm->next_in;
    temp->avail_in = strm->avail_in;
    temp->total_in = strm->total_in;
    temp->next_out = strm->next_out;
    temp->avail_out = strm->avail_out;
    temp->total_out = strm->total_out;
    temp->msg = strm->msg;
    temp->state = strm->state;
    temp->zalloc = strm->zalloc;
    temp->zfree = strm->zfree;
    temp->opaque = strm->opaque;
    temp->data_type = strm->data_type;
    temp->adler = strm->adler;
    temp->reserved = strm->reserved;
    if (temp->state)
        *(z_streamp *)(temp->state) = temp;
    return temp;
}
scalanative_z_streamp convertFromZStream(scalanative_z_streamp strm,
                                         z_streamp temp) {
    strm->next_in = temp->next_in;
    strm->avail_in = temp->avail_in;
    strm->total_in = temp->total_in;
    strm->next_out = temp->next_out;
    strm->avail_out = temp->avail_out;
    strm->total_out = temp->total_out;
    strm->msg = temp->msg;
    strm->state = temp->state;
    strm->zalloc = temp->zalloc;
    strm->zfree = temp->zfree;
    strm->opaque = temp->opaque;
    strm->data_type = temp->data_type;
    strm->adler = temp->adler;
    strm->reserved = temp->reserved;
    if (strm->state)
        *(scalanative_z_streamp *)(strm->state) = strm;
    return strm;
}
#else
typedef uLong scalanative_uLong;
typedef z_streamp scalanative_z_streamp;
#endif

int scalanative_Z_NO_FLUSH() { return Z_NO_FLUSH; }

int scalanative_Z_PARTIAL_FLUSH() { return Z_PARTIAL_FLUSH; }

int scalanative_Z_SYNC_FLUSH() { return Z_SYNC_FLUSH; }

int scalanative_Z_FULL_FLUSH() { return Z_FULL_FLUSH; }

int scalanative_Z_FINISH() { return Z_FINISH; }

int scalanative_Z_BLOCK() { return Z_BLOCK; }

int scalanative_Z_TREES() { return Z_TREES; }

int scalanative_Z_OK() { return Z_OK; }

int scalanative_Z_STREAM_END() { return Z_STREAM_END; }

int scalanative_Z_NEED_DICT() { return Z_NEED_DICT; }

int scalanative_Z_ERRNO() { return Z_ERRNO; }

int scalanative_Z_STREAM_ERROR() { return Z_STREAM_ERROR; }

int scalanative_Z_DATA_ERROR() { return Z_DATA_ERROR; }

int scalanative_Z_MEM_ERROR() { return Z_MEM_ERROR; }

int scalanative_Z_BUF_ERROR() { return Z_BUF_ERROR; }

int scalanative_Z_VERSION_ERROR() { return Z_VERSION_ERROR; }

int scalanative_Z_NO_COMPRESSION() { return Z_NO_COMPRESSION; }

int scalanative_Z_BEST_SPEED() { return Z_BEST_SPEED; }

int scalanative_Z_BEST_COMPRESSION() { return Z_BEST_COMPRESSION; }

int scalanative_Z_DEFAULT_COMPRESSION() { return Z_DEFAULT_COMPRESSION; }

int scalanative_Z_FILTERED() { return Z_FILTERED; }

int scalanative_Z_HUFFMAN_ONLY() { return Z_HUFFMAN_ONLY; }

int scalanative_Z_RLE() { return Z_RLE; }

int scalanative_Z_FIXED() { return Z_FIXED; }

int scalanative_Z_DEFAULT_STRATEGY() { return Z_DEFAULT_STRATEGY; }

int scalanative_Z_BINARY() { return Z_BINARY; }

int scalanative_Z_TEXT() { return Z_TEXT; }

int scalanative_Z_ASCII() { return Z_ASCII; }

int scalanative_Z_UNKNOWN() { return Z_UNKNOWN; }

int scalanative_Z_DEFLATED() { return Z_DEFLATED; }

int scalanative_Z_NULL() { return Z_NULL; }

const char *scalanative_zlibVersion() { return zlibVersion(); }

int scalanative_deflateInit(scalanative_z_streamp strm, int level) {
#ifndef _WIN32
    return deflateInit(strm, level);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = deflateInit(tp, level);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_deflate(scalanative_z_streamp strm, int flush) {
#ifndef _WIN32
    return deflate(strm, flush);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = deflate(tp, flush);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_deflateEnd(scalanative_z_streamp strm) {
#ifndef _WIN32
    return deflateEnd(strm);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = deflateEnd(tp);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflateInit(scalanative_z_streamp strm) {
#ifndef _WIN32
    return inflateInit(strm);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflateInit(tp);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflate(scalanative_z_streamp strm, int flush) {
#ifndef _WIN32
    return inflate(strm, flush);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflate(tp, flush);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflateEnd(scalanative_z_streamp strm) {
#ifndef _WIN32
    return inflateEnd(strm);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflateEnd(tp);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_deflateInit2(scalanative_z_streamp strm, int level, int method,
                             int windowBits, int memLevel, int strategy) {
#ifndef _WIN32
    return deflateInit2(strm, level, method, windowBits, memLevel, strategy);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result =
        deflateInit2(tp, level, method, windowBits, memLevel, strategy);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_deflateSetDictionary(scalanative_z_streamp strm,
                                     Bytef *dictionary, uInt dictLength) {
#ifndef _WIN32
    return deflateSetDictionary(strm, dictionary, dictLength);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = deflateSetDictionary(tp, dictionary, dictLength);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_deflateCopy(scalanative_z_streamp dest,
                            scalanative_z_streamp source) {
#ifndef _WIN32
    return deflateCopy(dest, source);
#else
    z_stream t;
    z_streamp tp = convertToZStream(dest, &t);
    z_stream t2;
    z_streamp tp2 = convertToZStream(source, &t2);
    int result = deflateCopy(tp, tp2);
    convertFromZStream(source, &t2);
    return result;
#endif
}

int scalanative_deflateReset(scalanative_z_streamp strm) {
#ifndef _WIN32
    return deflateReset(strm);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = deflateReset(tp);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_deflateParams(scalanative_z_streamp strm, int level,
                              int strategy) {
#ifndef _WIN32
    return deflateParams(strm, level, strategy);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = deflateParams(tp, level, strategy);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_deflateTune(scalanative_z_streamp strm, int good_length,
                            int max_lazy, int nice_length, int max_chain) {
#ifndef _WIN32
    return deflateTune(strm, good_length, max_lazy, nice_length, max_chain);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = deflateTune(tp, good_length, max_lazy, nice_length, max_chain);
    convertFromZStream(strm, &t);
    return result;
#endif
}

scalanative_uLong scalanative_deflateBound(scalanative_z_streamp strm,
                                           scalanative_uLong sourceLen) {
#ifndef _WIN32
    return deflateBound(strm, sourceLen);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = deflateBound(tp, (uLong)sourceLen);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_deflatePrime(scalanative_z_streamp strm, int bits, int value) {
#ifndef _WIN32
    return deflatePrime(strm, bits, value);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = deflatePrime(tp, bits, value);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_deflateSetHeader(scalanative_z_streamp strm, gz_headerp head) {
#ifndef _WIN32
    return deflateSetHeader(strm, head);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = deflateSetHeader(tp, head);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflateInit2(scalanative_z_streamp strm, int windowBits) {
#ifndef _WIN32
    return inflateInit2(strm, windowBits);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflateInit2(tp, windowBits);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflateSetDictionary(scalanative_z_streamp strm,
                                     Bytef *dictionary, uInt dictLength) {
#ifndef _WIN32
    return inflateSetDictionary(strm, dictionary, dictLength);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflateSetDictionary(tp, dictionary, dictLength);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflateSync(scalanative_z_streamp strm) {
#ifndef _WIN32
    return inflateSync(strm);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflateSync(tp);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflateCopy(scalanative_z_streamp dest,
                            scalanative_z_streamp source) {
#ifndef _WIN32
    return inflateCopy(dest, source);
#else
    z_stream t;
    z_streamp tp = convertToZStream(dest, &t);
    z_stream t2;
    z_streamp tp2 = convertToZStream(source, &t2);
    int result = inflateCopy(tp, tp2);
    convertFromZStream(source, &t2);
    return result;
#endif
}

int scalanative_inflateReset(scalanative_z_streamp strm) {
#ifndef _WIN32
    return inflateReset(strm);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflateReset(tp);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflateReset2(scalanative_z_streamp strm, int windowBits) {
#ifndef _WIN32
    return inflateReset2(strm, windowBits);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflateReset2(tp, windowBits);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflatePrime(scalanative_z_streamp strm, int bits, int value) {
#ifndef _WIN32
    return inflatePrime(strm, bits, value);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflatePrime(tp, bits, value);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflateMark(scalanative_z_streamp strm) {
#ifndef _WIN32
    return inflateMark(strm);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflateMark(tp);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflateGetHeader(scalanative_z_streamp strm, gz_headerp head) {
#ifndef _WIN32
    return inflateGetHeader(strm, head);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflateGetHeader(tp, head);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflateBackInit(scalanative_z_streamp strm, int windowBits,
                                unsigned char *window) {
#ifndef _WIN32
    return inflateBackInit(strm, windowBits, window);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflateBackInit(tp, windowBits, window);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflateBack(scalanative_z_streamp strm, in_func in,
                            void *in_desc, out_func out, void *out_desc) {
#ifndef _WIN32
    return inflateBack(strm, in, in_desc, out, out_desc);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflateBack(tp, in, in_desc, out, out_desc);
    convertFromZStream(strm, &t);
    return result;
#endif
}

int scalanative_inflateBackEnd(scalanative_z_streamp strm) {
#ifndef _WIN32
    return inflateBackEnd(strm);
#else
    z_stream t;
    z_streamp tp = convertToZStream(strm, &t);
    int result = inflateBackEnd(tp);
    convertFromZStream(strm, &t);
    return result;
#endif
}

scalanative_uLong scalanative_zlibCompileFlags() { return zlibCompileFlags(); }

int scalanative_compress(Bytef *dest, uLongf *destLen, Bytef *source,
                         scalanative_uLong sourceLength) {
    return compress(dest, destLen, source, (uLong)sourceLength);
}

int scalanative_compress2(Bytef *dest, uLongf *destLen, void *source,
                          scalanative_uLong sourceLength, int level) {
    return compress2(dest, destLen, source, (uLong)sourceLength, level);
}

scalanative_uLong scalanative_compressBound(scalanative_uLong sourceLen) {
    return compressBound((uLong)sourceLen);
}

int scalanative_uncompress(Bytef *dest, uLongf *destLen, Bytef *source,
                           scalanative_uLong sourceLen) {
    return uncompress(dest, destLen, source, (uLong)sourceLen);
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

scalanative_uLong scalanative_adler32(scalanative_uLong adler, Bytef *buf,
                                      uInt len) {
    return adler32((uLong)adler, buf, len);
}

scalanative_uLong scalanative_adler32_combine(scalanative_uLong adler1,
                                              scalanative_uLong adler2,
                                              z_off_t len2) {
    return adler32_combine((uLong)adler1, (uLong)adler2, len2);
}

scalanative_uLong scalanative_crc32(scalanative_uLong crc, Bytef *buf,
                                    uInt len) {
    return crc32((uLong)crc, buf, len);
}

scalanative_uLong scalanative_crc32_combine(scalanative_uLong crc1,
                                            scalanative_uLong crc2,
                                            z_off_t len2) {
    return crc32_combine((uLong)crc1, (uLong)crc2, len2);
}
