#include <cpio.h>

unsigned short scalanative_c_issock() { return C_ISSOCK; }
unsigned short scalanative_c_islnk() { return C_ISLNK; }
unsigned short scalanative_c_isctg() { return C_ISCTG; }
unsigned short scalanative_c_isreg() { return C_ISREG; }
unsigned short scalanative_c_isblk() { return C_ISBLK; }
unsigned short scalanative_c_isdir() { return C_ISDIR; }
unsigned short scalanative_c_ischr() { return C_ISCHR; }
unsigned short scalanative_c_isfifo() { return C_ISFIFO; }
unsigned short scalanative_c_isuid() { return C_ISUID; }
unsigned short scalanative_c_isgid() { return C_ISGID; }
unsigned short scalanative_c_isvtx() { return C_ISVTX; }
unsigned short scalanative_c_irusr() { return C_IRUSR; }
unsigned short scalanative_c_iwusr() { return C_IWUSR; }
unsigned short scalanative_c_ixusr() { return C_IXUSR; }
unsigned short scalanative_c_irgrp() { return C_IRGRP; }
unsigned short scalanative_c_iwgrp() { return C_IWGRP; }
unsigned short scalanative_c_ixgrp() { return C_IXGRP; }
unsigned short scalanative_c_iroth() { return C_IROTH; }
unsigned short scalanative_c_iwoth() { return C_IWOTH; }
unsigned short scalanative_c_ixoth() { return C_IXOTH; }

const char *scalanative_magic() { return MAGIC; }
