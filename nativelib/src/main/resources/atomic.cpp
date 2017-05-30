#include <atomic>
#include <stdint.h>

using namespace std;

extern "C" {
	
	
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

    int compare_and_swap_strong_uinz(uint32_t* value, uint32_t* expected, uint32_t desired) {
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

    // boolean
    int compare_and_swap_weak_bool(bool* value, bool* expected, bool desired) {
        const auto atm = reinterpret_cast<atomic<bool>*>(value);
        return atomic_compare_exchange_weak(atm, expected, desired);
    }

    int compare_and_swap_strong_bool(bool* value, bool* expected, bool desired) {
        const auto atm = reinterpret_cast<atomic<bool>*>(value);
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
     int8_t atomic_add_byte(int8_t* value, int8_t increment) {
        const auto atm = reinterpret_cast<atomic<int8_t>*>(value);
        atm -> fetch_add(increment);
        return atm -> load();
     }

     // short
     int16_t atomic_add_short(int16_t* value, int16_t increment) {
		const auto atm = reinterpret_cast<atomic<int16_t>*>(value);
        atm -> fetch_add(increment);
        return atm -> load();
     }
     
     // integer
     int32_t atomic_add_int(int32_t* value, int32_t increment) {
		const auto atm = reinterpret_cast<atomic<int32_t>*>(value);
        atm -> fetch_add(increment);
        return atm -> load();
     }
     
     // long
     int64_t atomic_add_long(int64_t* value, int64_t increment) {
		const auto atm = reinterpret_cast<atomic<int64_t>*>(value);
        atm -> fetch_add(increment);
        return atm -> load();
     }
     
     // unsigned byte
     uint8_t atomic_add_ubyte(uint8_t* value, uint8_t increment) {
        const auto atm = reinterpret_cast<atomic<uint8_t>*>(value);
        atm -> fetch_add(increment);
        return atm -> load();
     }
     
     // unsigned short
     uint16_t atomic_add_ushort(uint16_t* value, uint16_t increment) {
		const auto atm = reinterpret_cast<atomic<uint16_t>*>(value);
        atm -> fetch_add(increment);
        return atm -> load();
     }
     
     // unsigned integer
     uint32_t atomic_add_uint(uint32_t* value, uint32_t increment) {
		const auto atm = reinterpret_cast<atomic<uint32_t>*>(value);
        atm -> fetch_add(increment);
        return atm -> load();
     }
     
     // unsigned long
     uint64_t atomic_add_ulong(uint64_t* value, uint64_t increment) {
		const auto atm = reinterpret_cast<atomic<uint64_t>*>(value);
        atm -> fetch_add(increment);
        return atm -> load();
     }
     
     // char
     char atomic_add_char(char* value, char increment) {
		const auto atm = reinterpret_cast<atomic<char>*>(value);
        atm -> fetch_add(increment);
        return atm -> load();
     }
     
     // unsigned char
     unsigned char atomic_add_uchar(unsigned char* value, unsigned char increment) {
		const auto atm = reinterpret_cast<atomic<unsigned char>*>(value);
        atm -> fetch_add(increment);
        return atm -> load();
     }
     
     // size_t
     size_t atomic_add_csize(size_t* value, size_t increment) {
		const auto atm = reinterpret_cast<atomic<size_t>*>(value);
        atm -> fetch_add(increment);
        return atm -> load();
     }

	/**
     * Sub
     * */

     // byte
     int8_t atomic_sub_byte(int8_t* value, int8_t decrement) {
        const auto atm = reinterpret_cast<atomic<int8_t>*>(value);
        atm -> fetch_sub(decrement);
        return atm -> load();
     }

     // short
     int16_t atomic_sub_short(int16_t* value, int16_t decrement) {
		const auto atm = reinterpret_cast<atomic<int16_t>*>(value);
        atm -> fetch_sub(decrement);
        return atm -> load();
     }
     
     // integer
     int32_t atomic_sub_int(int32_t* value, int32_t decrement) {
		const auto atm = reinterpret_cast<atomic<int32_t>*>(value);
        atm -> fetch_sub(decrement);
        return atm -> load();
     }
     
     // long
     int64_t atomic_sub_long(int64_t* value, int64_t decrement) {
		const auto atm = reinterpret_cast<atomic<int64_t>*>(value);
        atm -> fetch_sub(decrement);
        return atm -> load();
     }
     
     // unsigned byte
     uint8_t atomic_sub_ubyte(uint8_t* value, uint8_t decrement) {
        const auto atm = reinterpret_cast<atomic<uint8_t>*>(value);
        atm -> fetch_sub(decrement);
        return atm -> load();
     }
     
     // unsigned short
     uint16_t atomic_sub_ushort(uint16_t* value, uint16_t decrement) {
		const auto atm = reinterpret_cast<atomic<uint16_t>*>(value);
        atm -> fetch_sub(decrement);
        return atm -> load();
     }
     
     // unsigned integer
     uint32_t atomic_sub_uint(uint32_t* value, uint32_t decrement) {
		const auto atm = reinterpret_cast<atomic<uint32_t>*>(value);
        atm -> fetch_sub(decrement);
        return atm -> load();
     }
     
     // unsigned long
     uint64_t atomic_sub_ulong(uint64_t* value, uint64_t decrement) {
		const auto atm = reinterpret_cast<atomic<uint64_t>*>(value);
        atm -> fetch_sub(decrement);
        return atm -> load();
     }
     
     // char
     char atomic_sub_char(char* value, char decrement) {
		const auto atm = reinterpret_cast<atomic<char>*>(value);
        atm -> fetch_sub(decrement);
        return atm -> load();
     }
     
     // unsigned char
     unsigned char atomic_sub_uchar(unsigned char* value, unsigned char decrement) {
		const auto atm = reinterpret_cast<atomic<unsigned char>*>(value);
        atm -> fetch_sub(decrement);
        return atm -> load();
     }
     
     // size_t
     size_t atomic_sub_csize(size_t* value, size_t decrement) {
		const auto atm = reinterpret_cast<atomic<size_t>*>(value);
        atm -> fetch_sub(decrement);
        return atm -> load();
     }


	/**
     * And
     * */
     
     // C boolean
     int32_t atomic_and(int32_t* value, int32_t arg) {
		const auto atm = reinterpret_cast<atomic<int32_t>*>(value);
        atm -> fetch_and(arg);
        return atm -> load();
     }

	/**
     * Or
     * */

     // C boolean
     int32_t atomic_or(int32_t* value, int32_t arg) {
		const auto atm = reinterpret_cast<atomic<int32_t>*>(value);
        atm -> fetch_or(arg);
        return atm -> load();
     }

	/**
     * Xor
     * */

     // C boolean
     int32_t atomic_xor(int32_t* value, int32_t arg) {
		const auto atm = reinterpret_cast<atomic<int32_t>*>(value);
        atm -> fetch_xor(arg);
        return atm -> load();
     }
}
