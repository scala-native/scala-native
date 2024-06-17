package java.nio.file

import java.io.{IOException, FileNotFoundException, UncheckedIOException}

import java.nio.file.attribute.BasicFileAttributes

import java.util._
import java.util.function.{BiPredicate, Consumer}
import java.util.stream.{Stream, StreamSupport}

import scala.annotation.tailrec

/* Design Notes:
 *
 *  - Overview: I/O execution & memory costs will by far dominate
 *    quecto (10E-30) scale improvements here. That means that "extra" methods
 *    can be and are used to write clearer and believably correct code.
 *
 *    If measurement at some point in the future, identifies actual
 *    performance hotspots, some methods can be manually inlined.
 *
 *  The Java documentation and implementation of Files.find(), Files.walk(),
 *  and Files.walkFileTree() present a number of cases of interacting
 *  concerns and special cases.
 *
 *  This file uses a "engine-within-engine" design to separate concerns
 *  and highlight and centralize points of interaction.
 *
 *  - The FileTreeWalker class is the outer engine. It makes two "porcelain"
 *    methods, stream() and perform(), available to the Files class.
 *
 *    + FileTreeWalker.next() is the key "plumbing" method. It provides
 *      the class "porcelain" methods with the abstraction of a flow of
 *      Optional[Path]. It is also the center of complexity for its class.
 *
 *      When FileTreeWalker is called, calls the inner engine (see below) to
 *      get a WalkEntry. It then applies FileVisitor and matcher constraints,
 *      possibly looping, until it can either return a suitably qualified
 *      Path to its caller or the inner engine flow reaches its end.
 *
 *  - The WalkContext class is the inner engine. It provides an Iterator
 *    interface to FileTreeWalker, returning WalkEntry elements which are
 *    Paths from the operating system. These elements may be filtered or
 *    re-ordered by the status of a previous FileVisit method or a prior
 *    call from FileTreeWalker to start taking elements from a new directory.
 *
 *    + WalkContext.hasNext() is the center of complexity.
 */

private case class WalkEntry(
    path: Path,
    attrs: BasicFileAttributes,
    mustCheckFsCycles: Boolean
)

private case class WalkLevel(
    path: Path,
    attrs: BasicFileAttributes, // for possible fkey() use in FsLoop detection
    depth: Int, // use to limit WalkLevels to <= maxDepth.
    stream: Stream[Path], // null at Level 0, else Stream[Path]
    iter: java.util.Iterator[Path]
)

private class WalkFileAttrRetriever(
    start: Path,
    followLinks: Boolean,
    visitor: FileVisitor[? >: Path]
) {

  /* This class concentrates mind bending intricacy. The goal is to
   * not do a double fetch of attributes for 'start', given that JVM
   * looks up its attributes _before_ first use.
   *
   * A second lookup in the file system is expensive at startup, even if
   * the attrs are likely to be cached by the file system. This expense
   * is most significant when only a small number of operating system elements
   * are considered.
   *
   * If 'start' is a directory, retrieve() gets called twice. First
   * The fist call, level 0, is always in 'non-directory' a.k.a 'file'
   * milieu.
   *
   * Every call after the first will always be in 'directory' milieu.
   *
   * On JVM, the 'file milieu' returns IOException and 'directory milieu'
   * UncheckedIOException.
   *
   * Now fold in the fact that a visitor method, when supplied, can
   * return either, which should get passed through as thrown.
   */

  val startAttrs =
    try {
      retrieveAttributes(
        start,
        followLinks,
        visitor
      )
    } catch {
      case exc: UncheckedIOException =>
        val cause = exc.getCause()

        if (cause.getClass() == classOf[NoSuchFileException]) throw cause
        else throw exc
    }

  var startAttrsRetrieved = false

  /* This is the kind of special case your mother & your mentors warned
   * you about. Thank you, JVM.
   */
  def peekStartAttrs(): BasicFileAttributes =
    startAttrs.attrs

  def retrieve(p: Path): WalkEntry = {
    // To trace "Why this?", see comments at top of retrieveAttributes()
    if (!startAttrsRetrieved) {
      startAttrsRetrieved = true
      startAttrs
    } else {
      retrieveAttributes(p, followLinks, visitor)
    }
  }

  private object FileTreeWalk {
    // Allocate once, then use frequently.
    final val FOLLOW_LINKS = Array.empty[LinkOption]
    final val NOFOLLOW_LINKS = Array(LinkOption.NOFOLLOW_LINKS)
  }

  private def retrieveAttributes(
      path: Path,
      useLinks: Boolean,
      visitor: FileVisitor[? >: Path]
  ): WalkEntry = {
    import FileTreeWalk.{FOLLOW_LINKS, NOFOLLOW_LINKS}

    /* Note:
     *
     *   Retrieving attribute when not following links is straight forward.
     *
     *   When following links, complexity comes from any eventual call to
     *   detectFileSystemLoop() to be able to determine if a candidate
     *   directory is the result of having followed a symbolic link.
     *
     *   There are at least two algorithms available. The algorithm
     *   used here is optimized for the expected situation of encountering
     *   more directories than symbolic links. It handles errors in-line
     *   rather than after having caught an exception.
     *
     *   An alternate algorithm calls the readAttributes using useLinks
     *   directly and then handles edge cases as they may occur.
     *   It is more efficient in cases where more symlinks than directories are
     *   expected. It has the complexity of handling any edge cases in a
     *   catch block.
     */

    /* retrieveAttributes() can return null attrs if
     * context.visitFileFailed(path, exc) does not re-throw the
     * exception argument and returns, say, FileVisitResult.CONTINUE.
     * A strange thing to do but somebody, someday, is bound to do that.
     */

    val srcAttrs =
      try {
        Files.readAttributes(
          path,
          classOf[BasicFileAttributes],
          NOFOLLOW_LINKS
        )
      } catch {
        case exc: IOException =>
          if (visitor == null) {
            throw new UncheckedIOException(exc.getMessage(), exc)
          } else {
            visitor.visitFileFailed(path, exc)
            null
          }
      }

    val result =
      if (!useLinks || !srcAttrs.isSymbolicLink() || (srcAttrs == null)) {
        new WalkEntry(path, srcAttrs, false)
      } else {
        try {
          val targetAttrs = Files.readAttributes(
            path,
            classOf[BasicFileAttributes],
            FOLLOW_LINKS
          )

          // proper link
          new WalkEntry(path, targetAttrs, targetAttrs.isDirectory())
        } catch {
          case exc: IOException =>
            // broken link or could not read target attrs
            new WalkEntry(path, srcAttrs, false)
        }
      }

    result // attrs may be null
  }
}

