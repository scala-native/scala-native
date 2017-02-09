#include <errno.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <zlib.h>
#include <sys/stat.h>

#define CHUNK_SIZE (1024 * 1024 * 4) // 4 MB

#define TAG_CALL       1
#define TAG_LOAD       2
#define TAG_STORE      3
#define TAG_CLASSALLOC 4
#define TAG_METHOD     5
#define TAG_AS         6
#define TAG_IS         7
#define TAG_BOX        8
#define TAG_UNBOX      9

typedef struct chararray
{
    void* u;
    int length;
    int unused;
    short chars[];
} chararray;

typedef struct jstring
{
    void* u;
    int cachedHashCode;
    int count;
    int offset;
    chararray* value;
} jstring;

char* to_string(char* dst, size_t max_len, jstring* str) {
    size_t length = max_len > str->count ? str->count : max_len;

    for (int i = 0; i < length; ++i) {
        dst[i] = (char) str->value->chars[i];
    }
    dst[length] = '\0';

    return dst;
}

/** The current batch number. */
int current_batch = 1;

/** The buffer where events are written */
unsigned char* buffer;

/** Current position in the buffer */
unsigned char* buffer_cursor;

/** The directory where dumps will be written */
char* dump_directory;

/** Sets `dst` to the path to the next profiling file output */
void next_file(char* dst) {
    if (0 == strcmp(dump_directory, "/dev/null")) {
       strcpy(dst, dump_directory);
    } else {
        strncat(dst, dump_directory, PATH_MAX);
        strncat(dst, "/profile.", PATH_MAX);
        char* n = calloc(20, sizeof(char));
        snprintf(n, 20, "%d", current_batch);
        strncat(dst, n, PATH_MAX);
        free(n);
        current_batch += 1;
    }
}

/** Dumps all the blocks in `dump_location`. */
void block_dump() {
    char filename[PATH_MAX] = { 0 };
    unsigned char compressed_buffer[CHUNK_SIZE];
    unsigned long compressed_size = CHUNK_SIZE;

    next_file(filename);
    FILE* out = fopen(filename, "wb");

    if (out == NULL) {
        printf("Couldn't open '%s'. Errno %d", filename, errno);
        perror(filename);
        exit(1);
    }

    compress(compressed_buffer, &compressed_size, buffer, buffer_cursor - buffer);
    fwrite(compressed_buffer, compressed_size, 1, out);
    buffer_cursor = buffer;
    fclose(out);
}

/** Init the profiling data structures. */
void profiling_init(jstring* target_directory) {
    char* target = calloc(target_directory->count + 1, sizeof(char));
    buffer = calloc(CHUNK_SIZE, sizeof(unsigned char));
    to_string(target, target_directory->count, target_directory);
    dump_directory = target;
    mkdir(dump_directory, 0755);
    buffer_cursor = buffer;
}

/** Adds `value` to the current block. */
void block_push(char value) {

    if (buffer_cursor == buffer + CHUNK_SIZE) {
        block_dump();
    }

    *buffer_cursor = value;
    buffer_cursor += 1;
}

/** Pushes an int value encoded as unsigned LEB128 */
void block_push_leb128(int value) {
    do {
        char byte = value & 0x7F;
        value >>= 7;
        if (value != 0) {
            byte |= 0x80;
        }
        block_push(byte);
    } while (value != 0);
}


/** Log a `call` instruction. */
void log_call(int typeid, int methid) {
    block_push(TAG_CALL);
    block_push_leb128(typeid);
    block_push_leb128(methid);
}

/** Log a `load` instructions. */
void log_load() {
    block_push(TAG_LOAD);
}

/** Log a `store` instruction. */
void log_store() {
    block_push(TAG_STORE);
}

/** Log a `classalloc` instruction. */
void log_classalloc(int typeid) {
    block_push(TAG_CLASSALLOC);
    block_push_leb128(typeid);
}

/** Log a `method` instruction */
void log_method(int actualTypeid, int scopeTypeid, int methid) {
    block_push(TAG_METHOD);
    block_push_leb128(actualTypeid);
    block_push_leb128(scopeTypeid);
    block_push_leb128(methid);
}

/** Log a `as` instruction. */
void log_as(int fromTypeId, int toTypeId) {
    block_push(TAG_AS);
    block_push_leb128(fromTypeId);
    block_push_leb128(toTypeId);
}

/** Log a `is` instruction. */
void log_is(int typeid, int expected) {
    block_push(TAG_IS);
    block_push_leb128(typeid);
    block_push_leb128(expected);
}

/** Log a `box` instruction. */
void log_box(int toTypeId) {
    block_push(TAG_BOX);
    block_push_leb128(toTypeId);
}

/** Log a `unbox` instruction. */
void log_unbox(int fromTypeId) {
    block_push(TAG_UNBOX);
    block_push_leb128(fromTypeId);
}

