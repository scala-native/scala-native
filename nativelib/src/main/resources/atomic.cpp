#include <atomic>
#include <stdint.h>
#include <stdlib.h>

using namespace std;

extern "C" {

	/**
     * Init
     * */

     // byte
     void init_byte(int8_t* atm, int8_t init_value) {
        *atm = ATOMIC_VAR_INIT(init_value);
     }

     // short
     void init_short(int16_t* atm, int16_t init_value) {
        *atm = ATOMIC_VAR_INIT(init_value);
     }

     // integer
     void init_int(int32_t* atm, int32_t init_value) {
        *atm = ATOMIC_VAR_INIT(init_value);
     }

     // long
     void init_long(int64_t* atm, int64_t init_value) {
        *atm = ATOMIC_VAR_INIT(init_value);
     }

     // unsigned byte
     void init_ubyte(uint8_t* atm, uint8_t init_value) {
        *atm = ATOMIC_VAR_INIT(init_value);
     }

     // unsigned short
     void init_ushort(uint16_t* atm, uint16_t init_value) {
        *atm = ATOMIC_VAR_INIT(init_value);
     }

     // unsigned integer
     void init_uint(uint32_t* atm, uint32_t init_value) {
        *atm = ATOMIC_VAR_INIT(init_value);
     }

     // unsigned long
     void init_ulong(uint64_t* atm, uint64_t init_value) {
        *atm = ATOMIC_VAR_INIT(init_value);
     }

     // char
     void init_char(char* atm, char init_value) {
        *atm = ATOMIC_VAR_INIT(init_value);
     }

     // unsigned char
     void init_uchar(unsigned char* atm, unsigned char init_value) {
        *atm = ATOMIC_VAR_INIT(init_value);
     }

     // size_t
     void init_csize(int32_t* atm, int32_t init_value) {
        *atm = ATOMIC_VAR_INIT(init_value);
     }

	/**
     * Memory
     * */
     void* alloc(size_t sz) {
        return malloc(sz);
     }

     void free(void* ptr) {
        free(ptr);
     }

	/**
     * Load
     * */

    // byte
    int8_t load_byte(atomic<int8_t>* atm) {
        return atm -> load();
    }

    // short
    int16_t load_short(atomic<int16_t>* atm) {
        return atm -> load();
    }

    // int
    int32_t load_int(atomic<int32_t>* atm) {
        return atm -> load();
    }

    // long
    int64_t load_long(atomic<int64_t>* atm) {
        return atm -> load();
    }

    // unsigned byte
    uint8_t load_ubyte(atomic<uint8_t>* atm) {
        return atm -> load();
    }

    // unsigned short
    uint16_t load_ushort(atomic<uint16_t>* atm) {
        return atm -> load();
    }

    // unsigned int
    uint32_t load_uint(atomic<uint32_t>* atm) {
        return atm -> load();
    }

    // unsigned long
    uint64_t load_ulong(atomic<uint64_t>* atm) {
        return atm -> load();
    }

    // char
    char load_char(atomic<char>* atm) {
        return atm -> load();
    }

    // unsigned char
    unsigned char load_uchar(atomic<unsigned char>* atm) {
        return atm -> load();
    }

    // size_t
    size_t load_csize(atomic<size_t>* atm) {
        return atm -> load();
    }

	
	/**
	 * Compare and Swap
	 * */

    // byte
    int compare_and_swap_weak_byte(int8_t* value, int8_t* expected, int8_t desired) {
        const auto atm = reinterpret_cast<atomic<int8_t>*>(value);
        return atomic_compare_exchange_weak(atm, expected, desired);
    }

    int compare_and_swap_strong_byte(int8_t* value, int8_t* expected, int8_t desired) {
        const auto atm = reinterpret_cast<atomic<int8_t>*>(value);
        return atomic_compare_exchange_strong(atm, expected, desired);
    }

    // short
    int compare_and_swap_weak_short(int16_t* value, int16_t* expected, int16_t desired) {
        const auto atm = reinterpret_cast<atomic<int16_t>*>(value);
        return atomic_compare_exchange_weak(atm, expected, desired);
    }

    int compare_and_swap_strong_short(int16_t* value, int16_t* expected, int16_t desired) {
        const auto atm = reinterpret_cast<atomic<int16_t>*>(value);
        return atomic_compare_exchange_strong(atm, expected, desired);
    }

    // integer
    int compare_and_swap_weak_int(int32_t* value, int32_t* expected, int32_t desired) {
        const auto atm = reinterpret_cast<atomic<int32_t>*>(value);
        return atomic_compare_exchange_weak(atm, expected, desired);
    }

    int compare_and_swap_strong_int(int32_t* value, int32_t* expected, int32_t desired) {
        const auto atm = reinterpret_cast<atomic<int32_t>*>(value);
        return atomic_compare_exchange_strong(atm, expected, desired);
    }

    // long
    int compare_and_swap_weak_long(int64_t* value, int64_t* expected, int64_t desired) {
        const auto atm = reinterpret_cast<atomic<int64_t>*>(value);
        return atomic_compare_exchange_weak(atm, expected, desired);
    }

    int compare_and_swap_strong_long(int64_t* value, int64_t* expected, int64_t desired) {
        const auto atm = reinterpret_cast<atomic<int64_t>*>(value);
        return atomic_compare_exchange_strong(atm, expected, desired);
    }


    // unsigned byte
    int compare_and_swap_weak_ubyte(uint8_t* value, uint8_t* expected, uint8_t desired) {
        const auto atm = reinterpret_cast<atomic<uint8_t>*>(value);
        return atomic_compare_exchange_weak(atm, expected, desired);
    }

    int compare_and_swap_strong_ubyte(uint8_t* value, uint8_t* expected, uint8_t desired) {
        const auto atm = reinterpret_cast<atomic<uint8_t>*>(value);
        return atomic_compare_exchange_strong(atm, expected, desired);
    }

    // unsigned short
    int compare_and_swap_weak_ushort(uint16_t* value, uint16_t* expected, uint16_t desired) {
        const auto atm = reinterpret_cast<atomic<uint16_t>*>(value);
        return atomic_compare_exchange_weak(atm, expected, desired);
    }

    int compare_and_swap_strong_ushort(uint16_t* value, uint16_t* expected, uint16_t desired) {
        const auto atm = reinterpret_cast<atomic<uint16_t>*>(value);
        return atomic_compare_exchange_strong(atm, expected, desired);
    }


    // unsigned integer
    int compare_and_swap_weak_uint(uint32_t* value, uint32_t* expected, uint32_t desired) {
        const auto atm = reinterpret_cast<atomic<uint32_t>*>(value);
        return atomic_compare_exchange_weak(atm, expected, desired);
    }

    int compare_and_swap_strong_uint(uint32_t* value, uint32_t* expected, uint32_t desired) {
        const auto atm = reinterpret_cast<atomic<uint32_t>*>(value);
        return atomic_compare_exchange_strong(atm, expected, desired);
    }


    // unsigned long
    int compare_and_swap_weak_ulong(uint64_t* value, uint64_t* expected, uint64_t desired) {
        const auto atm = reinterpret_cast<atomic<uint64_t>*>(value);
        return atomic_compare_exchange_weak(atm, expected, desired);
    }

    int compare_and_swap_strong_ulong(uint64_t* value, uint64_t* expected, uint64_t desired) {
        const auto atm = reinterpret_cast<atomic<uint64_t>*>(value);
        return atomic_compare_exchange_strong(atm, expected, desired);
    }

	// char
	int compare_and_swap_weak_char(char* value, char* expected, char desired) {
		const auto atm = reinterpret_cast<atomic<char>*>(value);
		return atomic_compare_exchange_weak(atm, expected, desired);
	}

	int compare_and_swap_strong_char(char* value, char* expected, char desired) {
		const auto atm = reinterpret_cast<atomic<char>*>(value);
		return atomic_compare_exchange_strong(atm, expected, desired);
	}
	
	// unsigned char
	int compare_and_swap_weak_uchar(unsigned char* value, unsigned char* expected, unsigned char desired) {
		const auto atm = reinterpret_cast<atomic<unsigned char>*>(value);
		return atomic_compare_exchange_weak(atm, expected, desired);
	}

	int compare_and_swap_strong_uchar(unsigned char* value, unsigned char* expected, unsigned char desired) {
		const auto atm = reinterpret_cast<atomic<unsigned char>*>(value);
		return atomic_compare_exchange_strong(atm, expected, desired);
	}
	
	// size_t
	int compare_and_swap_weak_csize(size_t* value, size_t* expected, size_t desired) {
		const auto atm = reinterpret_cast<atomic<size_t>*>(value);
		return atomic_compare_exchange_weak(atm, expected, desired);
	}

	int compare_and_swap_strong_csize(size_t* value, size_t* expected, size_t desired) {
		const auto atm = reinterpret_cast<atomic<size_t>*>(value);
		return atomic_compare_exchange_strong(atm, expected, desired);
	}

	/**
     * Add
     * */

     // byte
     int8_t atomic_add_byte(atomic<int8_t>* atm, int8_t increment) {
        return atm -> fetch_add(increment);
     }

     // short
     int16_t atomic_add_short(atomic<int16_t>* atm, int16_t increment) {
        return atm -> fetch_add(increment);
     }
     
     // integer
     int32_t atomic_add_int(atomic<int32_t>* value, int32_t increment) {
        return value -> fetch_add(increment);
     }
     
     // long
     int64_t atomic_add_long(atomic<int64_t>* atm, int64_t increment) {
        return atm -> fetch_add(increment);
     }
     
     // unsigned byte
     uint8_t atomic_add_ubyte(atomic<uint8_t>* atm, uint8_t increment) {
        return atm -> fetch_add(increment);
     }
     
     // unsigned short
     uint16_t atomic_add_ushort(atomic<uint16_t>* atm, uint16_t increment) {
        return atm -> fetch_add(increment);
     }
     
     // unsigned integer
     uint32_t atomic_add_uint(atomic<uint32_t>* atm, uint32_t increment) {
        return atm -> fetch_add(increment);
     }
     
     // unsigned long
     uint64_t atomic_add_ulong(atomic<uint64_t>* atm, uint64_t increment) {
        return atm -> fetch_add(increment);
     }
     
     // char
     char atomic_add_char(atomic<char>* atm, char increment) {
        return atm -> fetch_add(increment);
     }
     
     // unsigned char
     unsigned char atomic_add_uchar(atomic<unsigned char>* atm, unsigned char increment) {
        return atm -> fetch_add(increment);
     }
     
     // size_t
     size_t atomic_add_csize(atomic<size_t>* atm, size_t increment) {
        return atm -> fetch_add(increment);
     }

	/**
     * Sub
     * */

     // byte
     int8_t atomic_sub_byte(atomic<int8_t>* atm, int8_t increment) {
        return atm -> fetch_sub(increment);
     }

     // short
     int16_t atomic_sub_short(atomic<int16_t>* atm, int16_t increment) {
        return atm -> fetch_sub(increment);
     }
     
     // integer
     int32_t atomic_sub_int(atomic<int32_t>* value, int32_t increment) {
        return value -> fetch_sub(increment);
     }
     
     // long
     int64_t atomic_sub_long(atomic<int64_t>* atm, int64_t increment) {
        return atm -> fetch_sub(increment);
     }
     
     // unsigned byte
     uint8_t atomic_sub_ubyte(atomic<uint8_t>* atm, uint8_t increment) {
        return atm -> fetch_sub(increment);
     }
     
     // unsigned short
     uint16_t atomic_sub_ushort(atomic<uint16_t>* atm, uint16_t increment) {
        return atm -> fetch_sub(increment);
     }
     
     // unsigned integer
     uint32_t atomic_sub_uint(atomic<uint32_t>* atm, uint32_t increment) {
        return atm -> fetch_sub(increment);
     }
     
     // unsigned long
     uint64_t atomic_sub_ulong(atomic<uint64_t>* atm, uint64_t increment) {
        return atm -> fetch_sub(increment);
     }
     
     // char
     char atomic_sub_char(atomic<char>* atm, char increment) {
        return atm -> fetch_sub(increment);
     }
     
     // unsigned char
     unsigned char atomic_sub_uchar(atomic<unsigned char>* atm, unsigned char increment) {
        return atm -> fetch_sub(increment);
     }
     
     // size_t
     size_t atomic_sub_csize(atomic<size_t>* atm, size_t increment) {
        return atm -> fetch_sub(increment);
     }

	/**
     * And
     * */
     
     // byte
     int8_t atomic_and_byte(atomic<int8_t>* atm, int8_t increment) {
        return atm -> fetch_and(increment);
     }

     // short
     int16_t atomic_and_short(atomic<int16_t>* atm, int16_t increment) {
        return atm -> fetch_and(increment);
     }
     
     // integer
     int32_t atomic_and_int(atomic<int32_t>* value, int32_t increment) {
        return value -> fetch_and(increment);
     }
     
     // long
     int64_t atomic_and_long(atomic<int64_t>* atm, int64_t increment) {
        return atm -> fetch_and(increment);
     }
     
     // unsigned byte
     uint8_t atomic_and_ubyte(atomic<uint8_t>* atm, uint8_t increment) {
        return atm -> fetch_and(increment);
     }
     
     // unsigned short
     uint16_t atomic_and_ushort(atomic<uint16_t>* atm, uint16_t increment) {
        return atm -> fetch_and(increment);
     }
     
     // unsigned integer
     uint32_t atomic_and_uint(atomic<uint32_t>* atm, uint32_t increment) {
        return atm -> fetch_and(increment);
     }
     
     // unsigned long
     uint64_t atomic_and_ulong(atomic<uint64_t>* atm, uint64_t increment) {
        return atm -> fetch_and(increment);
     }
     
     // char
     char atomic_and_char(atomic<char>* atm, char increment) {
        return atm -> fetch_and(increment);
     }
     
     // unsigned char
     unsigned char atomic_and_uchar(atomic<unsigned char>* atm, unsigned char increment) {
        return atm -> fetch_and(increment);
     }
     
     // size_t
     size_t atomic_and_csize(atomic<size_t>* atm, size_t increment) {
        return atm -> fetch_and(increment);
     }

	/**
     * Or
     * */

     // byte
     int8_t atomic_or_byte(atomic<int8_t>* atm, int8_t increment) {
        return atm -> fetch_or(increment);
     }

     // short
     int16_t atomic_or_short(atomic<int16_t>* atm, int16_t increment) {
        return atm -> fetch_or(increment);
     }
     
     // integer
     int32_t atomic_or_int(atomic<int32_t>* value, int32_t increment) {
        return value -> fetch_or(increment);
     }
     
     // long
     int64_t atomic_or_long(atomic<int64_t>* atm, int64_t increment) {
        return atm -> fetch_or(increment);
     }
     
     // unsigned byte
     uint8_t atomic_or_ubyte(atomic<uint8_t>* atm, uint8_t increment) {
        return atm -> fetch_or(increment);
     }
     
     // unsigned short
     uint16_t atomic_or_ushort(atomic<uint16_t>* atm, uint16_t increment) {
        return atm -> fetch_or(increment);
     }
     
     // unsigned integer
     uint32_t atomic_or_uint(atomic<uint32_t>* atm, uint32_t increment) {
        return atm -> fetch_or(increment);
     }
     
     // unsigned long
     uint64_t atomic_or_ulong(atomic<uint64_t>* atm, uint64_t increment) {
        return atm -> fetch_or(increment);
     }
     
     // char
     char atomic_or_char(atomic<char>* atm, char increment) {
        return atm -> fetch_or(increment);
     }
     
     // unsigned char
     unsigned char atomic_or_uchar(atomic<unsigned char>* atm, unsigned char increment) {
        return atm -> fetch_or(increment);
     }
     
     // size_t
     size_t atomic_or_csize(atomic<size_t>* atm, size_t increment) {
        return atm -> fetch_or(increment);
     }

	/**
     * Xor
     * */

     // byte
     int8_t atomic_xor_byte(atomic<int8_t>* atm, int8_t increment) {
        return atm -> fetch_xor(increment);
     }

     // short
     int16_t atomic_xor_short(atomic<int16_t>* atm, int16_t increment) {
        return atm -> fetch_xor(increment);
     }
     
     // integer
     int32_t atomic_xor_int(atomic<int32_t>* value, int32_t increment) {
        return value -> fetch_xor(increment);
     }
     
     // long
     int64_t atomic_xor_long(atomic<int64_t>* atm, int64_t increment) {
        return atm -> fetch_xor(increment);
     }
     
     // unsigned byte
     uint8_t atomic_xor_ubyte(atomic<uint8_t>* atm, uint8_t increment) {
        return atm -> fetch_xor(increment);
     }
     
     // unsigned short
     uint16_t atomic_xor_ushort(atomic<uint16_t>* atm, uint16_t increment) {
        return atm -> fetch_xor(increment);
     }
     
     // unsigned integer
     uint32_t atomic_xor_uint(atomic<uint32_t>* atm, uint32_t increment) {
        return atm -> fetch_xor(increment);
     }
     
     // unsigned long
     uint64_t atomic_xor_ulong(atomic<uint64_t>* atm, uint64_t increment) {
        return atm -> fetch_xor(increment);
     }
     
     // char
     char atomic_xor_char(atomic<char>* atm, char increment) {
        return atm -> fetch_xor(increment);
     }
     
     // unsigned char
     unsigned char atomic_xor_uchar(atomic<unsigned char>* atm, unsigned char increment) {
        return atm -> fetch_xor(increment);
     }
     
     // size_t
     size_t atomic_xor_csize(atomic<size_t>* atm, size_t increment) {
        return atm -> fetch_xor(increment);
     }
}
