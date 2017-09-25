package java.util.zip

private[util] trait ZipConstants {
  final val LOCSIG: Long = 67324752
  final val EXTSIG: Long = 134695760
  final val CENSIG: Long = 33639248L
  final val ENDSIG: Long = 101010256L
  final val LOCHDR: Int  = 30
  final val EXTHDR: Int  = 16
  final val CENHDR: Int  = 46
  final val ENDHDR: Int  = 22
  final val LOCVER: Int  = 4
  final val LOCFLG: Int  = 6
  final val LOCHOW: Int  = 8
  final val LOCTIM: Int  = 10
  final val LOCCRC: Int  = 14
  final val LOCSIZ: Int  = 18
  final val LOCLEN: Int  = 22
  final val LOCNAM: Int  = 26
  final val LOCEXT: Int  = 28
  final val EXTCRC: Int  = 4
  final val EXTSIZ: Int  = 8
  final val EXTLEN: Int  = 12
  final val CENVEM: Int  = 4
  final val CENVER: Int  = 6
  final val CENFLG: Int  = 8
  final val CENHOW: Int  = 10
  final val CENTIM: Int  = 12
  final val CENCRC: Int  = 16
  final val CENSIZ: Int  = 20
  final val CENLEN: Int  = 24
  final val CENNAM: Int  = 28
  final val CENEXT: Int  = 30
  final val CENCOM: Int  = 32
  final val CENDSK: Int  = 34
  final val CENATT: Int  = 36
  final val CENATX: Int  = 38
  final val CENOFF: Int  = 42
  final val ENDSUB: Int  = 8
  final val ENDTOT: Int  = 10
  final val ENDSIZ: Int  = 12
  final val ENDOFF: Int  = 16
  final val ENDCOM: Int  = 20
}
