#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
// sscanf and getenv are deprecated in WinCRT, disable warnings
#define _CRT_SECURE_NO_WARNINGS
#include <windows.h>
#endif

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include "Parsing.h"

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

size_t Parse_Env_Or_Default(const char *envName, size_t defaultSizeInBytes) {
    return Parse_Size_Or_Default(getenv(envName), defaultSizeInBytes);
}

size_t Parse_Env_Or_Default_String(const char *envName,
                                   const char *defaultSizeString) {
    if (envName == NULL)
        return Parse_Size_Or_Default(defaultSizeString, 0L);
    else
        return Parse_Size_Or_Default(
            getenv(envName), Parse_Size_Or_Default(defaultSizeString, 0L));
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
