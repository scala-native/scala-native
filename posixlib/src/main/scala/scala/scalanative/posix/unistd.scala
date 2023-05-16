package scala.scalanative
package posix

import scalanative.unsafe._
import scalanative.posix.sys.types

/** unistd.h for Scala
 *  @see
 *    [[https://scala-native.readthedocs.io/en/latest/lib/posixlib.html]]
 */
@extern
object unistd {

  type gid_t = types.gid_t
  type off_t = types.off_t
  type pid_t = types.pid_t
  type size_t = types.size_t
  type ssize_t = types.ssize_t
  type uid_t = types.uid_t

  /* The <unistd.h> header shall define the intptr_t type as
   * described in <stdint.h>.
   */
  type intptr_t = Ptr[CInt] // no stdint.scala yet, declare directly

// POSIX external variables (vals, not vars)

  @name("scalanative__posix_version")
  def _POSIX_VERSION: CLong = extern

  //  _POSIX2_VERSION, not implemented

  @name("scalanative__xopen_version")
  def _XOPEN_VERSION: CInt = extern

  var environ: Ptr[CString] = extern

  // optarg, opterr, optopt, and optopt are used by getopt().

  var optarg: CString = extern

  var opterr: CInt = extern

  var optind: CInt = extern

  var optopt: CInt = extern

// Methods/functions
  def access(pathname: CString, mode: CInt): CInt = extern
  def alarm(seconds: CUnsignedInt): CUnsignedInt = extern

  def chdir(path: CString): CInt = extern
  def chown(path: CString, owner: uid_t, group: gid_t): CInt = extern
  def close(fildes: CInt): CInt = extern
  def confstr(name: CInt, buf: Ptr[CChar], len: size_t): size_t = extern

  // XSI
  def crypt(phrase: CString, setting: CString): CString = extern

  def dup(fildes: CInt): CInt = extern
  def dup2(fildes: CInt, fildesnew: CInt): CInt = extern

  def _exit(status: CInt): Unit = extern

  // XSI
  def encrypt(block: Ptr[Byte], edflag: Int): Unit = extern

  def execl(pathname: CString, arg: CString, vargs: Any*): CInt = extern
  def execlp(file: CString, arg: CString, vargs: Any*): CInt = extern
  def execle(pathname: CString, arg: CString, vargs: Any*): CInt = extern
  def execv(pathname: CString, argv: Ptr[CString]): CInt = extern
  def execve(pathname: CString, argv: Ptr[CString], envp: Ptr[CString]): CInt =
    extern
  def execvp(file: CString, argv: Ptr[CString]): CInt = extern

  def faccessat(fd: CInt, path: CString, amode: CInt, flag: CInt): CInt = extern
  def fchdir(fildes: CInt): CInt = extern
  def fchown(filedes: CInt, owner: uid_t, group: gid_t): CInt = extern
  def fchownat(
      fd: CInt,
      path: CString,
      owner: uid_t,
      group: gid_t,
      flag: CInt
  ): CInt = extern

  // POSIX SIO
  @blocking
  def fdatasync(filedes: CInt): CInt = extern

  def fexecve(fd: CInt, argv: Ptr[CString], envp: Ptr[CString]): CInt = extern
  def fork(): pid_t = extern
  def fpathconf(fd: CInt, name: CInt): CLong = extern
  @blocking
  def fsync(fildes: CInt): CInt = extern
  @blocking
  def ftruncate(fildes: CInt, length: off_t): CInt = extern

  def getcwd(buf: CString, size: CSize): CString = extern
  def getegid(): gid_t = extern
  def geteuid(): uid_t = extern
  def getgid(): gid_t = extern
  def getgroups(size: CInt, list: Ptr[gid_t]): CInt = extern

  // XSI
  def gethostid(): CLong = extern

