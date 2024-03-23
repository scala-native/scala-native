#if defined(__SCALANATIVE_POSIX_UNISTD)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

// #define _POSIX_C_SOURCE 2 // constr
// #define _X_OPEN // constr

#include <unistd.h>
#include "types.h" // scalanative_* types, not <sys/types.h>

#if defined(__FreeBSD__) || defined(__OpenBSD__) || defined(__NetBSD__)

/* Apply a Pareto cost/benefit analysis here.
 *
 * Some relevant constants are not defined on FreeBSD.
 * This implementation is one of at least 3 design possibilities. One can:
 *   1) cause a runtime or semantic error by returning "known wrong" values
 *      as done here. This causes only the parts of applications which
 *      actually use the constants to, hopefully, fail.
 *
 *   2) cause a link time error.
 *
 *   3) cause a compile time error.
 *
 * The last ensure that no wrong constants slip out to a user but they also
 * prevent an application developer from getting the parts of an application
 * which do not actually use the constants from running.
 */
#define _XOPEN_VERSION 0
#define _PC_2_SYMLINKS 0
#define _SC_SS_REPL_MAX 0
#define _SC_TRACE_EVENT_NAME_MAX 0
#define _SC_TRACE_NAME_MAX 0
#define _SC_TRACE_SYS_MAX 0
#define _SC_TRACE_USER_EVENT_MAX 0
#endif // __FreeBSD__ || __OpenBSD__ || __NetBSD__

long scalanative__posix_version() { return _POSIX_VERSION; }

int scalanative__xopen_version() { return _XOPEN_VERSION; }

int scalanative_f_ok() { return F_OK; }

int scalanative_r_ok() { return R_OK; }

int scalanative_w_ok() { return W_OK; }

int scalanative_x_ok() { return X_OK; }

// SEEK_CUR, SEEK_END, SEEK_SET implementations are in clib stdio.c

// lockf

// XSI - Begin
int scalanative_f_lock() { return F_LOCK; }

int scalanative_f_test() { return F_TEST; }

int scalanative_f_tlock() { return F_TLOCK; }

int scalanative_f_ulock() { return F_ULOCK; }
// XSI - End

int scalanative_stdin_fileno() { return STDIN_FILENO; }

int scalanative_stdout_fileno() { return STDOUT_FILENO; }

int scalanative_stderr_fileno() { return STDERR_FILENO; }

int scalanative__posix_vdisable() { return _POSIX_VDISABLE; }

// confstr

int scalanative__cs_path() { return _CS_PATH; };

/* Not implemented, not defined on macOS.
 *	_CS_POSIX_V7_ILP32_OFF32_CFLAGS
 *	_CS_POSIX_V7_ILP32_OFF32_LDFLAGS:
 *	_CS_POSIX_V7_ILP32_OFF32_LIBS
 *	_CS_POSIX_V7_ILP32_OFFBIG_CFLAGS
 *	_CS_POSIX_V7_ILP32_OFFBIG_LDFLAGS
 *	_CS_POSIX_V7_ILP32_OFFBIG_LIBS
 *	_CS_POSIX_V7_LP64_OFF64_CFLAGS
 *	_CS_POSIX_V7_LP64_OFF64_LDFLAGS
 *	_CS_POSIX_V7_LP64_OFF64_LIBS
 *	_CS_POSIX_V7_LPBIG_OFFBIG_CFLAGS
 *	_CS_POSIX_V7_LPBIG_OFFBIG_LDFLAGS
 *	_CS_POSIX_V7_LPBIG_OFFBIG_LIBS
 */

/* Not implemented, not defined on Linux & probably macOS
 *   _CS_POSIX_V7_THREADS_CFLAGS
 *   _CS_POSIX_V7_THREADS_LDFLAGS
 */

/* Not implemented, not defined on macOS.
 *	_CS_POSIX_V7_WIDTH_RESTRICTED_ENVS
 *	_CS_V7_ENV
 */