private final class WalkContext(
    start: Path,
    followLinks: Boolean,
    val visitor: FileVisitor[? >: Path]
) extends Iterator[WalkEntry]
    with FileVisitor[Path] {

  private def makeStartLevel(
      start: Path,
      attrRetriever: WalkFileAttrRetriever
  ): WalkLevel = {
    val startIter = new Iterator[Path] {
      var done = false

      def hasNext(): Boolean = !done

      def next(): Path = {
        if (done) {
          throw new NoSuchElementException(start.toString())
        } else {
          done = true
          start
        }
      }
    }

    new WalkLevel(
      start,
      attrRetriever.peekStartAttrs(),
      depth = 0,
      null,
      startIter
    )
  }

  /* JVM eagerly looks up the attributes of "start" _before_ the
   * first element of any stream is acted upon. Set such a lookup in motion.
   */
  private val attrRetriever =
    new WalkFileAttrRetriever(start, followLinks, visitor)

  private var currentLevel = makeStartLevel(start, attrRetriever)

  private var currentIter = currentLevel.iter

  private var currentEntry: WalkEntry = null // detect unexpected call sequence

  private var lastFileVisitResult = FileVisitResult.CONTINUE

  /* levelStack is roughly a stack of levels from manual recursion.
   * Avoid resizing in expected use; 32 is a guess, hoping for Goldilocks.
   */
  private val levelStack = new ArrayDeque[WalkLevel](32)

  /* When false, context is hard-closed: class hasNext() will always return
   * false. This happens when the levelStack is empty or
   * FileVisitResult.TERMINATE has been seen.
   */
  private var walkContextHasNext =
    true // startLevel has "start" path, return it.

  /* methods to extend Iterator
   */

  // This is where complexity comes to roost.
  final def hasNext(): Boolean = {

    @tailrec
    def hasNextImpl(): Boolean = {
      def changeLevel(): Boolean = {
        /* A lot of complexity and quirkiness is concentrated in the
         * initial or start level, that is level 0. It is always considered
         * a "file" and never a directory. That means that it has no associated
         * directory stream to close or postVisit.
         */

        if (currentLevel.depth > 0) {
          currentLevel.stream.close()

          if (visitor != null)
            postVisitDirectory(currentLevel.path, null)
        }

        popLevel() // will poison/close Context if it fails
      }

      lastFileVisitResult match {
        case FileVisitResult.CONTINUE =>
          if (currentIter.hasNext()) true
          else if (!changeLevel()) false
          else hasNextImpl()

        case FileVisitResult.SKIP_SIBLINGS =>
          currentIter = Collections.emptyIterator[Path]()
          lastFileVisitResult = FileVisitResult.CONTINUE
          hasNextImpl()

        case FileVisitResult.SKIP_SUBTREE =>
          lastFileVisitResult = FileVisitResult.CONTINUE
          hasNextImpl()

        case FileVisitResult.TERMINATE =>
          walkContextHasNext = false
          walkContextHasNext
      }
    }

    if (!walkContextHasNext) false
    else hasNextImpl()
  }

  final def next(): WalkEntry = {
    if (!walkContextHasNext) {
      throw new NoSuchElementException()
    } else {
      val path = currentIter.next()
      currentEntry = attrRetriever.retrieve(path)
      currentEntry
    }
  }

  /* methods to extend FileVisitor
   *
   * WalkContext.hasNext() critically depends upon lastFileVisitResult being
   * valid in order to provide a sensible result.
   * These four methods defensively ensure that
   * lastFileVisitResult is set on every call to a FileVisitor method.
   */

  def visitFile(file: Path, attrs: BasicFileAttributes) = {
    lastFileVisitResult = visitor.visitFile(file, attrs)
    lastFileVisitResult
  }

  def visitFileFailed(
      file: Path,
      exc: IOException
  ): FileVisitResult = {
    lastFileVisitResult = visitor.visitFileFailed(file, exc)
    lastFileVisitResult
  }

  def preVisitDirectory(
      dir: Path,
      attrs: BasicFileAttributes
  ): FileVisitResult = {
    lastFileVisitResult = visitor.preVisitDirectory(dir, attrs)
    lastFileVisitResult
  }

  def postVisitDirectory(
      dir: Path,
      exc: IOException
  ): FileVisitResult = {
    lastFileVisitResult = visitor.postVisitDirectory(dir, exc)
    lastFileVisitResult
  }

  /* internal OS entry engine methods
   */

  def close(): Unit = {
    /* Be safe for close-after-close but cause NullPointerException for
     * use-after-close. That makes the use-after-close evident.
     *
     * The Stream implementation should never be calling either tryAdvance()
     * or close() after a stream has been closed. Still, be robust or
     * at least detecting.
     */

    if (currentLevel != null) // no Stream to close in startLevel.
      levelStack.addFirst(currentLevel)

    levelStack.forEach(e =>
      if (e.stream != null)
        e.stream.close()
    )

    levelStack.clear()

    poisonContext() // No use-after-close, etc.
  }

  def depth(): Integer =
    currentLevel.depth

  def detectFileSystemLoop(
      path: Path,
      attrs: BasicFileAttributes
  ): Unit = {
    val fKey = attrs.fileKey()

    def isLoop(lvl: WalkLevel): Boolean = {
      if (currentLevel.depth == 0) false // startLevel can never be a directory
      else if (fKey != null) fKey == lvl.attrs.fileKey()
      else Files.isSameFile(path, lvl.path)
    }

    // process every level the same way; consistency is next to cleanliness
    val levels = (new ArrayDeque[WalkLevel](levelStack))
    levels.push(currentLevel)

    levels.forEach(level =>
      if (isLoop(level)) {
        val exc = new FileSystemLoopException(path.toString())
        if (visitor != null) {
          this.visitFileFailed(path, exc)
        } else {
          val msg = s"java.nio.file.FileSystemLoopException: ${path}"
          throw new java.io.UncheckedIOException(msg, exc)
        }
      }
    )
  }

  private def poisonContext(): Unit = {
    /* Katie, bar the door!
     * Sometimes NPE is your friend. Change context to make
     * use-after-close evident via NPE.
     */
    currentLevel = null
    currentIter = null
    currentEntry = null

    lastFileVisitResult == FileVisitResult.TERMINATE
    walkContextHasNext = false
  }

  private def popLevel(): Boolean = {
    if (levelStack.isEmpty()) {
      if (currentLevel.depth > 0) {
        // should never happen, but we want to know about it if it does.
        throw new IOException(
          s"empty recursion stack, current depth: ${currentLevel.depth}"
        )
      }
      poisonContext() // No use-after-close, etc.
      false
    } else {
      currentLevel = levelStack.pop()
      currentIter = currentLevel.iter
      currentEntry = null
      true
    }
  }

  def visitCurrentEntryAsDirectory(): Unit = {
    /* Be defensive & self-contained, even though we control the only caller.
     * Use the last WalkEntry returned by WalkContext.next().
     * This gives caller no way to supply bogus or destructive arguments.
     */

    val dirPath = currentEntry.path
    val dirStream = Files.list(dirPath)

    val newLevel =
      WalkLevel(
        dirPath,
        currentEntry.attrs,
        depth = Math.addExact(currentLevel.depth, 1), // report overflow
        dirStream,
        dirStream.iterator()
      )

    levelStack.push(currentLevel)

    currentLevel = newLevel
    currentIter = newLevel.iter
  }
}

