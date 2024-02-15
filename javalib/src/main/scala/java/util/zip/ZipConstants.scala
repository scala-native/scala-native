package java.util.zip

// FIXME :: describe use of hex to ease matching Linux/macOS "hexdump -C"
// with documentation.
/* Reference:
 *   https://en.wikipedia.org/wiki/ZIP_(file_format) // or your local language
 *
 * Use hexadecimal for *SIG to ease manual parsing of Linux/macOS
 * "hexdump -C foo.zip" output. May your Fate never bring you there.
 *
 * Other fields are decimal, ordered alphabetically, as they are in the
 * JDK description of ZipFile and kin.
 */

private[util] trait ZipConstants {
  // Header signatures
  final val CENSIG = 0x02014b50L // decimal: 33639248L  "PK\1\2"
  final val ENDSIG = 0x06054b50L // decimal: 101010256L "PK\5\6"
  final val EXTSIG = 0x08074b50L // decimal: 134695760L "PK\7\8"
  final val LOCSIG = 0x04034b50L // decimal: 67324752L "PK\3\4"

  // Offsets to fields in various headers
  final val CENATT = 36
  final val CENATX = 38
  final val CENCOM = 32
  final val CENCRC = 16
  final val CENDSK = 34
  final val CENEXT = 30
  final val CENFLG = 8
  final val CENHDR = 46
  final val CENHOW = 10
  final val CENLEN = 24
  final val CENNAM = 28
  final val CENOFF = 42
  // CENSIG is a header, so it is defined above.
  final val CENSIZ = 20
  final val CENTIM = 12
  final val CENVEM = 4
  final val CENVER = 6
  final val ENDCOM = 20
  final val ENDHDR = 22
  final val ENDOFF = 16
  // ENDSIG is a header, so it is defined above.
  final val ENDSIZ = 12
  final val ENDSUB = 8
  final val ENDTOT = 10
  final val EXTCRC = 4
  final val EXTHDR = 16
  final val EXTLEN = 12
  // EXTSIG is a header, so it is defined above.
  final val EXTSIZ = 8
  final val LOCCRC = 14
  final val LOCEXT = 28
  final val LOCFLG = 6
  final val LOCHDR = 30
  final val LOCHOW = 8
  final val LOCLEN = 22
  final val LOCNAM = 26
  // LOCSIG is a header, so it is defined above.
  final val LOCSIZ = 18
  final val LOCTIM = 10
  final val LOCVER = 4
}