  def gethostname(name: CString, len: CSize): CInt = extern
  def getlogin(): CString = extern
  def getlogin_r(buf: Ptr[CChar], bufsize: CSize): CInt = extern
  def getopt(argc: CInt, argv: Ptr[CString], optstring: CString): CInt = extern
  def getpgid(pid: pid_t): pid_t = extern
  def getpgrp(): pid_t = extern
  def getpid(): pid_t = extern
  def getppid(): pid_t = extern
  def getsid(pid: pid_t): pid_t = extern;
  def getuid(): uid_t = extern

  def isatty(fd: CInt): CInt = extern

  def lchown(path: CString, owner: uid_t, group: gid_t): CInt = extern
  def link(path1: CString, path2: CString): CInt = extern
  def linkat(
      fd1: CInt,
      path1: CString,
      fd2: CInt,
      path2: CString,
      flag: CInt
  ): CInt = extern

  // XSI
  @blocking def lockf(fd: CInt, cmd: CInt, len: off_t): CInt = extern

  def lseek(fildes: CInt, offset: off_t, whence: CInt): off_t = extern

  // XSI
  def nice(inc: CInt): CInt = extern

  def pathconf(path: CString, name: CInt): CLong = extern
  @blocking
  def pause(): CInt = extern
  def pipe(fildes: Ptr[CInt]): CInt = extern
  @blocking
  def pread(fd: CInt, buf: Ptr[Byte], count: size_t, offset: off_t): ssize_t =
    extern
  @blocking
  def pwrite(fd: CInt, buf: Ptr[Byte], count: size_t, offset: off_t): ssize_t =
    extern

  @blocking
  def read(fildes: CInt, buf: Ptr[_], nbyte: CSize): CInt = extern
  def readlink(path: CString, buf: CString, bufsize: CSize): CInt = extern
  def readlinkat(
      dirfd: CInt,
      pathname: CString,
      buf: Ptr[CChar],
      bufsize: size_t
  ): ssize_t = extern
  def rmdir(pathname: CString): CInt = extern

  def setegid(egid: gid_t): CInt = extern
  def seteuid(euid: uid_t): CInt = extern
  def setgid(gid: gid_t): CInt = extern
  def setpgid(pid: pid_t, pgid: pid_t): CInt = extern

  // pid_t setpgrp(void); // [OB XSI], not implemented

// XSI
  def setregid(rgid: gid_t, egid: gid_t): CInt = extern
  def setreuid(ruid: gid_t, euid: gid_t): CInt = extern

  def setsid(): pid_t = extern
  def setuid(uid: uid_t): CInt = extern
  @blocking
  def sleep(seconds: CUnsignedInt): CUnsignedInt = extern

// XSI
  def swab(from: Ptr[Byte], to: Ptr[Byte], n: ssize_t): Unit = extern

  def symlink(path1: CString, path2: CString): CInt = extern
  def symlinkat(path1: CString, fd: CInt, path2: CString): CInt = extern

// XSI
  @blocking
  def sync(): Unit = extern

  def sysconf(name: CInt): CLong = extern

  @deprecated(
    "Not POSIX, subject to complete removal in the future.",
    since = "posixlib 0.5.0"
  )
  def sethostname(name: CString, len: CSize): CInt = extern

  def tcgetpgrp(fd: CInt): pid_t = extern
  def tcsetpgrp(fc: CInt, pgrp: pid_t): CInt = extern
  def truncate(path: CString, length: off_t): CInt = extern
  def ttyname(fd: CInt): CString = extern
  def ttyname_r(fd: CInt, buf: Ptr[CChar], buflen: size_t): CInt = extern

  def unlink(path: CString): CInt = extern
  def unlinkat(dirfd: CInt, pathname: CString, flags: CInt): CInt = extern

  // Maintainer: See 'Developer Note' in Issue #2395 about complete removal.
  @deprecated(
    "Removed in POSIX.1-2008. Use POSIX time.h nanosleep().",
    since = "posixlib 0.4.5"
  )
  @blocking def usleep(usecs: CUnsignedInt): CInt = extern

