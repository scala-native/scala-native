#include <syslog.h>
#include <stdarg.h>

void scalanative_closelog() { closelog(); }

void scalanative_openlog(const char *ident, int logopt, int facility) {
    openlog(ident, logopt, facility);
}

int scalanative_setlogmask(int maskpri) { return setlogmask(maskpri); }

int scalanative_log_emerg() { return LOG_EMERG; }
int scalanative_log_alert() { return LOG_ALERT; }
int scalanative_log_crit() { return LOG_CRIT; }
int scalanative_log_err() { return LOG_ERR; }
int scalanative_log_warning() { return LOG_WARNING; }
int scalanative_log_notice() { return LOG_NOTICE; }
int scalanative_log_info() { return LOG_INFO; }
int scalanative_log_debug() { return LOG_DEBUG; }
int scalanative_log_primask() { return LOG_PRIMASK; }

int scalanative_log_pri(int p) { return LOG_PRI(p); }
int scalanative_log_makepri(int fac, int pri) { return LOG_MAKEPRI(fac, pri); }

int scalanative_log_kern() { return LOG_KERN; }
int scalanative_log_user() { return LOG_USER; }
int scalanative_log_mail() { return LOG_MAIL; }
int scalanative_log_daemon() { return LOG_DAEMON; }
int scalanative_log_auth() { return LOG_AUTH; }
int scalanative_log_syslog() { return LOG_SYSLOG; }
int scalanative_log_lpr() { return LOG_LPR; }
int scalanative_log_news() { return LOG_NEWS; }
int scalanative_log_uucp() { return LOG_UUCP; }
int scalanative_log_cron() { return LOG_CRON; }
int scalanative_log_authpriv() { return LOG_AUTHPRIV; }
int scalanative_log_ftp() { return LOG_FTP; }

int scalanative_log_local0() { return LOG_LOCAL0; }
int scalanative_log_local1() { return LOG_LOCAL1; }
int scalanative_log_local2() { return LOG_LOCAL2; }
int scalanative_log_local3() { return LOG_LOCAL3; }
int scalanative_log_local4() { return LOG_LOCAL4; }
int scalanative_log_local5() { return LOG_LOCAL5; }
int scalanative_log_local6() { return LOG_LOCAL6; }
int scalanative_log_local7() { return LOG_LOCAL7; }

int scalanative_log_nfacilities() { return LOG_NFACILITIES; }
int scalanative_log_facmask() { return LOG_FACMASK; }

int scalanative_log_fac(int p) { return LOG_FAC(p); }

int scalanative_log_mask(int pri) { return LOG_MASK(pri); }
int scalanative_log_upto(int pri) { return LOG_UPTO(pri); }

int scalanative_log_pid() { return LOG_PID; }
int scalanative_log_cons() { return LOG_CONS; }
int scalanative_log_odelay() { return LOG_ODELAY; }
int scalanative_log_ndelay() { return LOG_NDELAY; }
int scalanative_log_nowait() { return LOG_NOWAIT; }
int scalanative_log_perror() { return LOG_PERROR; }
