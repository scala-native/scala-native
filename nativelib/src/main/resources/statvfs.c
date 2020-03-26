#include <sys/statvfs.h>

unsigned long scalanative_st_rdonly() { return ST_RDONLY; }

unsigned long scalanative_st_nosuid() { return ST_NOSUID; }
