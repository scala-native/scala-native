#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_TERMIOS)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <assert.h>
#include <limits.h>
#include <termios.h>
#if defined(__FreeBSD__)
#define COMPAT_43TTY
#include <sys/ioctl_compat.h>
#define NLDLY NLDELAY
#define CRDLY CRDELAY
#define BSDLY BSDELAY
#define VTDLY VTDELAY
#endif

#if defined(__OpenBSD__) || defined(__NetBSD__)
// OpenBSD and NetBSD has missed some constatn, use 0 instead
#define NLDLY 0
#define CRDLY 0
#define BSDLY 0
#define VTDLY 0
#define BS0 0
#define BS1 0
#define CR0 0
#define CR1 0
#define CR2 0
#define CR3 0
#define FF0 0
#define FF1 0
#define NL0 0
#define NL1 0
#define TAB1 0
#define TAB2 0

// NetBSD requires a few more
#ifdef __NetBSD__
#define TABDLY 0
#define TAB0 0
#define TAB3 0
#endif // NetBSD

#endif // OpenBSD || NetBSD

// symbolic constants for use as subscripts for the array c_cc

int scalanative_termios_veof() { return VEOF; }
int scalanative_termios_veol() { return VEOL; }
int scalanative_termios_verase() { return VERASE; }
int scalanative_termios_vintr() { return VINTR; }
int scalanative_termios_vkill() { return VKILL; }
int scalanative_termios_vmin() { return VMIN; }
int scalanative_termios_vquit() { return VQUIT; }
int scalanative_termios_vstart() { return VSTART; }
int scalanative_termios_vstop() { return VSTOP; }
int scalanative_termios_vsusp() { return VSUSP; }
int scalanative_termios_vtime() { return VTIME; }

// Input Modes - symbolic constants for use as flags in the c_iflag field

int scalanative_termios_brkint() { return BRKINT; }
int scalanative_termios_icrnl() { return ICRNL; }
int scalanative_termios_ignbrk() { return IGNBRK; }
int scalanative_termios_igncr() { return IGNCR; }
int scalanative_termios_ignpar() { return IGNPAR; }
int scalanative_termios_inlcr() { return INLCR; }
int scalanative_termios_inpck() { return INPCK; }
int scalanative_termios_istrip() { return ISTRIP; }
int scalanative_termios_ixany() { return IXANY; }
int scalanative_termios_ixoff() { return IXOFF; }
int scalanative_termios_ixon() { return IXON; }
int scalanative_termios_parmrk() { return PARMRK; }

// Output Modes - symbolic constants for use as flags in the c_oflag field

int scalanative_termios_opost() { return OPOST; }
// [XSI] follows
int scalanative_termios_onlcr() { return ONLCR; }
int scalanative_termios_ocrnl() { return OCRNL; }
int scalanative_termios_onocr() { return ONOCR; }
int scalanative_termios_onlret() { return ONLRET; }
int scalanative_termios_ofdel() {
#ifdef OFDEL
    return OFDEL;
#else
    return 0;
#endif
}
int scalanative_termios_ofill() {
#ifdef OFILL
    return OFILL;
#else
    return 0;
#endif
}
// Masks - Linux [requires _BSD_SOURCE or _SVID_SOURCE or _XOPEN_SOURCE]
int scalanative_termios_nldly() { return NLDLY; }
int scalanative_termios_nl0() { return NL0; }
int scalanative_termios_nl1() { return NL1; }
int scalanative_termios_crdly() { return CRDLY; }
int scalanative_termios_cr0() { return CR0; }
int scalanative_termios_cr1() { return CR1; }
int scalanative_termios_cr2() { return CR2; }
int scalanative_termios_cr3() { return CR3; }
int scalanative_termios_tabdly() { return TABDLY; }
int scalanative_termios_tab0() { return TAB0; }
int scalanative_termios_tab1() { return TAB1; }
int scalanative_termios_tab2() { return TAB2; }
int scalanative_termios_tab3() { return TAB3; }
int scalanative_termios_bsdly() { return BSDLY; }
int scalanative_termios_bs0() { return BS0; }
int scalanative_termios_bs1() { return BS1; }
int scalanative_termios_vtdly() { return VTDLY; }
int scalanative_termios_vt0() {
#ifdef VT0
    return VT0;
#else
    return 0;
#endif
}
int scalanative_termios_vt1() {
#ifdef VT1
    return VT1;
#else
    return 0;
#endif
}
int scalanative_termios_ffdly() {
#ifdef FFDLY
    return FFDLY;
#else
    return 0;
#endif
}
int scalanative_termios_ff0() { return FF0; }
int scalanative_termios_ff1() { return FF1; }