  @deprecated(
    "Removed in POSIX.1-2008. Consider posix_spawn().",
    "posixlib 0.5.0"
  )
  def vfork(): CInt = extern

  @blocking def write(fildes: CInt, buf: Ptr[_], nbyte: CSize): CInt = extern

// Symbolic constants

  // NULL, see POSIX stddef

  @name("scalanative_f_ok")
  def F_OK: CInt = extern

  @name("scalanative_r_ok")
  def R_OK: CInt = extern

  @name("scalanative_w_ok")
  def W_OK: CInt = extern

  @name("scalanative_x_ok")
  def X_OK: CInt = extern

  // SEEK_CUR, SEEK_END, SEEK_SET, use clib stdio c implementation.
  @name("scalanative_seek_cur")
  def SEEK_CUR: CInt = extern

  @name("scalanative_seek_end")
  def SEEK_END: CInt = extern

  @name("scalanative_seek_set")
  def SEEK_SET: CInt = extern

// lockf								       // XSI - Begin
  @name("scalanative_f_lock")
  def F_LOCK: CInt = extern

  @name("scalanative_f_test")
  def F_TEST: CInt = extern

  @name("scalanative_f_tlock")
  def F_TLOCK: CInt = extern

  @name("scalanative_f_ulock")
  def F_ULOCK: CInt = extern
// XSI - End

  /* stdin, stdout, stderr are runtime calls rather than 'final val'
   * inline constants because, at the time of writing, one could not mix
   * extern and 'normal' declarations in an extern object.
   */
  @name("scalanative_stderr_fileno")
  def STDERR_FILENO: CInt = extern

  @name("scalanative_stdin_fileno")
  def STDIN_FILENO: CInt = extern

  @name("scalanative_stdout_fileno")
  def STDOUT_FILENO: CInt = extern

  @name("scalanative__posix_vdisable")
  def _POSIX_VDISABLE: CInt = extern

  // confstr

  @name("scalanative__cs_path")
  def _CS_PATH: CInt = extern

  /* Not implemented, not defined on macOS.
   *    _CS_POSIX_V7_ILP32_OFF32_CFLAGS
   *    _CS_POSIX_V7_ILP32_OFF32_LDFLAGS:
   *    _CS_POSIX_V7_ILP32_OFF32_LIBS
   *    _CS_POSIX_V7_ILP32_OFFBIG_CFLAGS
   *    _CS_POSIX_V7_ILP32_OFFBIG_LDFLAGS
   *    _CS_POSIX_V7_ILP32_OFFBIG_LIBS
   *    _CS_POSIX_V7_LP64_OFF64_CFLAGS
   *    _CS_POSIX_V7_LP64_OFF64_LDFLAGS
   *    _CS_POSIX_V7_LP64_OFF64_LIBS
   *    _CS_POSIX_V7_LPBIG_OFFBIG_CFLAGS
   *    _CS_POSIX_V7_LPBIG_OFFBIG_LDFLAGS
   *    _CS_POSIX_V7_LPBIG_OFFBIG_LIBS
   */

  /* Not implemented, not defined on Linux & probably macOS
   *   _CS_POSIX_V7_THREADS_CFLAGS
   *   _CS_POSIX_V7_THREADS_LDFLAGS
   */

  /* Not implemented, not defined on macOS.
   *    _CS_POSIX_V7_WIDTH_RESTRICTED_ENVS
   *    _CS_V7_ENV
   */

  // pathconf

  @name("scalanative__pc_2_symlinks")
  def _PC_2_SYMLINKS: CInt = extern

  @name("scalanative__pc_alloc_size_min")
  def _PC_ALLOC_SIZE_MIN: CInt = extern

  @name("scalanative__pc_async_io")
  def _PC_ASYNC_IO: CInt = extern

