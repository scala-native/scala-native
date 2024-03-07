#ifndef WEAK_REFERENCES_H
#define WEAK_REFERENCES_H

void WeakReferences_Nullify(void);
void WeakReferences_SetGCFinishedCallback(void *handler);
void WeakReferences_InvokeGCFinishedCallback(void);

#endif
