#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

#include "immix_commix/References.h"

bool References_clearSoft = false;
atomic_bool References_softFound = false;
atomic_uint_fast64_t References_softClearings = 0;

#endif