  @name("scalanative__pc_chown_restricted")
  def _PC_CHOWN_RESTRICTED: CInt = extern

  @name("scalanative__pc_filesizebits")
  def _PC_FILESIZEBITS: CInt = extern

  @name("scalanative__pc_link_max")
  def _PC_LINK_MAX: CInt = extern

  @name("scalanative__pc_max_canon")
  def _PC_MAX_CANON: CInt = extern

  @name("scalanative__pc_max_input")
  def _PC_MAX_INPUT: CInt = extern

  @name("scalanative__pc_name_max")
  def _PC_NAME_MAX: CInt = extern

  @name("scalanative__pc_no_trunc")
  def _PC_NO_TRUNC: CInt = extern

  @name("scalanative__pc_path_max")
  def _PC_PATH_MAX: CInt = extern

  @name("scalanative__pc_pipe_buf")
  def _PC_PIPE_BUF: CInt = extern

  @name("scalanative__pc_prio_io")
  def _PC_PRIO_IO: CInt = extern

  @name("scalanative__pc_rec_incr_xfer_size")
  def _PC_REC_INCR_XFER_SIZE: CInt = extern

  @name("scalanative__pc_rec_max_xfer_size")
  def _PC_REC_MAX_XFER_SIZE: CInt = extern

  @name("scalanative__pc_rec_min_xfer_size")
  def _PC_REC_MIN_XFER_SIZE: CInt = extern

  @name("scalanative__pc_rec_xfer_align")
  def _PC_REC_XFER_ALIGN: CInt = extern

  @name("scalanative__pc_symlink_max")
  def _PC_SYMLINK_MAX: CInt = extern

  @name("scalanative__pc_sync_io")
  def _PC_SYNC_IO: CInt = extern

  /* Not implemented, not defined on Linux.
   *    _PC_TIMESTAMP_RESOLUTION
   */

  @name("scalanative__pc_vdisable")
  def _PC_VDISABLE: CInt = extern

// sysconf

  @name("scalanative__sc_2_c_bind")
  def _SC_2_C_BIND: CInt = extern

  @name("scalanative__sc_2_c_dev")
  def _SC_2_C_DEV: CInt = extern

  @name("scalanative__sc_2_char_term")
  def _SC_2_CHAR_TERM: CInt = extern

  @name("scalanative__sc_2_fort_dev")
  def _SC_2_FORT_DEV: CInt = extern

  @name("scalanative__sc_2_fort_run")
  def _SC_2_FORT_RUN: CInt = extern

  @name("scalanative__sc_2_localedef")
  def _SC_2_LOCALEDEF: CInt = extern

  @name("scalanative__sc_2_pbs")
  def _SC_2_PBS: CInt = extern

  @name("scalanative__sc_2_pbs_accounting")
  def _SC_2_PBS_ACCOUNTING: CInt = extern

  @name("scalanative__sc_2_pbs_checkpoint")
  def _SC_2_PBS_CHECKPOINT: CInt = extern

  @name("scalanative__sc_2_pbs_locate")
  def _SC_2_PBS_LOCATE: CInt = extern

  @name("scalanative__sc_2_pbs_message")
  def _SC_2_PBS_MESSAGE: CInt = extern

  @name("scalanative__sc_2_pbs_track")
  def _SC_2_PBS_TRACK: CInt = extern

  @name("scalanative__sc_2_sw_dev")
  def _SC_2_SW_DEV: CInt = extern

  @name("scalanative__sc_2_upe")
  def _SC_2_UPE: CInt = extern

  @name("scalanative__sc_2_version")
  def _SC_2_VERSION: CInt = extern

  @name("scalanative__sc_advisory_info")
  def _SC_ADVISORY_INFO: CInt = extern

  @name("scalanative__sc_aio_listio_max")
  def _SC_AIO_LISTIO_MAX: CInt = extern

