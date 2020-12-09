#include <termios.h>

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
int scalanative_termios_ofdel() { return OFDEL; }
int scalanative_termios_ofill() { return OFILL; }
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
int scalanative_termios_vt0() { return VT0; }
int scalanative_termios_vt1() { return VT1; }
int scalanative_termios_ffdly() { return FFDLY; }
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