// pathconf

int scalanative__pc_2_symlinks() { return _PC_2_SYMLINKS; };

int scalanative__pc_alloc_size_min() {
#ifdef _PC_ALLOC_SIZE_MIN
    return _PC_ALLOC_SIZE_MIN;
#else
    return 0;
#endif
};

int scalanative__pc_async_io() {
#ifdef _PC_ASYNC_IO
    return _PC_ASYNC_IO;
#else
    return 0;
#endif
};

int scalanative__pc_chown_restricted() { return _PC_CHOWN_RESTRICTED; };

int scalanative__pc_filesizebits() { return _PC_FILESIZEBITS; };

int scalanative__pc_link_max() { return _PC_LINK_MAX; };

int scalanative__pc_max_canon() { return _PC_MAX_CANON; };

int scalanative__pc_max_input() { return _PC_MAX_INPUT; };

int scalanative__pc_name_max() { return _PC_NAME_MAX; };

int scalanative__pc_no_trunc() { return _PC_NO_TRUNC; };

int scalanative__pc_path_max() { return _PC_PATH_MAX; };

int scalanative__pc_pipe_buf() { return _PC_PIPE_BUF; };

int scalanative__pc_prio_io() {
#ifdef _PC_PRIO_IO
    return _PC_PRIO_IO;
#else
    return 0;
#endif
};

int scalanative__pc_rec_incr_xfer_size() {
#ifdef _PC_REC_INCR_XFER_SIZE
    return _PC_REC_INCR_XFER_SIZE;
#else
    return 0;
#endif
};

int scalanative__pc_rec_max_xfer_size() {
#ifdef _PC_REC_MAX_XFER_SIZE
    return _PC_REC_MAX_XFER_SIZE;
#else
    return 0;
#endif
};

int scalanative__pc_rec_min_xfer_size() {
#ifdef _PC_REC_MIN_XFER_SIZE
    return _PC_REC_MIN_XFER_SIZE;
#else
    return 0;
#endif
};

int scalanative__pc_rec_xfer_align() {
#ifdef _PC_REC_XFER_ALIGN
    return _PC_REC_XFER_ALIGN;
#else
    return 0;
#endif
};

int scalanative__pc_symlink_max() { return _PC_SYMLINK_MAX; };

int scalanative__pc_sync_io() { return _PC_SYNC_IO; };

/* Not implemented, not defined on Linux.
 *   _PC_TIMESTAMP_RESOLUTION
 */

int scalanative__pc_vdisable() { return _PC_VDISABLE; };

// sysconf

int scalanative__sc_2_c_bind() { return _SC_2_C_BIND; };

int scalanative__sc_2_c_dev() { return _SC_2_C_DEV; };

int scalanative__sc_2_char_term() { return _SC_2_CHAR_TERM; };

int scalanative__sc_2_fort_dev() { return _SC_2_FORT_DEV; };

int scalanative__sc_2_fort_run() { return _SC_2_FORT_RUN; };

int scalanative__sc_2_localedef() { return _SC_2_LOCALEDEF; };

int scalanative__sc_2_pbs() { return _SC_2_PBS; };

int scalanative__sc_2_pbs_accounting() { return _SC_2_PBS_ACCOUNTING; };

int scalanative__sc_2_pbs_checkpoint() { return _SC_2_PBS_CHECKPOINT; };

int scalanative__sc_2_pbs_locate() { return _SC_2_PBS_LOCATE; };

int scalanative__sc_2_pbs_message() { return _SC_2_PBS_MESSAGE; };

int scalanative__sc_2_pbs_track() { return _SC_2_PBS_TRACK; };

int scalanative__sc_2_sw_dev() { return _SC_2_SW_DEV; };

int scalanative__sc_2_upe() { return _SC_2_UPE; };

int scalanative__sc_2_version() { return _SC_2_VERSION; };