  @name("scalanative__sc_aio_max")
  def _SC_AIO_MAX: CInt = extern

  @name("scalanative__sc_aio_prio_delta_max")
  def _SC_AIO_PRIO_DELTA_MAX: CInt = extern

  @name("scalanative__sc_arg_max")
  def _SC_ARG_MAX: CInt = extern

  @name("scalanative__sc_asynchronous_io")
  def _SC_ASYNCHRONOUS_IO: CInt = extern

  @name("scalanative__sc_atexit_max")
  def _SC_ATEXIT_MAX: CInt = extern

  @name("scalanative__sc_barriers")
  def _SC_BARRIERS: CInt = extern

  @name("scalanative__sc_bc_base_max")
  def _SC_BC_BASE_MAX: CInt = extern

  @name("scalanative__sc_bc_dim_max")
  def _SC_BC_DIM_MAX: CInt = extern

  @name("scalanative__sc_bc_scale_max")
  def _SC_BC_SCALE_MAX: CInt = extern

  @name("scalanative__sc_bc_string_max")
  def _SC_BC_STRING_MAX: CInt = extern

  @name("scalanative__sc_child_max")
  def _SC_CHILD_MAX: CInt = extern

  @name("scalanative__sc_clk_tck")
  def _SC_CLK_TCK: CInt = extern

  @name("scalanative__sc_clock_selection")
  def _SC_CLOCK_SELECTION: CInt = extern

  @name("scalanative__sc_coll_weights_max")
  def _SC_COLL_WEIGHTS_MAX: CInt = extern

  @name("scalanative__sc_cputime")
  def _SC_CPUTIME: CInt = extern

  @name("scalanative__sc_delaytimer_max")
  def _SC_DELAYTIMER_MAX: CInt = extern

  @name("scalanative__sc_expr_nest_max")
  def _SC_EXPR_NEST_MAX: CInt = extern

  @name("scalanative__sc_fsync")
  def _SC_FSYNC: CInt = extern

  @name("scalanative__sc_getgr_r_size_max")
  def _SC_GETGR_R_SIZE_MAX: CInt = extern

  @name("scalanative__sc_getpw_r_size_max")
  def _SC_GETPW_R_SIZE_MAX: CInt = extern

  @name("scalanative__sc_host_name_max")
  def _SC_HOST_NAME_MAX: CInt = extern

  @name("scalanative__sc_iov_max")
  def _SC_IOV_MAX: CInt = extern

  @name("scalanative__sc_ipv6")
  def _SC_IPV6: CInt = extern

  @name("scalanative__sc_job_control")
  def _SC_JOB_CONTROL: CInt = extern

  @name("scalanative__sc_line_max")
  def _SC_LINE_MAX: CInt = extern

  @name("scalanative__sc_login_name_max")
  def _SC_LOGIN_NAME_MAX: CInt = extern

  @name("scalanative__sc_mapped_files")
  def _SC_MAPPED_FILES: CInt = extern

  @name("scalanative__sc_memlock")
  def _SC_MEMLOCK: CInt = extern

  @name("scalanative__sc_memlock_range")
  def _SC_MEMLOCK_RANGE: CInt = extern

  @name("scalanative__sc_memory_protection")
  def _SC_MEMORY_PROTECTION: CInt = extern

  @name("scalanative__sc_message_passing")
  def _SC_MESSAGE_PASSING: CInt = extern

  @name("scalanative__sc_monotonic_clock")
  def _SC_MONOTONIC_CLOCK: CInt = extern

  @name("scalanative__sc_mq_open_max")
  def _SC_MQ_OPEN_MAX: CInt = extern

  @name("scalanative__sc_mq_prio_max")
  def _SC_MQ_PRIO_MAX: CInt = extern

  @name("scalanative__sc_ngroups_max")
  def _SC_NGROUPS_MAX: CInt = extern

  @name("scalanative__sc_nprocessors_conf")
  def _SC_NPROCESSORS_CONF: CInt = extern

