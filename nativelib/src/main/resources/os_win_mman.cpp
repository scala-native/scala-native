#ifdef _WIN32
#include "Windows.h"

extern "C" {
    #include "os_win_mman.h"

    int mprotect(void *addr, size_t len, int prot)
    {
        static int oldProt;
        if (prot == PROT_NONE)
        {
#ifdef SCALA_NATIVE_EXPERIMENTAL_MEMORY_SAFEPOINT
            return VirtualProtect(addr, len, PAGE_NOACCESS | PAGE_GUARD, &oldProt) == TRUE ? 0 : -1;
#else
            return 0;
#endif
        }
        else
        {
#ifdef SCALA_NATIVE_EXPERIMENTAL_MEMORY_SAFEPOINT
            return VirtualProtect(addr, len, PAGE_READ, oldProt) == TRUE ? 0 : -1;
#else
            return 0;
#endif
        }
        return -1;
    }
}

#endif