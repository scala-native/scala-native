#include <iostream>

extern "C" void* scalanative_cpp_ios_stdin()
{
    return &std::cin;
}

extern "C" void* scalanative_cpp_ios_stdout()
{
    return &std::cout;
}

extern "C" void* scalanative_cpp_ios_stderr()
{
    return &std::cerr;
}

extern "C" void scalanative_cpp_ios_iostream_write(void* obj, const char* buf, size_t count)
{
    if (!obj)
        return;
    static_cast<std::ostream *>(obj)->write(buf, count);
}