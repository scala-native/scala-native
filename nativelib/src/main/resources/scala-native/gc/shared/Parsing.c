// Parsing.c is used by all GCs

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
// sscanf and getenv are deprecated in WinCRT, disable warnings
#define _CRT_SECURE_NO_WARNINGS
#include <windows.h>
#endif

// Idiomatic 'stringify' macros for compile time memory
#define VAL_STR(x) STR(x)
#define STR(x) #x

#include <errno.h>
#include <stdbool.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include "shared/Parsing.h"
#include "shared/Log.h"

// Parses a size with an optional K/M/G suffix into *size. Returns false when
// str is NULL, empty, has no leading digits or the size does not fit size_t.
static bool Parse_Size(const char *str, size_t *size) {
    if (str == NULL) {
        return false;
    }
    char *end;
    errno = 0;
    unsigned long long parsed = strtoull(str, &end, 10);
    // Empty or no leading digits, e.g. GC_MAXIMUM_HEAP_SIZE= or "G"
    if (end == str) {
        return false;
    }
    // Out of range, or would wrap when narrowed on 32-bit targets
    if (errno == ERANGE || parsed > SIZE_MAX) {
        return false;
    }
    *size = (size_t)parsed;
    int shift = 0;
    switch (str[strlen(str) - 1]) {
    case 'k':
    case 'K':
        shift = 10;
        break;
    case 'm':
    case 'M':
        shift = 20;
        break;
    case 'g':
    case 'G':
        shift = 30;
        break;
    }
    if (shift > 0) {
        if (*size >= (1ULL << (8 * sizeof(size_t) - shift))) {
            return false;
        }
        *size <<= shift;
    }
    return true;
}

size_t Parse_Size_Or_Default(const char *str, size_t defaultSizeInBytes) {
    size_t size;
    return Parse_Size(str, &size) ? size : defaultSizeInBytes;
}

const char *get_defined_or_env(const char *envName) {
    if (envName == NULL)
        return NULL;

    const char *env = getenv(envName);
    if (env != NULL)
        return env; // environment overrides compile time

    return get_defined(envName);
}

const char *get_defined(const char *envName) {
    if (envName == NULL)
        return NULL;

// check for compile time defined values
#if defined(GC_INITIAL_HEAP_SIZE)
    if (strcmp(envName, "GC_INITIAL_HEAP_SIZE") == 0) {
        return VAL_STR(GC_INITIAL_HEAP_SIZE);
    }
#endif

#if defined(GC_MAXIMUM_HEAP_SIZE)
    if (strcmp(envName, "GC_MAXIMUM_HEAP_SIZE") == 0) {
        return VAL_STR(GC_MAXIMUM_HEAP_SIZE);
    }
#endif

#if defined(GC_THREAD_HEAP_BLOCK_SIZE)
    if (strcmp(envName, "GC_THREAD_HEAP_BLOCK_SIZE") == 0) {
        return VAL_STR(GC_THREAD_HEAP_BLOCK_SIZE);
    }
#endif

#if defined(GC_NPROCS)
    if (strcmp(envName, "GC_NPROCS") == 0) {
        return VAL_STR(GC_NPROCS);
    }
#endif

    return NULL; // no compile time value
}

size_t Parse_Env_Or_Default(const char *envName, size_t defaultSizeInBytes) {
    const char *res = get_defined_or_env(envName);
    size_t result;
    if (res == NULL) {
        GC_LOG_INFO("%s not set, using default %zu", envName,
                    defaultSizeInBytes);
        return defaultSizeInBytes;
    }
    if (!Parse_Size(res, &result)) {
        GC_LOG_WARN("Ignoring %s=%s, which is not a valid size, using default "
                    "%zu",
                    envName, res, defaultSizeInBytes);
        return defaultSizeInBytes;
    }
    GC_LOG_INFO("Found %s=%s, parsed to %zu", envName, res, result);
    return result;
}

size_t Parse_Env_Or_Default_String(const char *envName,
                                   const char *defaultSizeString) {
    size_t defaultSizeInBytes = Parse_Size_Or_Default(defaultSizeString, 0L);
    if (envName == NULL)
        return defaultSizeInBytes;
    else
        return Parse_Env_Or_Default(envName, defaultSizeInBytes);
}

// =============================================================================
// Plain Integer Parsing (for timeouts, counts, etc.)
// =============================================================================

uint64_t Parse_Long_Or_Default(const char *str, uint64_t defaultValue) {
    if (str == NULL || str[0] == '\0') {
        return defaultValue;
    }

    char *endptr;
    unsigned long long value = strtoull(str, &endptr, 10);

    // Check if parsing succeeded (at least one digit consumed)
    if (endptr == str) {
        return defaultValue;
    }

    return (uint64_t)value;
}

uint64_t Parse_Env_Or_Default_Long(const char *envName, uint64_t defaultValue) {
    if (envName == NULL) {
        return defaultValue;
    }

    const char *env = getenv(envName);
    uint64_t result = Parse_Long_Or_Default(env, defaultValue);
    if (env != NULL) {
        GC_LOG_DEBUG("Found %s=%s, parsed to %llu", envName, env,
                     (unsigned long long)result);
    } else {
        GC_LOG_DEBUG("%s not set, using default %llu", envName,
                     (unsigned long long)defaultValue);
    }
    return result;
}

size_t Choose_IF(size_t left, qualifier qualifier, size_t right) {
    switch (qualifier) {
    case Greater_Than:
        if (left > right) {
            return left;
        } else {
            return right;
        }

    case Less_Than:
        if (left < right) {
            return left;
        } else {
            return right;
        }
    case Equal_To:
        if (left == right) {
            return left;
        } else {
            return right;
        }
    case Greater_OR_Equal:
        if (left >= right) {
            return left;
        } else {
            return right;
        }
    case Less_OR_Equal:
        if (left <= right) {
            return left;
        } else {
            return right;
        }
    }
}
