package java.util
package regex

import scalanative.native._
import scalanative.libc._, stdlib._, stdio._, string._
import cre2h._
import annotation.tailrec

// Inspired by: https://github.com/google/re2j/blob/master/java/com/google/re2j/Pattern.java

object Pattern {
  def CANON_EQ: Int                = 128
  def CASE_INSENSITIVE: Int        = 2
  def COMMENTS: Int                = 4
  def DOTALL: Int                  = 32
  def LITERAL: Int                 = 16
  def MULTILINE: Int               = 8
  def UNICODE_CASE: Int            = 64
  def UNICODE_CHARACTER_CLASS: Int = 256
  def UNIX_LINES: Int              = 1

  def compile(regex: String): Pattern = compile(regex, 0)

  def compile(regex: String, flags: Int): Pattern = {
    // make sure the provided regex is compiled
    CompiledPatternStore.withRE2Regex(regex, flags)(_ => ())

    new Pattern(
      _pattern = regex,
      _flags = flags
    )
  }

  def matches(regex: String, input: CharSequence): Boolean =
    compile(regex).matcher(input).matches

  def quote(s: String): String = {
    s"\\Q${s}\\E"
  }

  private object CompiledPatternStore {
    final case class Key(regex: String, flags: Int)
    final class Node(var key: Key,
                     var value: RE2RegExpOps,
                     var rc: Int,
                     var next: Node)

    private def freshNode(next: Node) =
      new Node(null, new RE2RegExpOps(null), 0, next)

    // The tip of Nodes. The Nodes form a ring buffer of some length.
    private var last: Node = {
      // Populate the ringbuffer
      @tailrec def f(n: Node, num: Int): Node =
        if (num < 0) {
          n
        } else {
          f(freshNode(n), num - 1)
        }
      val last = freshNode(null)
      last.next = f(last, 128)
      last
    }

    // Used to quickly look up a Node from a Key.
    private val map = scala.collection.mutable.HashMap.empty[Key, Node]

    private def selectNode(regex: String, flags: Int): Node = {
      // Look up a RE2RegExpOps from the map.
      // If the map doesn't contain the key, look for an unused Node (whose refcount(rc) is 0),
      // delete its old compiled pattern if any, and then compile a new RE2 pattern and cache it
      // before returning it.
      // If all of the nodes are in use, expand the ringbuffer by 1 as a last resort.
      map.get(Key(regex, flags)).getOrElse {
        @tailrec def findUnused(n: Node): Node = {
          if (n eq last) {
            // No unused nodes in the ringbuffer; expand its size by 1
            val newnode = freshNode(last.next)
            last.next = newnode
            newnode
          } else if (n.rc <= 0) {
            n
          } else {
            findUnused(n.next)
          }
        }
        val reused = {
          if (last.rc <= 0) last
          else findUnused(last.next)
        }
        // delete the old pattern (if any)
        map -= reused.key
        if (reused.value.ptr != null) {
          cre2.delete(reused.value.ptr)
          reused.value = new RE2RegExpOps(null)
        }
        // reuse the node by replacing its members with new contents
        reused.key = Key(regex, flags)
        reused.value = doCompile(regex, flags)
        map += reused.key -> reused
        // advance `last` so that it points to the next node (which is likely the least recently used one)
        last = reused.next
        reused
      }
    }

    def withRE2Regex[A](regex: String, flags: Int)(f: RE2RegExpOps => A): A = {
      // increase the refcount of the selected node while in use to prevent it from deleted
      val node = {
        val n = selectNode(regex, flags)
        n.rc += 1
        n
      }
      try f(node.value)
      finally node.rc -= 1
    }

