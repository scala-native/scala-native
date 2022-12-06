.. _javalib:

Java Standard Library
=====================

Scala Native supports a subset of the JDK core libraries reimplemented in Scala.

Supported classes
-----------------

Here is the list of currently available classes:

* ``java.io.BufferedInputStream``
* ``java.io.BufferedOutputStream``
* ``java.io.BufferedReader``
* ``java.io.BufferedWriter``
* ``java.io.ByteArrayInputStream``
* ``java.io.ByteArrayOutputStream``
* ``java.io.Closeable``
* ``java.io.DataInput``
* ``java.io.DataInputStream``
* ``java.io.DataOutput``
* ``java.io.DataOutputStream``
* ``java.io.EOFException``
* ``java.io.File``
* ``java.io.FileDescriptor``
* ``java.io.FileFilter``
* ``java.io.FileInputStream``
* ``java.io.FileNotFoundException``
* ``java.io.FileOutputStream``
* ``java.io.FileReader``
* ``java.io.FileWriter``
* ``java.io.FilenameFilter``
* ``java.io.FilterInputStream``
* ``java.io.FilterOutputStream``
* ``java.io.FilterReader``
* ``java.io.Flushable``
* ``java.io.IOException``
* ``java.io.InputStream``
* ``java.io.InputStreamReader``
* ``java.io.InterruptedIOException``
* ``java.io.LineNumberReader``
* ``java.io.NotSerializableException``
* ``java.io.ObjectStreamException``
* ``java.io.OutputStream``
* ``java.io.OutputStreamWriter``
* ``java.io.PrintStream``
* ``java.io.PrintWriter``
* ``java.io.PushbackInputStream``
* ``java.io.PushbackReader``
* ``java.io.RandomAccessFile``
* ``java.io.Reader``
* ``java.io.Serializable``
* ``java.io.StringReader``
* ``java.io.StringWriter``
* ``java.io.SyncFailedException``
* ``java.io.UTFDataFormatException``
* ``java.io.UncheckedIOException``
* ``java.io.UnsupportedEncodingException``
* ``java.io.Writer``
* ``java.lang.AbstractMethodError``
* ``java.lang.AbstractStringBuilder``
* ``java.lang.Appendable``
* ``java.lang.ArithmeticException``
* ``java.lang.ArrayIndexOutOfBoundsException``
* ``java.lang.ArrayStoreException``
* ``java.lang.AssertionError``
* ``java.lang.AutoCloseable``
* ``java.lang.Boolean``
* ``java.lang.BootstrapMethodError``
* ``java.lang.Byte``
* ``java.lang.ByteCache``
* ``java.lang.CharSequence``
* ``java.lang.Character``
* ``java.lang.Character$Subset``
* ``java.lang.Character$UnicodeBlock``
* ``java.lang.CharacterCache``
* ``java.lang.ClassCastException``
* ``java.lang.ClassCircularityError``
* ``java.lang.ClassFormatError``
* ``java.lang.ClassLoader``
* ``java.lang.ClassNotFoundException``
* ``java.lang.CloneNotSupportedException``
* ``java.lang.Cloneable``
* ``java.lang.Comparable``
* ``java.lang.Double``
* ``java.lang.Enum``
* ``java.lang.EnumConstantNotPresentException``
* ``java.lang.Error``
* ``java.lang.Exception``
* ``java.lang.ExceptionInInitializerError``
* ``java.lang.Float``
* ``java.lang.IllegalAccessError``
* ``java.lang.IllegalAccessException``
* ``java.lang.IllegalArgumentException``
* ``java.lang.IllegalMonitorStateException``
* ``java.lang.IllegalStateException``
* ``java.lang.IllegalThreadStateException``
* ``java.lang.IncompatibleClassChangeError``
* ``java.lang.IndexOutOfBoundsException``
* ``java.lang.InheritableThreadLocal``
* ``java.lang.InstantiationError``
* ``java.lang.InstantiationException``
* ``java.lang.Integer``
* ``java.lang.IntegerCache``
* ``java.lang.IntegerDecimalScale``
* ``java.lang.InternalError``
* ``java.lang.InterruptedException``
* ``java.lang.Iterable``
* ``java.lang.LinkageError``
* ``java.lang.Long``
* ``java.lang.LongCache``
* ``java.lang.Math``
* ``java.lang.MathRand``
* ``java.lang.NegativeArraySizeException``
* ``java.lang.NoClassDefFoundError``
* ``java.lang.NoSuchFieldError``
* ``java.lang.NoSuchFieldException``
* ``java.lang.NoSuchMethodError``
* ``java.lang.NoSuchMethodException``
* ``java.lang.NullPointerException``
* ``java.lang.Number``
* ``java.lang.NumberFormatException``
* ``java.lang.OutOfMemoryError``
* ``java.lang.Process``
* ``java.lang.ProcessBuilder``
* ``java.lang.ProcessBuilder$Redirect``
* ``java.lang.ProcessBuilder$Redirect$Type``
* ``java.lang.Readable``
* ``java.lang.ReflectiveOperationException``
* ``java.lang.RejectedExecutionException``
* ``java.lang.Runnable``
* ``java.lang.Runtime``
* ``java.lang.Runtime$ProcessBuilderOps``
* ``java.lang.RuntimeException``
* ``java.lang.SecurityException``
* ``java.lang.Short``
* ``java.lang.ShortCache``
* ``java.lang.StackOverflowError``
* ``java.lang.StackTrace``
* ``java.lang.StackTraceElement``
* ``java.lang.StackTraceElement$Fail``
* ``java.lang.String``
* ``java.lang.StringBuffer``
* ``java.lang.StringBuilder``
* ``java.lang.StringIndexOutOfBoundsException``
* ``java.lang.System``
* ``java.lang.Thread``
* ``java.lang.Thread$UncaughtExceptionHandler``
* ``java.lang.ThreadDeath``
* ``java.lang.ThreadLocal``
* ``java.lang.Throwable``
* ``java.lang.TypeNotPresentException``
* ``java.lang.UnknownError``
* ``java.lang.UnsatisfiedLinkError``
* ``java.lang.UnsupportedClassVersionError``
* ``java.lang.UnsupportedOperationException``
* ``java.lang.VerifyError``
* ``java.lang.VirtualMachineError``
* ``java.lang.Void``
* ``java.lang.annotation.Annotation``
* ``java.lang.annotation.Retention``
* ``java.lang.annotation.RetentionPolicy``
* ``java.lang.constant.Constable``
* ``java.lang.constant.ConstantDesc``
* ``java.lang.ref.PhantomReference``
* ``java.lang.ref.Reference``
* ``java.lang.ref.ReferenceQueue``
* ``java.lang.ref.SoftReference``
* ``java.lang.ref.WeakReference``
* ``java.lang.reflect.AccessibleObject``
* ``java.lang.reflect.Array``
* ``java.lang.reflect.Constructor``
* ``java.lang.reflect.Executable``
* ``java.lang.reflect.Field``
* ``java.lang.reflect.InvocationTargetException``
* ``java.lang.reflect.Method``
* ``java.lang.reflect.UndeclaredThrowableException``
* ``java.math.BigDecimal``
* ``java.math.BigInteger``
* ``java.math.BitLevel``
* ``java.math.Conversion``
* ``java.math.Division``
* ``java.math.Elementary``
* ``java.math.Logical``
* ``java.math.MathContext``
* ``java.math.Multiplication``
* ``java.math.Primality``
* ``java.math.RoundingMode``
* ``java.net.BindException``
* ``java.net.ConnectException``
* ``java.net.Inet4Address``
* ``java.net.Inet6Address``
* ``java.net.InetAddress``
* ``java.net.InetSocketAddress``
* ``java.net.InterfaceAddress``
* ``java.net.MalformedURLException``
* ``java.net.NetworkInterface``
* ``java.net.NoRouteToHostException``
* ``java.net.PortUnreachableException``
* ``java.net.ServerSocket``
* ``java.net.Socket``
* ``java.net.SocketAddress``
* ``java.net.SocketException``
* ``java.net.SocketImpl``
* ``java.net.SocketInputStream``
* ``java.net.SocketOption``
* ``java.net.SocketOptions``
* ``java.net.SocketOutputStream``
* ``java.net.SocketTimeoutException``
* ``java.net.URI``
* ``java.net.URI$Helper``
* ``java.net.URIEncoderDecoder``
* ``java.net.URISyntaxException``
* ``java.net.URL``
* ``java.net.URLClassLoader``
* ``java.net.URLConnection``
* ``java.net.URLDecoder``
* ``java.net.URLEncoder``
* ``java.net.UnknownHostException``
* ``java.net.UnknownServiceException``
* ``java.nio.Buffer``
* ``java.nio.BufferOverflowException``
* ``java.nio.BufferUnderflowException``
* ``java.nio.ByteBuffer``
* ``java.nio.ByteOrder``
* ``java.nio.CharBuffer``
* ``java.nio.DoubleBuffer``
* ``java.nio.FloatBuffer``
* ``java.nio.IntBuffer``
* ``java.nio.InvalidMarkException``
* ``java.nio.LongBuffer``
* ``java.nio.MappedByteBuffer``
* ``java.nio.ReadOnlyBufferException``
* ``java.nio.ShortBuffer``
* ``java.nio.channels.ByteChannel``
* ``java.nio.channels.Channel``
* ``java.nio.channels.Channels``
* ``java.nio.channels.ClosedChannelException``
* ``java.nio.channels.FileChannel``
* ``java.nio.channels.FileChannel$MapMode``
* ``java.nio.channels.FileLock``
* ``java.nio.channels.GatheringByteChannel``
* ``java.nio.channels.InterruptibleChannel``
* ``java.nio.channels.NonReadableChannelException``
* ``java.nio.channels.NonWritableChannelException``
* ``java.nio.channels.OverlappingFileLockException``
* ``java.nio.channels.ReadableByteChannel``
* ``java.nio.channels.ScatteringByteChannel``
* ``java.nio.channels.SeekableByteChannel``
* ``java.nio.channels.WritableByteChannel``
* ``java.nio.channels.spi.AbstractInterruptibleChannel``
* ``java.nio.charset.CharacterCodingException``
* ``java.nio.charset.Charset``
* ``java.nio.charset.CharsetDecoder``
* ``java.nio.charset.CharsetEncoder``
* ``java.nio.charset.CoderMalfunctionError``
* ``java.nio.charset.CoderResult``
* ``java.nio.charset.CodingErrorAction``
* ``java.nio.charset.IllegalCharsetNameException``
* ``java.nio.charset.MalformedInputException``
* ``java.nio.charset.StandardCharsets``
* ``java.nio.charset.UnmappableCharacterException``
* ``java.nio.charset.UnsupportedCharsetException``
* ``java.nio.file.AccessDeniedException``
* ``java.nio.file.CopyOption``
* ``java.nio.file.DirectoryIteratorException``
* ``java.nio.file.DirectoryNotEmptyException``
* ``java.nio.file.DirectoryStream``
* ``java.nio.file.DirectoryStream$Filter``
* ``java.nio.file.DirectoryStreamImpl``
* ``java.nio.file.FileAlreadyExistsException``
* ``java.nio.file.FileSystem``
* ``java.nio.file.FileSystemException``
* ``java.nio.file.FileSystemLoopException``
* ``java.nio.file.FileSystemNotFoundException``
* ``java.nio.file.FileSystems``
* ``java.nio.file.FileVisitOption``
* ``java.nio.file.FileVisitResult``
* ``java.nio.file.FileVisitor``
* ``java.nio.file.Files``
* ``java.nio.file.Files$TerminateTraversalException``
* ``java.nio.file.InvalidPathException``
* ``java.nio.file.LinkOption``
* ``java.nio.file.NoSuchFileException``
* ``java.nio.file.NotDirectoryException``
* ``java.nio.file.NotLinkException``
* ``java.nio.file.OpenOption``
* ``java.nio.file.Path``
* ``java.nio.file.PathMatcher``
* ``java.nio.file.Paths``
* ``java.nio.file.RegexPathMatcher``
* ``java.nio.file.SimpleFileVisitor``
* ``java.nio.file.StandardCopyOption``
* ``java.nio.file.StandardOpenOption``
* ``java.nio.file.StandardWatchEventKinds``
* ``java.nio.file.WatchEvent``
* ``java.nio.file.WatchEvent$Kind``
* ``java.nio.file.WatchEvent$Modifier``
* ``java.nio.file.WatchKey``
* ``java.nio.file.WatchService``
* ``java.nio.file.Watchable``
* ``java.nio.file.attribute.AclEntry``
* ``java.nio.file.attribute.AclFileAttributeView``
* ``java.nio.file.attribute.AttributeView``
* ``java.nio.file.attribute.BasicFileAttributeView``
* ``java.nio.file.attribute.BasicFileAttributes``
* ``java.nio.file.attribute.DosFileAttributeView``
* ``java.nio.file.attribute.DosFileAttributes``
* ``java.nio.file.attribute.FileAttribute``
* ``java.nio.file.attribute.FileAttributeView``
* ``java.nio.file.attribute.FileOwnerAttributeView``
* ``java.nio.file.attribute.FileStoreAttributeView``
* ``java.nio.file.attribute.FileTime``
* ``java.nio.file.attribute.GroupPrincipal``
* ``java.nio.file.attribute.PosixFileAttributeView``
* ``java.nio.file.attribute.PosixFileAttributes``
* ``java.nio.file.attribute.PosixFilePermission``
* ``java.nio.file.attribute.PosixFilePermissions``
* ``java.nio.file.attribute.UserDefinedFileAttributeView``
* ``java.nio.file.attribute.UserPrincipal``
* ``java.nio.file.attribute.UserPrincipalLookupService``
* ``java.nio.file.attribute.UserPrincipalNotFoundException``
* ``java.nio.file.spi.FileSystemProvider``
* ``java.rmi.Remote``
* ``java.rmi.RemoteException``
* ``java.security.AccessControlException``
* ``java.security.CodeSigner``
* ``java.security.DummyMessageDigest``
* ``java.security.GeneralSecurityException``
* ``java.security.MessageDigest``
* ``java.security.MessageDigestSpi``
* ``java.security.NoSuchAlgorithmException``
* ``java.security.Principal``
* ``java.security.Timestamp``
* ``java.security.TimestampConstructorHelper``
* ``java.security.cert.CertPath``
* ``java.security.cert.Certificate``
* ``java.security.cert.CertificateEncodingException``
* ``java.security.cert.CertificateException``
* ``java.security.cert.CertificateFactory``
* ``java.security.cert.X509Certificate``
* ``java.security.cert.X509Extension``
* ``java.util.AbstractCollection``
* ``java.util.AbstractList``
* ``java.util.AbstractListView``
* ``java.util.AbstractMap``
* ``java.util.AbstractMap$SimpleEntry``
* ``java.util.AbstractMap$SimpleImmutableEntry``
* ``java.util.AbstractQueue``
* ``java.util.AbstractRandomAccessListIterator``
* ``java.util.AbstractSequentialList``
* ``java.util.AbstractSet``
* ``java.util.ArrayDeque``
* ``java.util.ArrayList``
* ``java.util.Arrays``
* ``java.util.Arrays$AsRef``
* ``java.util.BackedUpListIterator``
* ``java.util.Base64``
* ``java.util.Base64$Decoder``
* ``java.util.Base64$DecodingInputStream``
* ``java.util.Base64$Encoder``
* ``java.util.Base64$EncodingOutputStream``
* ``java.util.Base64$Wrapper``
* ``java.util.BitSet``
* ``java.util.Calendar``
* ``java.util.Collection``
* ``java.util.Collections``
* ``java.util.Collections$CheckedCollection``
* ``java.util.Collections$CheckedList``
* ``java.util.Collections$CheckedListIterator``
* ``java.util.Collections$CheckedMap``
* ``java.util.Collections$CheckedSet``
* ``java.util.Collections$CheckedSortedMap``
* ``java.util.Collections$CheckedSortedSet``
* ``java.util.Collections$EmptyIterator``
* ``java.util.Collections$EmptyListIterator``
* ``java.util.Collections$ImmutableList``
* ``java.util.Collections$ImmutableMap``
* ``java.util.Collections$ImmutableSet``
* ``java.util.Collections$UnmodifiableCollection``
* ``java.util.Collections$UnmodifiableIterator``
* ``java.util.Collections$UnmodifiableList``
* ``java.util.Collections$UnmodifiableListIterator``
* ``java.util.Collections$UnmodifiableMap``
* ``java.util.Collections$UnmodifiableSet``
* ``java.util.Collections$UnmodifiableSortedMap``
* ``java.util.Collections$UnmodifiableSortedSet``
* ``java.util.Collections$WrappedCollection``
* ``java.util.Collections$WrappedEquals``
* ``java.util.Collections$WrappedIterator``
* ``java.util.Collections$WrappedList``
* ``java.util.Collections$WrappedListIterator``
* ``java.util.Collections$WrappedMap``
* ``java.util.Collections$WrappedSet``
* ``java.util.Collections$WrappedSortedMap``
* ``java.util.Collections$WrappedSortedSet``
* ``java.util.Comparator``
* ``java.util.ConcurrentModificationException``
* ``java.util.Date``
* ``java.util.Deque``
* ``java.util.Dictionary``
* ``java.util.DuplicateFormatFlagsException``
* ``java.util.EmptyStackException``
* ``java.util.EnumSet``
* ``java.util.Enumeration``
* ``java.util.FormatFlagsConversionMismatchException``
* ``java.util.Formattable``
* ``java.util.FormattableFlags``
* ``java.util.Formatter``
* ``java.util.Formatter$BigDecimalLayoutForm``
* ``java.util.FormatterClosedException``
* ``java.util.GregorianCalendar``
* ``java.util.HashMap``
* ``java.util.HashSet``
* ``java.util.Hashtable``
* ``java.util.Hashtable$UnboxedEntry$1``
* ``java.util.IdentityHashMap``
* ``java.util.IllegalFormatCodePointException``
* ``java.util.IllegalFormatConversionException``
* ``java.util.IllegalFormatException``
* ``java.util.IllegalFormatFlagsException``
* ``java.util.IllegalFormatPrecisionException``
* ``java.util.IllegalFormatWidthException``
* ``java.util.IllformedLocaleException``
* ``java.util.InputMismatchException``
* ``java.util.InvalidPropertiesFormatException``
* ``java.util.Iterator``
* ``java.util.LinkedHashMap``
* ``java.util.LinkedHashSet``
* ``java.util.LinkedList``
* ``java.util.List``
* ``java.util.ListIterator``
* ``java.util.Map``
* ``java.util.Map$Entry``
* ``java.util.MissingFormatArgumentException``
* ``java.util.MissingFormatWidthException``
* ``java.util.MissingResourceException``
* ``java.util.NavigableMap``
* ``java.util.NavigableSet``
* ``java.util.NoSuchElementException``
* ``java.util.Objects``
* ``java.util.Optional``
* ``java.util.PriorityQueue``
* ``java.util.Properties``
* ``java.util.Queue``
* ``java.util.Random``
* ``java.util.RandomAccess``
* ``java.util.RandomAccessListIterator``
* ``java.util.ServiceConfigurationError``
* ``java.util.Set``
* ``java.util.SizeChangeEvent``
* ``java.util.SortedMap``
* ``java.util.SortedSet``
* ``java.util.StringTokenizer``
* ``java.util.TooManyListenersException``
* ``java.util.TreeSet``
* ``java.util.UUID``
* ``java.util.UnknownFormatConversionException``
* ``java.util.UnknownFormatFlagsException``
* ``java.util.WeakHashMap``
* ``java.util.concurrent.Callable``
* ``java.util.concurrent.CancellationException``
* ``java.util.concurrent.ConcurrentHashMap``
* ``java.util.concurrent.ConcurrentHashMap$KeySetView``
* ``java.util.concurrent.ConcurrentLinkedQueue``
* ``java.util.concurrent.ConcurrentMap``
* ``java.util.concurrent.ConcurrentSkipListSet``
* ``java.util.concurrent.ExecutionException``
* ``java.util.concurrent.Executor``
* ``java.util.concurrent.RejectedExecutionException``
* ``java.util.concurrent.Semaphore``
* ``java.util.concurrent.ThreadFactory``
* ``java.util.concurrent.ThreadLocalRandom``
* ``java.util.concurrent.TimeUnit``
* ``java.util.concurrent.TimeoutException``
* ``java.util.concurrent.atomic.AtomicBoolean``
* ``java.util.concurrent.atomic.AtomicInteger``
* ``java.util.concurrent.atomic.AtomicLong``
* ``java.util.concurrent.atomic.AtomicLongArray``
* ``java.util.concurrent.atomic.AtomicReference``
* ``java.util.concurrent.atomic.AtomicReferenceArray``
* ``java.util.concurrent.atomic.LongAdder``
* ``java.util.concurrent.locks.AbstractOwnableSynchronizer``
* ``java.util.concurrent.locks.AbstractQueuedSynchronizer``
* ``java.util.concurrent.locks.Lock``
* ``java.util.concurrent.locks.ReentrantLock``
* ``java.util.function.BiConsumer``
* ``java.util.function.BiFunction``
* ``java.util.function.BiPredicate``
* ``java.util.function.BinaryOperator``
* ``java.util.function.Consumer``
* ``java.util.function.Function``
* ``java.util.function.IntUnaryOperator``
* ``java.util.function.Predicate``
* ``java.util.function.Supplier``
* ``java.util.function.UnaryOperator``
* ``java.util.jar.Attributes``
* ``java.util.jar.Attributes$Name``
* ``java.util.jar.InitManifest``
* ``java.util.jar.JarEntry``
* ``java.util.jar.JarFile``
* ``java.util.jar.JarInputStream``
* ``java.util.jar.JarOutputStream``
* ``java.util.jar.Manifest``
* ``java.util.regex.MatchResult``
* ``java.util.regex.Matcher``
* ``java.util.regex.Pattern``
* ``java.util.regex.PatternSyntaxException``
* ``java.util.stream.BaseStream``
* ``java.util.stream.CompositeStream``
* ``java.util.stream.EmptyIterator``
* ``java.util.stream.Stream``
* ``java.util.stream.Stream$Builder``
* ``java.util.zip.Adler32``
* ``java.util.zip.CRC32``
* ``java.util.zip.CheckedInputStream``
* ``java.util.zip.CheckedOutputStream``
* ``java.util.zip.Checksum``
* ``java.util.zip.DataFormatException``
* ``java.util.zip.Deflater``
* ``java.util.zip.DeflaterOutputStream``
* ``java.util.zip.GZIPInputStream``
* ``java.util.zip.GZIPOutputStream``
* ``java.util.zip.Inflater``
* ``java.util.zip.InflaterInputStream``
* ``java.util.zip.ZipConstants``
* ``java.util.zip.ZipEntry``
* ``java.util.zip.ZipException``
* ``java.util.zip.ZipFile``
* ``java.util.zip.ZipInputStream``
* ``java.util.zip.ZipOutputStream``

