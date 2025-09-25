package java.lang.process

private[process] object WindowsUtils {

  // https://learn.microsoft.com/en-us/windows/win32/api/shellapi/nf-shellapi-commandlinetoargvw#remarks
  def argvToCommand(argv: java.util.Iterator[String]): String = {
    val sb = new java.lang.StringBuilder
    val sbBackslash = new java.lang.StringBuilder
    def appendBackslashes(double: Boolean): Unit = {
      val cnt = sbBackslash.length()
      if (cnt != 0) {
        sb.append(sbBackslash, 0, cnt)
        if (double) sb.append(sbBackslash, 0, cnt)
        sbBackslash.setLength(0)
      }
    }

    argv.forEachRemaining { arg =>
      if (sb.length() != 0) sb.append(' ')
      if (arg.isEmpty || arg.exists(c => c.isWhitespace || c == '"')) {
        sb.append('"')
        arg.foreach { c =>
          if (c == '\\') sbBackslash.append(c)
          else {
            val isQuote = c == '"'
            appendBackslashes(double = isQuote) // double them before a quote
            if (isQuote) sb.append('\\') // escape the quote
            sb.append(c)
          }
        }
        appendBackslashes(double = true) // if trailing
        sb.append('"')
      } else sb.append(arg)
    }

    sb.toString
  }

}