int scalanative__sc_advisory_info() {
#ifdef _SC_ADVISORY_INFO
    return _SC_ADVISORY_INFO;
#else
    return 0;
#endif
};

int scalanative__sc_aio_listio_max() { return _SC_AIO_LISTIO_MAX; };

int scalanative__sc_aio_max() { return _SC_AIO_MAX; };

int scalanative__sc_aio_prio_delta_max() {
#ifdef _SC_AIO_PRIO_DELTA_MAX
    return _SC_AIO_PRIO_DELTA_MAX;
#else
    return 0;
#endif
};

int scalanative__sc_arg_max() { return _SC_ARG_MAX; };

int scalanative__sc_asynchronous_io() { return _SC_ASYNCHRONOUS_IO; };

int scalanative__sc_atexit_max() { return _SC_ATEXIT_MAX; };

int scalanative__sc_barriers() { return _SC_BARRIERS; };

int scalanative__sc_bc_base_max() { return _SC_BC_BASE_MAX; };

int scalanative__sc_bc_dim_max() { return _SC_BC_DIM_MAX; };

int scalanative__sc_bc_scale_max() { return _SC_BC_SCALE_MAX; };

int scalanative__sc_bc_string_max() { return _SC_BC_STRING_MAX; };

int scalanative__sc_child_max() { return _SC_CHILD_MAX; };

int scalanative__sc_clk_tck() { return _SC_CLK_TCK; };

int scalanative__sc_clock_selection() { return _SC_CLOCK_SELECTION; };

int scalanative__sc_coll_weights_max() { return _SC_COLL_WEIGHTS_MAX; };

int scalanative__sc_cputime() { return _SC_CPUTIME; };

int scalanative__sc_delaytimer_max() { return _SC_DELAYTIMER_MAX; };

int scalanative__sc_expr_nest_max() { return _SC_EXPR_NEST_MAX; };

int scalanative__sc_fsync() { return _SC_FSYNC; };

int scalanative__sc_getgr_r_size_max() { return _SC_GETGR_R_SIZE_MAX; };

int scalanative__sc_getpw_r_size_max() { return _SC_GETPW_R_SIZE_MAX; };

int scalanative__sc_host_name_max() { return _SC_HOST_NAME_MAX; };

int scalanative__sc_iov_max() { return _SC_IOV_MAX; };

int scalanative__sc_ipv6() {
#ifdef _SC_IPV6
    return _SC_IPV6;
#else
    return 0;
#endif
};

int scalanative__sc_job_control() { return _SC_JOB_CONTROL; };

int scalanative__sc_line_max() { return _SC_LINE_MAX; };

int scalanative__sc_login_name_max() { return _SC_LOGIN_NAME_MAX; };

int scalanative__sc_mapped_files() { return _SC_MAPPED_FILES; };

int scalanative__sc_memlock() { return _SC_MEMLOCK; };

int scalanative__sc_memlock_range() { return _SC_MEMLOCK_RANGE; };

int scalanative__sc_memory_protection() { return _SC_MEMORY_PROTECTION; };

int scalanative__sc_message_passing() { return _SC_MESSAGE_PASSING; };

int scalanative__sc_monotonic_clock() { return _SC_MONOTONIC_CLOCK; };

int scalanative__sc_mq_open_max() { return _SC_MQ_OPEN_MAX; };

int scalanative__sc_mq_prio_max() { return _SC_MQ_PRIO_MAX; };

int scalanative__sc_ngroups_max() { return _SC_NGROUPS_MAX; };

int scalanative__sc_nprocessors_conf() { return _SC_NPROCESSORS_CONF; }

int scalanative__sc_nprocessors_onln() { return _SC_NPROCESSORS_ONLN; }

int scalanative__sc_open_max() { return _SC_OPEN_MAX; };

int scalanative__sc_page_size() { return _SC_PAGE_SIZE; };

int scalanative__sc_pagesize() { return _SC_PAGESIZE; };

