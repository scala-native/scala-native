#ifndef GC_SHARED_CONFIG_H
#define GC_SHARED_CONFIG_H

// Settings of the garbage collector built into the program.
//
// The values, including the defaults, are defined in one place: the
// GarbageCollectorConfig of the Scala Native build tools, which passes them
// to these sources as SCALANATIVE_GC_CONFIG_<NAME> defines. When the program
// starts each of them can be overridden with its environment variable.
//
//   Define                    Environment variable          Value
//   INITIAL_HEAP_SIZE         GC_INITIAL_HEAP_SIZE          bytes, optional
//   MAX_HEAP_SIZE             GC_MAXIMUM_HEAP_SIZE          bytes, optional
//   THREAD_HEAP_BLOCK_SIZE    GC_THREAD_HEAP_BLOCK_SIZE     bytes
//   THREADS                   GC_NPROCS                     int, optional
//   TIME_RATIO                GC_TIME_RATIO                 double
//   FREE_RATIO                GC_FREE_RATIO                 double
//   MAX_FREE_RATIO            GC_MAX_FREE_RATIO             double
//   SYNC_TIMEOUT_MS           SCALANATIVE_GC_SYNC_TIMEOUT_MS            ms
//   SYNC_WARNING_INTERVAL_MS  SCALANATIVE_GC_SYNC_WARNING_INTERVAL_MS   ms
//   LOG_LEVEL                 SCALANATIVE_GC_LOG_LEVEL      GC_LogLevel
//   LOG_FILE                  GC_LOG_FILE                   char[], optional
//   STATS_FILE                GC_STATS_FILE                 char[], optional
//   STATS_ENABLED             (none)                        0 or 1
//
// Optional settings are not defined unless set in the build, their default is
// computed when the program starts.

#ifndef SCALANATIVE_GC_CONFIG_LOG_LEVEL
#error                                                                         \
    "SCALANATIVE_GC_CONFIG_LOG_LEVEL is not defined, the garbage collector has to be compiled by the Scala Native build tools which define its settings"
#endif

#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)
#if !defined(SCALANATIVE_GC_CONFIG_TIME_RATIO) ||                              \
    !defined(SCALANATIVE_GC_CONFIG_MAX_FREE_RATIO) ||                          \
    !defined(SCALANATIVE_GC_CONFIG_SYNC_TIMEOUT_MS) ||                         \
    !defined(SCALANATIVE_GC_CONFIG_SYNC_WARNING_INTERVAL_MS) ||                \
    !defined(SCALANATIVE_GC_CONFIG_STATS_ENABLED)
#error                                                                         \
    "SCALANATIVE_GC_CONFIG_{TIME_RATIO,MAX_FREE_RATIO,SYNC_TIMEOUT_MS,SYNC_WARNING_INTERVAL_MS,STATS_ENABLED} have to be defined by the Scala Native build tools"
#endif
#endif

#if defined(SCALANATIVE_GC_COMMIX)
#if !defined(SCALANATIVE_GC_CONFIG_FREE_RATIO)
#error                                                                         \
    "SCALANATIVE_GC_CONFIG_FREE_RATIO has to be defined by the Scala Native build tools"
#endif
#endif

#if defined(SCALANATIVE_GC_NONE) &&                                            \
    defined(SCALANATIVE_MULTITHREADING_ENABLED) &&                             \
    !defined(SCALANATIVE_GC_CONFIG_THREAD_HEAP_BLOCK_SIZE)
#error                                                                         \
    "SCALANATIVE_GC_CONFIG_THREAD_HEAP_BLOCK_SIZE has to be defined by the Scala Native build tools"
#endif

// The heap sizes set in the build, or `computed` if there are none.
#ifdef SCALANATIVE_GC_CONFIG_INITIAL_HEAP_SIZE
#define GC_CONFIG_INITIAL_HEAP_SIZE_OR(computed)                               \
    ((size_t)SCALANATIVE_GC_CONFIG_INITIAL_HEAP_SIZE)
#else
#define GC_CONFIG_INITIAL_HEAP_SIZE_OR(computed) (computed)
#endif

#ifdef SCALANATIVE_GC_CONFIG_MAX_HEAP_SIZE
#define GC_CONFIG_MAX_HEAP_SIZE_OR(computed)                                   \
    ((size_t)SCALANATIVE_GC_CONFIG_MAX_HEAP_SIZE)
#else
#define GC_CONFIG_MAX_HEAP_SIZE_OR(computed) (computed)
#endif

#endif // GC_SHARED_CONFIG_H
