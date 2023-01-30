// clang-format off

#include <stdatomic.h>
#include <stdbool.h>
#include <stdint.h>

memory_order scalanative_atomic_memory_order_relaxed() { return memory_order_relaxed;}
memory_order scalanative_atomic_memory_order_consume() { return memory_order_consume;}
memory_order scalanative_atomic_memory_order_acquire() { return memory_order_acquire;}
memory_order scalanative_atomic_memory_order_release() { return memory_order_release;}
memory_order scalanative_atomic_memory_order_acq_rel() { return memory_order_acq_rel;}
memory_order scalanative_atomic_memory_order_seq_cst() { return memory_order_seq_cst;}

void scalanative_atomic_thread_fence(memory_order order) { atomic_thread_fence(order);}
void scalanative_atomic_signal_fence(memory_order order) { atomic_signal_fence(order);}

void scalanative_atomic_init_byte(_Atomic(char)* atm, char init_value) { atomic_init(atm, init_value);}
char scalanative_atomic_load_byte(_Atomic(char)* atm) { return atomic_load(atm);}
char scalanative_atomic_load_explicit_byte(_Atomic(char)* atm, memory_order memoryOrder) { return atomic_load_explicit(atm, memoryOrder);}
void scalanative_atomic_store_byte(_Atomic(char)* atm, char val) {atomic_store(atm, val);}
void scalanative_atomic_store_explicit_byte(_Atomic(char)* atm, char val, memory_order memoryOrder) { atomic_store_explicit(atm, val, memoryOrder);}
char scalanative_atomic_exchange_byte(_Atomic(char)* atm, char val) { return atomic_exchange(atm, val);}
char scalanative_atomic_exchange_explicit_byte(_Atomic(char)* atm, char val, memory_order memoryOrder) { return atomic_exchange_explicit(atm, val, memoryOrder);}
bool scalanative_atomic_compare_exchange_strong_byte(_Atomic(char)* atm, char* expected, char desired) { return atomic_compare_exchange_strong(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_strong_explicit_byte(_Atomic(char)* atm, char* expected, char desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_strong_explicit(atm, expected, desired, onSucc, onFail);}
bool scalanative_atomic_compare_exchange_weak_byte(_Atomic(char)* atm, char* expected, char desired) { return atomic_compare_exchange_weak(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_weak_explicit_byte(_Atomic(char)* atm, char* expected, char desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_weak_explicit(atm, expected, desired, onSucc, onFail);}
char scalanative_atomic_fetch_add_byte(_Atomic(char)* atm, char val) { return atomic_fetch_add(atm, val);}
char scalanative_atomic_fetch_add_explicit_byte(_Atomic(char)* atm, char val, memory_order memoryOrder) { return atomic_fetch_add_explicit(atm, val, memoryOrder);}
char scalanative_atomic_fetch_sub_byte(_Atomic(char)* atm, char val) { return atomic_fetch_sub(atm, val);}
char scalanative_atomic_fetch_sub_explicit_byte(_Atomic(char)* atm, char val, memory_order memoryOrder) { return atomic_fetch_sub_explicit(atm, val, memoryOrder);}
char scalanative_atomic_fetch_and_byte(_Atomic(char)* atm, char val) { return atomic_fetch_and(atm, val);}
char scalanative_atomic_fetch_and_explicit_byte(_Atomic(char)* atm, char val, memory_order memoryOrder) { return atomic_fetch_and_explicit(atm, val, memoryOrder);}
char scalanative_atomic_fetch_or_byte(_Atomic(char)* atm, char val) { return atomic_fetch_or(atm, val);}
char scalanative_atomic_fetch_or_explicit_byte(_Atomic(char)* atm, char val, memory_order memoryOrder) { return atomic_fetch_or_explicit(atm, val, memoryOrder);}
char scalanative_atomic_fetch_xor_byte(_Atomic(char)* atm, char val) { return atomic_fetch_xor(atm, val);}
char scalanative_atomic_fetch_xor_explicit_byte(_Atomic(char)* atm, char val, memory_order memoryOrder) { return atomic_fetch_xor_explicit(atm, val, memoryOrder);}

void scalanative_atomic_init_ubyte(_Atomic(unsigned char)* atm, unsigned char init_value) { atomic_init(atm, init_value);}
unsigned char scalanative_atomic_load_ubyte(_Atomic(unsigned char)* atm) { return atomic_load(atm);}
unsigned char scalanative_atomic_load_explicit_ubyte(_Atomic(unsigned char)* atm, memory_order memoryOrder) { return atomic_load_explicit(atm, memoryOrder);}
void scalanative_atomic_store_ubyte(_Atomic(unsigned char)* atm, unsigned char val) {atomic_store(atm, val);}
void scalanative_atomic_store_explicit_ubyte(_Atomic(unsigned char)* atm, unsigned char val, memory_order memoryOrder) { atomic_store_explicit(atm, val, memoryOrder);}
unsigned char scalanative_atomic_exchange_ubyte(_Atomic(unsigned char)* atm, unsigned char val) { return atomic_exchange(atm, val);}
unsigned char scalanative_atomic_exchange_explicit_ubyte(_Atomic(unsigned char)* atm, unsigned char val, memory_order memoryOrder) { return atomic_exchange_explicit(atm, val, memoryOrder);}
bool scalanative_atomic_compare_exchange_strong_ubyte(_Atomic(unsigned char)* atm, unsigned char* expected, unsigned char desired) { return atomic_compare_exchange_strong(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_strong_explicit_ubyte(_Atomic(unsigned char)* atm, unsigned char* expected, unsigned char desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_strong_explicit(atm, expected, desired, onSucc, onFail);}
bool scalanative_atomic_compare_exchange_weak_ubyte(_Atomic(unsigned char)* atm, unsigned char* expected, unsigned char desired) { return atomic_compare_exchange_weak(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_weak_explicit_ubyte(_Atomic(unsigned char)* atm, unsigned char* expected, unsigned char desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_weak_explicit(atm, expected, desired, onSucc, onFail);}
unsigned char scalanative_atomic_fetch_add_ubyte(_Atomic(unsigned char)* atm, unsigned char val) { return atomic_fetch_add(atm, val);}
unsigned char scalanative_atomic_fetch_add_explicit_ubyte(_Atomic(unsigned char)* atm, unsigned char val, memory_order memoryOrder) { return atomic_fetch_add_explicit(atm, val, memoryOrder);}
unsigned char scalanative_atomic_fetch_sub_ubyte(_Atomic(unsigned char)* atm, unsigned char val) { return atomic_fetch_sub(atm, val);}
unsigned char scalanative_atomic_fetch_sub_explicit_ubyte(_Atomic(unsigned char)* atm, unsigned char val, memory_order memoryOrder) { return atomic_fetch_sub_explicit(atm, val, memoryOrder);}
unsigned char scalanative_atomic_fetch_and_ubyte(_Atomic(unsigned char)* atm, unsigned char val) { return atomic_fetch_and(atm, val);}
unsigned char scalanative_atomic_fetch_and_explicit_ubyte(_Atomic(unsigned char)* atm, unsigned char val, memory_order memoryOrder) { return atomic_fetch_and_explicit(atm, val, memoryOrder);}
unsigned char scalanative_atomic_fetch_or_ubyte(_Atomic(unsigned char)* atm, unsigned char val) { return atomic_fetch_or(atm, val);}
unsigned char scalanative_atomic_fetch_or_explicit_ubyte(_Atomic(unsigned char)* atm, unsigned char val, memory_order memoryOrder) { return atomic_fetch_or_explicit(atm, val, memoryOrder);}
unsigned char scalanative_atomic_fetch_xor_ubyte(_Atomic(unsigned char)* atm, unsigned char val) { return atomic_fetch_xor(atm, val);}
unsigned char scalanative_atomic_fetch_xor_explicit_ubyte(_Atomic(unsigned char)* atm, unsigned char val, memory_order memoryOrder) { return atomic_fetch_xor_explicit(atm, val, memoryOrder);}

void scalanative_atomic_init_short(_Atomic(short)* atm, short init_value) { atomic_init(atm, init_value);}
short scalanative_atomic_load_short(_Atomic(short)* atm) { return atomic_load(atm);}
short scalanative_atomic_load_explicit_short(_Atomic(short)* atm, memory_order memoryOrder) { return atomic_load_explicit(atm, memoryOrder);}
void scalanative_atomic_store_short(_Atomic(short)* atm, short val) {atomic_store(atm, val);}
void scalanative_atomic_store_explicit_short(_Atomic(short)* atm, short val, memory_order memoryOrder) { atomic_store_explicit(atm, val, memoryOrder);}
short scalanative_atomic_exchange_short(_Atomic(short)* atm, short val) { return atomic_exchange(atm, val);}
short scalanative_atomic_exchange_explicit_short(_Atomic(short)* atm, short val, memory_order memoryOrder) { return atomic_exchange_explicit(atm, val, memoryOrder);}
bool scalanative_atomic_compare_exchange_strong_short(_Atomic(short)* atm, short* expected, short desired) { return atomic_compare_exchange_strong(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_strong_explicit_short(_Atomic(short)* atm, short* expected, short desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_strong_explicit(atm, expected, desired, onSucc, onFail);}
bool scalanative_atomic_compare_exchange_weak_short(_Atomic(short)* atm, short* expected, short desired) { return atomic_compare_exchange_weak(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_weak_explicit_short(_Atomic(short)* atm, short* expected, short desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_weak_explicit(atm, expected, desired, onSucc, onFail);}
short scalanative_atomic_fetch_add_short(_Atomic(short)* atm, short val) { return atomic_fetch_add(atm, val);}
short scalanative_atomic_fetch_add_explicit_short(_Atomic(short)* atm, short val, memory_order memoryOrder) { return atomic_fetch_add_explicit(atm, val, memoryOrder);}
short scalanative_atomic_fetch_sub_short(_Atomic(short)* atm, short val) { return atomic_fetch_sub(atm, val);}
short scalanative_atomic_fetch_sub_explicit_short(_Atomic(short)* atm, short val, memory_order memoryOrder) { return atomic_fetch_sub_explicit(atm, val, memoryOrder);}
short scalanative_atomic_fetch_and_short(_Atomic(short)* atm, short val) { return atomic_fetch_and(atm, val);}
short scalanative_atomic_fetch_and_explicit_short(_Atomic(short)* atm, short val, memory_order memoryOrder) { return atomic_fetch_and_explicit(atm, val, memoryOrder);}
short scalanative_atomic_fetch_or_short(_Atomic(short)* atm, short val) { return atomic_fetch_or(atm, val);}
short scalanative_atomic_fetch_or_explicit_short(_Atomic(short)* atm, short val, memory_order memoryOrder) { return atomic_fetch_or_explicit(atm, val, memoryOrder);}
short scalanative_atomic_fetch_xor_short(_Atomic(short)* atm, short val) { return atomic_fetch_xor(atm, val);}
short scalanative_atomic_fetch_xor_explicit_short(_Atomic(short)* atm, short val, memory_order memoryOrder) { return atomic_fetch_xor_explicit(atm, val, memoryOrder);}

void scalanative_atomic_init_ushort(_Atomic(unsigned short)* atm, unsigned short init_value) { atomic_init(atm, init_value);}
unsigned short scalanative_atomic_load_ushort(_Atomic(unsigned short)* atm) { return atomic_load(atm);}
unsigned short scalanative_atomic_load_explicit_ushort(_Atomic(unsigned short)* atm, memory_order memoryOrder) { return atomic_load_explicit(atm, memoryOrder);}
void scalanative_atomic_store_ushort(_Atomic(unsigned short)* atm, unsigned short val) {atomic_store(atm, val);}
void scalanative_atomic_store_explicit_ushort(_Atomic(unsigned short)* atm, unsigned short val, memory_order memoryOrder) { atomic_store_explicit(atm, val, memoryOrder);}
unsigned short scalanative_atomic_exchange_ushort(_Atomic(unsigned short)* atm, unsigned short val) { return atomic_exchange(atm, val);}
unsigned short scalanative_atomic_exchange_explicit_ushort(_Atomic(unsigned short)* atm, unsigned short val, memory_order memoryOrder) { return atomic_exchange_explicit(atm, val, memoryOrder);}
bool scalanative_atomic_compare_exchange_strong_ushort(_Atomic(unsigned short)* atm, unsigned short* expected, unsigned short desired) { return atomic_compare_exchange_strong(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_strong_explicit_ushort(_Atomic(unsigned short)* atm, unsigned short* expected, unsigned short desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_strong_explicit(atm, expected, desired, onSucc, onFail);}
bool scalanative_atomic_compare_exchange_weak_ushort(_Atomic(unsigned short)* atm, unsigned short* expected, unsigned short desired) { return atomic_compare_exchange_weak(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_weak_explicit_ushort(_Atomic(unsigned short)* atm, unsigned short* expected, unsigned short desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_weak_explicit(atm, expected, desired, onSucc, onFail);}
unsigned short scalanative_atomic_fetch_add_ushort(_Atomic(unsigned short)* atm, unsigned short val) { return atomic_fetch_add(atm, val);}
unsigned short scalanative_atomic_fetch_add_explicit_ushort(_Atomic(unsigned short)* atm, unsigned short val, memory_order memoryOrder) { return atomic_fetch_add_explicit(atm, val, memoryOrder);}
unsigned short scalanative_atomic_fetch_sub_ushort(_Atomic(unsigned short)* atm, unsigned short val) { return atomic_fetch_sub(atm, val);}
unsigned short scalanative_atomic_fetch_sub_explicit_ushort(_Atomic(unsigned short)* atm, unsigned short val, memory_order memoryOrder) { return atomic_fetch_sub_explicit(atm, val, memoryOrder);}
unsigned short scalanative_atomic_fetch_and_ushort(_Atomic(unsigned short)* atm, unsigned short val) { return atomic_fetch_and(atm, val);}
unsigned short scalanative_atomic_fetch_and_explicit_ushort(_Atomic(unsigned short)* atm, unsigned short val, memory_order memoryOrder) { return atomic_fetch_and_explicit(atm, val, memoryOrder);}
unsigned short scalanative_atomic_fetch_or_ushort(_Atomic(unsigned short)* atm, unsigned short val) { return atomic_fetch_or(atm, val);}
unsigned short scalanative_atomic_fetch_or_explicit_ushort(_Atomic(unsigned short)* atm, unsigned short val, memory_order memoryOrder) { return atomic_fetch_or_explicit(atm, val, memoryOrder);}
unsigned short scalanative_atomic_fetch_xor_ushort(_Atomic(unsigned short)* atm, unsigned short val) { return atomic_fetch_xor(atm, val);}
unsigned short scalanative_atomic_fetch_xor_explicit_ushort(_Atomic(unsigned short)* atm, unsigned short val, memory_order memoryOrder) { return atomic_fetch_xor_explicit(atm, val, memoryOrder);}

void scalanative_atomic_init_int(_Atomic(int)* atm, int init_value) { atomic_init(atm, init_value);}
int scalanative_atomic_load_int(_Atomic(int)* atm) { return atomic_load(atm);}
int scalanative_atomic_load_explicit_int(_Atomic(int)* atm, memory_order memoryOrder) { return atomic_load_explicit(atm, memoryOrder);}
void scalanative_atomic_store_int(_Atomic(int)* atm, int val) {atomic_store(atm, val);}
void scalanative_atomic_store_explicit_int(_Atomic(int)* atm, int val, memory_order memoryOrder) { atomic_store_explicit(atm, val, memoryOrder);}
int scalanative_atomic_exchange_int(_Atomic(int)* atm, int val) { return atomic_exchange(atm, val);}
int scalanative_atomic_exchange_explicit_int(_Atomic(int)* atm, int val, memory_order memoryOrder) { return atomic_exchange_explicit(atm, val, memoryOrder);}
bool scalanative_atomic_compare_exchange_strong_int(_Atomic(int)* atm, int* expected, int desired) { return atomic_compare_exchange_strong(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_strong_explicit_int(_Atomic(int)* atm, int* expected, int desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_strong_explicit(atm, expected, desired, onSucc, onFail);}
bool scalanative_atomic_compare_exchange_weak_int(_Atomic(int)* atm, int* expected, int desired) { return atomic_compare_exchange_weak(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_weak_explicit_int(_Atomic(int)* atm, int* expected, int desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_weak_explicit(atm, expected, desired, onSucc, onFail);}
int scalanative_atomic_fetch_add_int(_Atomic(int)* atm, int val) { return atomic_fetch_add(atm, val);}
int scalanative_atomic_fetch_add_explicit_int(_Atomic(int)* atm, int val, memory_order memoryOrder) { return atomic_fetch_add_explicit(atm, val, memoryOrder);}
int scalanative_atomic_fetch_sub_int(_Atomic(int)* atm, int val) { return atomic_fetch_sub(atm, val);}
int scalanative_atomic_fetch_sub_explicit_int(_Atomic(int)* atm, int val, memory_order memoryOrder) { return atomic_fetch_sub_explicit(atm, val, memoryOrder);}
int scalanative_atomic_fetch_and_int(_Atomic(int)* atm, int val) { return atomic_fetch_and(atm, val);}
int scalanative_atomic_fetch_and_explicit_int(_Atomic(int)* atm, int val, memory_order memoryOrder) { return atomic_fetch_and_explicit(atm, val, memoryOrder);}
int scalanative_atomic_fetch_or_int(_Atomic(int)* atm, int val) { return atomic_fetch_or(atm, val);}
int scalanative_atomic_fetch_or_explicit_int(_Atomic(int)* atm, int val, memory_order memoryOrder) { return atomic_fetch_or_explicit(atm, val, memoryOrder);}
int scalanative_atomic_fetch_xor_int(_Atomic(int)* atm, int val) { return atomic_fetch_xor(atm, val);}
int scalanative_atomic_fetch_xor_explicit_int(_Atomic(int)* atm, int val, memory_order memoryOrder) { return atomic_fetch_xor_explicit(atm, val, memoryOrder);}

void scalanative_atomic_init_uint(_Atomic(unsigned int)* atm, unsigned int init_value) { atomic_init(atm, init_value);}
unsigned int scalanative_atomic_load_uint(_Atomic(unsigned int)* atm) { return atomic_load(atm);}
unsigned int scalanative_atomic_load_explicit_uint(_Atomic(unsigned int)* atm, memory_order memoryOrder) { return atomic_load_explicit(atm, memoryOrder);}
void scalanative_atomic_store_uint(_Atomic(unsigned int)* atm, unsigned int val) {atomic_store(atm, val);}
void scalanative_atomic_store_explicit_uint(_Atomic(unsigned int)* atm, unsigned int val, memory_order memoryOrder) { atomic_store_explicit(atm, val, memoryOrder);}
unsigned int scalanative_atomic_exchange_uint(_Atomic(unsigned int)* atm, unsigned int val) { return atomic_exchange(atm, val);}
unsigned int scalanative_atomic_exchange_explicit_uint(_Atomic(unsigned int)* atm, unsigned int val, memory_order memoryOrder) { return atomic_exchange_explicit(atm, val, memoryOrder);}
bool scalanative_atomic_compare_exchange_strong_uint(_Atomic(unsigned int)* atm, unsigned int* expected, unsigned int desired) { return atomic_compare_exchange_strong(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_strong_explicit_uint(_Atomic(unsigned int)* atm, unsigned int* expected, unsigned int desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_strong_explicit(atm, expected, desired, onSucc, onFail);}
bool scalanative_atomic_compare_exchange_weak_uint(_Atomic(unsigned int)* atm, unsigned int* expected, unsigned int desired) { return atomic_compare_exchange_weak(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_weak_explicit_uint(_Atomic(unsigned int)* atm, unsigned int* expected, unsigned int desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_weak_explicit(atm, expected, desired, onSucc, onFail);}
unsigned int scalanative_atomic_fetch_add_uint(_Atomic(unsigned int)* atm, unsigned int val) { return atomic_fetch_add(atm, val);}
unsigned int scalanative_atomic_fetch_add_explicit_uint(_Atomic(unsigned int)* atm, unsigned int val, memory_order memoryOrder) { return atomic_fetch_add_explicit(atm, val, memoryOrder);}
unsigned int scalanative_atomic_fetch_sub_uint(_Atomic(unsigned int)* atm, unsigned int val) { return atomic_fetch_sub(atm, val);}
unsigned int scalanative_atomic_fetch_sub_explicit_uint(_Atomic(unsigned int)* atm, unsigned int val, memory_order memoryOrder) { return atomic_fetch_sub_explicit(atm, val, memoryOrder);}
unsigned int scalanative_atomic_fetch_and_uint(_Atomic(unsigned int)* atm, unsigned int val) { return atomic_fetch_and(atm, val);}
unsigned int scalanative_atomic_fetch_and_explicit_uint(_Atomic(unsigned int)* atm, unsigned int val, memory_order memoryOrder) { return atomic_fetch_and_explicit(atm, val, memoryOrder);}
unsigned int scalanative_atomic_fetch_or_uint(_Atomic(unsigned int)* atm, unsigned int val) { return atomic_fetch_or(atm, val);}
unsigned int scalanative_atomic_fetch_or_explicit_uint(_Atomic(unsigned int)* atm, unsigned int val, memory_order memoryOrder) { return atomic_fetch_or_explicit(atm, val, memoryOrder);}
unsigned int scalanative_atomic_fetch_xor_uint(_Atomic(unsigned int)* atm, unsigned int val) { return atomic_fetch_xor(atm, val);}
unsigned int scalanative_atomic_fetch_xor_explicit_uint(_Atomic(unsigned int)* atm, unsigned int val, memory_order memoryOrder) { return atomic_fetch_xor_explicit(atm, val, memoryOrder);}

void scalanative_atomic_init_long(_Atomic(long)* atm, long init_value) { atomic_init(atm, init_value);}
long scalanative_atomic_load_long(_Atomic(long)* atm) { return atomic_load(atm);}
long scalanative_atomic_load_explicit_long(_Atomic(long)* atm, memory_order memoryOrder) { return atomic_load_explicit(atm, memoryOrder);}
void scalanative_atomic_store_long(_Atomic(long)* atm, long val) {atomic_store(atm, val);}
void scalanative_atomic_store_explicit_long(_Atomic(long)* atm, long val, memory_order memoryOrder) { atomic_store_explicit(atm, val, memoryOrder);}
long scalanative_atomic_exchange_long(_Atomic(long)* atm, long val) { return atomic_exchange(atm, val);}
long scalanative_atomic_exchange_explicit_long(_Atomic(long)* atm, long val, memory_order memoryOrder) { return atomic_exchange_explicit(atm, val, memoryOrder);}
bool scalanative_atomic_compare_exchange_strong_long(_Atomic(long)* atm, long* expected, long desired) { return atomic_compare_exchange_strong(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_strong_explicit_long(_Atomic(long)* atm, long* expected, long desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_strong_explicit(atm, expected, desired, onSucc, onFail);}
bool scalanative_atomic_compare_exchange_weak_long(_Atomic(long)* atm, long* expected, long desired) { return atomic_compare_exchange_weak(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_weak_explicit_long(_Atomic(long)* atm, long* expected, long desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_weak_explicit(atm, expected, desired, onSucc, onFail);}
long scalanative_atomic_fetch_add_long(_Atomic(long)* atm, long val) { return atomic_fetch_add(atm, val);}
long scalanative_atomic_fetch_add_explicit_long(_Atomic(long)* atm, long val, memory_order memoryOrder) { return atomic_fetch_add_explicit(atm, val, memoryOrder);}
long scalanative_atomic_fetch_sub_long(_Atomic(long)* atm, long val) { return atomic_fetch_sub(atm, val);}
long scalanative_atomic_fetch_sub_explicit_long(_Atomic(long)* atm, long val, memory_order memoryOrder) { return atomic_fetch_sub_explicit(atm, val, memoryOrder);}
long scalanative_atomic_fetch_and_long(_Atomic(long)* atm, long val) { return atomic_fetch_and(atm, val);}
long scalanative_atomic_fetch_and_explicit_long(_Atomic(long)* atm, long val, memory_order memoryOrder) { return atomic_fetch_and_explicit(atm, val, memoryOrder);}
long scalanative_atomic_fetch_or_long(_Atomic(long)* atm, long val) { return atomic_fetch_or(atm, val);}
long scalanative_atomic_fetch_or_explicit_long(_Atomic(long)* atm, long val, memory_order memoryOrder) { return atomic_fetch_or_explicit(atm, val, memoryOrder);}
long scalanative_atomic_fetch_xor_long(_Atomic(long)* atm, long val) { return atomic_fetch_xor(atm, val);}
long scalanative_atomic_fetch_xor_explicit_long(_Atomic(long)* atm, long val, memory_order memoryOrder) { return atomic_fetch_xor_explicit(atm, val, memoryOrder);}

void scalanative_atomic_init_ulong(_Atomic(unsigned long)* atm, unsigned long init_value) { atomic_init(atm, init_value);}
unsigned long scalanative_atomic_load_ulong(_Atomic(unsigned long)* atm) { return atomic_load(atm);}
unsigned long scalanative_atomic_load_explicit_ulong(_Atomic(unsigned long)* atm, memory_order memoryOrder) { return atomic_load_explicit(atm, memoryOrder);}
void scalanative_atomic_store_ulong(_Atomic(unsigned long)* atm, unsigned long val) {atomic_store(atm, val);}
void scalanative_atomic_store_explicit_ulong(_Atomic(unsigned long)* atm, unsigned long val, memory_order memoryOrder) { atomic_store_explicit(atm, val, memoryOrder);}
unsigned long scalanative_atomic_exchange_ulong(_Atomic(unsigned long)* atm, unsigned long val) { return atomic_exchange(atm, val);}
unsigned long scalanative_atomic_exchange_explicit_ulong(_Atomic(unsigned long)* atm, unsigned long val, memory_order memoryOrder) { return atomic_exchange_explicit(atm, val, memoryOrder);}
bool scalanative_atomic_compare_exchange_strong_ulong(_Atomic(unsigned long)* atm, unsigned long* expected, unsigned long desired) { return atomic_compare_exchange_strong(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_strong_explicit_ulong(_Atomic(unsigned long)* atm, unsigned long* expected, unsigned long desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_strong_explicit(atm, expected, desired, onSucc, onFail);}
bool scalanative_atomic_compare_exchange_weak_ulong(_Atomic(unsigned long)* atm, unsigned long* expected, unsigned long desired) { return atomic_compare_exchange_weak(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_weak_explicit_ulong(_Atomic(unsigned long)* atm, unsigned long* expected, unsigned long desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_weak_explicit(atm, expected, desired, onSucc, onFail);}
unsigned long scalanative_atomic_fetch_add_ulong(_Atomic(unsigned long)* atm, unsigned long val) { return atomic_fetch_add(atm, val);}
unsigned long scalanative_atomic_fetch_add_explicit_ulong(_Atomic(unsigned long)* atm, unsigned long val, memory_order memoryOrder) { return atomic_fetch_add_explicit(atm, val, memoryOrder);}
unsigned long scalanative_atomic_fetch_sub_ulong(_Atomic(unsigned long)* atm, unsigned long val) { return atomic_fetch_sub(atm, val);}
unsigned long scalanative_atomic_fetch_sub_explicit_ulong(_Atomic(unsigned long)* atm, unsigned long val, memory_order memoryOrder) { return atomic_fetch_sub_explicit(atm, val, memoryOrder);}
unsigned long scalanative_atomic_fetch_and_ulong(_Atomic(unsigned long)* atm, unsigned long val) { return atomic_fetch_and(atm, val);}
unsigned long scalanative_atomic_fetch_and_explicit_ulong(_Atomic(unsigned long)* atm, unsigned long val, memory_order memoryOrder) { return atomic_fetch_and_explicit(atm, val, memoryOrder);}
unsigned long scalanative_atomic_fetch_or_ulong(_Atomic(unsigned long)* atm, unsigned long val) { return atomic_fetch_or(atm, val);}
unsigned long scalanative_atomic_fetch_or_explicit_ulong(_Atomic(unsigned long)* atm, unsigned long val, memory_order memoryOrder) { return atomic_fetch_or_explicit(atm, val, memoryOrder);}
unsigned long scalanative_atomic_fetch_xor_ulong(_Atomic(unsigned long)* atm, unsigned long val) { return atomic_fetch_xor(atm, val);}
unsigned long scalanative_atomic_fetch_xor_explicit_ulong(_Atomic(unsigned long)* atm, unsigned long val, memory_order memoryOrder) { return atomic_fetch_xor_explicit(atm, val, memoryOrder);}

void scalanative_atomic_init_llong(_Atomic(long long)* atm, long long init_value) { atomic_init(atm, init_value);}
long long scalanative_atomic_load_llong(_Atomic(long long)* atm) { return atomic_load(atm);}
long long scalanative_atomic_load_explicit_llong(_Atomic(long long)* atm, memory_order memoryOrder) { return atomic_load_explicit(atm, memoryOrder);}
void scalanative_atomic_store_llong(_Atomic(long long)* atm, long long val) {atomic_store(atm, val);}
void scalanative_atomic_store_explicit_llong(_Atomic(long long)* atm, long long val, memory_order memoryOrder) { atomic_store_explicit(atm, val, memoryOrder);}
long long scalanative_atomic_exchange_llong(_Atomic(long long)* atm, long long val) { return atomic_exchange(atm, val);}
long long scalanative_atomic_exchange_explicit_llong(_Atomic(long long)* atm, long long val, memory_order memoryOrder) { return atomic_exchange_explicit(atm, val, memoryOrder);}
bool scalanative_atomic_compare_exchange_strong_llong(_Atomic(long long)* atm, long long* expected, long long desired) { return atomic_compare_exchange_strong(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_strong_explicit_llong(_Atomic(long long)* atm, long long* expected, long long desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_strong_explicit(atm, expected, desired, onSucc, onFail);}
bool scalanative_atomic_compare_exchange_weak_llong(_Atomic(long long)* atm, long long* expected, long long desired) { return atomic_compare_exchange_weak(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_weak_explicit_llong(_Atomic(long long)* atm, long long* expected, long long desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_weak_explicit(atm, expected, desired, onSucc, onFail);}
long long scalanative_atomic_fetch_add_llong(_Atomic(long long)* atm, long long val) { return atomic_fetch_add(atm, val);}
long long scalanative_atomic_fetch_add_explicit_llong(_Atomic(long long)* atm, long long val, memory_order memoryOrder) { return atomic_fetch_add_explicit(atm, val, memoryOrder);}
long long scalanative_atomic_fetch_sub_llong(_Atomic(long long)* atm, long long val) { return atomic_fetch_sub(atm, val);}
long long scalanative_atomic_fetch_sub_explicit_llong(_Atomic(long long)* atm, long long val, memory_order memoryOrder) { return atomic_fetch_sub_explicit(atm, val, memoryOrder);}
long long scalanative_atomic_fetch_and_llong(_Atomic(long long)* atm, long long val) { return atomic_fetch_and(atm, val);}
long long scalanative_atomic_fetch_and_explicit_llong(_Atomic(long long)* atm, long long val, memory_order memoryOrder) { return atomic_fetch_and_explicit(atm, val, memoryOrder);}
long long scalanative_atomic_fetch_or_llong(_Atomic(long long)* atm, long long val) { return atomic_fetch_or(atm, val);}
long long scalanative_atomic_fetch_or_explicit_llong(_Atomic(long long)* atm, long long val, memory_order memoryOrder) { return atomic_fetch_or_explicit(atm, val, memoryOrder);}
long long scalanative_atomic_fetch_xor_llong(_Atomic(long long)* atm, long long val) { return atomic_fetch_xor(atm, val);}
long long scalanative_atomic_fetch_xor_explicit_llong(_Atomic(long long)* atm, long long val, memory_order memoryOrder) { return atomic_fetch_xor_explicit(atm, val, memoryOrder);}

void scalanative_atomic_init_ullong(_Atomic(unsigned long long)* atm, unsigned long long init_value) { atomic_init(atm, init_value);}
unsigned long long scalanative_atomic_load_ullong(_Atomic(unsigned long long)* atm) { return atomic_load(atm);}
unsigned long long scalanative_atomic_load_explicit_ullong(_Atomic(unsigned long long)* atm, memory_order memoryOrder) { return atomic_load_explicit(atm, memoryOrder);}
void scalanative_atomic_store_ullong(_Atomic(unsigned long long)* atm, unsigned long long val) {atomic_store(atm, val);}
void scalanative_atomic_store_explicit_ullong(_Atomic(unsigned long long)* atm, unsigned long long val, memory_order memoryOrder) { atomic_store_explicit(atm, val, memoryOrder);}
unsigned long long scalanative_atomic_exchange_ullong(_Atomic(unsigned long long)* atm, unsigned long long val) { return atomic_exchange(atm, val);}
unsigned long long scalanative_atomic_exchange_explicit_ullong(_Atomic(unsigned long long)* atm, unsigned long long val, memory_order memoryOrder) { return atomic_exchange_explicit(atm, val, memoryOrder);}
bool scalanative_atomic_compare_exchange_strong_ullong(_Atomic(unsigned long long)* atm, unsigned long long* expected, unsigned long long desired) { return atomic_compare_exchange_strong(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_strong_explicit_ullong(_Atomic(unsigned long long)* atm, unsigned long long* expected, unsigned long long desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_strong_explicit(atm, expected, desired, onSucc, onFail);}
bool scalanative_atomic_compare_exchange_weak_ullong(_Atomic(unsigned long long)* atm, unsigned long long* expected, unsigned long long desired) { return atomic_compare_exchange_weak(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_weak_explicit_ullong(_Atomic(unsigned long long)* atm, unsigned long long* expected, unsigned long long desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_weak_explicit(atm, expected, desired, onSucc, onFail);}
unsigned long long scalanative_atomic_fetch_add_ullong(_Atomic(unsigned long long)* atm, unsigned long long val) { return atomic_fetch_add(atm, val);}
unsigned long long scalanative_atomic_fetch_add_explicit_ullong(_Atomic(unsigned long long)* atm, unsigned long long val, memory_order memoryOrder) { return atomic_fetch_add_explicit(atm, val, memoryOrder);}
unsigned long long scalanative_atomic_fetch_sub_ullong(_Atomic(unsigned long long)* atm, unsigned long long val) { return atomic_fetch_sub(atm, val);}
unsigned long long scalanative_atomic_fetch_sub_explicit_ullong(_Atomic(unsigned long long)* atm, unsigned long long val, memory_order memoryOrder) { return atomic_fetch_sub_explicit(atm, val, memoryOrder);}
unsigned long long scalanative_atomic_fetch_and_ullong(_Atomic(unsigned long long)* atm, unsigned long long val) { return atomic_fetch_and(atm, val);}
unsigned long long scalanative_atomic_fetch_and_explicit_ullong(_Atomic(unsigned long long)* atm, unsigned long long val, memory_order memoryOrder) { return atomic_fetch_and_explicit(atm, val, memoryOrder);}
unsigned long long scalanative_atomic_fetch_or_ullong(_Atomic(unsigned long long)* atm, unsigned long long val) { return atomic_fetch_or(atm, val);}
unsigned long long scalanative_atomic_fetch_or_explicit_ullong(_Atomic(unsigned long long)* atm, unsigned long long val, memory_order memoryOrder) { return atomic_fetch_or_explicit(atm, val, memoryOrder);}
unsigned long long scalanative_atomic_fetch_xor_ullong(_Atomic(unsigned long long)* atm, unsigned long long val) { return atomic_fetch_xor(atm, val);}
unsigned long long scalanative_atomic_fetch_xor_explicit_ullong(_Atomic(unsigned long long)* atm, unsigned long long val, memory_order memoryOrder) { return atomic_fetch_xor_explicit(atm, val, memoryOrder);}

void scalanative_atomic_init_intptr(_Atomic(intptr_t)* atm, intptr_t init_value) { atomic_init(atm, init_value);}
intptr_t scalanative_atomic_load_intptr(_Atomic(intptr_t)* atm) { return atomic_load(atm);}
intptr_t scalanative_atomic_load_explicit_intptr(_Atomic(intptr_t)* atm, memory_order memoryOrder) { return atomic_load_explicit(atm, memoryOrder);}
void scalanative_atomic_store_intptr(_Atomic(intptr_t)* atm, intptr_t val) {atomic_store(atm, val);}
void scalanative_atomic_store_explicit_intptr(_Atomic(intptr_t)* atm, intptr_t val, memory_order memoryOrder) { atomic_store_explicit(atm, val, memoryOrder);}
intptr_t scalanative_atomic_exchange_intptr(_Atomic(intptr_t)* atm, intptr_t val) { return atomic_exchange(atm, val);}
intptr_t scalanative_atomic_exchange_explicit_intptr(_Atomic(intptr_t)* atm, intptr_t val, memory_order memoryOrder) { return atomic_exchange_explicit(atm, val, memoryOrder);}
bool scalanative_atomic_compare_exchange_strong_intptr(_Atomic(intptr_t)* atm, intptr_t* expected, intptr_t desired) { return atomic_compare_exchange_strong(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_strong_explicit_intptr(_Atomic(intptr_t)* atm, intptr_t* expected, intptr_t desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_strong_explicit(atm, expected, desired, onSucc, onFail);}
bool scalanative_atomic_compare_exchange_weak_intptr(_Atomic(intptr_t)* atm, intptr_t* expected, intptr_t desired) { return atomic_compare_exchange_weak(atm, expected, desired);}
bool scalanative_atomic_compare_exchange_weak_explicit_intptr(_Atomic(intptr_t)* atm, intptr_t* expected, intptr_t desired, memory_order onSucc, memory_order onFail) { return atomic_compare_exchange_weak_explicit(atm, expected, desired, onSucc, onFail);}
intptr_t scalanative_atomic_fetch_add_intptr(_Atomic(intptr_t)* atm, intptr_t val) { return atomic_fetch_add(atm, val);}
intptr_t scalanative_atomic_fetch_add_explicit_intptr(_Atomic(intptr_t)* atm, intptr_t val, memory_order memoryOrder) { return atomic_fetch_add_explicit(atm, val, memoryOrder);}
intptr_t scalanative_atomic_fetch_sub_intptr(_Atomic(intptr_t)* atm, intptr_t val) { return atomic_fetch_sub(atm, val);}
intptr_t scalanative_atomic_fetch_sub_explicit_intptr(_Atomic(intptr_t)* atm, intptr_t val, memory_order memoryOrder) { return atomic_fetch_sub_explicit(atm, val, memoryOrder);}
intptr_t scalanative_atomic_fetch_and_intptr(_Atomic(intptr_t)* atm, intptr_t val) { return atomic_fetch_and(atm, val);}
intptr_t scalanative_atomic_fetch_and_explicit_intptr(_Atomic(intptr_t)* atm, intptr_t val, memory_order memoryOrder) { return atomic_fetch_and_explicit(atm, val, memoryOrder);}
intptr_t scalanative_atomic_fetch_or_intptr(_Atomic(intptr_t)* atm, intptr_t val) { return atomic_fetch_or(atm, val);}
intptr_t scalanative_atomic_fetch_or_explicit_intptr(_Atomic(intptr_t)* atm, intptr_t val, memory_order memoryOrder) { return atomic_fetch_or_explicit(atm, val, memoryOrder);}
intptr_t scalanative_atomic_fetch_xor_intptr(_Atomic(intptr_t)* atm, intptr_t val) { return atomic_fetch_xor(atm, val);}
intptr_t scalanative_atomic_fetch_xor_explicit_intptr(_Atomic(intptr_t)* atm, intptr_t val, memory_order memoryOrder) { return atomic_fetch_xor_explicit(atm, val, memoryOrder);}
