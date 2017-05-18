#include "Bitmap.h"
#include "../Constants.h"
#include "../Log.h"
#include "../utils/MathUtils.h"

Bitmap* Bitmap_alloc(size_t size, word_t *offset) {
    assert(size % WORD_SIZE == 0);
    assert(size % MIN_BLOCK_SIZE == 0);
    size_t nbBlocks = size / MIN_BLOCK_SIZE;
    unsigned long nbWords = divAndRoundUp(nbBlocks, BITS_PER_WORD);
    void* words = calloc(nbWords, WORD_SIZE);
    Bitmap* bitmap = malloc(sizeof(Bitmap));
    bitmap->words = words;
    bitmap->size = size;
    bitmap->offset = (ubyte_t*)offset;
    return bitmap;
}

size_t addressToIndex(ubyte_t* offset, ubyte_t* addr) {
    return (addr - offset) / BITMAP_GRANULARITY;
}

void Bitmap_setBit(Bitmap *bitmap, ubyte_t *addr) {
    assert(addr >= bitmap->offset && addr < bitmap->offset + bitmap->size * MIN_BLOCK_SIZE);
    size_t index = addressToIndex(bitmap->offset, addr);
    bitmap->words[WORD_OFFSET(index)] |= (1LLU << BIT_OFFSET(index));
}

void Bitmap_clearBit(Bitmap *bitmap, ubyte_t *addr) {
    assert(addr >= bitmap->offset && addr < bitmap->offset + bitmap->size * MIN_BLOCK_SIZE);

    size_t index = addressToIndex(bitmap->offset, addr);

    bitmap->words[WORD_OFFSET(index)] &= ~(1LLU << BIT_OFFSET(index));
}

int Bitmap_getBit(Bitmap *bitmap, ubyte_t *addr) {
    assert(addr >= bitmap->offset && addr < bitmap->offset + bitmap->size * MIN_BLOCK_SIZE);

    size_t index = addressToIndex(bitmap->offset, addr);
    word_t bit = bitmap->words[WORD_OFFSET(index)] & (1LLU << BIT_OFFSET(index));
    return bit != 0;
}


void Bitmap_grow(Bitmap *bitmap, size_t nb_words) {
    size_t current_nb_words = divAndRoundUp(bitmap->size / WORD_SIZE, BITS_PER_WORD);
    size_t new_nb_words = current_nb_words + BITS_PER_WORD * nb_words;
    bitmap->words = realloc(bitmap->words, new_nb_words * WORD_SIZE);
    bitmap->size = new_nb_words * WORD_SIZE;
}