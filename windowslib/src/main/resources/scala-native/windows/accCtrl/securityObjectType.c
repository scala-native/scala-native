#if (defined(_WIN32) || defined(WIN32))
#define WIN32_LEAN_AND_MEAN
#include <accctrl.h>

int scalanative_se_unknown_object_type() { return SE_UNKNOWN_OBJECT_TYPE; }
int scalanative_se_file_object() { return SE_FILE_OBJECT; }
int scalanative_se_service() { return SE_SERVICE; }
int scalanative_se_printer() { return SE_PRINTER; }
int scalanative_se_registry_key() { return SE_REGISTRY_KEY; }
int scalanative_se_lmshare() { return SE_LMSHARE; }
int scalanative_se_kernel_object() { return SE_KERNEL_OBJECT; }
int scalanative_se_window_object() { return SE_WINDOW_OBJECT; }
int scalanative_se_ds_object() { return SE_DS_OBJECT; }
int scalanative_se_ds_object_all() { return SE_DS_OBJECT_ALL; }
int scalanative_se_provider_defined_object() {
    return SE_PROVIDER_DEFINED_OBJECT;
}
int scalanative_se_wmiguid_object() { return SE_WMIGUID_OBJECT; }
int scalanative_se_registry_wow64_32key() { return SE_REGISTRY_WOW64_32KEY; }
#ifndef __MINGW32__
// missing in mingw 3.11 / mingw-64 12.0.0
int scalanative_se_registry_wow64_64key() { return SE_REGISTRY_WOW64_64KEY; }
#endif
#endif // defined(_WIN32)
