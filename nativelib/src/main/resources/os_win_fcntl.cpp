#ifdef _WIN32
#include "os_win_fcntl.h"
#include <exception>

extern "C" int fcntl(int fd, int cmd, va_list args)
{
    //throw std::exception("`fcntl` is not implemented");
    return 0;
}

#endif