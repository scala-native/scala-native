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

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include "shared/Parsing.h"

size_t Parse_Size_Or_Default(const char *str, size_t defaultSizeInBytes) {
    if (str == NULL) {
        return defaultSizeInBytes;
    } else {
        int length = strlen(str);
        size_t size;
        sscanf(str, "%zu", &size);
        char possibleSuffix = str[length - 1];
        switch (possibleSuffix) {
        case 'k':
        case 'K':
            if (size < (1ULL << (8 * sizeof(size_t) - 10))) {
                size <<= 10;
            } else {
                size = defaultSizeInBytes;
            }
            break;
        case 'm':
        case 'M':
            if (size < (1ULL << (8 * sizeof(size_t) - 20))) {
                size <<= 20;
            } else {
                size = defaultSizeInBytes;
            }
            break;
        case 'g':
        case 'G':
            if (size < (1ULL << (8 * sizeof(size_t) - 30))) {
                size <<= 30;
            } else {
                size = defaultSizeInBytes;
            }
        }
        return size;
    }
    return defaultSizeInBytes;
}

const char *get_defined_or_env(const char *envName) {
    if (envName == NULL)
        return NULL;

    const char *env = getenv(envName);
    if (env != NULL)
        return env; // environment overrides compile time

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

    return NULL; // no compile time or runtime value
}

size_t Parse_Env_Or_Default(const char *envName, size_t defaultSizeInBytes) {
    const char *res = get_defined_or_env(envName);
#ifdef DEBUG_PRINT
    printf("%s=%s\n", envName, res);
    fflush(stdout);
#endif
    return Parse_Size_Or_Default(res, defaultSizeInBytes);
}

size_t Parse_Env_Or_Default_String(const char *envName,
                                   const char *defaultSizeString) {
    size_t defaultSizeInBytes = Parse_Size_Or_Default(defaultSizeString, 0L);
    if (envName == NULL)
        return defaultSizeInBytes;
    else
        return Parse_Env_Or_Default(envName, defaultSizeInBytes);
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
