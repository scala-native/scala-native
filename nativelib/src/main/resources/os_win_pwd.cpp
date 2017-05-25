#ifdef _WIN32

#include "os_win_pwd.h"

extern "C" struct passwd *getpwuid(uid_t uid) { return 0; }

extern "C" struct passwd *getpwnam(const char *name) { return 0; }

#endif