  @name("scalanative__sc_nprocessors_onln")
  def _SC_NPROCESSORS_ONLN: CInt = extern

  @name("scalanative__sc_open_max")
  def _SC_OPEN_MAX: CInt = extern

  @name("scalanative__sc_page_size")
  def _SC_PAGE_SIZE: CInt = extern

  @name("scalanative__sc_pagesize")
  def _SC_PAGESIZE: CInt = extern

  @name("scalanative__sc_prioritized_io")
  def _SC_PRIORITIZED_IO: CInt = extern

  @name("scalanative__sc_priority_scheduling")
  def _SC_PRIORITY_SCHEDULING: CInt = extern

  @name("scalanative__sc_raw_sockets")
  def _SC_RAW_SOCKETS: CInt = extern

  @name("scalanative__sc_re_dup_max")
  def _SC_RE_DUP_MAX: CInt = extern

  @name("scalanative__sc_reader_writer_locks")
  def _SC_READER_WRITER_LOCKS: CInt = extern

  @name("scalanative__sc_realtime_signals")
  def _SC_REALTIME_SIGNALS: CInt = extern

  @name("scalanative__sc_regexp")
  def _SC_REGEXP: CInt = extern

  @name("scalanative__sc_rtsig_max")
  def _SC_RTSIG_MAX: CInt = extern

  @name("scalanative__sc_saved_ids")
  def _SC_SAVED_IDS: CInt = extern

  @name("scalanative__sc_sem_nsems_max")
  def _SC_SEM_NSEMS_MAX: CInt = extern

  @name("scalanative__sc_sem_value_max")
  def _SC_SEM_VALUE_MAX: CInt = extern

  @name("scalanative__sc_semaphores")
  def _SC_SEMAPHORES: CInt = extern

  @name("scalanative__sc_shared_memory_objects")
  def _SC_SHARED_MEMORY_OBJECTS: CInt = extern

  @name("scalanative__sc_shell")
  def _SC_SHELL: CInt = extern

  @name("scalanative__sc_sigqueue_max")
  def _SC_SIGQUEUE_MAX: CInt = extern

  @name("scalanative__sc_spawn")
  def _SC_SPAWN: CInt = extern

  @name("scalanative__sc_spin_locks")
  def _SC_SPIN_LOCKS: CInt = extern

  @name("scalanative__sc_sporadic_server")
  def _SC_SPORADIC_SERVER: CInt = extern

  @name("scalanative__sc_ss_repl_max")
  def _SC_SS_REPL_MAX: CInt = extern

  @name("scalanative__sc_stream_max")
  def _SC_STREAM_MAX: CInt = extern

  @name("scalanative__sc_symloop_max")
  def _SC_SYMLOOP_MAX: CInt = extern

  @name("scalanative__sc_synchronized_io")
  def _SC_SYNCHRONIZED_IO: CInt = extern

  @name("scalanative__sc_thread_attr_stackaddr")
  def _SC_THREAD_ATTR_STACKADDR: CInt = extern

  @name("scalanative__sc_thread_attr_stacksize")
  def _SC_THREAD_ATTR_STACKSIZE: CInt = extern

  @name("scalanative__sc_thread_cputime")
  def _SC_THREAD_CPUTIME: CInt = extern

  @name("scalanative__sc_thread_destructor_iterations")
  def _SC_THREAD_DESTRUCTOR_ITERATIONS: CInt = extern

  @name("scalanative__sc_thread_keys_max")
  def _SC_THREAD_KEYS_MAX: CInt = extern

  /* Not implemented, not defined on macOS.
   *    _SC_THREAD_PRIO_INHERIT
   *    _SC_THREAD_PRIO_PROTECT
   */

  @name("scalanative__sc_thread_priority_scheduling")
  def _SC_THREAD_PRIORITY_SCHEDULING: CInt = extern

