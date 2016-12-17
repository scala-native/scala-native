#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "gc.h"

#define CHUNK_SIZE (1024 * 1024 * 128) // 128 MB

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

char* to_string(jstring* str) {
    size_t length = str->count;
    char* cs = (char*) scalanative_alloc(NULL, (length + 1) * sizeof(char));

    for (int i = 0; i < length; ++i) {
        cs[i] = (char) str->value->chars[i];
    }
    cs[length] = '\0';

    return cs;
}

/** Defines a block that holds events */
typedef struct profiling_block {
    /** First address of the block. */
    char* start;

    /** Last untouched address of the block. */
    char* last;

    /** Pointer to the next block, if any. */
    struct profiling_block* next;
} profiling_block;

/** First block that holds events. */
profiling_block* start;

/** The block we're currently writing to. */
profiling_block* current_block;

/** Create a new block */
profiling_block* profiling_get_block() {
    char* buffer = (char*) calloc(CHUNK_SIZE, sizeof(char));
    profiling_block* block = (profiling_block*) malloc(sizeof(profiling_block));

    block->start = buffer;
    block->last  = buffer;
    block->next  = NULL;

    return block;
}

/** Init the profiling data structures. */
void profiling_init() {
    profiling_block* block = profiling_get_block();
    start         = block;
    current_block = block;
}

/** Adds `value` to the current block. */
void block_push(char value) {

    if (start == NULL) {
        profiling_init();
    }

    if (current_block->last == current_block->start + CHUNK_SIZE) {
        profiling_block* new_block = profiling_get_block();
        current_block->next = new_block;
        current_block = new_block;
    }

    *current_block->last = value;
    current_block->last += 1;
}

/** Adds `count` elements from `values` to the current block. */
void block_push_many(char* values, int count) {
    for (int i = 0; i < count; ++i) {
        block_push(values[i]);
    }
}

void block_push_long(long value) {
    char bytes[8];
    bytes[0] = (value >> 56) & 0xFF;
    bytes[1] = (value >> 48) & 0xFF;
    bytes[2] = (value >> 40) & 0xFF;
    bytes[3] = (value >> 32) & 0xFF;
    bytes[4] = (value >> 24) & 0xFF;
    bytes[5] = (value >> 16) & 0xFF;
    bytes[6] = (value >>  8) & 0xFF;
    bytes[7] =  value        & 0xFF;

    block_push_many(bytes, 8);
}

/** Pushes an int on the current block */
void block_push_int(int value) {
    char bytes[4];
    bytes[0] = (value >> 24) & 0xFF;
    bytes[1] = (value >> 16) & 0xFF;
    bytes[2] = (value >> 8 ) & 0xFF;
    bytes[3] =  value        & 0xFF;

    block_push_many(bytes, 4);
}

/** Pushes a string on the current block. */
void block_push_string(char* str) {
    size_t count = strlen(str);
    block_push_int(count);
    block_push_many(str, count);
}

/** Pushes a pointer on the current block. */
void block_push_ptr(void* ptr) {
    block_push_long((long) ptr);
}

/** Dumps all the blocks to a binary file at `dump_location`. */
void block_dump(jstring* dump_location) {
    FILE* out = fopen(to_string(dump_location), "wb");
    profiling_block* block = start;

    while (block != NULL) {
        fwrite(block->start, block->last - block->start, 1, out);
        block = block->next;
    }

    fclose(out);
}

/** Log a `call` instruction. */
void log_call(void* ptr, int argc) {
    block_push(TAG_CALL);
    block_push_ptr(ptr);
    block_push_int(argc);
}

/** Log a `load` instructions. */
void log_load(void* ptr) {
    block_push(TAG_LOAD);
    block_push_ptr(ptr);
}

/** Log a `store` instruction. */
void log_store(void* ptr) {
    block_push(TAG_STORE);
    block_push_ptr(ptr);
}

/** Log a `classalloc` instruction. */
void log_classalloc(int typeid, jstring* name) {
    block_push(TAG_CLASSALLOC);
    block_push_int(typeid);
    block_push_string(to_string(name));
}

/** Log a `method` instruction */
void log_method(int actualTypeid, int scopeTypeid, jstring* name) {
    block_push(TAG_METHOD);
    block_push_int(actualTypeid);
    block_push_int(scopeTypeid);
    block_push_string(to_string(name));
}

/** Log a `as` instruction. */
void log_as(int fromTypeId, int toTypeId, void* obj) {
    block_push(TAG_AS);
    block_push_int(fromTypeId);
    block_push_int(toTypeId);
    block_push_ptr(obj);
}

/** Log a `is` instruction. */
void log_is(int typeid, int expected, void* obj) {
    block_push(TAG_IS);
    block_push_int(typeid);
    block_push_int(expected);
    block_push_ptr(obj);
}

/** Log a `box` instruction. */
void log_box(int toTypeId) {
    block_push(TAG_BOX);
    block_push_int(toTypeId);
}

/** Log a `unbox` instruction. */
void log_unbox(int fromTypeId, void* obj) {
    block_push(TAG_UNBOX);
    block_push_int(fromTypeId);
    block_push_ptr(obj);
}

