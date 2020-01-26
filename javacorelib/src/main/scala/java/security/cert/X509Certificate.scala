package java.security.cert

import javax.security.auth.x500.X500Principal

abstract class X509Certificate extends Certificate("X.509") with X509Extension {

  def getIssuerX500Principal(): X500Principal

}
