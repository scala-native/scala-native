package scala.scalanative
package posix

import scalanative.native.{CInt, extern, name}

@extern
object errno {
  @name("scalanative_e2big")
  def E2BIG: CInt = extern
  @name("scalanative_eacces")
  def EACCES: CInt = extern
  @name("scalanative_eaddrinuse")
  def EADDRINUSE: CInt = extern
  @name("scalanative_eafnosupport")
  def EAFNOSUPPORT: CInt = extern
  @name("scalanative_eagain")
  def EAGAIN: CInt = extern
  @name("scalanative_ealready")
  def EALREADY: CInt = extern
  @name("scalanative_ebadf")
  def EBADF: CInt = extern
  @name("scalanative_ebadmsg")
  def EBADMSG: CInt = extern
  @name("scalanative_ebusy")
  def EBUSY: CInt = extern
  @name("scalanative_ecanceled")
  def ECANCELED: CInt = extern
  @name("scalanative_echild")
  def ECHILD: CInt = extern
  @name("scalanative_econnaborted")
  def ECONNABORTED: CInt = extern
  @name("scalanative_econnrefused")
  def ECONNREFUSED: CInt = extern
  @name("scalanative_econnreset")
  def ECONNRESET: CInt = extern
  @name("scalanative_edeadlk")
  def EDEADLK: CInt = extern
  @name("scalanative_edestaddrreq")
  def EDESTADDRREQ: CInt = extern
  @name("scalanative_edom")
  def EDOM: CInt = extern
  @name("scalanative_edquot")
  def EDQUOT: CInt = extern
  @name("scalanative_eexist")
  def EEXIST: CInt = extern
  @name("scalanative_efault")
  def EFAULT: CInt = extern
  @name("scalanative_efbig")
  def EFBIG: CInt = extern
  @name("scalanative_ehostunreach")
  def EHOSTUNREACH: CInt = extern
  @name("scalanative_eidrm")
  def EIDRM: CInt = extern
  @name("scalanative_eilseq")
  def EILSEQ: CInt = extern
  @name("scalanative_einprogress")
  def EINPROGRESS: CInt = extern
  @name("scalanative_eintr")
  def EINTR: CInt = extern
  @name("scalanative_einval")
  def EINVAL: CInt = extern
  @name("scalanative_eio")
  def EIO: CInt = extern
  @name("scalanative_eisconn")
  def EISCONN: CInt = extern
  @name("scalanative_eisdir")
  def EISDIR: CInt = extern
  @name("scalanative_eloop")
  def ELOOP: CInt = extern
  @name("scalanative_emfile")
  def EMFILE: CInt = extern
  @name("scalanative_emlink")
  def EMLINK: CInt = extern
  @name("scalanative_emsgsize")
  def EMSGSIZE: CInt = extern
  @name("scalanative_emultihup")
  def EMULTIHOP: CInt = extern
  @name("scalanative_enametoolong")
  def ENAMETOOLONG: CInt = extern
  @name("scalanative_enetdown")
  def ENETDOWN: CInt = extern
  @name("scalanative_enetreset")
  def ENETRESET: CInt = extern
  @name("scalanative_enetunreach")
  def ENETUNREACH: CInt = extern
  @name("scalanative_enfile")
  def ENFILE: CInt = extern
  @name("scalanative_enobufs")
  def ENOBUFS: CInt = extern
  @name("scalanative_enodata")
  def ENODATA: CInt = extern
  @name("scalanative_enodev")
  def ENODEV: CInt = extern
  @name("scalanative_enoent")
  def ENOENT: CInt = extern
  @name("scalanative_enoexec")
  def ENOEXEC: CInt = extern
  @name("scalanative_enolck")
  def ENOLCK: CInt = extern
  @name("scalanative_enolink")
  def ENOLINK: CInt = extern
  @name("scalanative_enomem")
  def ENOMEM: CInt = extern
  @name("scalanative_enomsg")
  def ENOMSG: CInt = extern
  @name("scalanative_enoprotoopt")
  def ENOPROTOOPT: CInt = extern
  @name("scalanative_enospc")
  def ENOSPC: CInt = extern
  @name("scalanative_enosr")
  def ENOSR: CInt = extern
  @name("scalanative_enostr")
  def ENOSTR: CInt = extern
  @name("scalanative_enosys")
  def ENOSYS: CInt = extern
  @name("scalanative_enotconn")
  def ENOTCONN: CInt = extern
  @name("scalanative_enotdir")
  def ENOTDIR: CInt = extern
  @name("scalanative_enotempty")
  def ENOTEMPTY: CInt = extern
  @name("scalanative_enotrecoverable")
  def ENOTRECOVERABLE: CInt = extern
  @name("scalanative_enotsock")
  def ENOTSOCK: CInt = extern
  @name("scalanative_enotsup")
  def ENOTSUP: CInt = extern
  @name("scalanative_enotty")
  def ENOTTY: CInt = extern
  @name("scalanative_enxio")
  def ENXIO: CInt = extern
  @name("scalanative_eopnotsupp")
  def EOPNOTSUPP: CInt = extern
  @name("scalanative_eoverflow")
  def EOVERFLOW: CInt = extern
  @name("scalanative_eownerdead")
  def EOWNERDEAD: CInt = extern
  @name("scalanative_eperm")
  def EPERM: CInt = extern
  @name("scalanative_epipe")
  def EPIPE: CInt = extern
  @name("scalanative_eproto")
  def EPROTO: CInt = extern
  @name("scalanative_eprotonosupport")
  def EPROTONOSUPPORT: CInt = extern
  @name("scalanative_eprototype")
  def EPROTOTYPE: CInt = extern
  @name("scalanative_erange")
  def ERANGE: CInt = extern
  @name("scalanative_erofs")
  def EROFS: CInt = extern
  @name("scalanative_espipe")
  def ESPIPE: CInt = extern
  @name("scalanative_esrch")
  def ESRCH: CInt = extern
  @name("scalanative_estale")
  def ESTALE: CInt = extern
  @name("scalanative_etime")
  def ETIME: CInt = extern
  @name("scalanative_etimedout")
  def ETIMEDOUT: CInt = extern
  @name("scalanative_etxtbsy")
  def ETXTBSY: CInt = extern
  @name("scalanative_ewouldblock")
  def EWOULDBLOCK: CInt = extern
  @name("scalanative_exdev")
  def EXDEV: CInt = extern
}
