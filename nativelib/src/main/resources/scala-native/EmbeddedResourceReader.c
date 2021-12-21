#include <stdint.h>
extern int **__resources_all_path;
extern int **__resources_all_content;
extern int32_t *__resources_all_content_lengths;
extern int32_t *__resources_all_path_lengths;
extern int32_t __resources_amount;

int scalanative_resource_get_content_byte(int32_t embeddedFileId,
                                          int32_t byteIndex) {
    return __resources_all_content[embeddedFileId][byteIndex];
}

int scalanative_resource_get_path_byte(int32_t embeddedFileId,
                                       int32_t byteIndex) {
    return __resources_all_path[embeddedFileId][byteIndex];
}

int32_t scalanative_resource_get_embedded_size() { return __resources_amount; }

int32_t scalanative_resource_get_path_length(int32_t embeddedFileId) {
    return __resources_all_path_lengths[embeddedFileId];
}

int32_t scalanative_resource_get_content_length(int32_t embeddedFileId) {
    return __resources_all_content_lengths[embeddedFileId];
}
