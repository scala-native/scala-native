#include <stdint.h>
#include <stddef.h>

// integer

int compare_and_swap_int(int32_t* value, int32_t expected, int32_t desired) {
	return __sync_bool_compare_and_swap(value, expected, desired);
}

int32_t add_and_fetch_int(int32_t* ptr, int32_t value) {
    return __sync_add_and_fetch(ptr, value);
}

int32_t sub_and_fetch_int(int32_t* ptr, int32_t value) {
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

int nand_and_fetch_bool(int* ptr, int value) {
    return __sync_xor_and_fetch(ptr, value);
}


// long

int compare_and_swap_long(int64_t* value, int64_t expected, int64_t desired) {
	return __sync_bool_compare_and_swap(value, expected, desired);
}

int64_t add_and_fetch_long(int64_t* ptr, int64_t value) {
    return __sync_add_and_fetch(ptr, value);
}

int64_t sub_and_fetch_long(int64_t* ptr, int64_t value) {
    return __sync_sub_and_fetch(ptr, value);
}

// short

int compare_and_swap_short(int16_t* value, int16_t expected, int16_t desired) {
	return __sync_bool_compare_and_swap(value, expected, desired);
}

int16_t add_and_fetch_short(int16_t* ptr, int16_t value) {
    return __sync_add_and_fetch(ptr, value);
}

int16_t sub_and_fetch_short(int16_t* ptr, int16_t value) {
    return __sync_sub_and_fetch(ptr, value);
}

// char

int compare_and_swap_char(char* value, char expected, char desired) {
	return __sync_bool_compare_and_swap(value, expected, desired);
}

char add_and_fetch_char(char* ptr, char value) {
    return __sync_add_and_fetch(ptr, value);
}

char sub_and_fetch_char(char* ptr, char value) {
    return __sync_sub_and_fetch(ptr, value);
}

// byte

int compare_and_swap_byte(int8_t* value, int8_t expected, int8_t desired) {
	return __sync_bool_compare_and_swap(value, expected, desired);
}

int8_t add_and_fetch_byte(int8_t* ptr, int8_t value) {
    return __sync_add_and_fetch(ptr, value);
}

int8_t sub_and_fetch_byte(int8_t* ptr, int8_t value) {
    return __sync_sub_and_fetch(ptr, value);
}

// unsigned integer

int compare_and_swap_uint(uint32_t* value, uint32_t expected, uint32_t desired) {
	return __sync_bool_compare_and_swap(value, expected, desired);
}

uint32_t add_and_fetch_uint(uint32_t* ptr, uint32_t value) {
    return __sync_add_and_fetch(ptr, value);
}

uint32_t sub_and_fetch_uint(uint32_t* ptr, uint32_t value) {
    return __sync_sub_and_fetch(ptr, value);
}

// unsigned short

int compare_and_swap_ushort(uint16_t* value, uint16_t expected, uint16_t desired) {
	return __sync_bool_compare_and_swap(value, expected, desired);
}

uint16_t add_and_fetch_ushort(uint16_t* ptr, uint16_t value) {
    return __sync_add_and_fetch(ptr, value);
}

uint16_t sub_and_fetch_ushort(uint16_t* ptr, uint16_t value) {
    return __sync_sub_and_fetch(ptr, value);
}

// unsigned long

int compare_and_swap_ulong(uint64_t* value, uint64_t expected, uint64_t desired) {
	return __sync_bool_compare_and_swap(value, expected, desired);
}

uint64_t add_and_fetch_ulong(uint64_t* ptr, uint64_t value) {
    return __sync_add_and_fetch(ptr, value);
}

uint64_t sub_and_fetch_ulong(uint64_t* ptr, uint64_t value) {
    return __sync_sub_and_fetch(ptr, value);
}

// unsigned char

int compare_and_swap_uchar(unsigned char* value, unsigned char expected, unsigned char desired) {
	return __sync_bool_compare_and_swap(value, expected, desired);
}

unsigned char add_and_fetch_uchar(unsigned char* ptr, unsigned char value) {
    return __sync_add_and_fetch(ptr, value);
}

unsigned char sub_and_fetch_uchar(unsigned char* ptr, unsigned char value) {
    return __sync_sub_and_fetch(ptr, value);
}

// size_t

int compare_and_swap_csize(size_t* value, size_t expected, size_t desired) {
	return __sync_bool_compare_and_swap(value, expected, desired);
}

size_t add_and_fetch_csize(size_t* ptr, size_t value) {
    return __sync_add_and_fetch(ptr, value);
}

size_t sub_and_fetch_csize(size_t* ptr, size_t value) {
    return __sync_sub_and_fetch(ptr, value);
}
