package scala.scalanative
package posix

import native.{CArray, CChar, CInt, CLong, CStruct7, Nat, Ptr, extern, name}
import posix.sys.types.pid_t

@extern
object termios {
  import Nat._

  // types

  type tcflag_t = CLong
  type cc_t     = CChar
  type speed_t  = CLong
  type NCCS     = Digit[_2, _0]
  type c_cc     = CArray[cc_t, NCCS]

  type termios = CStruct7[
    tcflag_t, /* c_iflag - input flags   */
    tcflag_t, /* c_oflag - output flags  */
    tcflag_t, /* c_cflag - control flags */
    tcflag_t, /* c_lflag - local flags   */
    c_cc, /* cc_t c_cc[NCCS] - control chars */
    speed_t, /* c_ispeed - input speed   */
    speed_t /* c_ospeed - output speed  */
  ]

  // functions

  def cfgetispeed(termios_p: Ptr[termios]): speed_t              = extern
  def cfgetospeed(termios_p: Ptr[termios]): speed_t              = extern
  def cfsetispeed(termios_p: Ptr[termios], speed: speed_t): CInt = extern
  def cfsetospeed(termios_p: Ptr[termios], speed: speed_t): CInt = extern
  def tcdrain(fd: CInt): CInt                                    = extern
  def tcflow(fd: CInt, action: CInt): CInt                       = extern
  def tcflush(fd: CInt, queueSelector: CInt): CInt               = extern
  def tcgetattr(fd: CInt, termios_p: Ptr[termios]): CInt         = extern
  def tcgetsid(i: CInt): pid_t                                   = extern
  def tcsendbreak(fd: CInt, duration: CInt): CInt                = extern
  def tcsetattr(fd: CInt,
                optionalActions: CInt,
                termios_p: Ptr[termios]): CInt = extern

  // symbolic constants for use as subscripts for the array c_cc

  @name("scalanative_termios_veof")
  def VEOF: CInt = extern
  @name("scalanative_termios_veof")
  def VEOL: CInt = extern
  @name("scalanative_termios_verase")
  def VERASE: CInt = extern
  @name("scalanative_termios_vintr")
  def VINTR: CInt = extern
  @name("scalanative_termios_vkill")
  def VKILL: CInt = extern
  @name("scalanative_termios_vmin")
  def VMIN: CInt = extern
  @name("scalanative_termios_vquit")
  def VQUIT: CInt = extern
  @name("scalanative_termios_vstart")
  def VSTART: CInt = extern
  @name("scalanative_termios_vstop")
  def VSTOP: CInt = extern
  @name("scalanative_termios_vsusp")
  def VSUSP: CInt = extern
  @name("scalanative_termios_vtime")
  def VTIME: CInt = extern

  // Input Modes - symbolic constants for use as flags in the c_iflag field

  @name("scalanative_termios_brkint")
  def BRKINT: CInt = extern
  @name("scalanative_termios_icrnl")
  def ICRNL: CInt = extern
  @name("scalanative_termios_ignbrk")
  def IGNBRK: CInt = extern
  @name("scalanative_termios_igncr")
  def IGNCR: CInt = extern
  @name("scalanative_termios_ignpar")
  def IGNPAR: CInt = extern
  @name("scalanative_termios_inlcr")
  def INLCR: CInt = extern
  @name("scalanative_termios_inpck")
  def INPCK: CInt = extern
  @name("scalanative_termios_istrip")
  def ISTRIP: CInt = extern
  @name("scalanative_termios_ixany")
  def IXANY: CInt = extern
  @name("scalanative_termios_ixoff")
  def IXOFF: CInt = extern
  @name("scalanative_termios_ixon")
  def IXON: CInt = extern
  @name("scalanative_termios_parmrk")
  def PARMRK: CInt = extern

  // Output Modes - symbolic constants for use as flags in the c_oflag field

  @name("scalanative_termios_opost")
  def OPOST: CInt = extern
  // [XSI] follows
  @name("scalanative_termios_onlcr")
  def ONLCR: CInt = extern
  @name("scalanative_termios_ocrnl")
  def OCRNL: CInt = extern
  @name("scalanative_termios_onocr")
  def ONOCR: CInt = extern
  @name("scalanative_termios_onlret")
  def ONLRET: CInt = extern
  @name("scalanative_termios_ofdel")
  def OFDEL: CInt = extern
  @name("scalanative_termios_ofill")
  def OFILL: CInt = extern
  // Masks - Linux [requires _BSD_SOURCE or _SVID_SOURCE or _XOPEN_SOURCE]
  @name("scalanative_termios_nldly")
  def NLDLY: CInt = extern
  @name("scalanative_termios_nl0")
  def NL0: CInt = extern
  @name("scalanative_termios_nl1")
  def NL1: CInt = extern
  @name("scalanative_termios_crdly")
  def CRDLY: CInt = extern
  @name("scalanative_termios_cr0")
  def CR0: CInt = extern
  @name("scalanative_termios_cr1")
  def CR1: CInt = extern
  @name("scalanative_termios_cr2")
  def CR2: CInt = extern
  @name("scalanative_termios_cr3")
  def CR3: CInt = extern
  @name("scalanative_termios_tabdly")
  def TABDLY: CInt = extern
  @name("scalanative_termios_tab0")
  def TAB0: CInt = extern
  @name("scalanative_termios_tab1")
  def TAB1: CInt = extern
  @name("scalanative_termios_tab2")
  def TAB2: CInt = extern
  @name("scalanative_termios_tab3")
  def TAB3: CInt = extern
  @name("scalanative_termios_bsdly")
  def BSDLY: CInt = extern
  @name("scalanative_termios_bs0")
  def BS0: CInt = extern
  @name("scalanative_termios_bs1")
  def BS1: CInt = extern
  @name("scalanative_termios_vtdly")
  def VTDLY: CInt = extern
  @name("scalanative_termios_vt0")
  def VT0: CInt = extern
  @name("scalanative_termios_vt1")
  def VT1: CInt = extern
  @name("scalanative_termios_ffdly")
  def FFDLY: CInt = extern
  @name("scalanative_termios_ff0")
  def FF0: CInt = extern
  @name("scalanative_termios_ff1")
  def FF1: CInt = extern

