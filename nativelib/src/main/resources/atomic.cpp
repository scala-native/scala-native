// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 1)
#include <atomic>
#include <stdint.h>
#include <stdlib.h>

using namespace std;

// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 16)

extern "C" {

/**
* Init
* */

// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 24)
// byte
void init_byte(int8_t *atm, int8_t init_value) {
    *atm = ATOMIC_VAR_INIT(init_value);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 24)
// short
void init_short(int16_t *atm, int16_t init_value) {
    *atm = ATOMIC_VAR_INIT(init_value);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 24)
// int
void init_int(int32_t *atm, int32_t init_value) {
    *atm = ATOMIC_VAR_INIT(init_value);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 24)
// long
void init_long(int64_t *atm, int64_t init_value) {
    *atm = ATOMIC_VAR_INIT(init_value);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 24)
// ubyte
void init_ubyte(uint8_t *atm, uint8_t init_value) {
    *atm = ATOMIC_VAR_INIT(init_value);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 24)
// ushort
void init_ushort(uint16_t *atm, uint16_t init_value) {
    *atm = ATOMIC_VAR_INIT(init_value);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 24)
// uint
void init_uint(uint32_t *atm, uint32_t init_value) {
    *atm = ATOMIC_VAR_INIT(init_value);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 24)
// ulong
void init_ulong(uint64_t *atm, uint64_t init_value) {
    *atm = ATOMIC_VAR_INIT(init_value);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 24)
// char
void init_char(char *atm, char init_value) {
    *atm = ATOMIC_VAR_INIT(init_value);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 24)
// uchar
void init_uchar(unsigned char *atm, unsigned char init_value) {
    *atm = ATOMIC_VAR_INIT(init_value);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 24)
// csize
void init_csize(size_t *atm, size_t init_value) {
    *atm = ATOMIC_VAR_INIT(init_value);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 29)

/**
* Load
* */

// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 47)
// byte
int8_t load_byte(atomic<int8_t> *atm) { return atm->load(); }

void store_byte(atomic<int8_t> *atm, int8_t val) { atm->store(val); }
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 47)
// short
int16_t load_short(atomic<int16_t> *atm) { return atm->load(); }

void store_short(atomic<int16_t> *atm, int16_t val) { atm->store(val); }
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 47)
// int
int32_t load_int(atomic<int32_t> *atm) { return atm->load(); }

void store_int(atomic<int32_t> *atm, int32_t val) { atm->store(val); }
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 47)
// long
int64_t load_long(atomic<int64_t> *atm) { return atm->load(); }

void store_long(atomic<int64_t> *atm, int64_t val) { atm->store(val); }
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 47)
// ubyte
uint8_t load_ubyte(atomic<uint8_t> *atm) { return atm->load(); }

void store_ubyte(atomic<uint8_t> *atm, uint8_t val) { atm->store(val); }
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 47)
// ushort
uint16_t load_ushort(atomic<uint16_t> *atm) { return atm->load(); }

void store_ushort(atomic<uint16_t> *atm, uint16_t val) { atm->store(val); }
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 47)
// uint
uint32_t load_uint(atomic<uint32_t> *atm) { return atm->load(); }

void store_uint(atomic<uint32_t> *atm, uint32_t val) { atm->store(val); }
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 47)
// ulong
uint64_t load_ulong(atomic<uint64_t> *atm) { return atm->load(); }

void store_ulong(atomic<uint64_t> *atm, uint64_t val) { atm->store(val); }
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 47)
// char
char load_char(atomic<char> *atm) { return atm->load(); }

void store_char(atomic<char> *atm, char val) { atm->store(val); }
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 47)
// uchar
unsigned char load_uchar(atomic<unsigned char> *atm) { return atm->load(); }

void store_uchar(atomic<unsigned char> *atm, unsigned char val) {
    atm->store(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 47)
// csize
size_t load_csize(atomic<size_t> *atm) { return atm->load(); }

void store_csize(atomic<size_t> *atm, size_t val) { atm->store(val); }
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 56)

/**
 * Compare and Swap
 * */

// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 62)
// byte
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_strong_byte(atomic<int8_t> *atm, int8_t *expected,
                                 int8_t desired) {
    return atomic_compare_exchange_strong(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_weak_byte(atomic<int8_t> *atm, int8_t *expected,
                               int8_t desired) {
    return atomic_compare_exchange_weak(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 62)
// short
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_strong_short(atomic<int16_t> *atm, int16_t *expected,
                                  int16_t desired) {
    return atomic_compare_exchange_strong(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_weak_short(atomic<int16_t> *atm, int16_t *expected,
                                int16_t desired) {
    return atomic_compare_exchange_weak(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 62)
// int
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_strong_int(atomic<int32_t> *atm, int32_t *expected,
                                int32_t desired) {
    return atomic_compare_exchange_strong(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_weak_int(atomic<int32_t> *atm, int32_t *expected,
                              int32_t desired) {
    return atomic_compare_exchange_weak(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 62)
// long
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_strong_long(atomic<int64_t> *atm, int64_t *expected,
                                 int64_t desired) {
    return atomic_compare_exchange_strong(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_weak_long(atomic<int64_t> *atm, int64_t *expected,
                               int64_t desired) {
    return atomic_compare_exchange_weak(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 62)
// ubyte
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_strong_ubyte(atomic<uint8_t> *atm, uint8_t *expected,
                                  uint8_t desired) {
    return atomic_compare_exchange_strong(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_weak_ubyte(atomic<uint8_t> *atm, uint8_t *expected,
                                uint8_t desired) {
    return atomic_compare_exchange_weak(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 62)
// ushort
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_strong_ushort(atomic<uint16_t> *atm, uint16_t *expected,
                                   uint16_t desired) {
    return atomic_compare_exchange_strong(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_weak_ushort(atomic<uint16_t> *atm, uint16_t *expected,
                                 uint16_t desired) {
    return atomic_compare_exchange_weak(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 62)
// uint
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_strong_uint(atomic<uint32_t> *atm, uint32_t *expected,
                                 uint32_t desired) {
    return atomic_compare_exchange_strong(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_weak_uint(atomic<uint32_t> *atm, uint32_t *expected,
                               uint32_t desired) {
    return atomic_compare_exchange_weak(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 62)
// ulong
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_strong_ulong(atomic<uint64_t> *atm, uint64_t *expected,
                                  uint64_t desired) {
    return atomic_compare_exchange_strong(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_weak_ulong(atomic<uint64_t> *atm, uint64_t *expected,
                                uint64_t desired) {
    return atomic_compare_exchange_weak(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 62)
// char
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_strong_char(atomic<char> *atm, char *expected,
                                 char desired) {
    return atomic_compare_exchange_strong(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_weak_char(atomic<char> *atm, char *expected,
                               char desired) {
    return atomic_compare_exchange_weak(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 62)
// uchar
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_strong_uchar(atomic<unsigned char> *atm,
                                  unsigned char *expected,
                                  unsigned char desired) {
    return atomic_compare_exchange_strong(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_weak_uchar(atomic<unsigned char> *atm,
                                unsigned char *expected,
                                unsigned char desired) {
    return atomic_compare_exchange_weak(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 62)
// csize
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_strong_csize(atomic<size_t> *atm, size_t *expected,
                                  size_t desired) {
    return atomic_compare_exchange_strong(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 64)
int compare_and_swap_weak_csize(atomic<size_t> *atm, size_t *expected,
                                size_t desired) {
    return atomic_compare_exchange_weak(atm, expected, desired);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 69)

// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 71)
/**
* add
* */

// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// byte
int8_t atomic_add_byte(atomic<int8_t> *atm, int8_t val) {
    return atm->fetch_add(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// short
int16_t atomic_add_short(atomic<int16_t> *atm, int16_t val) {
    return atm->fetch_add(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// int
int32_t atomic_add_int(atomic<int32_t> *atm, int32_t val) {
    return atm->fetch_add(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// long
int64_t atomic_add_long(atomic<int64_t> *atm, int64_t val) {
    return atm->fetch_add(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ubyte
uint8_t atomic_add_ubyte(atomic<uint8_t> *atm, uint8_t val) {
    return atm->fetch_add(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ushort
uint16_t atomic_add_ushort(atomic<uint16_t> *atm, uint16_t val) {
    return atm->fetch_add(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// uint
uint32_t atomic_add_uint(atomic<uint32_t> *atm, uint32_t val) {
    return atm->fetch_add(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ulong
uint64_t atomic_add_ulong(atomic<uint64_t> *atm, uint64_t val) {
    return atm->fetch_add(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// char
char atomic_add_char(atomic<char> *atm, char val) {
    return atm->fetch_add(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// uchar
unsigned char atomic_add_uchar(atomic<unsigned char> *atm, unsigned char val) {
    return atm->fetch_add(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// csize
size_t atomic_add_csize(atomic<size_t> *atm, size_t val) {
    return atm->fetch_add(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 71)
/**
* sub
* */

// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// byte
int8_t atomic_sub_byte(atomic<int8_t> *atm, int8_t val) {
    return atm->fetch_sub(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// short
int16_t atomic_sub_short(atomic<int16_t> *atm, int16_t val) {
    return atm->fetch_sub(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// int
int32_t atomic_sub_int(atomic<int32_t> *atm, int32_t val) {
    return atm->fetch_sub(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// long
int64_t atomic_sub_long(atomic<int64_t> *atm, int64_t val) {
    return atm->fetch_sub(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ubyte
uint8_t atomic_sub_ubyte(atomic<uint8_t> *atm, uint8_t val) {
    return atm->fetch_sub(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ushort
uint16_t atomic_sub_ushort(atomic<uint16_t> *atm, uint16_t val) {
    return atm->fetch_sub(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// uint
uint32_t atomic_sub_uint(atomic<uint32_t> *atm, uint32_t val) {
    return atm->fetch_sub(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ulong
uint64_t atomic_sub_ulong(atomic<uint64_t> *atm, uint64_t val) {
    return atm->fetch_sub(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// char
char atomic_sub_char(atomic<char> *atm, char val) {
    return atm->fetch_sub(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// uchar
unsigned char atomic_sub_uchar(atomic<unsigned char> *atm, unsigned char val) {
    return atm->fetch_sub(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// csize
size_t atomic_sub_csize(atomic<size_t> *atm, size_t val) {
    return atm->fetch_sub(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 71)
/**
* and
* */

// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// byte
int8_t atomic_and_byte(atomic<int8_t> *atm, int8_t val) {
    return atm->fetch_and(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// short
int16_t atomic_and_short(atomic<int16_t> *atm, int16_t val) {
    return atm->fetch_and(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// int
int32_t atomic_and_int(atomic<int32_t> *atm, int32_t val) {
    return atm->fetch_and(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// long
int64_t atomic_and_long(atomic<int64_t> *atm, int64_t val) {
    return atm->fetch_and(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ubyte
uint8_t atomic_and_ubyte(atomic<uint8_t> *atm, uint8_t val) {
    return atm->fetch_and(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ushort
uint16_t atomic_and_ushort(atomic<uint16_t> *atm, uint16_t val) {
    return atm->fetch_and(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// uint
uint32_t atomic_and_uint(atomic<uint32_t> *atm, uint32_t val) {
    return atm->fetch_and(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ulong
uint64_t atomic_and_ulong(atomic<uint64_t> *atm, uint64_t val) {
    return atm->fetch_and(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// char
char atomic_and_char(atomic<char> *atm, char val) {
    return atm->fetch_and(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// uchar
unsigned char atomic_and_uchar(atomic<unsigned char> *atm, unsigned char val) {
    return atm->fetch_and(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// csize
size_t atomic_and_csize(atomic<size_t> *atm, size_t val) {
    return atm->fetch_and(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 71)
/**
* or
* */

// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// byte
int8_t atomic_or_byte(atomic<int8_t> *atm, int8_t val) {
    return atm->fetch_or(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// short
int16_t atomic_or_short(atomic<int16_t> *atm, int16_t val) {
    return atm->fetch_or(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// int
int32_t atomic_or_int(atomic<int32_t> *atm, int32_t val) {
    return atm->fetch_or(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// long
int64_t atomic_or_long(atomic<int64_t> *atm, int64_t val) {
    return atm->fetch_or(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ubyte
uint8_t atomic_or_ubyte(atomic<uint8_t> *atm, uint8_t val) {
    return atm->fetch_or(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ushort
uint16_t atomic_or_ushort(atomic<uint16_t> *atm, uint16_t val) {
    return atm->fetch_or(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// uint
uint32_t atomic_or_uint(atomic<uint32_t> *atm, uint32_t val) {
    return atm->fetch_or(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ulong
uint64_t atomic_or_ulong(atomic<uint64_t> *atm, uint64_t val) {
    return atm->fetch_or(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// char
char atomic_or_char(atomic<char> *atm, char val) { return atm->fetch_or(val); }
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// uchar
unsigned char atomic_or_uchar(atomic<unsigned char> *atm, unsigned char val) {
    return atm->fetch_or(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// csize
size_t atomic_or_csize(atomic<size_t> *atm, size_t val) {
    return atm->fetch_or(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 71)
/**
* xor
* */

// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// byte
int8_t atomic_xor_byte(atomic<int8_t> *atm, int8_t val) {
    return atm->fetch_xor(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// short
int16_t atomic_xor_short(atomic<int16_t> *atm, int16_t val) {
    return atm->fetch_xor(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// int
int32_t atomic_xor_int(atomic<int32_t> *atm, int32_t val) {
    return atm->fetch_xor(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// long
int64_t atomic_xor_long(atomic<int64_t> *atm, int64_t val) {
    return atm->fetch_xor(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ubyte
uint8_t atomic_xor_ubyte(atomic<uint8_t> *atm, uint8_t val) {
    return atm->fetch_xor(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ushort
uint16_t atomic_xor_ushort(atomic<uint16_t> *atm, uint16_t val) {
    return atm->fetch_xor(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// uint
uint32_t atomic_xor_uint(atomic<uint32_t> *atm, uint32_t val) {
    return atm->fetch_xor(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// ulong
uint64_t atomic_xor_ulong(atomic<uint64_t> *atm, uint64_t val) {
    return atm->fetch_xor(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// char
char atomic_xor_char(atomic<char> *atm, char val) {
    return atm->fetch_xor(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// uchar
unsigned char atomic_xor_uchar(atomic<unsigned char> *atm, unsigned char val) {
    return atm->fetch_xor(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 77)
// csize
size_t atomic_xor_csize(atomic<size_t> *atm, size_t val) {
    return atm->fetch_xor(val);
}
// ###sourceLocation(file:
// "/home/remi/perso/Projects/scala-native/nativelib/src/main/resources/atomic.cpp.gyb",
// line: 84)
}