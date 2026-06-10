package scala.scalanative.nio.fs.zipfs

import java.net.URI
import java.nio.file.{LinkOption, NoSuchFileException, Path, Paths}

private[zipfs] object ZipUtils {

  /** True iff `env[key]` is either the string `"true"` or the boolean `true`.
   *  Matches the jdk.zipfs convention of accepting both forms for the `create`
   *  key. `accessMode` is a string enum and parsed separately.
   */
  def envFlag(env: java.util.Map[String, _], key: String): Boolean = {
    if (env == null) false
    else {
      val v = env.get(key)
      v match {
        case b: java.lang.Boolean => b.booleanValue()
        case null                 => false
        case _                    => "true" == String.valueOf(v)
      }
    }
  }

  /** Resolve a `jar:`-scheme URI to its underlying archive `Path`. Returns the
   *  archive path plus the optional `inner` part (after `!/`) if present, or
   *  `""` if absent.
   *
   *  Accepts: `jar:file:/a/b.jar`, `jar:file:/a/b.jar!/entry/name`,
   *  percent-encoded characters in either side, archive names containing
   *  literal `!` (only the LAST `!/` separates archive from entry).
   *
   *  Rejects: non-`jar:` scheme, URIs with authority or fragment.
   */
  def parseJarUri(uri: URI): (Path, String) = {
    if (uri == null) throw new NullPointerException("uri")
    val scheme = uri.getScheme()
    if (scheme == null || !scheme.equalsIgnoreCase("jar"))
      throw new IllegalArgumentException(
        s"URI scheme is not 'jar': $uri"
      )
    if (uri.getFragment() != null)
      throw new IllegalArgumentException(s"URI has fragment: $uri")
    // Java's URI parser treats `?` as a query delimiter even for opaque
    // URIs, which would silently truncate an entry tail like `!/a?b.txt`
    // to `!/a`. Reject so the caller has to percent-encode `?` (`%3F`).
    if (uri.getRawQuery() != null)
      throw new IllegalArgumentException(
        s"URI has query (encode `?` in entry name as `%3F`): $uri"
      )

    // `jar:` is an opaque URI; the inner URI lives in scheme-specific-part.
    val ssp = uri.getRawSchemeSpecificPart()
    if (ssp == null || ssp.isEmpty())
      throw new IllegalArgumentException(s"Empty jar URI: $uri")

    val sepIdx = ssp.lastIndexOf("!/")
    val (innerRaw, entry) =
      if (sepIdx < 0) (ssp, "")
      else (ssp.substring(0, sepIdx), ssp.substring(sepIdx + 1))

    val inner =
      try new URI(innerRaw)
      catch {
        case e: Exception =>
          throw new IllegalArgumentException(
            s"Malformed inner URI in $uri: ${e.getMessage()}",
            e
          )
      }
    if (inner.getAuthority() != null)
      throw new IllegalArgumentException(
        s"Inner URI must not have authority: $uri"
      )
    val innerScheme = inner.getScheme()
    if (innerScheme == null)
      throw new IllegalArgumentException(
        s"Inner URI must have a scheme: $uri"
      )

    // Only file: is supported for v1. Other schemes (http, etc.) would
    // need a streaming ZipInputStream-backed FS.
    if (!innerScheme.equalsIgnoreCase("file"))
      throw new IllegalArgumentException(
        s"Unsupported inner URI scheme '$innerScheme' (only 'file' is supported)"
      )

    val archive = Paths.get(inner)
    (archive, entry)
  }

  /** Canonicalise `archive` to a stable registry key:
   *    - `toRealPath()` if the file exists,
   *    - else (file missing) `toAbsolutePath().normalize()`.
   *
   *  Other I/O failures from `toRealPath` (permission denied, symlink loop, …)
   *  propagate — they're real errors, not "file does not exist".
   */
  def canonicalize(archive: Path): Path = {
    try archive.toRealPath(Array.empty[LinkOption])
    catch {
      case _: NoSuchFileException => archive.toAbsolutePath().normalize()
    }
  }

  /** Percent-decode `%xx`-escaped UTF-8 bytes in the given string, leaving
   *  every other character (including `+`, `?`, `#`, `&`, etc.) untouched.
   *
   *  This is the right decoder for ZIP-entry names sitting in the tail of a
   *  `jar:` URI's SSP. Cannot use `URLDecoder.decode` (form-encoding — turns
   *  `+` into a space). Cannot delegate to `new URI("file://" + s) .getPath()`
   *  either, because URI parsing strips `?...` as a query component and `#...`
   *  as a fragment, silently truncating entry names that legitimately contain
   *  those characters.
   *
   *  Malformed `%xx` (out-of-bounds, non-hex) → `IllegalArgumentException`.
   */
  def decodeUriPath(raw: String): String = {
    import java.nio.{ByteBuffer, CharBuffer}
    import java.nio.charset.{
      CharacterCodingException, CodingErrorAction, StandardCharsets
    }
    val n = raw.length
    val sb = new java.lang.StringBuilder(n)
    val bytes = new java.io.ByteArrayOutputStream(4)
    // Strict UTF-8 decoder: malformed or unmappable byte sequences raise
    // CharacterCodingException instead of silently inserting U+FFFD.
    val decoder = StandardCharsets.UTF_8
      .newDecoder()
      .onMalformedInput(CodingErrorAction.REPORT)
      .onUnmappableCharacter(CodingErrorAction.REPORT)
    def flushBytes(): Unit = {
      if (bytes.size() > 0) {
        try {
          val cb = decoder.decode(ByteBuffer.wrap(bytes.toByteArray()))
          sb.append(cb.toString)
        } catch {
          case e: CharacterCodingException =>
            throw new IllegalArgumentException(
              s"Malformed UTF-8 percent-escape sequence in: $raw",
              e
            )
        } finally {
          bytes.reset()
          decoder.reset()
        }
      }
    }
    var i = 0
    while (i < n) {
      val c = raw.charAt(i)
      if (c == '%') {
        if (i + 2 >= n)
          throw new IllegalArgumentException(
            s"Truncated percent-escape at index $i in: $raw"
          )
        val hi = Character.digit(raw.charAt(i + 1), 16)
        val lo = Character.digit(raw.charAt(i + 2), 16)
        if (hi < 0 || lo < 0)
          throw new IllegalArgumentException(
            s"Malformed percent-escape at index $i in: $raw"
          )
        bytes.write((hi << 4) | lo)
        i += 3
      } else {
        flushBytes()
        sb.append(c)
        i += 1
      }
    }
    flushBytes()
    sb.toString
  }

  /** Cheap "looks like a zip?" probe used to discriminate "not my format" from
   *  "corrupt zip" in path-based provider probing.
   *
   *  ZIP files start with one of:
   *    - `PK\003\004` — local file header (any non-empty archive)
   *    - `PK\005\006` — EOCD (empty archive, no entries)
   *    - `PK\007\010` — spanned/split archive marker (rare; still a zip)
   *
   *  If the file is shorter than 4 bytes or its prefix is none of those, it is
   *  not a zip and the provider should yield UOE so probing advances to the
   *  next candidate.
   */
  def looksLikeZip(archive: Path): Boolean = {
    val ch = java.nio.file.Files.newByteChannel(
      archive,
      java.util.Collections
        .singleton(java.nio.file.StandardOpenOption.READ),
      Array.empty[java.nio.file.attribute.FileAttribute[_]]
    )
    try {
      val buf = java.nio.ByteBuffer.allocate(4)
      var total = 0
      while (total < 4) {
        val n = ch.read(buf)
        if (n < 0) return false
        total += n
      }
      val arr = buf.array()
      val isPK = arr(0) == 'P'.toByte && arr(1) == 'K'.toByte
      isPK && (
        (arr(2) == 3 && arr(3) == 4) ||
        (arr(2) == 5 && arr(3) == 6) ||
        (arr(2) == 7 && arr(3) == 8)
      )
    } finally ch.close()
  }
}