  @name("scalanative__sc_thread_process_shared")
  def _SC_THREAD_PROCESS_SHARED: CInt = extern

  /* Not implemented, not defined on macOS.
   *    _SC_THREAD_ROBUST_PRIO_INHERIT
   *    _SC_THREAD_ROBUST_PRIO_PROTECT
   */

  @name("scalanative__sc_thread_safe_functions")
  def _SC_THREAD_SAFE_FUNCTIONS: CInt = extern

  @name("scalanative__sc_thread_sporadic_server")
  def _SC_THREAD_SPORADIC_SERVER: CInt = extern

  @name("scalanative__sc_thread_stack_min")
  def _SC_THREAD_STACK_MIN: CInt = extern

  @name("scalanative__sc_thread_threads_max")
  def _SC_THREAD_THREADS_MAX: CInt = extern

  @name("scalanative__sc_threads")
  def _SC_THREADS: CInt = extern

  @name("scalanative__sc_timeouts")
  def _SC_TIMEOUTS: CInt = extern

  @name("scalanative__sc_timer_max")
  def _SC_TIMER_MAX: CInt = extern

  @name("scalanative__sc_timers")
  def _SC_TIMERS: CInt = extern

  @name("scalanative__sc_trace")
  def _SC_TRACE: CInt = extern

  @name("scalanative__sc_trace_event_filter")
  def _SC_TRACE_EVENT_FILTER: CInt = extern

  @name("scalanative__sc_trace_event_name_max")
  def _SC_TRACE_EVENT_NAME_MAX: CInt = extern

  @name("scalanative__sc_trace_inherit")
  def _SC_TRACE_INHERIT: CInt = extern

  @name("scalanative__sc_trace_log")
  def _SC_TRACE_LOG: CInt = extern

  @name("scalanative__sc_trace_name_max")
  def _SC_TRACE_NAME_MAX: CInt = extern

  @name("scalanative__sc_trace_sys_max")
  def _SC_TRACE_SYS_MAX: CInt = extern

  @name("scalanative__sc_trace_user_event_max")
  def _SC_TRACE_USER_EVENT_MAX: CInt = extern

  @name("scalanative__sc_tty_name_max")
  def _SC_TTY_NAME_MAX: CInt = extern

  @name("scalanative__sc_typed_memory_objects")
  def _SC_TYPED_MEMORY_OBJECTS: CInt = extern

  @name("scalanative__sc_tzname_max")
  def _SC_TZNAME_MAX: CInt = extern

  /* Not implemented, not defined on macOS.
   *    _SC_V7_ILP32_OFF32
   *    _SC_V7_ILP32_OFFBIG
   *    _SC_V7_LP64_OFF64
   *    _SC_V7_LPBIG_OFFBIG
   */

  @name("scalanative__sc_version")
  def _SC_VERSION: CInt = extern

  @name("scalanative__sc_xopen_crypt")
  def _SC_XOPEN_CRYPT: CInt = extern

  @name("scalanative__sc_xopen_enh_i18n")
  def _SC_XOPEN_ENH_I18N: CInt = extern

  @name("scalanative__sc_xopen_realtime")
  def _SC_XOPEN_REALTIME: CInt = extern

  @name("scalanative__sc_xopen_realtime_threads")
  def _SC_XOPEN_REALTIME_THREADS: CInt = extern

  @name("scalanative__sc_xopen_shm")
  def _SC_XOPEN_SHM: CInt = extern

  @name("scalanative__sc_xopen_streams")
  def _SC_XOPEN_STREAMS: CInt = extern

  @name("scalanative__sc_xopen_unix")
  def _SC_XOPEN_UNIX: CInt = extern

  /* Not implemented, not defined on Linux.
   *    _SC_XOPEN_UUCP
   */

  @name("scalanative__sc_xopen_version")
  def _SC_XOPEN_VERSION: CInt = extern

}