int scalanative__sc_prioritized_io() {
#ifdef _SC_PRIORITIZED_IO
    return _SC_PRIORITIZED_IO;
#else
    return 0;
#endif
};

int scalanative__sc_priority_scheduling() { return _SC_PRIORITY_SCHEDULING; };

int scalanative__sc_raw_sockets() {
#ifdef _SC_RAW_SOCKETS
    return _SC_RAW_SOCKETS;
#else
    return 0;
#endif
};

int scalanative__sc_re_dup_max() { return _SC_RE_DUP_MAX; };

int scalanative__sc_reader_writer_locks() { return _SC_READER_WRITER_LOCKS; };

int scalanative__sc_realtime_signals() { return _SC_REALTIME_SIGNALS; };

int scalanative__sc_regexp() { return _SC_REGEXP; };

int scalanative__sc_rtsig_max() {
#ifdef _SC_RTSIG_MAX
    return _SC_RTSIG_MAX;
#else
    return 0;
#endif
};

int scalanative__sc_saved_ids() { return _SC_SAVED_IDS; };

int scalanative__sc_sem_nsems_max() { return _SC_SEM_NSEMS_MAX; };

int scalanative__sc_sem_value_max() {
#ifdef _SC_SEM_VALUE_MAX
    return _SC_SEM_VALUE_MAX;
#else
    return 0;
#endif
};

int scalanative__sc_semaphores() { return _SC_SEMAPHORES; };

int scalanative__sc_shared_memory_objects() {
    return _SC_SHARED_MEMORY_OBJECTS;
};

int scalanative__sc_shell() { return _SC_SHELL; };

int scalanative__sc_sigqueue_max() { return _SC_SIGQUEUE_MAX; };

int scalanative__sc_spawn() { return _SC_SPAWN; };

int scalanative__sc_spin_locks() { return _SC_SPIN_LOCKS; };

int scalanative__sc_sporadic_server() {
#ifdef _SC_SPORADIC_SERVER
    return _SC_SPORADIC_SERVER;
#else
    return 0;
#endif
};

int scalanative__sc_ss_repl_max() { return _SC_SS_REPL_MAX; };

int scalanative__sc_stream_max() { return _SC_STREAM_MAX; };

int scalanative__sc_symloop_max() { return _SC_SYMLOOP_MAX; };

int scalanative__sc_synchronized_io() { return _SC_SYNCHRONIZED_IO; };

int scalanative__sc_thread_attr_stackaddr() {
    return _SC_THREAD_ATTR_STACKADDR;
};

int scalanative__sc_thread_attr_stacksize() {
    return _SC_THREAD_ATTR_STACKSIZE;
};

int scalanative__sc_thread_cputime() { return _SC_THREAD_CPUTIME; };

int scalanative__sc_thread_destructor_iterations() {
    return _SC_THREAD_DESTRUCTOR_ITERATIONS;
};

int scalanative__sc_thread_keys_max() { return _SC_THREAD_KEYS_MAX; };

/* Not implemented, not defined on macOS.
 *	_SC_THREAD_PRIO_INHERIT
 *	_SC_THREAD_PRIO_PROTECT
 */

int scalanative__sc_thread_priority_scheduling() {
    return _SC_THREAD_PRIORITY_SCHEDULING;
};

int scalanative__sc_thread_process_shared() {
    return _SC_THREAD_PROCESS_SHARED;
};

/* Not implemented, not defined on macOS.
 *	_SC_THREAD_ROBUST_PRIO_INHERIT
 *	_SC_THREAD_ROBUST_PRIO_PROTECT
 */

int scalanative__sc_thread_safe_functions() {
    return _SC_THREAD_SAFE_FUNCTIONS;
};

int scalanative__sc_thread_sporadic_server() {
#ifdef _SC_THREAD_SPORADIC_SERVER
    return _SC_THREAD_SPORADIC_SERVER;
#else
    return 0;
#endif
};

