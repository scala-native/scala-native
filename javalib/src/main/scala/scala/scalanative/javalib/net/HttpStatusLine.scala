package scala.scalanative.javalib.net

/** Parts of an HTTP status line such as `HTTP/1.1 200 OK`. */
object HttpStatusLine {

  /** The status code, or -1 if `line` is not a valid HTTP status line. */
  def code(line: String): Int = {
    val sp = codeStart(line)
    if (sp < 0) -1
    else {
      val sp2 = line.indexOf(' ', sp + 1)
      try
        Integer.parseInt(
          line.substring(sp + 1, if (sp2 > 0) sp2 else line.length)
        )
      catch { case _: NumberFormatException => -1 }
    }
  }

  /** The reason phrase, or `null` if there is none. */
  def message(line: String): String = {
    val sp = codeStart(line)
    if (sp < 0) null
    else {
      val sp2 = line.indexOf(' ', sp + 1)
      if (sp2 > 0) line.substring(sp2 + 1) else null
    }
  }

  private def codeStart(line: String): Int =
    if (line == null || !line.startsWith("HTTP/")) -1 else line.indexOf(' ')
}
