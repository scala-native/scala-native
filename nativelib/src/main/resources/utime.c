#ifndef _WIN32
  #include <utime.h>
#else
  #include <sys/types.h>
  #include <sys/utime.h>
#endif

int scalanative_utime(char *path, struct utimbuf *times) {
    return utime(path, times);
}
