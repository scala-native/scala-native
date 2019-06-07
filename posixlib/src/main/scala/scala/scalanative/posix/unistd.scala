package scala.scalanative
package posix

import scalanative.unsafe._
// import scalanative.native.stdint._ // as soon as it is implemented.
import scalanative.libc.stdio._
import scalanative.libc.stdlib._
import scalanative.posix.sys.types

// Synchronized with: The Open Group Base Specifications Issue 7, 2018 edition
// https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/unistd.h.html
//
// Please note the following caveats:
//   * Many "macro" constants are not provided:
//     - Version test macros
//     - Constants for Options and Option Groups
//     - Execution-Time Symbolic Constants
//
//   * unistd.h type NULL is not provided. Scala has a null type in the
//     language.
//
//   * OBSOLETE & removed methods usleep() and vfork() are included
//     for compability for earlier versions of Posix. New code should
//     use modern practices, such as using nanosleep() and avoiding vfork().
//
//   * A few gnu extensions, such as environ, are included.
//
//   * Three execl* methods, execl, execle, and execlp, are not declared
//     because they require the "..." form of varargs. That form is not
//     supported by Scala Native.

@extern
object unistd {

  type gid_t      = types.gid_t
  type size_t     = types.size_t
  type ssize_t    = types.ssize_t
  type off_t      = types.off_t
  type pid_t      = types.pid_t
  type uid_t      = types.uid_t
  type useconds_t = CUnsignedInt // primary definition here, not types.scala

  type intptr_t = Ptr[CInt] // Explicit because no stdint.scala yet

  def access(pathname: CString, mode: CInt): CInt = extern
  def alarm(seconds: CUnsignedInt): CUnsignedInt  = extern

  def chdir(path: CString): CInt                             = extern
  def chown(path: CString, owner: uid_t, group: gid_t): CInt = extern
  def close(fildes: CInt): CInt                              = extern
  def confstr(name: CInt, buf: Ptr[_], len: size_t): size_t  = extern
  def crypt(key: Ptr[CChar], salt: Ptr[CChar]): Ptr[CChar]   = extern
  // crypt_r is GNU. data should be Ptr[struct crypt_data].
  def crypt_r(key: Ptr[CChar], salt: Ptr[CChar], data: Ptr[CChar]): Ptr[CChar] =
    extern

  def dup(fildes: CInt): CInt                   = extern
  def dup2(fildes: CInt, fildesnew: CInt): CInt = extern

  def _exit(status: CInt): Unit                      = extern
  def encrypt(block: Ptr[CChar], edflag: CInt): Unit = extern
  // encrypt_r is GNU. data should be Ptr[struct crypt_data].
  def encrypt_r(block: Ptr[CChar], edflag: CInt, data: Ptr[CChar]): Unit =
    extern
  def execv(path: CString, argv: Ptr[CString]): CInt =
    extern
  def execve(path: CString, argv: Ptr[CString], envp: Ptr[CString]): CInt =
    extern
  def execvp(file: CString, argv: Ptr[CString]): CInt =
    extern
  // execvpe() is GNU
  def execvpe(file: CString, argv: Ptr[CString], envp: Ptr[CString]): CInt =
    extern

  def faccessat(dirfd: CInt, pathname: CString, mode: CInt, flags: CInt): CInt =
    extern
  def fchdir(fd: CInt): CInt                         = extern
  def fchown(fd: CInt, uid: uid_t, gid: gid_t): CInt = extern
  def fchownat(dirfd: CInt, pathname: CString, uid: uid_t, gid: gid_t): CInt =
    extern
  def fdatasync(fd: CInt): CInt = extern
  def fexecve(fd: CInt, argv: Ptr[CString], envp: Ptr[CString]): CInt =
    extern
  def fork(): CInt                                 = extern
  def fpathconf(fd: CInt, name: CInt): CLong       = extern
  def fsync(fildes: CInt): CInt                    = extern
  def ftruncate(fildes: CInt, length: off_t): CInt = extern

