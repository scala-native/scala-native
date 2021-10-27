extern int** __resources_all_path;
extern int** __resources_all_content;
extern long* __resources_all_content_lengths;
extern long* __resources_all_path_lengths;
extern long __resources_size;

int scalanative_resource_get_content_byte(int embeddedFileId, int byteIndex) {
    return __resources_all_content[embeddedFileId][byteIndex];
}

int scalanative_resource_get_path_byte(int embeddedFileId, int byteIndex) {
    return __resources_all_path[embeddedFileId][byteIndex];
}

long scalanative_resource_get_embedded_size() {
    return __resources_size;
}

long scalanative_resource_get_path_length(int embeddedFileId) {
    return __resources_all_path_lengths[embeddedFileId];
}

long scalanative_resource_get_content_length(int embeddedFileId) {
    return __resources_all_content_lengths[embeddedFileId];
}