// Baud Rate Selection - valid values for objects of type speed_t

int scalanative_termios_b0() { return B0; }
int scalanative_termios_b50() { return B50; }
int scalanative_termios_b75() { return B75; }
int scalanative_termios_b110() { return B110; }
int scalanative_termios_b134() { return B134; }
int scalanative_termios_b150() { return B150; }
int scalanative_termios_b200() { return B200; }
int scalanative_termios_b300() { return B300; }
int scalanative_termios_b600() { return B600; }
int scalanative_termios_b1200() { return B1200; }
int scalanative_termios_b1800() { return B1800; }
int scalanative_termios_b2400() { return B2400; }
int scalanative_termios_b4800() { return B4800; }
int scalanative_termios_b9600() { return B9600; }
int scalanative_termios_b19200() { return B19200; }
int scalanative_termios_b38400() { return B38400; }

// Control Modes - symbolic constants for use as flags in the c_cflag field

int scalanative_termios_csize() { return CSIZE; }
int scalanative_termios_cs5() { return CS5; }
int scalanative_termios_cs6() { return CS6; }
int scalanative_termios_cs7() { return CS7; }
int scalanative_termios_cs8() { return CS8; }
int scalanative_termios_cstopb() { return CSTOPB; }
int scalanative_termios_cread() { return CREAD; }
int scalanative_termios_parenb() { return PARENB; }
int scalanative_termios_parodd() { return PARODD; }
int scalanative_termios_hupcl() { return HUPCL; }
int scalanative_termios_clocal() { return CLOCAL; }

// Local Modes - symbolic constants for use as flags in the c_lflag field

int scalanative_termios_echo() { return ECHO; }
int scalanative_termios_echoe() { return ECHOE; }
int scalanative_termios_echok() { return ECHOK; }
int scalanative_termios_echonl() { return ECHONL; }
int scalanative_termios_icanon() { return ICANON; }
int scalanative_termios_iexten() { return IEXTEN; }
int scalanative_termios_isig() { return ISIG; }
int scalanative_termios_noflsh() { return NOFLSH; }
int scalanative_termios_tostop() { return TOSTOP; }

// Attribute Selection - symbolic constants for use with tcsetattr()

int scalanative_termios_tcsanow() { return TCSANOW; }
int scalanative_termios_tcsadrain() { return TCSADRAIN; }
int scalanative_termios_tcsaflush() { return TCSAFLUSH; }

// Line Control - symbolic constants for use with tcflush()

int scalanative_termios_tciflush() { return TCIFLUSH; }
int scalanative_termios_tcioflush() { return TCIOFLUSH; }
int scalanative_termios_tcoflush() { return TCOFLUSH; }

// Line Control cont. - symbolic constants for use with tcflow()

int scalanative_termios_tcioff() { return TCIOFF; }
int scalanative_termios_tcion() { return TCION; }
int scalanative_termios_tcooff() { return TCOOFF; }
int scalanative_termios_tcoon() { return TCOON; }

/* macOS and Linux uses different sizes for `termios` struct.
 * POSIX says unsigned types and Linux uses `unsigned int`
 * and macOS uses `unsigned long` for `tcflag_t`and `speed_t`.
 *
 * Since Scala needs the types upfront we use the smaller
 * `unsigned int` because that is a sufficient size.
 *
 * cc_t is `unsigned char` on both so we don't control it
 */