  // Baud Rate Selection - valid values for objects of type speed_t

  @name("scalanative_termios_b0")
  def B0: CInt = extern
  @name("scalanative_termios_b50")
  def B50: CInt = extern
  @name("scalanative_termios_b75")
  def B75: CInt = extern
  @name("scalanative_termios_b110")
  def B110: CInt = extern
  @name("scalanative_termios_b134")
  def B134: CInt = extern
  @name("scalanative_termios_b150")
  def B150: CInt = extern
  @name("scalanative_termios_b200")
  def B200: CInt = extern
  @name("scalanative_termios_b300")
  def B300: CInt = extern
  @name("scalanative_termios_b600")
  def B600: CInt = extern
  @name("scalanative_termios_b1200")
  def B1200: CInt = extern
  @name("scalanative_termios_b1800")
  def B1800: CInt = extern
  @name("scalanative_termios_b2400")
  def B2400: CInt = extern
  @name("scalanative_termios_b4800")
  def B4800: CInt = extern
  @name("scalanative_termios_b9600")
  def B9600: CInt = extern
  @name("scalanative_termios_b19200")
  def B19200: CInt = extern
  @name("scalanative_termios_b38400")
  def B38400: CInt = extern

  // Control Modes - symbolic constants for use as flags in the c_cflag field

  @name("scalanative_termios_csize")
  def CSIZE: CInt = extern
  @name("scalanative_termios_cs5")
  def CS5: CInt = extern
  @name("scalanative_termios_cs6")
  def CS6: CInt = extern
  @name("scalanative_termios_cs7")
  def CS7: CInt = extern
  @name("scalanative_termios_cs8")
  def CS8: CInt = extern
  @name("scalanative_termios_cstopb")
  def CSTOPB: CInt = extern
  @name("scalanative_termios_cread")
  def CREAD: CInt = extern
  @name("scalanative_termios_parenb")
  def PARENB: CInt = extern
  @name("scalanative_termios_parodd")
  def PARODD: CInt = extern
  @name("scalanative_termios_hupcl")
  def HUPCL: CInt = extern
  @name("scalanative_termios_clocal")
  def CLOCAL: CInt = extern

  // Local Modes - symbolic constants for use as flags in the c_lflag field

  @name("scalanative_termios_echo")
  def ECHO: CInt = extern
  @name("scalanative_termios_echoe")
  def ECHOE: CInt = extern
  @name("scalanative_termios_echok")
  def ECHOK: CInt = extern
  @name("scalanative_termios_echonl")
  def ECHONL: CInt = extern
  @name("scalanative_termios_icanon")
  def ICANON: CInt = extern
  @name("scalanative_termios_iexten")
  def IEXTEN: CInt = extern
  @name("scalanative_termios_isig")
  def ISIG: CInt = extern
  @name("scalanative_termios_noflsh")
  def NOFLSH: CInt = extern
  @name("scalanative_termios_tostop")
  def TOSTOP: CInt = extern

  // Attribute Selection - symbolic constants for use with tcsetattr()

  @name("scalanative_termios_tcsanow")
  def TCSANOW: CInt = extern
  @name("scalanative_termios_tcsadrain")
  def TCSADRAIN: CInt = extern
  @name("scalanative_termios_tcsaflush")
  def TCSAFLUSH: CInt = extern

  // Line Control - symbolic constants for use with tcflush()

  @name("scalanative_termios_tciflush")
  def TCIFLUSH: CInt = extern
  @name("scalanative_termios_tcioflush")
  def TCIOFLUSH: CInt = extern
  @name("scalanative_termios_tcoflush")
  def TCOFLUSH: CInt = extern

  // Line Control cont. - symbolic constants for use with tcflow()

  @name("scalanative_termios_tcioff")
  def TCIOFF: CInt = extern
  @name("scalanative_termios_tcion")
  def TCION: CInt = extern
  @name("scalanative_termios_tcooff")
  def TCOOFF: CInt = extern
  @name("scalanative_termios_tcoon")
  def TCOON: CInt = extern

}
