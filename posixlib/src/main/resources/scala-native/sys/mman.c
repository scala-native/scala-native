#if defined(__SCALANATIVE_POSIX_SYS_MMAN)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

#include <sys/mman.h>

int scalanative_prot_exec() { return PROT_EXEC; }
int scalanative_prot_read() { return PROT_READ; }
int scalanative_prot_write() { return PROT_WRITE; }
int scalanative_prot_none() { return PROT_NONE; }

int scalanative_map_shared() { return MAP_SHARED; }
int scalanative_map_private() { return MAP_PRIVATE; }
int scalanative_map_fixed() { return MAP_FIXED; }

int scalanative_ms_sync() { return MS_SYNC; }
int scalanative_ms_async() { return MS_ASYNC; }
int scalanative_ms_invalidate() { return MS_INVALIDATE; }

#endif // Unix or Mac OS
#endif