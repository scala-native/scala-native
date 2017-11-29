
package scala.scalanative
package posix

import scalanative.native._

@extern
object termios {

  type pid_t = CInt
  type speed_t = CUnsignedLongInt
  type _20 = Nat.Digit[Nat._2, Nat._0]
  type cc_t = CArray[CUnsignedChar, _20]
  type tcflag_t = CUnsignedLongInt

  type termios = CStruct7[ /* termios */
    tcflag_t, /* c_iflag: input flags */
    tcflag_t, /* c_oflag: output flags */
    tcflag_t, /* c_cflag: control flags */
    tcflag_t, /* c_lflag: local flags */
    cc_t,     /* c_cc[NCCS]: control chars */
    speed_t,  /* c_ispeed: input speed */
    speed_t   /* c_ospeed: output speed */
  ]

  type termios_ptr = Ptr[termios]

  @name("scalanative_b0")
  def B0: CInt = extern

  @name("scalanative_b50")
  def B50: CInt = extern

  @name("scalanative_b75")
  def B75: CInt = extern

  @name("scalanative_b110")
  def B110: CInt = extern

  @name("scalanative_b134")
  def B134: CInt = extern

  @name("scalanative_b150")
  def B150: CInt = extern

  @name("scalanative_b200")
  def B200: CInt = extern

  @name("scalanative_b300")
  def B300: CInt = extern

  @name("scalanative_b600")
  def B600: CInt = extern

  @name("scalanative_b1200")
  def B1200: CInt = extern

  @name("scalanative_b1800")
  def B1800: CInt = extern

  @name("scalanative_b2400")
  def B2400: CInt = extern

  @name("scalanative_b4800")
  def B4800: CInt = extern

  @name("scalanative_b9600")
  def B9600: CInt = extern

  @name("scalanative_b19200")
  def B19200: CInt = extern

  @name("scalanative_b38400")
  def B38400: CInt = extern


  @name("scalanative_brkint")
  def BRKINT: CInt = extern

  @name("scalanative_icrnl")
  def ICRNL: CInt = extern

  @name("scalanative_ignbrk")
  def IGNBRK: CInt = extern

  @name("scalanative_igncr")
  def IGNCR: CInt = extern

  @name("scalanative_ignpar")
  def IGNPAR: CInt = extern

  @name("scalanative_inlcr")
  def INLCR: CInt = extern

  @name("scalanative_inpck")
  def INPCK: CInt = extern

  @name("scalanative_istrip")
  def ISTRIP: CInt = extern

  @name("scalanative_ixoff")
  def IXOFF: CInt = extern

  @name("scalanative_ixon")
  def IXON: CInt = extern

  @name("scalanative_parmrk")
  def PARMRK: CInt = extern


  @name("scalanative_opost")
  def OPOST: CInt = extern


  @name("scalanative_clocal")
  def CLOCAL: CInt = extern

  @name("scalanative_cread")
  def CREAD: CInt = extern

  @name("scalanative_cs5")
  def CS5: CInt = extern

  @name("scalanative_cs6")
  def CS6: CInt = extern

  @name("scalanative_cs7")
  def CS7: CInt = extern

  @name("scalanative_cs8")
  def CS8: CInt = extern

  @name("scalanative_csize")
  def CSIZE: CInt = extern

  @name("scalanative_cstopb")
  def CSTOPB: CInt = extern

  @name("scalanative_hupcl")
  def HUPCL: CInt = extern

  @name("scalanative_parenb")
  def PARENB: CInt = extern

  @name("scalanative_parodd")
  def PARODD: CInt = extern


  @name("scalanative_echo")
  def ECHO: CInt = extern

  @name("scalanative_echoe")
  def ECHOE: CInt = extern

  @name("scalanative_echok")
  def ECHOK: CInt = extern

  @name("scalanative_echonl")
  def ECHONL: CInt = extern

  @name("scalanative_icanon")
  def ICANON: CInt = extern

  @name("scalanative_iexten")
  def IEXTEN: CInt = extern

  @name("scalanative_isig")
  def ISIG: CInt = extern

  @name("scalanative_noflsh")
  def NOFLSH: CInt = extern

  @name("scalanative_tostop")
  def TOSTOP: CInt = extern


  @name("scalanative_tciflush")
  def TCIFLUSH: CInt = extern

  @name("scalanative_tcoflush")
  def TCOFLUSH: CInt = extern

  @name("scalanative_tcioflush")
  def TCIOFLUSH: CInt = extern

  @name("scalanative_tcooff")
  def TCOOFF: CInt = extern

  @name("scalanative_tcoon")
  def TCOON: CInt = extern

  @name("scalanative_tcioff")
  def TCIOFF: CInt = extern

  @name("scalanative_tcion")
  def TCION: CInt = extern


  @name("scalanative_tcsadrain")
  def TCSADRAIN: CInt = extern

  @name("scalanative_tcsaflush")
  def TCSAFLUSH: CInt = extern

  @name("scalanative_tcsanow")
  def TCSANOW: CInt = extern


  @name("scalanative_veof")
  def VEOF: CInt = extern

  @name("scalanative_veol")
  def VEOL: CInt = extern

  @name("scalanative_verase")
  def VERASE: CInt = extern

  @name("scalanative_vintr")
  def VINTR: CInt = extern

  @name("scalanative_vkill")
  def VKILL: CInt = extern

  @name("scalanative_vmin")
  def VMIN: CInt = extern

  @name("scalanative_vquit")
  def VQUIT: CInt = extern

  @name("scalanative_vstar")
  def VSTAR: CInt = extern

  @name("scalanative_vstop")
  def VSTOP: CInt = extern

  @name("scalanative_vsusp")
  def VSUSP: CInt = extern

  @name("scalanative_vtime")
  def VTIME: CInt = extern

  @name("scalanative_nccs")
  def NCCS: CInt = extern


  @name("scalanative_imaxbel")
  def IMAXBEL: CInt = extern


  @name("scalanative_echoke")
  def ECHOKE: CInt = extern

  @name("scalanative_echoctl")
  def ECHOCTL: CInt = extern


  @name("scalanative_onlcr")
  def ONLCR: CInt = extern

  @name("scalanative_ocrnl")
  def OCRNL: CInt = extern

  @name("scalanative_cfgetispeed")
  def cfgetispeed(termios_p: termios_ptr): speed_t = extern

  @name("scalanative_cfgetospeed")
  def cfgetospeed(termios_p: termios_ptr): speed_t = extern

  @name("scalanative_cfsetispeed")
  def cfsetispeed(termios_p: termios_ptr, speed: speed_t): CInt = extern

  @name("scalanative_cfsetospeed")
  def cfsetospeed(termios_p: termios_ptr, speed: speed_t): CInt = extern

  @name("scalanative_tcgetattr")
  def tcgetattr(fd: CInt, termios_p: termios_ptr): CInt = extern

  @name("scalanative_tcsetattr")
  def tcsetattr(fd: CInt, opertional_actions: CInt, termios_p: termios_ptr): CInt = extern

  @name("scalanative_tcdrain")
  def tcdrain(fd: CInt): CInt = extern

  @name("scalanative_tcflow")
  def tcflow(fd: CInt, action: CInt): CInt = extern

  @name("scalanative_tcflush")
  def tcflush(fd: CInt, queue_selector: CInt): CInt = extern

  @name("scalanative_tcsendbreak")
  def tcsendbreak(fd: CInt, duration: CInt): CInt = extern

  @name("scalanative_tcgetsid")
  def tcgetsid(fd: CInt): pid_t = extern
}