#define NCCS_L 20 // use macOS size, Linux 32 or 19 in kernel

struct scalanative_termios {
    unsigned int c_iflag;
    unsigned int c_oflag;
    unsigned int c_cflag;
    unsigned int c_lflag;
    cc_t c_cc[NCCS_L];
    unsigned int c_ispeed;
    unsigned int c_ospeed;
};

void scalanative_termios_copy_to_host(struct scalanative_termios *termios_sn,
                                      struct termios *termios) {
    termios->c_iflag = termios_sn->c_iflag;
    termios->c_oflag = termios_sn->c_oflag;
    termios->c_cflag = termios_sn->c_cflag;
    termios->c_lflag = termios_sn->c_lflag;
    for (int i = 0; i < NCCS_L; i++) {
        termios->c_cc[i] = termios_sn->c_cc[i];
    }
    termios->c_ispeed = termios_sn->c_ispeed;
    termios->c_ospeed = termios_sn->c_ospeed;
}

void scalanative_termios_copy_to_sn(struct scalanative_termios *termios_sn,
                                    struct termios *termios) {
    termios_sn->c_iflag = termios->c_iflag;
    termios_sn->c_oflag = termios->c_oflag;
    termios_sn->c_cflag = termios->c_cflag;
    termios_sn->c_lflag = termios->c_lflag;
    for (int i = 0; i < NCCS_L; i++) {
        termios_sn->c_cc[i] = termios->c_cc[i];
    }
    termios_sn->c_ispeed = termios->c_ispeed;
    termios_sn->c_ospeed = termios->c_ospeed;
}

// @name functions

// Linux speed_t is unsigned int
unsigned int
scalanative_termios_cfgetispeed(struct scalanative_termios *tio_sn) {
    struct termios tio;
    scalanative_termios_copy_to_host(tio_sn, &tio);
    unsigned long res = cfgetispeed(&tio);
    scalanative_termios_copy_to_sn(tio_sn, &tio);
    assert(res <= UINT_MAX && "unsigned long value exceeds unsigned int range");
    return (unsigned int)res;
}
unsigned int
scalanative_termios_cfgetospeed(struct scalanative_termios *tio_sn) {
    struct termios tio;
    scalanative_termios_copy_to_host(tio_sn, &tio);
    unsigned long res = cfgetospeed(&tio);
    scalanative_termios_copy_to_sn(tio_sn, &tio);
    assert(res <= UINT_MAX && "unsigned long value exceeds unsigned int range");
    return (unsigned int)res;
}
int scalanative_termios_cfsetispeed(struct scalanative_termios *tio_sn,
                                    unsigned int speed) {
    struct termios tio;
    scalanative_termios_copy_to_host(tio_sn, &tio);
    int res = cfsetispeed(&tio, speed);
    scalanative_termios_copy_to_sn(tio_sn, &tio);
    return res;
}
int scalanative_termios_cfsetospeed(struct scalanative_termios *tio_sn,
                                    unsigned int speed) {
    struct termios tio;
    scalanative_termios_copy_to_host(tio_sn, &tio);
    int res = cfsetospeed(&tio, speed);
    scalanative_termios_copy_to_sn(tio_sn, &tio);
    return res;
}
int scalanative_termios_tcgetattr(int fd, struct scalanative_termios *tio_sn) {
    struct termios tio;
    scalanative_termios_copy_to_host(tio_sn, &tio);
    int res = tcgetattr(fd, &tio);
    scalanative_termios_copy_to_sn(tio_sn, &tio);
    return res;
}
int scalanative_termios_tcsetattr(int fd, int optionalActions,
                                  struct scalanative_termios *tio_sn) {
    struct termios tio;
    scalanative_termios_copy_to_host(tio_sn, &tio);
    int res = tcsetattr(fd, optionalActions, &tio);
    scalanative_termios_copy_to_sn(tio_sn, &tio);
    return res;
}

#endif // Unix or Mac OS
#endif