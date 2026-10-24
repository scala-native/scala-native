#ifndef STRING_CONSTANTS_H
#define STRING_CONSTANTS_H

/* Inline string prefixes instead of cross-TU extern symbols. Windows thin LTO
 * can drop non-bitcode C objects and leave Scala @extern references undefined.
 */
#define SN_ERROR_PREFIX "ScalaNative:"
#define SN_FATAL_ERROR_PREFIX "ScalaNative Fatal Error:"
#define SN_ERROR_MSG(msg) SN_ERROR_PREFIX " " msg
#define SN_FATAL_ERROR_MSG(msg) SN_FATAL_ERROR_PREFIX " " msg

#endif // STRING_CONSTANTS_H
