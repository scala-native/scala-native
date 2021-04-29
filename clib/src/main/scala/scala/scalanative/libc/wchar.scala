package scala.scalanative
package libc

import scalanative.unsafe._
import stdio._

@extern
object wchar {
  type wint_t    = CInt
  type wctype_t  = CInt
  type wchar_t   = CWideChar
  type WString   = CWideString
  type mbstate_t = Byte
  type mbstate   = Ptr[mbstate_t]

  def btowc(c: Int): wint_t                                     = extern
  def fgetwc(file: Ptr[FILE]): wint_t                           = extern
  def fgetws(ws: WString, num: Int, stream: Ptr[FILE]): WString = extern
  def fputwc(wc: wchar_t, stream: Ptr[FILE]): wint_t            = extern
  def fputws(ws: WString, stream: Ptr[FILE]): CInt              = extern
  def fwide(stream: Ptr[FILE], mode: Int): CInt                 = extern
  def fwprintf(stream: Ptr[FILE], format: WString, args: CVarArgList): CInt =
    extern
  def fwscanf(stream: Ptr[FILE], format: WString, args: CVarArgList): CInt =
    extern
  def getwc(stream: Ptr[FILE]): wint_t                     = extern
  def getwchar(): wint_t                                   = extern
  def mbrlen(pmb: CString, max: CSize, ps: mbstate): CSize = extern
  def mbrtowc(pwc: WString, pmb: CString, max: CSize, ps: mbstate): CSize =
    extern
  def mbsinit(ps: mbstate): CInt = extern
  def mbsrtowcs(dest: WString,
                src: Ptr[CString],
                max: CSize,
                ps: mbstate): CSize                 = extern
  def putwc(wc: wchar_t, stream: Ptr[FILE]): wint_t = extern
  def putwchar(wc: wchar_t): wint_t                 = extern
  def swprintf(ws: WString,
               len: CSize,
               format: WString,
               args: CVarArgList): CInt                              = extern
  def swscanf(ws: WString, format: WString, args: CVarArgList): CInt = extern
  def ungetwc(wc: wint_t, stream: Ptr[FILE]): wint_t                 = extern
  def vfwprintf(stream: Ptr[FILE], format: WString, args: CVarArgList): CInt =
    extern
  def vfwscanf(stream: Ptr[FILE], format: WString, args: CVarArgList): CInt =
    extern
  def vwprintf(format: WString, args: CVarArgList): CInt = extern
  def vswprintf(ws: WString,
                len: CSize,
                format: WString,
                args: CVarArgList): CInt                              = extern
  def vswscanf(ws: WString, format: WString, args: CVarArgList): CInt = extern
  def vwscanf(format: WString, args: CVarArgList): CInt               = extern
  def wcrtomb(pmb: CString, wc: wchar_t, ps: mbstate): CSize          = extern
  def wcscat(destination: WString, source: Ptr[WString]): WString     = extern
  def wcschr(ws: WString, wc: wchar_t): WString                       = extern
  def wcscmp(wcs1: WString, wcs2: WString): CInt                      = extern
  def wcscoll(wcs1: WString, wcs2: WString): CInt                     = extern
  def wcscpy(destination: WString, source: WString): WString          = extern
  def wcscspn(wcs1: WString, wcs2: WString): CSize                    = extern
// Skipped as it would create circular dependency with posixlib
//  def wcsftime(ptr: WString,
//               maxSize: CSize,
//               format: WString,
//               timeptr: Ptr[tm]): CSize = extern
  def wcslen(ws: WString): CSize = extern
  def wcsncat(destination: WString, source: WString, num: CSize): WString =
    extern
  def wcsncmp(wcs1: WString, wcs2: WString, num: CSize): CInt = extern
  def wcsncpy(destination: WString, source: WString, num: CSize): WString =
    extern
  def wcspbrk(wcs1: WString, wcs2: WString): WString = extern
  def wcsrchr(ws: WString, wc: wchar_t): WString     = extern
  def wcsrtombs(dest: CString,
                src: Ptr[WString],
                max: CSize,
                ps: mbstate): CSize                       = extern
  def wcsspn(wcs1: WString, wcs2: WString): CSize         = extern
  def wcsstr(wcs1: WString, wcs2: WString): WString       = extern
  def wcstod(wcs: WString, endPtr: Ptr[WString]): CDouble = extern
  def wcstof(wcs: WString, endPtr: Ptr[WString]): CFloat  = extern
  def wcstok(wcs: WString, delimiters: WString, p: Ptr[WString]): WString =
    extern
  def wcstol(wcs: WString, endPtr: Ptr[WString], base: CInt): CLongInt = extern
  def wcstold(wcs: WString, endPtr: Ptr[WString]): CDouble             = extern
  def wcstoll(wcs: WString, endPtr: Ptr[WString], base: CInt): CLongLong =
    extern
  def wcstoul(wcs: WString, endPtr: Ptr[WString], base: CInt): CUnsignedLong =
    extern
  def wcstoullwcs(wcs: WString,
                  endPtr: Ptr[WString],
                  base: CInt): CUnsignedLongLong                        = extern
  def wcsxfrm(destination: WString, source: WString, num: CSize): CSize = extern
  def wctob(wc: wint_t): CInt                                           = extern
  def wmemchr(ptr: WString, wc: wchar_t, num: CSize): WString           = extern
  def wmemcmp(ptr: WString, wc: wchar_t, num: CSize): CInt              = extern
  def wmemcpy(destination: WString, source: WString, num: CSize): WString =
    extern
  def wmemmove(destination: WString, source: WString, num: CSize): WString =
    extern
  def wmemset(ptr: WString, wc: wchar_t, num: CSize): WString = extern
  def wprintf(format: WString, args: CVarArgList): CInt       = extern
  def wscanf(format: WString, args: CVarArgList): CInt        = extern

  // XSI extension methods
  def iswalnum(c: wint_t): CInt                = extern
  def iswalpha(c: wint_t): CInt                = extern
  def iswcntrl(c: wint_t): CInt                = extern
  def iswctype(c: wint_t, tpe: wctype_t): CInt = extern
  def iswdigit(c: wint_t): CInt                = extern
  def iswgraph(c: wint_t): CInt                = extern
  def iswlower(c: wint_t): CInt                = extern
  def iswprint(c: wint_t): CInt                = extern
  def iswpunct(c: wint_t): CInt                = extern
  def iswspace(c: wint_t): CInt                = extern
  def iswupper(c: wint_t): CInt                = extern
  def iswxdigit(c: wint_t): CInt               = extern

  def towlower(c: wint_t): wint_t = extern
  def towupper(c: wint_t): wint_t = extern

  def wcswcs(wcs1: WString, wcs2: WString): WString = extern
  def wcswidth(wcs: WString, num: CSize): CInt      = extern
  def wctype(str: CString): wctype_t                = extern
  def wcwidth(wc: wchar_t): CInt                    = extern
}
