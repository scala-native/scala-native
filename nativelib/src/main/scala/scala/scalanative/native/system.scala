package scala.scalanative.native

object system {

  /**
   * Sets an environment variable.
   * @param name of the variable - must not be null, empty, or contain the = character
   * @param value of the variable
   * @param overwrite if overwriting the existing value is desired
   * @return true if successful, false if not enough memory in the environment
   */
  def setenv(name: String,
             value: String,
             overwrite: Boolean = true): Boolean = {
    checkName(name)
    val ow = if (overwrite) 1 else 0
    Zone { implicit z =>
      val res = stdlib.setenv(toCString(name), toCString(value), ow)
      if (res == 0) true else false
    }
  }

  /**
   * Unset the environment variable.
   * @param name of the variable - must not be null, empty, or contain the = character
   */
  def unsetenv(name: String): Unit = {
    checkName(name)
    Zone { implicit z =>
      stdlib.unsetenv(toCString(name))
    }
  }

  private[system] def checkName(name: String): Unit = {
    if (name == null || name.isEmpty || name.contains('='))
      throw new IllegalArgumentException(s"name is invalid '$name'")
  }

}
