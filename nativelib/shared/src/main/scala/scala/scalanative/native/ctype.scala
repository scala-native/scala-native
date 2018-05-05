package scala.scalanative
package native

@extern
object ctype {
  def isascii(c: CInt): CInt     = extern
  def isalnum(c: CInt): CInt     = extern
  def isalpha(c: CInt): CInt     = extern
  def isblank(c: CInt): CInt     = extern
  def iscntrl(c: CInt): CInt     = extern
  def isdigit(c: CInt): CInt     = extern
  def isgraph(c: CInt): CInt     = extern
  def islower(c: CInt): CInt     = extern
  def isprint(c: CInt): CInt     = extern
  def ispunct(c: CInt): CInt     = extern
  def isspace(c: CInt): CInt     = extern
  def isupper(c: CInt): CInt     = extern
  def isxdigit(c: CInt): CInt    = extern
  def toascii(c: CInt): CInt     = extern
  def tolower(c: CInt): CInt     = extern
  def toupper(c: CInt): CInt     = extern
  def digittoint(c: CInt): CInt  = extern
  def ishexnumber(c: CInt): CInt = extern
  def isideogram(c: CInt): CInt  = extern
  def isnumber(c: CInt): CInt    = extern
  def isphonogram(c: CInt): CInt = extern
  def isrune(c: CInt): CInt      = extern
  def isspecial(c: CInt): CInt   = extern
}
