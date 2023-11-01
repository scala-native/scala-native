package java.util.zip

private[util] trait ZipConstants {
  final val LOCSIG = 67324752L
  final val EXTSIG = 134695760L
  final val CENSIG = 33639248L
  final val ENDSIG = 101010256L
  final val LOCHDR = 30
  final val EXTHDR = 16
  final val CENHDR = 46
  final val ENDHDR = 22
  final val LOCVER = 4
  final val LOCFLG = 6
  final val LOCHOW = 8
  final val LOCTIM = 10
  final val LOCCRC = 14
  final val LOCSIZ = 18
  final val LOCLEN = 22
  final val LOCNAM = 26
  final val LOCEXT = 28
  final val EXTCRC = 4
  final val EXTSIZ = 8
  final val EXTLEN = 12
  final val CENVEM = 4
  final val CENVER = 6
  final val CENFLG = 8
  final val CENHOW = 10
  final val CENTIM = 12
  final val CENCRC = 16
  final val CENSIZ = 20
  final val CENLEN = 24
  final val CENNAM = 28
  final val CENEXT = 30
  final val CENCOM = 32
  final val CENDSK = 34
  final val CENATT = 36
  final val CENATX = 38
  final val CENOFF = 42
  final val ENDSUB = 8
  final val ENDTOT = 10
  final val ENDSIZ = 12
  final val ENDOFF = 16
  final val ENDCOM = 20
}
