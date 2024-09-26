#ifndef GC_MEMORY_H
#define GC_MEMORY_H

/*
 * Author:  David Robert Nadeau
 * Site:    http://NadeauSoftware.com/
 * License: Creative Commons Attribution 3.0 Unported License
 *          http://creativecommons.org/licenses/by/3.0/deed.en_US
 */

#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#elif defined(__unix__) || defined(__unix) || defined(unix) ||                 \
    (defined(__APPLE__) && defined(__MACH__))
#include <unistd.h>
#include <sys/types.h>
#include <sys/param.h>
#if defined(BSD)
#include <sys/sysctl.h>
#endif

#else
#error "Unable to define getMemorySize() for an unknown OS."
#endif

/**
 * Returns the size of physical memory (RAM) in bytes.
 */
size_t getMemorySize() {
#if defined(_WIN32) && (defined(__CYGWIN__) || defined(__CYGWIN32__))
    /* Cygwin under Windows. ------------------------------------ */
    /* New 64-bit MEMORYSTATUSEX isn't available.  Use old 32.bit */
    MEMORYSTATUS status;
    status.dwLength = sizeof(status);
    GlobalMemoryStatus(&status);
    return (size_t)status.dwTotalPhys;

#elif defined(_WIN32)
    /* Windows. ------------------------------------------------- */
    /* Use new 64-bit MEMORYSTATUSEX, not old 32-bit MEMORYSTATUS */
    MEMORYSTATUSEX status;
    status.dwLength = sizeof(status);
    GlobalMemoryStatusEx(&status);
    return (size_t)status.ullTotalPhys;

#elif defined(__unix__) || defined(__unix) || defined(unix) ||                 \
    (defined(__APPLE__) && defined(__MACH__))
    /* UNIX variants. ------------------------------------------- */
    /* Prefer sysctl() over sysconf() except sysctl() HW_REALMEM and HW_PHYSMEM
     */

#if defined(CTL_HW) && (defined(HW_MEMSIZE) || defined(HW_PHYSMEM64))
    int mib[2];
    mib[0] = CTL_HW;
#if defined(HW_MEMSIZE)
    mib[1] = HW_MEMSIZE; /* OSX. --------------------- */
#elif defined(HW_PHYSMEM64)
    mib[1] = HW_PHYSMEM64; /* NetBSD, OpenBSD. --------- */
#endif
    /* 64-bit */
    int64_t size = 0;
    size_t len = sizeof(size);
    if (sysctl(mib, 2, &size, &len, NULL, 0) == 0)
        return (size_t)size;
    return 0L; /* Failed? */

#elif defined(_SC_AIX_REALMEM)
    /* AIX. ----------------------------------------------------- */
    return (size_t)sysconf(_SC_AIX_REALMEM) * (size_t)1024L;

#elif defined(_SC_PHYS_PAGES) && defined(_SC_PAGESIZE)
    /* FreeBSD, Linux, OpenBSD, and Solaris. -------------------- */
    return (size_t)sysconf(_SC_PHYS_PAGES) * (size_t)sysconf(_SC_PAGESIZE);

#elif defined(_SC_PHYS_PAGES) && defined(_SC_PAGE_SIZE)
    /* Legacy. -------------------------------------------------- */
    return (size_t)sysconf(_SC_PHYS_PAGES) * (size_t)sysconf(_SC_PAGE_SIZE);

#elif defined(CTL_HW) && (defined(HW_PHYSMEM) || defined(HW_REALMEM))
    /* DragonFly BSD, FreeBSD, NetBSD, OpenBSD, and OSX. -------- */
    int mib[2];
    mib[0] = CTL_HW;
#if defined(HW_REALMEM)
    /* FreeBSD. ----------------- */
    mib[1] = HW_REALMEM;
#elif defined(HW_PYSMEM)
    mib[1] = HW_PHYSMEM; /* Others. ------------------ */
#endif
    unsigned int size = 0; /* 32-bit */
    size_t len = sizeof(size);
    if (sysctl(mib, 2, &size, &len, NULL, 0) == 0)
        return (size_t)size;
    return 0L; /* Failed? */
#endif /* sysctl and sysconf variants */

#else
    return 0L; /* Unknown OS. */
#endif
}

/**
 * Returns the size of available free memory (RAM) in bytes.
 */
size_t getFreeMemorySize() {
#if defined(_WIN32) && (defined(__CYGWIN__) || defined(__CYGWIN32__))
    /* Cygwin under Windows. ------------------------------------ */
    /* New 64-bit MEMORYSTATUSEX isn't available.  Use old 32.bit */
    MEMORYSTATUS status;
    status.dwLength = sizeof(status);
    GlobalMemoryStatus(&status);
    return (size_t)status.dwAvailPhys;

#elif defined(_WIN32)
    /* Windows. ------------------------------------------------- */
    /* Use new 64-bit MEMORYSTATUSEX, not old 32-bit MEMORYSTATUS */
    MEMORYSTATUSEX status;
    status.dwLength = sizeof(status);
    GlobalMemoryStatusEx(&status);
    return (size_t)status.ullAvailPhys;

#elif defined(__FreeBSD__)
    /* FreeBSD uses a different paging model. It documents that it tries
     * to keep the number of free pages low; basically zero plus a few
     * guard pages intended for its own use.
     *
     * The concept of _SC_AVPHYS_PAGES makes little to no sense on FreeBSD
     * so it does not have that sysconf parameter, causing a build failure
     * without this #elif.
     *
     * An approximation of that parameter can be calculated from
     * a detailed knowledge of FreeBSD practices, but those are subject to
     * change.
     *
     * This function is currently used to provide values for informational
     * messages, where the "remaining" is likely to be close to zero, in
     * the Linux sense, already. Return the "close to zero" limit 0L.
     * No sense in calculating a better approximation.
     */

    return 0L;

#elif (defined(__unix__) || defined(__unix) || defined(unix) ||                \
       (defined(__APPLE__) && defined(__MACH__)) && defined(_SC_AVPHYS_PAGES))
    /* UNIX variants; yes NetBSD, but not FreeBSD --------------------- */
    long pages = sysconf(_SC_AVPHYS_PAGES);
    long page_size = sysconf(_SC_PAGESIZE);
    if (pages == -1 || page_size == -1)
        return 0L;
    return (size_t)pages * (size_t)page_size;

#else
    return 0L; /* Unknown OS. */
#endif
}

#endif // GC_MEMORY_H
