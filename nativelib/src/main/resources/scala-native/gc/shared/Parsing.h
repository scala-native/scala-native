#ifndef PARSING_H
#define PARSING_H
#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
// sscanf and getEnv is deprecated in WinCRT, disable warnings
#define _CRT_SECURE_NO_WARNINGS
#include <windows.h>
#endif

#include <stddef.h>

size_t Parse_Size_Or_Default(const char *str, size_t defaultSizeInBytes);

size_t Parse_Env_Or_Default(const char *envName, size_t defaultSizeInBytes);

size_t Parse_Env_Or_Default_String(const char *envName,
                                   const char *defaultSizeString);

typedef enum {
    Greater_Than,
    Less_Than,
    Equal_To,
    Greater_OR_Equal,
    Less_OR_Equal
} qualifier;

size_t Choose_IF(size_t left, qualifier qualifier, size_t right);

#endif // PARSING_H
