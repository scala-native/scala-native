#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <AccCtrl.h>

int scalanative_win32_se_object_type_unknown_object_type() {
    return SE_UNKNOWN_OBJECT_TYPE;
}
int scalanative_win32_se_object_type_file_object() { return SE_FILE_OBJECT; }
int scalanative_win32_se_object_type_service() { return SE_SERVICE; }
int scalanative_win32_se_object_type_printer() { return SE_PRINTER; }
int scalanative_win32_se_object_type_registry_key() { return SE_REGISTRY_KEY; }
int scalanative_win32_se_object_type_lmshare() { return SE_LMSHARE; }
int scalanative_win32_se_object_type_kernel_object() {
    return SE_KERNEL_OBJECT;
}
int scalanative_win32_se_object_type_window_object() {
    return SE_WINDOW_OBJECT;
}
int scalanative_win32_se_object_type_ds_object() { return SE_DS_OBJECT; }
int scalanative_win32_se_object_type_ds_object_all() {
    return SE_DS_OBJECT_ALL;
}
int scalanative_win32_se_object_type_provider_defined_object() {
    return SE_PROVIDER_DEFINED_OBJECT;
}
int scalanative_win32_se_object_type_wmiguid_object() {
    return SE_WMIGUID_OBJECT;
}
int scalanative_win32_se_object_type_registry_wow64_32key() {
    return SE_REGISTRY_WOW64_32KEY;
}
int scalanative_win32_se_object_type_registry_wow64_64key() {
    return SE_REGISTRY_WOW64_64KEY;
}

#endif // defined(_WIN32)