  def getcwd(buf: Ptr[CChar], size: size_t): CString                    = extern
  def getegid(): gid_t                                                  = extern
  def geteuid(): uid_t                                                  = extern
  def getgid(): gid_t                                                   = extern
  def getgroups(size: CInt, list: Ptr[gid_t]): CInt                     = extern
  def gethostid(): CLong                                                = extern
  def gethostname(name: CString, len: size_t): CInt                     = extern
  def getlogin(): CString                                               = extern
  def getlogin_r(buf: Ptr[CChar], bufsize: size_t): CInt                = extern
  def getopt(argc: CInt, argv: Ptr[CChar], optstring: Ptr[CChar]): CInt = extern
  def getpgid(pid: pid_t): pid_t                                        = extern
  def getpgrp(): pid_t                                                  = extern
  def getpid(): pid_t                                                   = extern
  def getppid(): pid_t                                                  = extern
  def getsid(pid: pid_t): pid_t                                         = extern
  def getuid(): uid_t                                                   = extern

  def isatty(fd: CInt): CInt = extern

  def lchown(pathname: CString, uid: uid_t, gid: gid_t): CInt = extern
  def link(path1: CString, path2: CString): CInt              = extern
  def linkat(fd1: CInt,
             path1: CString,
             fd2: CInt,
             path2: CString,
             flag: CInt): CInt                                = extern
  def lockf(fd: CInt, cmd: CInt, len: off_t): CInt            = extern
  def lseek(fildes: CInt, offset: off_t, whence: CInt): off_t = extern

  def nice(inc: CInt): CInt = extern

  def pathconf(path: CString, name: CInt): CLong = extern
  def pause(): CInt                              = extern
  def pipe(fildes: Ptr[CInt]): CInt              = extern
  def pread(fd: CInt, buf: Ptr[_], count: size_t, offset: off_t): ssize_t =
    extern
  def pwrite(fd: CInt, buf: Ptr[_], count: size_t, offset: off_t): ssize_t =
    extern

  def read(fildes: CInt, buf: Ptr[_], nbyte: size_t): ssize_t = extern
  def readlink(path: CString, buf: Ptr[CChar], bufsize: size_t): ssize_t =
    extern
  def readlinkat(dirfd: CInt,
                 pathname: CString,
                 buf: Ptr[CChar],
                 bufsize: size_t): ssize_t = extern
  def rmdir(pathname: CString): CInt       = extern

  def setegid(egid: gid_t): CInt                                = extern
  def seteuid(uid: uid_t): CInt                                 = extern
  def setgid(gid: gid_t): CInt                                  = extern
  def sethostname(name: CString, len: size_t): CInt             = extern
  def setpgid(pid: pid_t, pgid: pid_t): CInt                    = extern
  def setsid(): pid_t                                           = extern
  def setuid(uid: uid_t): pid_t                                 = extern
  def sleep(seconds: CUnsignedInt): CUnsignedInt                = extern
  def swab(from: Ptr[_], to: Ptr[_], n: ssize_t): Unit          = extern
  def symlink(path1: CString, path2: CString): CInt             = extern
  def symlinkat(path1: CString, fd: CInt, path2: CString): CInt = extern
  def sync(): Unit                                              = extern
  def sysconf(name: CInt): CLongInt                             = extern

  def tcgetpgrp(fd: CInt): pid_t                                 = extern
  def tcsetgrp(fd: CInt, pgrp: pid_t): CInt                      = extern
  def truncate(path: CString, length: off_t): CInt               = extern
  def ttyname(fd: CInt): CString                                 = extern
  def ttyname_r(fd: CInt, buf: Ptr[CChar], buflen: size_t): CInt = extern

  def unlink(path: CString): CInt                                 = extern
  def unlinkat(dirfd: CInt, pathname: CString, flags: CInt): CInt = extern
  def usleep(usec: useconds_t): CInt                              = extern

  def vfork(): pid_t = extern

  def write(fildes: CInt, buf: Ptr[_], nbyte: size_t): ssize_t = extern

  // "Constants" for Functions

  // access()
  @name("scalanative_f_ok")
  def F_OK: CInt = extern

