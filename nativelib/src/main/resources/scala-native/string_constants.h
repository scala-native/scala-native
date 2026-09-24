#ifndef STRING_CONSTANTS_H
#define STRING_CONSTANTS_H

#define SN_ERROR_MSG(msg) "ScalaNative: " msg
#define SN_FATAL_ERROR_MSG(msg) "ScalaNative Fatal Error: " msg

#ifdef __cplusplus
extern "C" {
#endif

extern const char *const snErrorPrefix;
extern const char *const snFatalErrorPrefix;

#ifdef __cplusplus
}
#endif

#endif // CONSTANTS_H
