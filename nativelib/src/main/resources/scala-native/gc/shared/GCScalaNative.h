#ifndef GC_SCALANATIVE_H
#define GC_SCALANATIVE_H

void scalanative_add_roots(void *addr_low, void *addr_high);

void scalanative_remove_roots(void *addr_low, void *addr_high);

#endif // GC_SCALANATIVE_H