private object FileTreeWalker {
  /* Pre-condition:
   * -  javalib Files caller has checked arguments.
   */

  def apply(
      start: Path,
      maxDepth: Int,
      followLinks: Boolean
  ) = new FileTreeWalker(start, maxDepth, followLinks, null, null)

  def apply(
      start: Path,
      maxDepth: Int,
      followLinks: Boolean,
      matcher: BiPredicate[Path, BasicFileAttributes]
  ) = new FileTreeWalker(start, maxDepth, followLinks, matcher, null)

  def apply(
      start: Path,
      maxDepth: Int,
      followLinks: Boolean,
      visitor: FileVisitor[? >: Path]
  ) = new FileTreeWalker(start, maxDepth, followLinks, null, visitor)
}

private final class FileTreeWalker(
    start: Path,
    maxDepth: Int,
    followLinks: Boolean, // Avoid Scala 2,3 varargs expansion differences.
    matcher: BiPredicate[Path, BasicFileAttributes],
    var visitorArg: FileVisitor[? >: Path]
) {
  /* Pre-condition:
   * -  javalib Files caller has checked arguments.
   * -  matcher and/or visitor argument may be and are null if unused.
   */

  /* Porcelain methods
   *  javalib Walk/Find API uses only two top level methods.
   */

  def stream(): Stream[Path] = {
    val spliter = this.spliterator()

    StreamSupport.stream(spliter, false).onClose(() => this.close())
  }

  def walk(): Unit = {
    val spliter = this.spliterator()

    while (spliter.tryAdvance(p => ())) {} // loop to invoke side effects
  }

  /* Porcelain Support methods
   */

  private def spliterator(): Spliterator[Path] = {

    new Spliterators.AbstractSpliterator[Path](Long.MaxValue, 0) {

      // Punt & avoid concurrency issues by forcing a sequential stream.
      override def trySplit(): Spliterator[Path] = null

      def tryAdvance(action: Consumer[? >: Path]): Boolean = {
        val s = next()

        if (s.isEmpty()) false
        else {
          action.accept(s.get())
          true
        }
      }
    }
  }

  private def close(): Unit =
    context.close()

  /* Plumbing methods
   */

  /* 'context' is conceptually an implicit variable.
   * Scala 2 & Scala 3 have different syntax for dealing with implicit
   * variables. Javalib code currently must support both, so use
   * an explicit context to centralize getting and setting of variables
   * and recursion.
   */

  private var context = new WalkContext(start, followLinks, visitorArg)

  visitorArg = null // ensure all access to visitor goes through context

  def visit(
      path: Path,
      attrs: BasicFileAttributes,
      mustCheckFsCycles: Boolean
  ): Unit = {
    // Pre-condition: caller, has screened out null paths and attrs.

    def visitDirectory(
        path: Path,
        attrs: BasicFileAttributes,
        mustCheckFsCycles: Boolean
    ): Unit = {
      // Pre-condition: Caller has checked that (context.depth() < maxDepth)

      // JVM checks for cycles before any preVisitDirectory()
      if (mustCheckFsCycles) // attrs are for a symbolic link target.
        context.detectFileSystemLoop(path, attrs)

      val continue =
        if (context.visitor == null) true
        else {
          val pvdResult = context.preVisitDirectory(path, attrs)
          pvdResult == FileVisitResult.CONTINUE
        }

      if (continue) {
        try {
          context.visitCurrentEntryAsDirectory()
        } catch {
          case exc: IOException =>
            if (context.visitor != null) {
              // Yes, JVM says to call visitFileFailed, not postVisitDirectory.
              context.visitFileFailed(path, exc)
            }
        }
      }
    }

    def visitFile(path: Path, attrs: BasicFileAttributes): Unit = {
      if (context.visitor != null) {
        try {
          context.visitFile(path, attrs)
        } catch {
          case exc: IOException => context.visitFileFailed(path, exc)
        }
      }
    }

    // Will probably see more regular files than directories, test those first.

    if ((!attrs.isDirectory()) || (context.depth() >= maxDepth))
      visitFile(path, attrs)
    else
      visitDirectory(path, attrs, mustCheckFsCycles)
  }

  @tailrec
  final def next(): Optional[Path] = {
    if (!context.hasNext()) { // EOF on context iterator
      Optional.empty[Path]()
    } else {
      val WalkEntry(path, attrs, mustCheckFsCycles) = context.next()

      if (attrs == null) {
        next() // Skip: not much can be done without attrs.
      } else {
        /* For all files, apply any visitor. Ensure that directories
         * always get expanded, visitor or not, even if they later
         * do not match and are not returned in the stream.
         *
         * Think an "endWith()" predicate. Enclosing directory would screen
         * out, but a file in that directory should be considered & pass.
         */
        if (attrs.isDirectory() || (context.visitor != null))
          visit(path, attrs, mustCheckFsCycles)

        val matched = (matcher == null) || matcher.test(path, attrs)

        if (!matched) next() // discard; loop & try again.
        else Optional.of(path) // provide value to spliterator
      }
    }
  }
}
