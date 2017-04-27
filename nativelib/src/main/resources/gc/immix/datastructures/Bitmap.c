#include "Bitmap.h"
#include "../Constants.h"
#include "../Log.h"

Bitmap* bitmap_alloc(size_t size, word_t* offset) {
    assert(size % sizeof(word_t) == 0);
    assert(size % MIN_BLOCK_SIZE == 0);
    size_t nbBlocks = size / MIN_BLOCK_SIZE;
    unsigned long nbWords = (nbBlocks + BITS_PER_WORD - 1) / BITS_PER_WORD;
    void* words = calloc(nbWords, sizeof(word_t));
    Bitmap* bitmap = malloc(sizeof(Bitmap));
    bitmap->words = words;
    bitmap->size = size;
    bitmap->offset = (ubyte_t*)offset;
    return bitmap;
}

size_t addressToIndex(ubyte_t* offset, ubyte_t* addr) {
    return (addr - offset) / BITMAP_GRANULARITY;
}

void bitmap_setBit(Bitmap* bitmap, ubyte_t* addr) {
    assert(addr >= bitmap->offset && addr < bitmap->offset + (bitmap->size * MIN_BLOCK_SIZE / WORD_SIZE));
    size_t index = addressToIndex(bitmap->offset, addr);
    bitmap->words[WORD_OFFSET(index)] |= (1LLU << BIT_OFFSET(index));
}

void bitmap_clearBit(Bitmap* bitmap, ubyte_t* addr) {
    assert(addr >= bitmap->offset && addr < bitmap->offset + (bitmap->size * MIN_BLOCK_SIZE / WORD_SIZE));

    size_t index = addressToIndex(bitmap->offset, addr);

    bitmap->words[WORD_OFFSET(index)] &= ~(1LLU << BIT_OFFSET(index));
}

int bitmap_getBit(Bitmap* bitmap, ubyte_t* addr) {
    assert(addr >= bitmap->offset && addr < bitmap->offset + (bitmap->size * MIN_BLOCK_SIZE / WORD_SIZE));

    size_t index = addressToIndex(bitmap->offset, addr);
    word_t bit = bitmap->words[WORD_OFFSET(index)] & (1LLU << BIT_OFFSET(index));
    return bit != 0;
}


void bitmap_grow(Bitmap* bitmap, size_t nb_words) {
    size_t current_nb_words = (bitmap->size / sizeof(word_t) + BITS_PER_WORD - 1) / BITS_PER_WORD;
    size_t new_nb_words = current_nb_words + BITS_PER_WORD * nb_words;
    bitmap->words = realloc(bitmap->words, new_nb_words * sizeof(word_t));
    bitmap->size = new_nb_words * sizeof(word_t);
}