int scalanative__sc_thread_stack_min() { return _SC_THREAD_STACK_MIN; };

int scalanative__sc_thread_threads_max() { return _SC_THREAD_THREADS_MAX; };

int scalanative__sc_threads() { return _SC_THREADS; };

int scalanative__sc_timeouts() {
#ifdef _SC_TIMEOUTS
    return _SC_TIMEOUTS;
#else
    return 0;
#endif
};

int scalanative__sc_timer_max() { return _SC_TIMER_MAX; };

int scalanative__sc_timers() { return _SC_TIMERS; };

int scalanative__sc_trace() {
#ifdef _SC_TRACE
    return _SC_TRACE;
#else
    return 0;
#endif
};

int scalanative__sc_trace_event_filter() {
#ifdef _SC_TRACE_EVENT_FILTER
    return _SC_TRACE_EVENT_FILTER;
#else
    return 0;
#endif
};

int scalanative__sc_trace_event_name_max() { return _SC_TRACE_EVENT_NAME_MAX; };

int scalanative__sc_trace_inherit() {
#ifdef _SC_TRACE_INHERIT
    return _SC_TRACE_INHERIT;
#else
    return 0;
#endif
};

int scalanative__sc_trace_log() {
#ifdef _SC_TRACE_LOG
    return _SC_TRACE_LOG;
#else
    return 0;
#endif
};

int scalanative__sc_trace_name_max() { return _SC_TRACE_NAME_MAX; };

int scalanative__sc_trace_sys_max() { return _SC_TRACE_SYS_MAX; };

int scalanative__sc_trace_user_event_max() { return _SC_TRACE_USER_EVENT_MAX; };

int scalanative__sc_tty_name_max() { return _SC_TTY_NAME_MAX; };

int scalanative__sc_typed_memory_objects() {
#ifdef _SC_TYPED_MEMORY_OBJECTS
    return _SC_TYPED_MEMORY_OBJECTS;
#else
    return 0;
#endif
};

int scalanative__sc_tzname_max() { return _SC_TZNAME_MAX; };

/* Not implemented, not defined on macOS.
 *	_SC_V7_ILP32_OFF32
 *	_SC_V7_ILP32_OFFBIG
 *	_SC_V7_LP64_OFF64
 *	_SC_V7_LPBIG_OFFBIG
 */

int scalanative__sc_version() { return _SC_VERSION; };

int scalanative__sc_xopen_crypt() {
#ifdef _SC_XOPEN_CRYPT
    return _SC_XOPEN_CRYPT;
#else
    return 0;
#endif
};

int scalanative__sc_xopen_enh_i18n() {
#ifdef _SC_XOPEN_ENH_I18N
    return _SC_XOPEN_ENH_I18N;
#else
    return 0;
#endif
};

int scalanative__sc_xopen_realtime() {
#ifdef _SC_XOPEN_REALTIME
    return _SC_XOPEN_REALTIME;
#else
    return 0;
#endif
};

int scalanative__sc_xopen_realtime_threads() {
#ifdef _SC_XOPEN_REALTIME_THREADS
    return _SC_XOPEN_REALTIME_THREADS;
#else
    return 0;
#endif
};

int scalanative__sc_xopen_shm() { return _SC_XOPEN_SHM; };

int scalanative__sc_xopen_streams() {
#ifdef _SC_XOPEN_STREAMS
    return _SC_XOPEN_STREAMS;
#else
    return 0;
#endif
};

int scalanative__sc_xopen_unix() {
#ifdef _SC_XOPEN_UNIX
    return _SC_XOPEN_UNIX;
#else
    return 0;
#endif
};

/* Not implemented, not defined on Linux.
 *	_SC_XOPEN_UUCP
 */

int scalanative__sc_xopen_version() {
#ifdef _SC_XOPEN_VERSION
    return _SC_XOPEN_VERSION;
#else
    return 0;
#endif
};

#endif // Unix or Mac OS
#endif