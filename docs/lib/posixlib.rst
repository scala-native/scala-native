.. _posixlib:

C POSIX Library
===============

Scala Native provides bindings for the core subset of the
`POSIX library <http://pubs.opengroup.org/onlinepubs/9699919799/idx/head.html>`_:

================= ==================================
C Header          Scala Native Module
================= ==================================
`aio.h`_          N/A
`arpa/inet.h`_    scala.scalanative.posix.arpa.inet_
`assert.h`_       N/A
`complex.h`_      N/A
`cpio.h`_         N/A
`ctype.h`_        N/A
`dirent.h`_       scala.scalanative.posix.dirent_
`dlfcn.h`_        N/A
`errno.h`_        scala.scalanative.posix.errno_
`fcntl.h`_        scala.scalanative.posix.fcntl_
`fenv.h`_         N/A
`float.h`_        N/A
`fmtmsg.h`_       N/A
`fnmatch.h`_      N/A
`ftw.h`_          N/A
`glob.h`_         N/A
`grp.h`_          scala.scalanative.posix.grp_
`iconv.h`_        N/A
`inttypes.h`_     scala.scalanative.posix.inttypes_
`iso646.h`_       N/A
`langinfo.h`_     N/A
`libgen.h`_       N/A
`limits.h`_       scala.scalanative.posix.limits_
`locale.h`_       N/A
`math.h`_         N/A
`monetary.h`_     N/A
`mqueue.h`_       N/A
`ndbm.h`_         N/A
`net/if.h`_       N/A
`netdb.h`_        N/A
`netinet/in.h`_   scala.scalanative.posix.netinet.in_
`netinet/tcp.h`_  N/A
`nl_types.h`_     N/A
`poll.h`_         N/A
`pthread.h`_      N/A
`pwd.h`_          scala.scalanative.posix.pwd_
`regex.h`_        scala.scalanative.posix.regex_
`sched.h`_        N/A
`search.h`_       N/A
`semaphore.h`_    N/A
`setjmp.h`_       N/A
`signal.h`_       N/A
`spawn.h`_        N/A
`stdarg.h`_       N/A
`stdbool.h`_      N/A
`stddef.h`_       N/A
`stdint.h`_       N/A
`stdio.h`_        N/A
`stdlib.h`_       N/A
`string.h`_       N/A
`strings.h`_      N/A
`stropts.h`_      N/A
`sys/ipc.h`_      N/A
`sys/mman.h`_     N/A
`sys/msg.h`_      N/A
`sys/resource.h`_ N/A
`sys/select.h`_   N/A
`sys/sem.h`_      N/A
`sys/shm.h`_      N/A
`sys/socket.h`_   scala.scalanative.posix.sys.socket_
`sys/stat.h`_     scala.scalanative.posix.sys.stat_
`sys/statvfs.h`_  N/A
`sys/time.h`_     scala.scalanative.posix.sys.time_
`sys/times.h`_    N/A
`sys/types.h`_    N/A
`sys/uio.h`_      scala.scalanative.posix.sys.uio_
`sys/un.h`_       N/A
`sys/utsname.h`_  N/A
`sys/wait.h`_     N/A
`syslog.h`_       N/A
`tar.h`_          N/A
`termios.h`_      N/A
`tgmath.h`_       N/A
`time.h`_         N/A
`trace.h`_        N/A
`ulimit.h`_       N/A
`unistd.h`_       scala.scalanative.posix.unistd_
`utime.h`_        scala.scalanative.posix.utime_
`utmpx.h`_        N/A
`wchar.h`_        N/A
`wctype.h`_       N/A
`wordexp.h`_      N/A
================= ==================================

