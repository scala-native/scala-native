#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_SYS_MMAN)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

#include <sys/mman.h>

// Return code
void *scalanative_map_failed() { return MAP_FAILED; }

// Symbolic "constants"

int scalanative_prot_exec() { return PROT_EXEC; }
int scalanative_prot_read() { return PROT_READ; }
int scalanative_prot_write() { return PROT_WRITE; }
int scalanative_prot_none() { return PROT_NONE; }

#if defined(MAP_ANONYMOUS)
int scalanative_map_anon() { return MAP_ANONYMOUS; }
int scalanative_map_anonymous() { return MAP_ANONYMOUS; }
#elif defined(MAP_ANON)
int scalanative_map_anon() { return MAP_ANON; }
int scalanative_map_anonymous() { return MAP_ANON; }
#else
#error "Neither MAP_ANONYMOUS nor MAP_ANON is defined."
#endif

int scalanative_map_shared() { return MAP_SHARED; }
int scalanative_map_private() { return MAP_PRIVATE; }
int scalanative_map_fixed() { return MAP_FIXED; }

// XSI|SIO 'constants' for msync()
int scalanative_ms_sync() { return MS_SYNC; }
int scalanative_ms_async() { return MS_ASYNC; }
int scalanative_ms_invalidate() { return MS_INVALIDATE; }

#endif // Unix or Mac OS
#endif
