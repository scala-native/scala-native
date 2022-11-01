#if !defined(_WIN32)

#include <stdbool.h>
#include <sys/types.h>
#include <sys/wait.h>

// Symbolic constants, roughly in POSIX declaration order

// idtype_t   
int scalanative_posix_p_all(){ return P_ALL; }   // POSIX enum: idtype_t
int scalanative_posix_p_pgid(){ return P_PGID; } // POSIX enum: idtype_t
int scalanative_posix_p_pid(){ return P_PID; }   // POSIX enum: idtype_t

// "constants" for waitpid()   

int scalanative_posix_wcontinued() { return WCONTINUED; }
int scalanative_posix_wnohang() { return WNOHANG; }
int scalanative_posix_wuntraced() { return WUNTRACED; }

// "constants" for waitid() options                                         
int scalanative_posix_wexited() { return WEXITED;  }
int scalanative_posix_wnowait() { return WNOWAIT; }
int scalanative_posix_wstopped() { return WSTOPPED; }

// POSIX "Macros"
int scalanative_posix_wexitstatus(int wstatus) { return WEXITSTATUS(wstatus); }

bool scalanative_posix_wifcontinued(int wstatus) { return WIFCONTINUED(wstatus); }

bool scalanative_posix_wifexited(int wstatus) { return WIFEXITED(wstatus); }

bool scalanative_posix_wifsignaled(int wstatus) { return WIFSIGNALED(wstatus); }

bool scalanative_posix_wifstopped(int wstatus) { return WIFSTOPPED(wstatus); }

int scalanative_posix_wstopsig(int wstatus) { return WSTOPSIG(wstatus); }

int scalanative_posix_wtermsig(int wstatus) { return WTERMSIG(wstatus); }

#endif // !_WIN32
