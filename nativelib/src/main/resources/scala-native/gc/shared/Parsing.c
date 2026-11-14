// Parsing.c is used by all GCs

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
// sscanf and getenv are deprecated in WinCRT, disable warnings
#define _CRT_SECURE_NO_WARNINGS
#include <windows.h>
#endif

#include <errno.h>
#include <stdbool.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include "shared/Parsing.h"
#include "shared/Log.h"

// Parses digits with an optional single K/M/G suffix into *size. Returns false
// for NULL, anything else (signs, spaces, other trailing text) or a size that
// does not fit size_t.
static bool Parse_Size(const char *str, size_t *size) {
    // strtoull would accept leading spaces and signs, "-1" being SIZE_MAX
    if (str == NULL || str[0] < '0' || str[0] > '9') {
        return false;
    }
    char *end;
    errno = 0;
    unsigned long long parsed = strtoull(str, &end, 10);
    // Out of range, or would wrap when narrowed on 32-bit targets
    if (errno == ERANGE || parsed > SIZE_MAX) {
        return false;
    }
    *size = (size_t)parsed;
    int shift = 0;
    switch (*end) {
    case '\0':
        break;
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
    default:
        return false;
    }
    if (shift > 0 && end[1] != '\0') {
        return false;
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

size_t Parse_Env_Or_Default(const char *envName, size_t defaultSizeInBytes) {
    const char *res = getenv(envName);
    size_t result;
    if (res == NULL) {
        GC_LOG_INFO("%s not set, using %zu", envName, defaultSizeInBytes);
        return defaultSizeInBytes;
    }
    if (!Parse_Size(res, &result)) {
        GC_LOG_WARN("Ignoring %s=%s, which is not a valid size, using %zu",
                    envName, res, defaultSizeInBytes);
        return defaultSizeInBytes;
    }
    GC_LOG_INFO("Found %s=%s, parsed to %zu", envName, res, result);
    return result;
}

bool Parse_Env_Is_Size(const char *envName) {
    size_t ignored;
    return Parse_Size(getenv(envName), &ignored);
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
