package scala.scalanative
package posix

import scalanative.unsafe._

@extern
object syslog {
  @name("scalanative_closelog")
  def closelog(): Unit = extern

  @name("scalanative_openlog")
  def openlog(ident: CString, logopt: CInt, facility: CInt): Unit = extern

  @name("scalanative_setlogmask")
  def setlogmask(maskpri: CInt): CInt = extern

  @name("scalanative_log_emerg")
  def LOG_EMERG: CInt = extern

  @name("scalanative_log_alert")
  def LOG_ALERT: CInt = extern

  @name("scalanative_log_crit")
  def LOG_CRIT: CInt = extern

  @name("scalanative_log_err")
  def LOG_ERR: CInt = extern

  @name("scalanative_log_warning")
  def LOG_WARNING: CInt = extern

  @name("scalanative_log_notice")
  def LOG_NOTICE: CInt = extern

  @name("scalanative_log_info")
  def LOG_INFO: CInt = extern

  @name("scalanative_log_debug")
  def LOG_DEBUG: CInt = extern

  @name("scalanative_log_primask")
  def LOG_PRIMASK: CInt = extern

  @name("scalanative_log_pri")
  def LOG_PRI(p: CInt): CInt = extern

  @name("scalanative_log_makepri")
  def LOG_MAKEPRI(fac: CInt, pri: CInt): CInt = extern

  @name("scalanative_log_kern")
  def LOG_KERN: CInt = extern

  @name("scalanative_log_user")
  def LOG_USER: CInt = extern

  @name("scalanative_log_mail")
  def LOG_MAIL: CInt = extern

  @name("scalanative_log_daemon")
  def LOG_DAEMON: CInt = extern

  @name("scalanative_log_auth")
  def LOG_AUTH: CInt = extern

  @name("scalanative_log_syslog")
  def LOG_SYSLOG: CInt = extern

  @name("scalanative_log_lpr")
  def LOG_LPR: CInt = extern

  @name("scalanative_log_news")
  def LOG_NEWS: CInt = extern

  @name("scalanative_log_uucp")
  def LOG_UUCP: CInt = extern

  @name("scalanative_log_cron")
  def LOG_CRON: CInt = extern

  @name("scalanative_log_authpriv")
  def LOG_AUTHPRIV: CInt = extern

  @name("scalanative_log_ftp")
  def LOG_FTP: CInt = extern

  @name("scalanative_log_netinfo")
  def LOG_NETINFO: CInt = extern

  @name("scalanative_log_remoteauth")
  def LOG_REMOTEAUTH: CInt = extern

  @name("scalanative_log_install")
  def LOG_INSTALL: CInt = extern

  @name("scalanative_log_ras")
  def LOG_RAS: CInt = extern

  @name("scalanative_log_local0")
  def LOG_LOCAL0: CInt = extern

  @name("scalanative_log_local1")
  def LOG_LOCAL1: CInt = extern

  @name("scalanative_log_local2")
  def LOG_LOCAL2: CInt = extern

  @name("scalanative_log_local3")
  def LOG_LOCAL3: CInt = extern

  @name("scalanative_log_local4")
  def LOG_LOCAL4: CInt = extern

  @name("scalanative_log_local5")
  def LOG_LOCAL5: CInt = extern

  @name("scalanative_log_local6")
  def LOG_LOCAL6: CInt = extern

  @name("scalanative_log_local7")
  def LOG_LOCAL7: CInt = extern

  @name("scalanative_log_nfacilities")
  def LOG_NFACILITIES: CInt = extern

  @name("scalanative_log_facmask")
  def LOG_FACMASK: CInt = extern

  @name("scalanative_log_fac")
  def LOG_FAC(p: CInt): CInt = extern

  @name("scalanative_log_mask")
  def LOG_MASK(pri: CInt): CInt = extern

  @name("scalanative_log_upto")
  def LOG_UPTO(pri: CInt): CInt = extern

  @name("scalanative_log_pid")
  def LOG_PID: CInt = extern

  @name("scalanative_log_cons")
  def LOG_CONS: CInt = extern

  @name("scalanative_log_odelay")
  def LOG_ODELAY: CInt = extern

  @name("scalanative_log_ndelay")
  def LOG_NDELAY: CInt = extern

  @name("scalanative_log_nowait")
  def LOG_NOWAIT: CInt = extern

  @name("scalanative_log_perror")
  def LOG_PERROR: CInt = extern
}
