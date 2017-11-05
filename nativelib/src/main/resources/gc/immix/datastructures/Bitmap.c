#include <string.h>
#include "Bitmap.h"
#include "../Constants.h"
#include "../Log.h"
#include "../utils/MathUtils.h"

Bitmap *Bitmap_Alloc(size_t size, word_t *offset) {
    assert(size % BITMAP_GRANULARITY == 0);

    size_t nbBlocks = size / BITMAP_GRANULARITY;

    unsigned long nbWords = MathUtils_DivAndRoundUp(nbBlocks, BITS_PER_WORD);
    void *words = calloc(nbWords, WORD_SIZE);
    Bitmap *bitmap = malloc(sizeof(Bitmap));
    bitmap->words = words;
    bitmap->size = size;
    bitmap->offset = (ubyte_t *)offset;
    return bitmap;
}

size_t addressToIndex(ubyte_t *offset, ubyte_t *addr) {
    return (addr - offset) / BITMAP_GRANULARITY;
}

void Bitmap_SetBit(Bitmap *bitmap, ubyte_t *addr) {
    assert(addr >= bitmap->offset &&
           addr < bitmap->offset + bitmap->size * MIN_BLOCK_SIZE);
    size_t index = addressToIndex(bitmap->offset, addr);
    bitmap->words[WORD_OFFSET(index)] |=
        ((unsigned long long)1LLU << BIT_OFFSET(index));
}

void Bitmap_ClearBit(Bitmap *bitmap, ubyte_t *addr) {
    assert(addr >= bitmap->offset &&
           addr < bitmap->offset + bitmap->size * MIN_BLOCK_SIZE);

    size_t index = addressToIndex(bitmap->offset, addr);

    bitmap->words[WORD_OFFSET(index)] &=
        ~((unsigned long long)1LLU << BIT_OFFSET(index));
}

int Bitmap_GetBit(Bitmap *bitmap, ubyte_t *addr) {
    assert(addr >= bitmap->offset &&
           addr < bitmap->offset + bitmap->size * MIN_BLOCK_SIZE);

    size_t index = addressToIndex(bitmap->offset, addr);
    word_t bit = bitmap->words[WORD_OFFSET(index)] &
                 ((unsigned long long)1LLU << BIT_OFFSET(index));
    return bit != 0;
}

// increment in bytes
void Bitmap_Grow(Bitmap *bitmap, size_t increment) {
    assert(increment % BITMAP_GRANULARITY == 0);

    size_t nbBlocks = bitmap->size / BITMAP_GRANULARITY;
    size_t nbBlockIncrement = increment / BITMAP_GRANULARITY;

    size_t previousNbWords = MathUtils_DivAndRoundUp(nbBlocks, BITS_PER_WORD);

    size_t totalNbWords =
        MathUtils_DivAndRoundUp(nbBlocks + nbBlockIncrement, BITS_PER_WORD);

    bitmap->words = realloc(bitmap->words, totalNbWords * WORD_SIZE);
    bitmap->size += increment;

    memset(bitmap->words + previousNbWords, 0,
           (totalNbWords - previousNbWords) * WORD_SIZE);
}