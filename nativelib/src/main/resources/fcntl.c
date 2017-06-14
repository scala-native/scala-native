#include <fcntl.h>

#ifdef _WIN32
#define	F_DUPFD		0		/* duplicate file descriptor */
#define	F_GETFD		1		/* get file descriptor flags */
#define	F_SETFD		2		/* set file descriptor flags */
#define	F_GETFL		3		/* get file status flags */
#define	F_SETFL		4		/* set file status flags */
#define	F_GETOWN	5		/* get SIGIO/SIGURG proc/pgrp */
#define F_SETOWN	6		/* set SIGIO/SIGURG proc/pgrp */
#define	F_GETLK		7		/* get record locking information */
#define	F_SETLK		8		/* set record locking information */
#define	F_SETLKW	9		/* F_SETLK; wait if blocked */
#endif

int scalanative_o_rdonly() { return O_RDONLY; }

int scalanative_o_wronly() { return O_WRONLY; }

int scalanative_o_rdwr() { return O_RDWR; }

int scalanative_o_append() { return O_APPEND; }

int scalanative_o_creat() { return O_CREAT; }

int scalanative_o_trunc() { return O_TRUNC; }

int scalanative_f_dupfd() { return F_DUPFD; }

int scalanative_f_getfd() { return F_GETFD; }

int scalanative_f_setfd() { return F_SETFD; }

int scalanative_f_getfl() { return F_GETFL; }

int scalanative_f_setfl() { return F_SETFL; }

int scalanative_f_getown() { return F_GETOWN; }

int scalanative_f_setown() { return F_SETOWN; }

int scalanative_f_getlk() { return F_GETLK; }

int scalanative_f_setlk() { return F_SETLK; }

int scalanative_f_setlkw() { return F_SETLKW; }
