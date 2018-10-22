// cross-platform c++11 time
#include <chrono>

// return nanoseconds
long long steady_clock() {
    static const auto start = std::chrono::steady_clock::now();
    const auto end = std::chrono::steady_clock::now();
    const auto result =
        std::chrono::duration_cast<std::chrono::nanoseconds>(end - start);
    return result.count();
}

extern "C" long long scalanative_nano_time() { return steady_clock(); }
