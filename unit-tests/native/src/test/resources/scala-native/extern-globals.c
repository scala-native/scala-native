struct scalanative_test_extern_point {
    long long x;
    int y;
};

const char scalanative_test_extern_chars[] = "fourty-two";
const char scalanative_test_extern_unsized[] = "unsized";
int scalanative_test_extern_ints[4] = {1, 2, 3, 4};
struct scalanative_test_extern_point scalanative_test_extern_struct = {84, 42};

void *scalanative_test_extern_chars_address(void) {
    return (void *)scalanative_test_extern_chars;
}
void *scalanative_test_extern_ints_address(void) {
    return (void *)scalanative_test_extern_ints;
}
void *scalanative_test_extern_struct_address(void) {
    return (void *)&scalanative_test_extern_struct;
}
void *scalanative_test_extern_unsized_address(void) {
    return (void *)scalanative_test_extern_unsized;
}
