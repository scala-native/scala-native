#ifndef IMMIX_COMMIX_HEAP_COMMON_H
#define IMMIX_COMMIX_HEAP_COMMON_H

// The heap functions that Immix and Commix share (HeapCommon.c), for the Heap
// struct of the collector that includes this header, right after the struct
// (Heap.h in either directory)

static inline bool Heap_IsWordInHeap(Heap *heap, word_t *word) {
    return word >= heap->heapStart && word < heap->heapEnd;
}

static inline LineMeta *Heap_LineMetaForWord(Heap *heap, word_t *word) {
    // assumes there are no gaps between lines
    assert(LINE_COUNT * LINE_SIZE == BLOCK_TOTAL_SIZE);
    assert(Heap_IsWordInHeap(heap, word));
    word_t lineGlobalIndex =
        ((word_t)word - (word_t)heap->heapStart) >> LINE_SIZE_BITS;
    assert(lineGlobalIndex >= 0);
    LineMeta *lineMeta = (LineMeta *)heap->lineMetaStart + lineGlobalIndex;
    assert(lineMeta < (LineMeta *)heap->lineMetaEnd);
    return lineMeta;
}

bool Heap_isGrowingPossible(Heap *heap, uint32_t incrementInBlocks);
void Heap_Grow(Heap *heap, uint32_t increment);
void Heap_RefillEmergencyBlock(Heap *heap);
void Heap_ThrowOutOfMemory(Heap *heap);
void Heap_exitWithOutOfMemory(const char *details);
size_t Heap_getMemoryLimit();
size_t Heap_getMemoryUsed(Heap *heap);
word_t *Heap_mapAndAlign(size_t memoryLimit, size_t alignmentSize);

#endif // IMMIX_COMMIX_HEAP_COMMON_H
