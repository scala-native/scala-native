#include <stdint.h>
extern int8_t **__scala_native_resources_all_path;
extern int8_t **__scala_native_resources_all_content;
extern int32_t *__scala_native_resources_all_content_lengths;
extern int32_t *__scala_native_resources_all_path_lengths;
extern int32_t __scala_native_resources_amount;

int8_t *scalanative_resource_get_content_ptr(int32_t embeddedFileId) {
    return __scala_native_resources_all_content[embeddedFileId];
}

int8_t *scalanative_resource_get_path_ptr(int32_t embeddedFileId) {
    return __scala_native_resources_all_path[embeddedFileId];
}

int32_t scalanative_resource_get_embedded_size() {
    return __scala_native_resources_amount;
}

int32_t scalanative_resource_get_path_length(int32_t embeddedFileId) {
    return __scala_native_resources_all_path_lengths[embeddedFileId];
}

int32_t scalanative_resource_get_content_length(int32_t embeddedFileId) {
    return __scala_native_resources_all_content_lengths[embeddedFileId];
}
