#if !defined(_WIN32) && defined(SCALANATIVE_COMPILE_ALWAYS) ||                 \
    defined(__SCALANATIVE_POSIX_SYS_WAIT)

#include <stdbool.h>
#include <sys/types.h>
#include <sys/wait.h>

// Symbolic constants, roughly in POSIX declaration order

// idtype_t
int scalanative_c_p_all() { return P_ALL; }   // POSIX enum: idtype_t
int scalanative_c_p_pgid() { return P_PGID; } // POSIX enum: idtype_t
int scalanative_c_p_pid() { return P_PID; }   // POSIX enum: idtype_t

// "constants" for waitpid()

int scalanative_c_wcontinued() { return WCONTINUED; }
int scalanative_c_wnohang() { return WNOHANG; }
int scalanative_c_wuntraced() { return WUNTRACED; }

// "constants" for waitid() options
int scalanative_c_wexited() { return WEXITED; }
int scalanative_c_wnowait() { return WNOWAIT; }
int scalanative_c_wstopped() { return WSTOPPED; }

// POSIX "Macros"
int scalanative_c_wexitstatus(int wstatus) { return WEXITSTATUS(wstatus); }

bool scalanative_c_wifcontinued(int wstatus) { return WIFCONTINUED(wstatus); }

bool scalanative_c_wifexited(int wstatus) { return WIFEXITED(wstatus); }

bool scalanative_c_wifsignaled(int wstatus) { return WIFSIGNALED(wstatus); }

bool scalanative_c_wifstopped(int wstatus) { return WIFSTOPPED(wstatus); }

int scalanative_c_wstopsig(int wstatus) { return WSTOPSIG(wstatus); }

int scalanative_c_wtermsig(int wstatus) { return WTERMSIG(wstatus); }

#endif // !_WIN32