.. _aio.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/aio.h.html
.. _arpa/inet.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/arpa_inet.h.html
.. _assert.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/assert.h.html
.. _complex.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/complex.h.html
.. _cpio.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/cpio.h.html
.. _ctype.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/ctype.h.html
.. _dirent.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/dirent.h.html
.. _dlfcn.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/dlfcn.h.html
.. _errno.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/errno.h.html
.. _fcntl.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fcntl.h.html
.. _fenv.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fenv.h.html
.. _float.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/float.h.html
.. _fmtmsg.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fmtmsg.h.html
.. _fnmatch.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fnmatch.h.html
.. _ftw.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/ftw.h.html
.. _glob.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/glob.h.html
.. _grp.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/grp.h.html
.. _iconv.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/iconv.h.html
.. _inttypes.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/inttypes.h.html
.. _iso646.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/iso646.h.html
.. _langinfo.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/langinfo.h.html
.. _libgen.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/libgen.h.html
.. _limits.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/limits.h.html
.. _locale.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/locale.h.html
.. _math.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/math.h.html
.. _monetary.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/monetary.h.html
.. _mqueue.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/mqueue.h.html
.. _ndbm.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/ndbm.h.html
.. _net/if.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/net_if.h.html
.. _netdb.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/netdb.h.html
.. _netinet/in.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/netinet_in.h.html
.. _netinet/tcp.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/netinet_tcp.h.html
.. _nl_types.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/nl_types.h.html
.. _poll.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/poll.h.html
.. _pthread.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/pthread.h.html
.. _pwd.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/pwd.h.html
.. _regex.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/regex.h.html
.. _sched.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sched.h.html
.. _search.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/search.h.html
.. _semaphore.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/semaphore.h.html
.. _setjmp.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/setjmp.h.html
.. _signal.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/signal.h.html
.. _spawn.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/spawn.h.html
.. _stdarg.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdarg.h.html
.. _stdbool.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdbool.h.html
.. _stddef.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stddef.h.html
.. _stdint.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdint.h.html
.. _stdio.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdio.h.html
.. _stdlib.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdlib.h.html
.. _string.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/string.h.html
.. _strings.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/strings.h.html
.. _stropts.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stropts.h.html
.. _sys/ipc.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_ipc.h.html
.. _sys/mman.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_mman.h.html
.. _sys/msg.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_msg.h.html
.. _sys/resource.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_resource.h.html
.. _sys/select.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_select.h.html
.. _sys/sem.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_sem.h.html
.. _sys/shm.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_shm.h.html
.. _sys/socket.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_socket.h.html
.. _sys/stat.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_stat.h.html
.. _sys/statvfs.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_statvfs.h.html
.. _sys/time.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_time.h.html
.. _sys/times.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_times.h.html
.. _sys/types.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_types.h.html
.. _sys/uio.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_uio.h.html
.. _sys/un.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_un.h.html
.. _sys/utsname.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_utsname.h.html
.. _sys/wait.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_wait.h.html
.. _syslog.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/syslog.h.html
.. _tar.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/tar.h.html
.. _termios.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/termios.h.html
.. _tgmath.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/tgmath.h.html
.. _time.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/time.h.html
.. _trace.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/trace.h.html
.. _ulimit.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/ulimit.h.html
.. _unistd.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/unistd.h.html
.. _utime.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/utime.h.html
.. _utmpx.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/utmpx.h.html
.. _wchar.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/wchar.h.html
.. _wctype.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/wctype.h.html
.. _wordexp.h: http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/wordexp.h.html

.. _scala.scalanative.posix.arpa.inet: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/arpa/inet.scala
.. _scala.scalanative.posix.dirent: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/dirent.scala
.. _scala.scalanative.posix.errno: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/errno.scala
.. _scala.scalanative.posix.fcntl: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/fcntl.scala
.. _scala.scalanative.posix.grp: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/grp.scala
.. _scala.scalanative.posix.inttypes: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/inttypes.scala
.. _scala.scalanative.posix.limits: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/limits.scala
.. _scala.scalanative.posix.netinet.in: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/netinet/in.scala
.. _scala.scalanative.posix.pwd: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/pwd.scala
.. _scala.scalanative.posix.regex: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/regex.scala
.. _scala.scalanative.posix.sys.socket: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/sys/socket.scala
.. _scala.scalanative.posix.sys.stat: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/sys/stat.scala
.. _scala.scalanative.posix.sys.time: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/sys/time.scala
.. _scala.scalanative.posix.sys.uio: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/sys/uio.scala
.. _scala.scalanative.posix.unistd: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/unistd.scala
.. _scala.scalanative.posix.utime: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/posix/utime.scala

Continue to :ref:`faq`.
