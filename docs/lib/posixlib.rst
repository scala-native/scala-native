.. _posixlib:

C POSIX Library
===============

Scala Native provides bindings for a core subset of the
`POSIX library <https://pubs.opengroup.org/onlinepubs/9699919799/idx/head.html>`_. See indicated source module for limitations, if any, and usage:

================= ==================================
C Header          Scala Native Module
================= ==================================
`aio.h`_          N/A - *indicates binding not available*
`arpa/inet.h`_    scala.scalanative.posix.arpa.inet_ [#inet_ntoa]_
`assert.h`_       N/A
`complex.h`_      scala.scalanative.posix.complex_
`cpio.h`_         scala.scalanative.posix.cpio_
`ctype.h`_        scala.scalanative.posix.ctype_
`dirent.h`_       scala.scalanative.posix.dirent_
`dlfcn.h`_        scala.scalanative.posix.dlfcn_
`errno.h`_        scala.scalanative.posix.errno_
`fcntl.h`_        scala.scalanative.posix.fcntl_
`fenv.h`_         scala.scalanative.posix.fenv_
`float.h`_        scala.scalanative.posix.float_
`fmtmsg.h`_       N/A
`fnmatch.h`_      scala.scalanative.posix.fnmatch_
`ftw.h`_          N/A
`getopt.h`_       scala.scalanative.posix.getopt_
`glob.h`_         scala.scalanative.posix.glob_
`grp.h`_          scala.scalanative.posix.grp_
`iconv.h`_        N/A
`inttypes.h`_     scala.scalanative.posix.inttypes_
`iso646.h`_       N/A
`langinfo.h`_     scala.scalanative.posix.langinfo_
`libgen.h`_       scala.scalanative.posix.libgen_
`limits.h`_       scala.scalanative.posix.limits_
`locale.h`_       scala.scalanative.posix.locale_
`math.h`_         scala.scalanative.posix.math_
`monetary.h`_     scala.scalanative.posix.monetary_ [#monetary_varargs]_
`mqueue.h`_       N/A
`ndbm.h`_         N/A
`net/if.h`_       scala.scalanative.posix.net.if_
`netdb.h`_        scala.scalanative.posix.netdb_
`netinet/in.h`_   scala.scalanative.posix.netinet.in_
`netinet/tcp.h`_  scala.scalanative.posix.netinet.tcp_
`nl_types.h`_     scala.scalanative.posix.nl_types_
`poll.h`_         scala.scalanative.posix.poll_
`pthread.h`_      scala.scalanative.posix.pthread_
`pwd.h`_          scala.scalanative.posix.pwd_
`regex.h`_        scala.scalanative.posix.regex_
`sched.h`_        scala.scalanative.posix.sched_
`search.h`_       N/A
`semaphore.h`_    N/A
`setjmp.h`_       N/A
`signal.h`_       scala.scalanative.posix.signal_
`spawn.h`_        scala.scalanative.posix.spawn_
`stdarg.h`_       N/A
`stdbool.h`_      N/A
`stddef.h`_       scala.scalanative.posix.stddef_
`stdint.h`_       scala.scalanative.posix.stdint_
`stdio.h`_        scala.scalanative.posix.stdio_
`stdlib.h`_       scala.scalanative.posix.stdlib_
`string.h`_       scala.scalanative.posix.string_
`strings.h`_      scala.scalanative.posix.strings_
`sys/ipc.h`_      N/A
`sys/mman.h`_     scala.scalanative.posix.sys.mman_
`sys/msg.h`_      N/A
`sys/resource.h`_ scala.scalanative.posix.sys.resource_
`sys/select.h`_   scala.scalanative.posix.sys.select_
`sys/sem.h`_      N/A
`sys/shm.h`_      N/A
`sys/socket.h`_   scala.scalanative.posix.sys.socket_
`sys/stat.h`_     scala.scalanative.posix.sys.stat_
`sys/statvfs.h`_  scala.scalanative.posix.sys.statvfs_
`sys/time.h`_     scala.scalanative.posix.sys.time_
`sys/times.h`_    scala.scalanative.posix.sys.times_
`sys/types.h`_    scala.scalanative.posix.sys.types_
`sys/uio.h`_      scala.scalanative.posix.sys.uio_
`sys/un.h`_       scala.scalanative.posix.sys.un_
`sys/utsname.h`_  scala.scalanative.posix.sys.utsname_
`sys/wait.h`_     scala.scalanative.posix.sys.wait_
`syslog.h`_       scala.scalanative.posix.syslog_
`tar.h`_          N/A
`termios.h`_      scala.scalanative.posix.termios_
`tgmath.h`_       scala.scalanative.posix.tgmath_
`time.h`_         scala.scalanative.posix.time_
`trace.h`_        N/A
`unistd.h`_       scala.scalanative.posix.unistd_
`utime.h`_        scala.scalanative.posix.utime_
`utmpx.h`_        N/A
`wchar.h`_        scala.scalanative.posix.wchar_
`wctype.h`_       N/A
`wordexp.h`_      scala.scalanative.posix.wordexp_
================= ==================================

.. _aio.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/aio.h.html
.. _arpa/inet.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/arpa_inet.h.html
.. _assert.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/assert.h.html
.. _complex.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/complex.h.html
.. _cpio.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/cpio.h.html
.. _ctype.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/ctype.h.html
.. _dirent.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/dirent.h.html
.. _dlfcn.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/dlfcn.h.html
.. _errno.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/errno.h.html
.. _fcntl.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fcntl.h.html
.. _fenv.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fenv.h.html
.. _float.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/float.h.html
.. _fmtmsg.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fmtmsg.h.html
.. _fnmatch.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fnmatch.h.html
.. _ftw.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/ftw.h.html
.. _getopt.h: https://pubs.opengroup.org/onlinepubs/9699919799/functions/getopt.html
.. _glob.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/glob.h.html
.. _grp.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/grp.h.html
.. _iconv.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/iconv.h.html
.. _inttypes.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/inttypes.h.html
.. _iso646.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/iso646.h.html
.. _langinfo.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/langinfo.h.html
.. _libgen.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/libgen.h.html
.. _limits.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/limits.h.html
.. _locale.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/locale.h.html
.. _math.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/math.h.html
.. _monetary.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/monetary.h.html
.. _mqueue.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/mqueue.h.html
.. _ndbm.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/ndbm.h.html
.. _net/if.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/net_if.h.html
.. _netdb.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/netdb.h.html
.. _netinet/in.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/netinet_in.h.html
.. _netinet/tcp.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/netinet_tcp.h.html
.. _nl_types.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/nl_types.h.html
.. _poll.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/poll.h.html
.. _pthread.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/pthread.h.html
.. _pwd.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/pwd.h.html
.. _regex.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/regex.h.html
.. _sched.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sched.h.html
.. _search.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/search.h.html
.. _semaphore.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/semaphore.h.html
.. _setjmp.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/setjmp.h.html
.. _signal.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/signal.h.html
.. _spawn.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/spawn.h.html
.. _stdarg.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdarg.h.html
.. _stdbool.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdbool.h.html
.. _stddef.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stddef.h.html
.. _stdint.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdint.h.html
.. _stdio.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdio.h.html
.. _stdlib.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdlib.h.html
.. _string.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/string.h.html
.. _strings.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/strings.h.html
.. _sys/ipc.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_ipc.h.html
.. _sys/mman.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_mman.h.html
.. _sys/msg.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_msg.h.html
.. _sys/resource.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_resource.h.html
.. _sys/select.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_select.h.html
.. _sys/sem.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_sem.h.html
.. _sys/shm.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_shm.h.html
.. _sys/socket.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_socket.h.html
.. _sys/stat.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_stat.h.html
.. _sys/statvfs.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_statvfs.h.html
.. _sys/tgmath.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_tgmath.h.html
.. _sys/time.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_time.h.html
.. _sys/times.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_times.h.html
.. _sys/types.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_types.h.html
.. _sys/uio.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_uio.h.html
.. _sys/un.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_un.h.html
.. _sys/utsname.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_utsname.h.html
.. _sys/wait.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_wait.h.html
.. _syslog.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/syslog.h.html
.. _tar.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/tar.h.html
.. _termios.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/termios.h.html
.. _tgmath.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/tgmath.h.html
.. _time.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/time.h.html
.. _trace.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/trace.h.html
.. _ulimit.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/ulimit.h.html
.. _unistd.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/unistd.h.html
.. _utime.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/utime.h.html
.. _utmpx.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/utmpx.h.html
.. _wchar.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/wchar.h.html
.. _wctype.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/wctype.h.html
.. _wordexp.h: https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/wordexp.h.html

.. _scala.scalanative.posix.arpa.inet: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/arpa/inet.scala
.. _scala.scalanative.posix.complex: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/complex.scala
.. _scala.scalanative.posix.ctype: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/ctype.scala
.. _scala.scalanative.posix.cpio: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/cpio.scala
.. _scala.scalanative.posix.dirent: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/dirent.scala
.. _scala.scalanative.posix.dlfcn: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/dlfcn.scala
.. _scala.scalanative.posix.errno: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/errno.scala
.. _scala.scalanative.posix.fcntl: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/fcntl.scala
.. _scala.scalanative.posix.fenv: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/fenv.scala
.. _scala.scalanative.posix.float: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/float.scala
.. _scala.scalanative.posix.fnmatch: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/fnmatch.scala
.. _scala.scalanative.posix.getopt: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/getopt.scala [#getopt_no_longer_posix_2018]_
.. _scala.scalanative.posix.glob: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/glob.scala
.. _scala.scalanative.posix.grp: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/grp.scala
.. _scala.scalanative.posix.inttypes: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/inttypes.scala
.. _scala.scalanative.posix.langinfo: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/langinfo.scala
.. _scala.scalanative.posix.limits: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/limits.scala
.. _scala.scalanative.posix.libgen: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/libgen.scala
.. _scala.scalanative.posix.locale: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/locale.scala
.. _scala.scalanative.posix.math: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/math.scala
.. _scala.scalanative.posix.monetary: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/monetaryh.scala
.. _scala.scalanative.posix.net.if: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/net/if.scala
.. _scala.scalanative.posix.netdb: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/netdb.scala
.. _scala.scalanative.posix.netinet.in: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/netinet/in.scala
.. _scala.scalanative.posix.netinet.tcp: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/netinet/tcp.scala
.. _scala.scalanative.posix.nl_types: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/nl_types.scala
.. _scala.scalanative.posix.poll: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/poll.scala
.. _scala.scalanative.posix.pthread: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/pthread.scala
.. _scala.scalanative.posix.pwd: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/pwd.scala
.. _scala.scalanative.posix.regex: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/regex.scala
.. _scala.scalanative.posix.sched: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sched.scala
.. _scala.scalanative.posix.signal: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/signal.scala
.. _scala.scalanative.posix.spawn: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/spawn.scala
.. _scala.scalanative.posix.stddef: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/stddef.scala
.. _scala.scalanative.posix.stdint: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/stdint.scala
.. _scala.scalanative.posix.stdio: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/stdio.scala
.. _scala.scalanative.posix.stdlib: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/stdlib.scala
.. _scala.scalanative.posix.string: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/string.scala
.. _scala.scalanative.posix.strings: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/strings.scala
.. _scala.scalanative.posix.sys.mman: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/mman.scala
.. _scala.scalanative.posix.sys.resource: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/resource.scala
.. _scala.scalanative.posix.sys.select: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/select.scala
.. _scala.scalanative.posix.sys.socket: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/socket.scala
.. _scala.scalanative.posix.sys.stat: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/stat.scala
.. _scala.scalanative.posix.sys.statvfs: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/statvfs.scala
.. _scala.scalanative.posix.sys.time: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/time.scala
.. _scala.scalanative.posix.sys.times: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/times.scala
.. _scala.scalanative.posix.sys.types: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/types.scala
.. _scala.scalanative.posix.sys.uio: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/uio.scala
.. _scala.scalanative.posix.sys.un: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/un.scala
.. _scala.scalanative.posix.sys.utsname: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/utsname.scala
.. _scala.scalanative.posix.sys.wait: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/wait.scala
.. _scala.scalanative.posix.syslog: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/syslog.scala
.. _scala.scalanative.posix.termios: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/termios.scala
.. _scala.scalanative.posix.tgmath: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/tgmath.scala
.. _scala.scalanative.posix.time: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/time.scala
.. _scala.scalanative.posix.unistd: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/unistd.scala
.. _scala.scalanative.posix.utime: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/utime.scala
.. _scala.scalanative.posix.wchar: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/wchar.scala
.. _scala.scalanative.posix.wordexp: https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/wordexp.scala

.. rubric Footnotes
.. [#inet_ntoa] The argument to inet_ntoa() differs from the POSIX
                specification because Scala Native supports only
                passing structures by reference.  See code for details
		and usage.
.. [#getopt_no_longer_posix_2018] getopt.scala, introduced in PR `Fix #202: Support assignment to extern variables #1348 <https://github.com/scala-native/scala-native/pull/1348/>`_ 
                                  is no longer part of POSIX 2018 and will be unified to unistd.scala in the future.
.. [#monetary_varargs] See file for limit on number of variable arguments.

Continue to :ref:`communitylib`.