**Note:** This is an ongoing effort, some of the classes listed here might
be partially implemented. Please consult `javalib sources
<https://github.com/scala-native/scala-native/tree/main/javalib/src/main/scala/java>`_
for details.

Regular expressions (java.util.regex)
-------------------------------------

Scala Native implements `java.util.regex`-compatible API using
`Google's RE2 library <https://github.com/google/re2>`_.
RE2 is not a drop-in replacement for `java.util.regex` but
handles most common cases well.

Some notes on the implementation:

1. The included RE2 implements a Unicode version lower than
   the version used in the Scala Native Character class (>= 7.0.0).
   The RE2 Unicode version is in the 6.n range. For reference, Java 8
   released with Unicode 6.2.0. 

   The RE2 implemented may not match codepoints added or changed
   in later Unicode versions. Similarly, there may be slight differences
   for Unicode codepoints with high numeric value between values used by RE2
   and those used by the Character class.

2. This implementation of RE2 does not support:

   * Character classes:

     * Unions: ``[a-d[m-p]]``
     * Intersections: ``[a-z&&[^aeiou]]``

   * Predefined character classes: ``\h``, ``\H``, ``\v``, ``\V``

   * Patterns:

     * Octal: ``\0100`` - use decimal or hexadecimal instead.
     * Two character Hexadecimal: ``\xFF`` - use ``\x00FF`` instead.
     * All alphabetic Unicode: ``\uBEEF`` - use hex ``\xBEEF`` instead.
     * Escape: ``\e`` - use ``\u001B`` instead.

   * Java character function classes:

     * ``\p{javaLowerCase}``
     * ``\p{javaUpperCase}``
     * ``\p{javaWhitespace}``
     * ``\p{javaMirrored}``

   * Boundary matchers: ``\G``, ``\R``, ``\Z``

   * Possessive quantifiers: ``X?+``, ``X*+``, ``X++``, ``X{n}+``,
     ``X{n,}+``, ``X{n,m}+``

   * Lookaheads: ``(?=X)``, ``(?!X)``, ``(?<=X)``, ``(?<!X)``, ``(?>X)``

   * Options

     *  CANON_EQ
     *  COMMENTS
     *  LITERAL
     *  UNICODE_CASE
     *  UNICODE_CHARACTER_CLASS
     *  UNIX_LINES

   * Patterns to match a Unicode binary property, such as
     ``\p{isAlphabetic}`` for a codepoint with the 'alphabetic' property,
     are not supported. Often another pattern ``\p{isAlpha}`` may be used
     instead, ``\p{isAlpha}`` in this case.

