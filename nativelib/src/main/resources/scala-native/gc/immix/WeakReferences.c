#if defined(SCALANATIVE_GC_IMMIX)

#include "WeakReferences.h"
#include "immix_commix/datastructures/Stack.h"
#include "immix_commix/metadata/ObjectMeta.h"
#include "immix_commix/headers/ObjectHeader.h"
#include "State.h"
#include "nativeThreadTLS.h"
#include <stdbool.h>

static bool collectedWeakReferences = false;
static void (*gcFinishedCallback)() = NULL;

// weakRefStack holds the references found while marking whose referent the
// marker did not mark: weak and phantom references, and soft references in a
// collection that clears them (see immix_commix/References.h). Referents that
// nothing else marked are set to NULL here.

void WeakReferences_Nullify(void) {
    Bytemap *bytemap = heap.bytemap;
    while (!Stack_IsEmpty(&weakRefStack)) {
        Object *object = Stack_Pop(&weakRefStack);
        Object **weakRefReferantField =
            (Object **)Reference_ReferentField(object);
        word_t *weakRefReferant = (word_t *)*weakRefReferantField;
        if (Heap_IsWordInHeap(&heap, weakRefReferant)) {
            ObjectMeta *objectMeta = Bytemap_Get(bytemap, weakRefReferant);
            if (ObjectMeta_IsAllocated(objectMeta) &&
                !ObjectMeta_IsMarked(objectMeta)) {
                // WeakReferences should have the held referent
                // field set to null if collected
                *weakRefReferantField = NULL;
                collectedWeakReferences = true;
            }
        }
    }
}

void WeakReferences_SetGCFinishedCallback(void *handler) {
    gcFinishedCallback = handler;
}

void WeakReferences_InvokeGCFinishedCallback(void) {
    // Only after a collection that cleared referents
    if (collectedWeakReferences && gcFinishedCallback != NULL &&
        currentThreadInfo.isInitialized) {
        collectedWeakReferences = false;
        gcFinishedCallback();
    }
}

#endif
