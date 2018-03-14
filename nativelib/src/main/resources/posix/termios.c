#include <termios.h>
#include <sys/types.h>

int scalanative_b0() { return B0; }
int scalanative_b50() { return B50; }
int scalanative_b75() { return B75; }
int scalanative_b110() { return B110; }
int scalanative_b134() { return B134; }
int scalanative_b150() { return B150; }
int scalanative_b200() { return B200; }
int scalanative_b300() { return B300; }
int scalanative_b600() { return B600; }
int scalanative_b1200() { return B1200; }
int scalanative_b1800() { return B1800; }
int scalanative_b2400() { return B2400; }
int scalanative_b4800() { return B4800; }
int scalanative_b9600() { return B9600; }
int scalanative_b19200() { return B19200; }
int scalanative_b38400() { return B38400; }

int scalanative_brkint() { return BRKINT; }
int scalanative_icrnl() { return ICRNL; }
int scalanative_ignbrk() { return IGNBRK; }
int scalanative_igncr() { return IGNCR; }
int scalanative_ignpar() { return IGNPAR; }
int scalanative_inlcr() { return INLCR; }
int scalanative_inpck() { return INPCK; }
int scalanative_istrip() { return ISTRIP; }
int scalanative_ixoff() { return IXOFF; }
int scalanative_ixon() { return IXON; }
int scalanative_parmrk() { return PARMRK; }

int scalanative_opost() { return OPOST; }

int scalanative_clocal() { return CLOCAL; }
int scalanative_cread() { return CREAD; }
int scalanative_cs5() { return CS5; }
int scalanative_cs6() { return CS6; }
int scalanative_cs7() { return CS7; }
int scalanative_cs8() { return CS8; }
int scalanative_csize() { return CSIZE; }
int scalanative_cstopb() { return CSTOPB; }
int scalanative_hupcl() { return HUPCL; }
int scalanative_parenb() { return PARENB; }
int scalanative_parodd() { return PARODD; }

int scalanative_echo() { return ECHO; }
int scalanative_echoe() { return ECHOE; }
int scalanative_echok() { return ECHOK; }
int scalanative_echonl() { return ECHONL; }
int scalanative_icanon() { return ICANON; }
int scalanative_iexten() { return IEXTEN; }
int scalanative_isig() { return ISIG; }
int scalanative_noflsh() { return NOFLSH; }
int scalanative_tostop() { return TOSTOP; }

int scalanative_tciflush() { return TCIFLUSH; }
int scalanative_tcoflush() { return TCOFLUSH; }
int scalanative_tcioflush() { return TCIOFLUSH; }
int scalanative_tcooff() { return TCOOFF; }
int scalanative_tcoon() { return TCOON; }
int scalanative_tcioff() { return TCIOFF; }
int scalanative_tcion() { return TCION; }

int scalanative_tcsadrain() { return TCSADRAIN; }
int scalanative_tcsaflush() { return TCSAFLUSH; }
int scalanative_tcsanow() { return TCSANOW; }

int scalanative_veof() { return VEOF; }
int scalanative_veol() { return VEOL; }
int scalanative_verase() { return VERASE; }
int scalanative_vintr() { return VINTR; }
int scalanative_vkill() { return VKILL; }
int scalanative_vmin() { return VMIN; }
int scalanative_vquit() { return VQUIT; }
int scalanative_vstar() { return VSTART; }
int scalanative_vstop() { return VSTOP; }
int scalanative_vsusp() { return VSUSP; }
int scalanative_vtime() { return VTIME; }
int scalanative_nccs() { return NCCS; }

int scalanative_imaxbel() { return IMAXBEL; }

int scalanative_echoke() { return ECHOKE; }
int scalanative_echoctl() { return ECHOCTL; }

int scalanative_onlcr() { return ONLCR; }
int scalanative_ocrnl() { return OCRNL; }

speed_t	scalanative_cfgetispeed(const struct termios *termios_p) { return cfgetispeed(termios_p); }
speed_t	scalanative_cfgetospeed(const struct termios *termios_p) { return cfgetospeed(termios_p); }
int	scalanative_cfsetispeed(struct termios *termios_p, speed_t speed) { return cfsetispeed(termios_p, speed); }
int	scalanative_cfsetospeed(struct termios *termios_p, speed_t speed) { return cfsetospeed(termios_p, speed); }
int	scalanative_tcgetattr(int fd, struct termios *termios_p) { return tcgetattr(fd, termios_p); }
int	scalanative_tcsetattr(int fd, int optional_actions, const struct termios *termios_p) { return tcsetattr(fd, optional_actions, termios_p); }
int	scalanative_tcdrain(int fd) { return tcdrain(fd); }
int	scalanative_tcflow(int fd, int action) { return tcflow(fd, action); }
int	scalanative_tcflush(int fd, int queue_selector) { return tcflush(fd, queue_selector); }
int	scalanative_tcsendbreak(int fd, int duration) { return tcsendbreak(fd, duration); }
pid_t scalanative_tcgetsid(int fd) { return tcgetsid(fd); }