3. The reference Java 8 regex package does not support certain commonly used
   Perl expressions supported by this implementation of RE2. For example,
   for named capture groups Java uses the expression "(?<foo>)" while
   Perl uses the expression "(?P<foo>)".

   Scala Native java.util.regex methods accept both forms. This extension
   is intended to useful but is not strictly Java 8 compliant. Not all RE2
   Perl expressions may be exposed in this way.

4. The following Matcher methods have a minimal implementation:

   * Matcher.hasAnchoringBounds() - always return true.
   * Matcher.hasTransparentBounds() - always throws
     UnsupportedOperationException because RE2 does not support lookaheads.
   * Matcher.hitEnd() - always throws UnsupportedOperationException.
   * Matcher.region(int, int)
   * Matcher.regionEnd()
   * Matcher.regionStart()
   * Matcher.requireEnd() - always throws UnsupportedOperationException.
   * Matcher.useAnchoringBounds(boolean)  - always throws
         UnsupportedOperationException
   * Matcher.useTransparentBounds(boolean) - always throws
     UnsupportedOperationException because RE2 does not support lookaheads.

5. Scala Native 0.3.8 required POSIX patterns to have the form
   ``[[:alpha:]]``.
   Now the Java standard form ``\p{Alpha}`` is accepted and the former variant
   pattern is not. This improves compatibility with Java but,
   regrettably, may require code changes when upgrading from Scala Native
   0.3.8.

