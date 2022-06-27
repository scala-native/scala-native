#ifndef WEAK_REF_STACK_H
#define WEAK_REF_STACK_H
#include "Object.h"
#include "Heap.h"

void WeakRefStack_Nullify(void);
void WeakRefStack_SetHandler(void *handler);
void WeakRefStack_CallHandlers(void);

#endif // WEAK_REF_STACK_H