  @name("scalanative_r_ok")
  def R_OK: CInt = extern

  @name("scalanative_w_ok")
  def W_OK: CInt = extern

  @name("scalanative_x_ok")
  def X_OK: CInt = extern

  // file streams
  @name("scalanative_stderr_fileno")
  def STDERR_FILENO: CInt = extern

  @name("scalanative_stdin_fileno")
  def STDIN_FILENO: CInt = extern

  @name("scalanative_stdout_fileno")
  def STDOUT_FILENO: CInt = extern

  // SEEK_CUR, SEEK_END, & SEEK_SET are visible because of
  // "import scalanative.libc.stdio._" above.

  /* // Work in Progress

// Symbolic constants for confstr()

_CS_PATH

_CS_POSIX_V7_ILP32_OFF32_CFLAGS

_CS_POSIX_V7_ILP32_OFF32_LDFLAGS

_CS_POSIX_V7_ILP32_OFF32_LIBS

_CS_POSIX_V7_ILP32_OFFBIG_CFLAGS

_CS_POSIX_V7_ILP32_OFFBIG_LDFLAGS

_CS_POSIX_V7_ILP32_OFFBIG_LIBS

_CS_POSIX_V7_LP64_OFF64_CFLAGS

_CS_POSIX_V7_LP64_OFF64_LDFLAGS

_CS_POSIX_V7_LP64_OFF64_LIBS

_CS_POSIX_V7_LPBIG_OFFBIG_CFLAGS

_CS_POSIX_V7_LPBIG_OFFBIG_LDFLAGS

_CS_POSIX_V7_LPBIG_OFFBIG_LIBS

_CS_POSIX_V7_THREADS_CFLAGS

_CS_POSIX_V7_THREADS_LDFLAGS

_CS_POSIX_V7_WIDTH_RESTRICTED_ENVS

_CS_V7_ENV


// Symbolic constants for lockf()

F_LOCK

F_TEST

F_TLOCK

F_ULOCK

------
// Symbolic constants for pathconf()

_PC_2_SYMLINKS
_PC_ALLOC_SIZE_MIN
_PC_ASYNC_IO
_PC_CHOWN_RESTRICTED
_PC_FILESIZEBITS
_PC_LINK_MAX
_PC_MAX_CANON
_PC_MAX_INPUT
_PC_NAME_MAX
_PC_NO_TRUNC
_PC_PATH_MAX
_PC_PIPE_BUF
_PC_PRIO_IO
_PC_REC_INCR_XFER_SIZE
_PC_REC_MAX_XFER_SIZE
_PC_REC_MIN_XFER_SIZE
_PC_REC_XFER_ALIGN
_PC_SYMLINK_MAX
_PC_SYNC_IO
_PC_TIMESTAMP_RESOLUTION
_PC_VDISABLE

-----
// Symbolic constants for sysconf():

_SC_2_C_BIND
_SC_2_C_DEV
_SC_2_CHAR_TERM
_SC_2_FORT_DEV
_SC_2_FORT_RUN
_SC_2_LOCALEDEF
_SC_2_PBS
_SC_2_PBS_ACCOUNTING
_SC_2_PBS_CHECKPOINT
_SC_2_PBS_LOCATE
_SC_2_PBS_MESSAGE
_SC_2_PBS_TRACK
_SC_2_SW_DEV
_SC_2_UPE
_SC_2_VERSION
_SC_ADVISORY_INFO
_SC_AIO_LISTIO_MAX
_SC_AIO_MAX
_SC_AIO_PRIO_DELTA_MAX
_SC_ARG_MAX
_SC_ASYNCHRONOUS_IO
_SC_ATEXIT_MAX
_SC_BARRIERS
_SC_BC_BASE_MAX
_SC_BC_DIM_MAX
_SC_BC_SCALE_MAX
_SC_BC_STRING_MAX
_SC_CHILD_MAX
_SC_CLK_TCK
_SC_CLOCK_SELECTION
_SC_COLL_WEIGHTS_MAX
_SC_CPUTIME
_SC_DELAYTIMER_MAX
_SC_EXPR_NEST_MAX
_SC_FSYNC
_SC_GETGR_R_SIZE_MAX
_SC_GETPW_R_SIZE_MAX
_SC_HOST_NAME_MAX
_SC_IOV_MAX
_SC_IPV6
_SC_JOB_CONTROL
_SC_LINE_MAX
_SC_LOGIN_NAME_MAX
_SC_MAPPED_FILES
_SC_MEMLOCK
_SC_MEMLOCK_RANGE
_SC_MEMORY_PROTECTION
_SC_MESSAGE_PASSING
_SC_MONOTONIC_CLOCK
_SC_MQ_OPEN_MAX
_SC_MQ_PRIO_MAX
_SC_NGROUPS_MAX
_SC_OPEN_MAX
_SC_PAGE_SIZE
_SC_PAGESIZE
_SC_PRIORITIZED_IO
_SC_PRIORITY_SCHEDULING
_SC_RAW_SOCKETS
_SC_RE_DUP_MAX
_SC_READER_WRITER_LOCKS
_SC_REALTIME_SIGNALS
_SC_REGEXP
_SC_RTSIG_MAX
_SC_SAVED_IDS
_SC_SEM_NSEMS_MAX
_SC_SEM_VALUE_MAX
_SC_SEMAPHORES
_SC_SHARED_MEMORY_OBJECTS
_SC_SHELL
_SC_SIGQUEUE_MAX
_SC_SPAWN
_SC_SPIN_LOCKS
_SC_SPORADIC_SERVER
_SC_SS_REPL_MAX
_SC_STREAM_MAX
_SC_SYMLOOP_MAX
_SC_SYNCHRONIZED_IO
_SC_THREAD_ATTR_STACKADDR
_SC_THREAD_ATTR_STACKSIZE
_SC_THREAD_CPUTIME
_SC_THREAD_DESTRUCTOR_ITERATIONS
_SC_THREAD_KEYS_MAX
_SC_THREAD_PRIO_INHERIT
_SC_THREAD_PRIO_PROTECT
_SC_THREAD_PRIORITY_SCHEDULING
_SC_THREAD_PROCESS_SHARED
_SC_THREAD_ROBUST_PRIO_INHERIT
_SC_THREAD_ROBUST_PRIO_PROTECT
_SC_THREAD_SAFE_FUNCTIONS
_SC_THREAD_SPORADIC_SERVER
_SC_THREAD_STACK_MIN
_SC_THREAD_THREADS_MAX
_SC_THREADS
_SC_TIMEOUTS
_SC_TIMER_MAX
_SC_TIMERS
_SC_TRACE
_SC_TRACE_EVENT_FILTER
_SC_TRACE_EVENT_NAME_MAX
_SC_TRACE_INHERIT
_SC_TRACE_LOG
_SC_TRACE_NAME_MAX
_SC_TRACE_SYS_MAX
_SC_TRACE_USER_EVENT_MAX
_SC_TTY_NAME_MAX
_SC_TYPED_MEMORY_OBJECTS
_SC_TZNAME_MAX
_SC_V7_ILP32_OFF32
_SC_V7_ILP32_OFFBIG
_SC_V7_LP64_OFF64
_SC_V7_LPBIG_OFFBIG
_SC_VERSION
_SC_XOPEN_CRYPT
_SC_XOPEN_ENH_I18N
_SC_XOPEN_REALTIME
_SC_XOPEN_REALTIME_THREADS
_SC_XOPEN_SHM
_SC_XOPEN_STREAMS
_SC_XOPEN_UNIX
_SC_XOPEN_UUCP
_SC_XOPEN_VERSION

   */ // Work in Progress

  // External Variables

  @name("scalanative_environ")
  def environ: Ptr[CString] = extern // GNU extension

  @name("scalanative_optarg")
  def optarg: Ptr[CString] = extern

  @name("scalanative_opterr")
  def opterr: CInt = extern

  @name("scalanative_optind")
  def optind: CInt = extern

  @name("scalanative_optopt")
  def optopt: CInt = extern

}