Embedding Resources
-------------------

In Scala Native, resources are implemented via embedding a resource in a resulting
binary file. Only ``getClass().getResourceAsInputStream()`` is implemented.
For that to work, you have to specify an additional NativeConfig option:

.. code-block:: scala

  nativeConfig ~= {
    _.withEmbedResources(true)
  }

This will include the resource files found on the classpath in the resulting
binary file. Please note that files with following extensions cannot be embedded
and used as a resource:

``".class", ".tasty", ".nir", ".scala", ".java", ".jar"``

This is to avoid unnecesarily embedding source files. If necessary, please
consider using a different file extension for embedding. Files found in the
``resources/scala-native`` directory will not be embedded as well. It is recommended
to add the ".c" nad ".h" files there.

Reasoning for the lack of ``getResource()`` and ``getResources()``:

In Scala Native, the outputted file that can be run is a binary, unlike JVM's
classfiles and jars. For that reason, if ``getResources()`` URI methods would be implemented,
a new URI format using a seperate FileSystem would have to be added (e.g. instead
of obtaining ``jar:file:path.ext`` you would obtain ``embedded:path.ext``). As this still
would provide a meaningful inconsistency between JVM's javalib API and Scala
Native's reimplementation, this remains not implemented for now. The added
``getClass().getResourceAsInputStream()`` however is able to be consistent between
the platforms.

Continue to :ref:`libc`.