    private def doCompile(regex: String, flags: Int): RE2RegExpOps = Zone {
      implicit z =>
        def notSupported(flag: Int, flagName: String): Unit = {
          if ((flags & flag) == flag) {
            assert(false, s"regex flag $flagName is not supported")
          }
        }

        notSupported(CANON_EQ, "CANON_EQ(canonical equivalences)")
        notSupported(COMMENTS, "COMMENTS")
        notSupported(UNICODE_CASE, "UNICODE_CASE")
        notSupported(UNICODE_CHARACTER_CLASS, "UNICODE_CHARACTER_CLASS")
        notSupported(UNIX_LINES, "UNIX_LINES")

        val options = cre2.optNew()
        try {
          cre2.setCaseSensitive(options, flags & CASE_INSENSITIVE)
          cre2.setDotNl(options, flags & DOTALL)
          cre2.setLiteral(options, flags & LITERAL)
          cre2.setLogErrors(options, 0)

          // setOneLine(false) is only available when limiting ourself to posix_syntax
          // https://github.com/google/re2/blob/2017-03-01/re2/re2.h#L548
          // regex flag MULTILINE cannot be disabled

          val re2 = {
            val regexre2 = alloc[cre2.string_t]
            toRE2String(regex, regexre2)
            cre2.compile(regexre2.data, regexre2.length, options)
          }

          val code = cre2.errorCode(re2)

          if (code != ERROR_NO_ERROR) {
            val errorPattern = {
              val arg = alloc[cre2.string_t]
              cre2.errorArg(re2, arg)
              fromRE2String(arg)
            }

            // we try to find the index of the parsing error
            // this could return the wrong index it only finds the first match
            // see https://groups.google.com/forum/#!topic/re2-dev/rnvFZ9Ki8nk
            val index =
              if (code == ERROR_TRAILING_BACKSLASH) regex.size - 1
              else regex.indexOfSlice(errorPattern)

            val reText = fromCString(cre2.errorString(re2))

            val description =
              code match {
                case ERROR_INTERNAL   => "Internal Error"
                case ERROR_BAD_ESCAPE => "Illegal/unsupported escape sequence"
                case ERROR_BAD_CHAR_CLASS =>
                  "Illegal/unsupported character class"
                case ERROR_BAD_CHAR_RANGE     => "Illegal character range"
                case ERROR_MISSING_BRACKET    => "Unclosed character class"
                case ERROR_MISSING_PAREN      => "Missing parenthesis"
                case ERROR_TRAILING_BACKSLASH => "Trailing Backslash"
                case ERROR_REPEAT_ARGUMENT    => "Dangling meta character '*'"
                case ERROR_REPEAT_SIZE        => "Bad repetition argument"
                case ERROR_REPEAT_OP          => "Bad repetition operator"
                case ERROR_BAD_PERL_OP        => "Bad perl operator"
                case ERROR_BAD_UTF8           => "Invalid UTF-8 in regexp"
                case ERROR_BAD_NAMED_CAPTURE  => "Bad named capture group"
                case ERROR_PATTERN_TOO_LARGE =>
                  "Pattern too large (compilation failed)"
                case _ => reText
              }

            cre2.delete(re2)
            throw new PatternSyntaxException(
              description,
              regex,
              index
            )
          }

          new RE2RegExpOps(re2)
        } finally {
          cre2.optDelete(options)
        }
    }
  }
}

final class Pattern private[regex] (
    _pattern: String,
    _flags: Int
) {

  // this loan pattern makes sure that the instance of cre2.regexp_t is kept alive while in use.
  private[regex] def withRE2Regex[A](f: RE2RegExpOps => A): A =
    Pattern.CompiledPatternStore.withRE2Regex(_pattern, _flags)(f)

  def split(input: CharSequence): Array[String] =
    split(input, 0)

  def split(input: CharSequence, limit: Int): Array[String] =
    split(new Matcher(this, input), limit)

  private def split(m: Matcher, limit: Int): Array[String] = {
    var matchCount = 0
    var arraySize  = 0
    var last       = 0
    while (m.find()) {
      matchCount += 1
      if (limit != 0 || last < m.start()) {
        arraySize = matchCount
      }
      last = m.end()
    }
    if (last < m.inputLength || limit != 0) {
      matchCount += 1
      arraySize = matchCount
    }
    var trunc = 0
    if (limit > 0 && arraySize > limit) {
      arraySize = limit
      trunc = 1
    }

    val array = Array.ofDim[String](arraySize)
    var i     = 0
    last = 0
    m.reset()
    while (m.find() && i < arraySize - trunc) {
      val t = i
      i += 1
      array(t) = m.substring(last, m.start())
      last = m.end()
    }
    if (i < arraySize) {
      array(i) = m.substring(last, m.inputLength)
    }
    array
  }

  def matcher(input: CharSequence): Matcher = new Matcher(this, input)

  def flags: Int                = _flags
  def pattern: String           = _pattern
  override def toString: String = _pattern
}
