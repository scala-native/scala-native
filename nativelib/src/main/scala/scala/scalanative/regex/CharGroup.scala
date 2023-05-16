package scala.scalanative
package regex

class CharGroup(val sign: Int, val cls: Array[Int])

object CharGroup {
  private val code1 = Array(0x30, 0x39) // \d
  private val code2 = Array(0x9, 0xa, 0xc, 0xd, 0x20, 0x20) // \s
  private val code3 =
    Array(0x30, 0x39, 0x41, 0x5a, 0x5f, 0x5f, 0x61, 0x7a) // \w
  private val code4 = Array(0x30, 0x39, 0x41, 0x5a, 0x61, 0x7a) // p{Alnum}
  private val code5 = Array(0x41, 0x5a, 0x61, 0x7a) // p{Alpha}
  private val code6 = Array(0x0, 0x7f) // p{ASCII}
  private val code7 = Array(0x9, 0x9, 0x20, 0x20) // p{Blank}
  private val code8 = Array(0x0, 0x1f, 0x7f, 0x7f) // p{Cntrl}
  private val code9 = Array(0x30, 0x39) // p{Digit}
  private val code10 = Array(0x21, 0x7e) // p{Graph}
  private val code11 = Array(0x61, 0x7a) // p{Lower}
  private val code12 = Array(0x20, 0x7e) // p{Print}
  private val code13 =
    Array(0x21, 0x2f, 0x3a, 0x40, 0x5b, 0x60, 0x7b, 0x7e) // p{Punct}
  private val code14 = Array(0x9, 0xd, 0x20, 0x20) // p{Space}
  private val code15 = Array(0x41, 0x5a) // p{Upper}
  private val code16 = Array(0x30, 0x39, 0x41, 0x46, 0x61, 0x66) // p{XDigit}

  val PERL_GROUPS = Map(
    "\\d" -> new CharGroup(+1, code1),
    "\\D" -> new CharGroup(-1, code1),
    "\\s" -> new CharGroup(+1, code2),
    "\\S" -> new CharGroup(-1, code2),
    "\\w" -> new CharGroup(+1, code3),
    "\\W" -> new CharGroup(-1, code3)
  )

  val POSIX_GROUPS = Map(
    "Alnum" -> new CharGroup(+1, code4),
    "Alpha" -> new CharGroup(+1, code5),
    "ASCII" -> new CharGroup(+1, code6),
    "Blank" -> new CharGroup(+1, code7),
    "Cntrl" -> new CharGroup(+1, code8),
    "Digit" -> new CharGroup(+1, code9),
    "Graph" -> new CharGroup(+1, code10),
    "Lower" -> new CharGroup(+1, code11),
    "Print" -> new CharGroup(+1, code12),
    "Punct" -> new CharGroup(+1, code13),
    "Space" -> new CharGroup(+1, code14),
    "Upper" -> new CharGroup(+1, code15),
    "XDigit" -> new CharGroup(+1, code16)
  )
}
