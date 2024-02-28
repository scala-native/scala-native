# C POSIX Library

Scala Native provides bindings for a core subset of the [POSIX
library](https://pubs.opengroup.org/onlinepubs/9699919799/idx/head.html).
See indicated source module for limitations, if any, and usage:

| C Header | Scala Native Module |
| -------- | ------------------- |
| [aio.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/aio.h.html)                    | N/A - *indicates binding not available* |
| [arpa/inet.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/arpa_inet.h.html)        | [scala.scalanative.posix.arpa.inet](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/arpa/inet.scala)[^1] |
| [arpa/inet.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/arpa_inet.h.html)        | [scala.scalanative.posix.arpa.inet](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/arpa/inet.scala)[^1] |
| [assert.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/assert.h.html)              | N/A |
| [assert.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/assert.h.html)              | N/A |
| [complex.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/complex.h.html)            | [scala.scalanative.posix.complex](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/complex.scala) |
| [complex.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/complex.h.html)            | [scala.scalanative.posix.complex](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/complex.scala) |
| [cpio.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/cpio.h.html)                  | [scala.scalanative.posix.cpio](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/cpio.scala) |
| [cpio.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/cpio.h.html)                  | [scala.scalanative.posix.cpio](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/cpio.scala) |
| [ctype.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/ctype.h.html)                | [scala.scalanative.posix.ctype](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/ctype.scala) |
| [ctype.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/ctype.h.html)                | [scala.scalanative.posix.ctype](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/ctype.scala) |
| [dirent.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/dirent.h.html)              | [scala.scalanative.posix.dirent](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/dirent.scala) |
| [dirent.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/dirent.h.html)              | [scala.scalanative.posix.dirent](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/dirent.scala) |
| [dlfcn.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/dlfcn.h.html)                | [scala.scalanative.posix.dlfcn](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/dlfcn.scala) |
| [dlfcn.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/dlfcn.h.html)                | [scala.scalanative.posix.dlfcn](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/dlfcn.scala) |
| [errno.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/errno.h.html)                | [scala.scalanative.posix.errno](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/errno.scala) |
| [errno.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/errno.h.html)                | [scala.scalanative.posix.errno](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/errno.scala) |
| [fcntl.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fcntl.h.html)                | [scala.scalanative.posix.fcntl](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/fcntl.scala) |
| [fcntl.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fcntl.h.html)                | [scala.scalanative.posix.fcntl](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/fcntl.scala) |
| [fenv.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fenv.h.html)                  | [scala.scalanative.posix.fenv](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/fenv.scala) |
| [fenv.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fenv.h.html)                  | [scala.scalanative.posix.fenv](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/fenv.scala) |
| [float.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/float.h.html)                | [scala.scalanative.posix.float](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/float.scala) |
| [float.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/float.h.html)                | [scala.scalanative.posix.float](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/float.scala) |
| [fmtmsg.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fmtmsg.h.html)              | N/A |
| [fmtmsg.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fmtmsg.h.html)              | N/A |
| [fnmatch.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fnmatch.h.html)            | [scala.scalanative.posix.fnmatch](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/fnmatch.scala) |
| [fnmatch.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/fnmatch.h.html)            | [scala.scalanative.posix.fnmatch](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/fnmatch.scala) |
| [ftw.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/ftw.h.html)                    | N/A |
| [ftw.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/ftw.h.html)                    | N/A |
| [getopt.h](https://pubs.opengroup.org/onlinepubs/9699919799/functions/getopt.html)               | [scala.scalanative.posix.getopt]() |
| [getopt.h](https://pubs.opengroup.org/onlinepubs/9699919799/functions/getopt.html)               | [scala.scalanative.posix.getopt]() |
| [glob.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/glob.h.html)                  | [scala.scalanative.posix.glob](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/glob.scala) |
| [glob.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/glob.h.html)                  | [scala.scalanative.posix.glob](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/glob.scala) |
| [grp.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/grp.h.html)                    | [scala.scalanative.posix.grp](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/grp.scala) |
| [grp.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/grp.h.html)                    | [scala.scalanative.posix.grp](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/grp.scala) |
| [iconv.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/iconv.h.html)                | N/A |
| [iconv.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/iconv.h.html)                | N/A |
| [inttypes.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/inttypes.h.html)          | [scala.scalanative.posix.inttypes](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/inttypes.scala) |
| [inttypes.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/inttypes.h.html)          | [scala.scalanative.posix.inttypes](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/inttypes.scala) |
| [iso646.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/iso646.h.html)              | N/A |
| [langinfo.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/langinfo.h.html)          | [scala.scalanative.posix.langinfo](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/langinfo.scala) |
| [libgen.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/libgen.h.html)              | [scala.scalanative.posix.libgen](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/libgen.scala) |
| [limits.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/limits.h.html)              | [scala.scalanative.posix.limits](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/limits.scala) |
| [locale.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/locale.h.html)              | [scala.scalanative.posix.locale](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/locale.scala) |
| [math.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/math.h.html)                  | [scala.scalanative.posix.math](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/math.scala) |
| [monetary.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/monetary.h.html)          | [scala.scalanative.posix.monetary](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/monetaryh.scala)[^2] |
| [mqueue.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/mqueue.h.html)              | N/A |
| [ndbm.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/ndbm.h.html)                  | N/A |
| [net/if.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/net_if.h.html)              | [scala.scalanative.posix.net.if](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/net/if.scala) |
| [netdb.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/netdb.h.html)                | [scala.scalanative.posix.netdb](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/netdb.scala) |
| [netinet/in.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/netinet_in.h.html)      | [scala.scalanative.posix.netinet.in](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/netinet/in.scala) |
| [netinet/tcp.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/netinet_tcp.h.html)    | [scala.scalanative.posix.netinet.tcp](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/netinet/tcp.scala) |
| [nl_types.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/nl_types.h.html)          | [scala.scalanative.posix.nl_types](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/nl_types.scala) |
| [poll.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/poll.h.html)                  | [scala.scalanative.posix.poll](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/poll.scala) |
| [pthread.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/pthread.h.html)            | [scala.scalanative.posix.pthread](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/pthread.scala) |
| [pwd.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/pwd.h.html)                    | [scala.scalanative.posix.pwd](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/pwd.scala) |
| [regex.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/regex.h.html)                | [scala.scalanative.posix.regex](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/regex.scala) |
| [sched.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sched.h.html)                | [scala.scalanative.posix.sched](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sched.scala) |
| [search.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/search.h.html)              | N/A |
| [semaphore.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/semaphore.h.html)        | N/A |
| [setjmp.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/setjmp.h.html)              | N/A |
| [signal.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/signal.h.html)              | [scala.scalanative.posix.signal](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/signal.scala) |
| [spawn.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/spawn.h.html)                | [scala.scalanative.posix.spawn](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/spawn.scala) |
| [stdarg.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdarg.h.html)              | N/A |
| [stdbool.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdbool.h.html)            | N/A |
| [stddef.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stddef.h.html)              | [scala.scalanative.posix.stddef](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/stddef.scala) |
| [stdint.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdint.h.html)              | [scala.scalanative.posix.stdint](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/stdint.scala) |
| [stdio.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdio.h.html)                | [scala.scalanative.posix.stdio](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/stdio.scala) |
| [stdlib.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdlib.h.html)              | [scala.scalanative.posix.stdlib](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/stdlib.scala) |
| [string.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/string.h.html)              | [scala.scalanative.posix.string](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/string.scala) |
| [strings.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/strings.h.html)            | [scala.scalanative.posix.strings](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/strings.scala) |
| [sys/ipc.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_ipc.h.html)            | N/A |
| [sys/mman.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_mman.h.html)          | [scala.scalanative.posix.sys.mman](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/mman.scala) |
| [sys/msg.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_msg.h.html)            | N/A |
| [sys/resource.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_resource.h.html)  | [scala.scalanative.posix.sys.resource](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/resource.scala) |
| [sys/select.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_select.h.html)      | [scala.scalanative.posix.sys.select](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/select.scala) |
| [sys/sem.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_sem.h.html)            | N/A |
| [sys/shm.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_shm.h.html)            | N/A |
| [sys/socket.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_socket.h.html)      | [scala.scalanative.posix.sys.socket](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/socket.scala) |
| [sys/stat.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_stat.h.html)          | [scala.scalanative.posix.sys.stat](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/stat.scala) |
| [sys/statvfs.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_statvfs.h.html)    | [scala.scalanative.posix.sys.statvfs](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/statvfs.scala) |
| [sys/time.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_time.h.html)          | [scala.scalanative.posix.sys.time](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/time.scala) |
| [sys/times.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_times.h.html)        | [scala.scalanative.posix.sys.times](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/times.scala) |
| [sys/types.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_types.h.html)        | [scala.scalanative.posix.sys.types](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/types.scala) |
| [sys/uio.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_uio.h.html)            | [scala.scalanative.posix.sys.uio](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/uio.scala) |
| [sys/un.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_un.h.html)              | [scala.scalanative.posix.sys.un](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/un.scala) |
| [sys/utsname.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_utsname.h.html)    | [scala.scalanative.posix.sys.utsname](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/utsname.scala) |
| [sys/wait.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/sys_wait.h.html)          | [scala.scalanative.posix.sys.wait](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/sys/wait.scala) |
| [syslog.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/syslog.h.html)              | [scala.scalanative.posix.syslog](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/syslog.scala) |
| [tar.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/tar.h.html)                    | N/A |
| [termios.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/termios.h.html)            | [scala.scalanative.posix.termios](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/termios.scala) |
| [tgmath.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/tgmath.h.html)              | [scala.scalanative.posix.tgmath](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/tgmath.scala) |
| [time.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/time.h.html)                  | [scala.scalanative.posix.time](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/time.scala) |
| [trace.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/trace.h.html)                | N/A |
| [unistd.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/unistd.h.html)              | [scala.scalanative.posix.unistd](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/unistd.scala) |
| [utime.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/utime.h.html)                | [scala.scalanative.posix.utime](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/utime.scala) |
| [utmpx.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/utmpx.h.html)                | N/A |
| [wchar.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/wchar.h.html)                | [scala.scalanative.posix.wchar](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/wchar.scala) |
| [wctype.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/wctype.h.html)              | N/A |
| [wordexp.h](https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/wordexp.h.html)            | [scala.scalanative.posix.wordexp](https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/wordexp.scala) |

Continue to [communitylib](./communitylib.md){.interpreted-text role="ref"}.

[^1]: The argument to inet_ntoa() differs from the POSIX specification
    because Scala Native supports only passing structures by reference.
    See code for details and usage.

[^2]: See file for limit on number of variable arguments.
