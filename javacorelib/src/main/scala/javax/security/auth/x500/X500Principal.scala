package javax.security.auth.x500

import java.security.Principal

final class X500Principal(name: String) extends Principal with Serializable {
  override def getName(): String = name
}
