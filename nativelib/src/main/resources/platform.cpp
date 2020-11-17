#include <thread>
#include <chrono>

bool scalanative_platform_thread_sleep(unsigned long long millis, int nanos)
{
    try {
        std::this_thread::sleep_for(
            std::chrono::milliseconds(millis) + std::chrono::nanoseconds(nanos)
        );
    }
    catch (...) { return false; }

    return true;
}