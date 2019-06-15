package java.nio.file.attribute

import java.io.IOException

class UserPrincipalNotFoundException(name: String) extends IOException {
  def getName(): String = name
}
