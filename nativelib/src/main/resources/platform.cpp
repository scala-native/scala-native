#include <thread>
#include <chrono>

bool scalanative_platform_thread_sleep(unsigned long long millis, int nanos)
{
    millis += nanos * 1000000;
    try { std::this_thread::sleep_for(std::chrono::microseconds(millis)); }
    catch (...) { return false; }

    return true;
}