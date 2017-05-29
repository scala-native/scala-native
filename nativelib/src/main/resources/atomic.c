// integer

int compare_and_swap_int(int* value, int expected, int desired) {
	return __sync_bool_compare_and_swap(value, expected, desired);
}

int add_and_fetch_int(int* ptr, int value) {
    return __sync_add_and_fetch(ptr, value);
}

int sub_and_fetch_int(int* ptr, int value) {
    return __sync_sub_and_fetch(ptr, value);
}

// boolean

int compare_and_swap_bool(int* value, int expected, int desired) {
	return __sync_bool_compare_and_swap(value, expected, desired);
}

int add_and_fetch_bool(int* ptr, int value) {
    return __sync_add_and_fetch(ptr, value);
}

int sub_and_fetch_bool(int* ptr, int value) {
    return __sync_sub_and_fetch(ptr, value);
}

int or_and_fetch_bool(int* ptr, int value) {
    return __sync_or_and_fetch(ptr, value);
}

int and_and_fetch_bool(int* ptr, int value) {
    return __sync_fetch_and_and(ptr, value);
}

int xor_and_fetch_bool(int* ptr, int value) {
    return __sync_xor_and_fetch(ptr, value);
}


// long

int compare_and_swap_long(long* value, long expected, long desired) {
	return __sync_bool_compare_and_swap(value, expected, desired);
}

long add_and_fetch_long(long* ptr, long value) {
    return __sync_add_and_fetch(ptr, value);
}

long sub_and_fetch_long(long* ptr, long value) {
    return __sync_sub_and_fetch(ptr, value);
}
