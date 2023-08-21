.. _javalib:

Java Standard Library
=====================

Scala Native supports a subset of the JDK core libraries reimplemented in Scala.

Supported classes
-----------------

The classes currently available are:

java.io
"""""""
* ``BufferedInputStream``
* ``BufferedOutputStream``
* ``BufferedReader``
* ``BufferedWriter``
* ``ByteArrayInputStream``
* ``ByteArrayOutputStream``
* ``Closeable``
* ``DataInput``
* ``DataInputStream``
* ``DataOutput``
* ``DataOutputStream``
* ``EOFException``
* ``File``
* ``FileDescriptor``
* ``FileFilter``
* ``FileInputStream``
* ``FileNotFoundException``
* ``FileOutputStream``
* ``FileReader``
* ``FileWriter``
* ``FilenameFilter``
* ``FilterInputStream``
* ``FilterOutputStream``
* ``FilterReader``
* ``Flushable``
* ``IOException``
* ``InputStream``
* ``InputStreamReader``
* ``InterruptedIOException``
* ``LineNumberReader``
* ``NotSerializableException``
* ``ObjectStreamException``
* ``OutputStream``
* ``OutputStreamWriter``
* ``PipedInputStream``
* ``PipedOutputStream``
* ``PipedReader``
* ``PipedWriter``
* ``PrintStream``
* ``PrintWriter``
* ``PushbackInputStream``
* ``PushbackReader``
* ``RandomAccessFile``
* ``Reader``
* ``Serializable``
* ``StringReader``
* ``StringWriter``
* ``SyncFailedException``
* ``UTFDataFormatException``
* ``UncheckedIOException``
* ``UnsupportedEncodingException``
* ``Writer``

java.lang
"""""""""
* ``AbstractMethodError``
* ``AbstractStringBuilder``
* ``Appendable``
* ``ArithmeticException``
* ``ArrayIndexOutOfBoundsException``
* ``ArrayStoreException``
* ``AssertionError``
* ``AutoCloseable``
* ``Boolean``
* ``BootstrapMethodError``
* ``Byte``
* ``ByteCache``
* ``CharSequence``
* ``Character``
* ``Character.Subset``
* ``Character.UnicodeBlock``
* ``CharacterCache``
* ``ClassCastException``
* ``ClassCircularityError``
* ``ClassFormatError``
* ``ClassLoader``
* ``ClassNotFoundException``
* ``CloneNotSupportedException``
* ``Cloneable``
* ``Comparable``
* ``Double``
* ``Enum``
* ``EnumConstantNotPresentException``
* ``Error``
* ``Exception``
* ``ExceptionInInitializerError``
* ``Float``
* ``IllegalAccessError``
* ``IllegalAccessException``
* ``IllegalArgumentException``
* ``IllegalMonitorStateException``
* ``IllegalStateException``
* ``IllegalThreadStateException``
* ``IncompatibleClassChangeError``
* ``IndexOutOfBoundsException``
* ``InheritableThreadLocal``
* ``InstantiationError``
* ``InstantiationException``
* ``Integer``
* ``IntegerCache``
* ``IntegerDecimalScale``
* ``InternalError``
* ``InterruptedException``
* ``Iterable``
* ``LinkageError``
* ``Long``
* ``LongCache``
* ``Math``
* ``MathRand``
* ``NegativeArraySizeException``
* ``NoClassDefFoundError``
* ``NoSuchFieldError``
* ``NoSuchFieldException``
* ``NoSuchMethodError``
* ``NoSuchMethodException``
* ``NullPointerException``
* ``Number``
* ``NumberFormatException``
* ``OutOfMemoryError``
* ``Process``
* ``ProcessBuilder``
* ``ProcessBuilder.Redirect``
* ``ProcessBuilder.Redirect.Type``
* ``Readable``
* ``ReflectiveOperationException``
* ``RejectedExecutionException``
* ``Runnable``
* ``Runtime``
* ``RuntimeException``
* ``SecurityException``
* ``Short``
* ``StackOverflowError``
* ``StackTrace``
* ``StackTraceElement``
* ``String``
* ``StringBuffer``
* ``StringBuilder``
* ``StringIndexOutOfBoundsException``
* ``System``
* ``Thread``
* ``Thread.UncaughtExceptionHandler``
* ``ThreadDeath``
* ``ThreadLocal``
* ``Throwable``
* ``TypeNotPresentException``
* ``UnknownError``
* ``UnsatisfiedLinkError``
* ``UnsupportedClassVersionError``
* ``UnsupportedOperationException``
* ``VerifyError``
* ``VirtualMachineError``
* ``Void``
* ``annotation.Annotation``
* ``annotation.Retention``
* ``annotation.RetentionPolicy``
* ``constant.Constable``
* ``constant.ConstantDesc``
* ``ref.PhantomReference``
* ``ref.Reference``
* ``ref.ReferenceQueue``
* ``ref.SoftReference``
* ``ref.WeakReference``
* ``reflect.AccessibleObject``
* ``reflect.Array``
* ``reflect.Constructor``
* ``reflect.Executable``
* ``reflect.Field``
* ``reflect.InvocationTargetException``
* ``reflect.Method``
* ``reflect.UndeclaredThrowableException``

java.math
"""""""""
* ``BigDecimal``
* ``BigInteger``
* ``BitLevel``
* ``Conversion``
* ``Division``
* ``Elementary``
* ``Logical``
* ``MathContext``
* ``Multiplication``
* ``Primality``
* ``RoundingMode``

java.net
""""""""
* ``BindException``
* ``ConnectException``
* ``Inet4Address``
* ``Inet6Address``
* ``InetAddress``
* ``InetAddressBase``
* ``InetSocketAddress``
* ``MalformedURLException``
* ``NoRouteToHostException``
* ``PortUnreachableException``
* ``ServerSocket``
* ``Socket``
* ``SocketAddress``
* ``SocketException``
* ``SocketImpl``
* ``SocketInputStream``
* ``SocketOption``
* ``SocketOptions``
* ``SocketOutputStream``
* ``SocketTimeoutException``
* ``URI``
* ``URIEncoderDecoder``
* ``URISyntaxException``
* ``URL``
* ``URLClassLoader``
* ``URLConnection``
* ``URLDecoder``
* ``URLEncoder``
* ``UnknownHostException``
* ``UnknownServiceException``

java.nio
"""""""""
* ``Buffer``
* ``BufferOverflowException``
* ``BufferUnderflowException``
* ``ByteBuffer``
* ``ByteOrder``
* ``CharBuffer``
* ``DoubleBuffer``
* ``FloatBuffer``
* ``IntBuffer``
* ``InvalidMarkException``
* ``LongBuffer``
* ``MappedByteBuffer``
* ``ReadOnlyBufferException``
* ``ShortBuffer``
* ``channels.ByteChannel``
* ``channels.Channel``
* ``channels.Channels``
* ``channels.ClosedChannelException``
* ``channels.FileChannel``
* ``channels.FileChannel.MapMode``
* ``channels.FileLock``
* ``channels.GatheringByteChannel``
* ``channels.InterruptibleChannel``
* ``channels.NonReadableChannelException``
* ``channels.NonWritableChannelException``
* ``channels.OverlappingFileLockException``
* ``channels.ReadableByteChannel``
* ``channels.ScatteringByteChannel``
* ``channels.SeekableByteChannel``
* ``channels.WritableByteChannel``
* ``channels.spi.AbstractInterruptibleChannel``
* ``charset.CharacterCodingException``
* ``charset.Charset``
* ``charset.CharsetDecoder``
* ``charset.CharsetEncoder``
* ``charset.CoderMalfunctionError``
* ``charset.CoderResult``
* ``charset.CodingErrorAction``
* ``charset.IllegalCharsetNameException``
* ``charset.MalformedInputException``
* ``charset.StandardCharsets``
* ``charset.UnmappableCharacterException``
* ``charset.UnsupportedCharsetException``
* ``file.AccessDeniedException``
* ``file.CopyOption``
* ``file.DirectoryIteratorException``
* ``file.DirectoryNotEmptyException``
* ``file.DirectoryStream``
* ``file.DirectoryStream.Filter``
* ``file.DirectoryStreamImpl``
* ``file.FileAlreadyExistsException``
* ``file.FileSystem``
* ``file.FileSystemException``
* ``file.FileSystemLoopException``
* ``file.FileSystemNotFoundException``
* ``file.FileSystems``
* ``file.FileVisitOption``
* ``file.FileVisitResult``
* ``file.FileVisitor``
* ``file.Files``
* ``file.InvalidPathException``
* ``file.LinkOption``
* ``file.NoSuchFileException``
* ``file.NotDirectoryException``
* ``file.NotLinkException``
* ``file.OpenOption``
* ``file.Path``
* ``file.PathMatcher``
* ``file.Paths``
* ``file.RegexPathMatcher``
* ``file.SimpleFileVisitor``
* ``file.StandardCopyOption``
* ``file.StandardOpenOption``
* ``file.StandardWatchEventKinds``
* ``file.WatchEvent``
* ``file.WatchEvent.Kind``
* ``file.WatchEvent.Modifier``
* ``file.WatchKey``
* ``file.WatchService``
* ``file.Watchable``
* ``file.attribute.AclEntry``
* ``file.attribute.AclFileAttributeView``
* ``file.attribute.AttributeView``
* ``file.attribute.BasicFileAttributeView``
* ``file.attribute.BasicFileAttributes``
* ``file.attribute.DosFileAttributeView``
* ``file.attribute.DosFileAttributes``
* ``file.attribute.FileAttribute``
* ``file.attribute.FileAttributeView``
* ``file.attribute.FileOwnerAttributeView``
* ``file.attribute.FileStoreAttributeView``
* ``file.attribute.FileTime``
* ``file.attribute.GroupPrincipal``
* ``file.attribute.PosixFileAttributeView``
* ``file.attribute.PosixFileAttributes``
* ``file.attribute.PosixFilePermission``
* ``file.attribute.PosixFilePermissions``
* ``file.attribute.UserDefinedFileAttributeView``
* ``file.attribute.UserPrincipal``
* ``file.attribute.UserPrincipalLookupService``
* ``file.attribute.UserPrincipalNotFoundException``
* ``file.spi.FileSystemProvider``

java.rmi
""""""""
* ``Remote``
* ``RemoteException``

java.security
"""""""""""""
* ``AccessControlException``
* ``CodeSigner``
* ``DummyMessageDigest``
* ``GeneralSecurityException``
* ``MessageDigest``
* ``MessageDigestSpi``
* ``NoSuchAlgorithmException``
* ``Principal``
* ``Timestamp``
* ``TimestampConstructorHelper``
* ``cert.CertPath``
* ``cert.Certificate``
* ``cert.CertificateEncodingException``
* ``cert.CertificateException``
* ``cert.CertificateFactory``
* ``cert.X509Certificate``
* ``cert.X509Extension``


java.util
"""""""""
* ``AbstractCollection``
* ``AbstractList``
* ``AbstractListView``
* ``AbstractMap``
* ``AbstractMap.SimpleEntry``
* ``AbstractMap.SimpleImmutableEntry``
* ``AbstractQueue``
* ``AbstractRandomAccessListIterator``
* ``AbstractSequentialList``
* ``AbstractSet``
* ``ArrayDeque``
* ``ArrayList``
* ``Arrays``
* ``BackedUpListIterator``
* ``Base64``
* ``Base64.Decoder``
* ``Base64.Encoder``
* ``BitSet``
* ``Calendar``
* ``Collection``
* ``Collections``
* ``Comparator``
* ``ConcurrentModificationException``
* ``Date``
* ``Deque``
* ``Dictionary``
* ``DoubleSummaryStatistics``
* ``DuplicateFormatFlagsException``
* ``EmptyStackException``
* ``EnumSet``
* ``Enumeration``
* ``FormatFlagsConversionMismatchException``
* ``Formattable``
* ``FormattableFlags``
* ``Formatter``
* ``Formatter.BigDecimalLayoutForm``
* ``FormatterClosedException``
* ``GregorianCalendar``
* ``HashMap``
* ``HashSet``
* ``Hashtable``
* ``IdentityHashMap``
* ``IllegalFormatCodePointException``
* ``IllegalFormatConversionException``
* ``IllegalFormatException``
* ``IllegalFormatFlagsException``
* ``IllegalFormatPrecisionException``
* ``IllegalFormatWidthException``
* ``IllformedLocaleException``
* ``InputMismatchException``
* ``InvalidPropertiesFormatException``
* ``IntSummaryStatistics``
* ``Iterator``
* ``LinkedHashMap``
* ``LinkedHashSet``
* ``LinkedList``
* ``List``
* ``ListIterator``
* ``LongSummaryStatistics``
* ``MissingFormatArgumentException``
* ``MissingFormatWidthException``
* ``MissingResourceException``
* ``Map``
* ``NavigableMap``
* ``NavigableSet``
* ``NoSuchElementException``
* ``Objects``
* ``Optional``
* ``OptionalDouble``
* ``OptionalInt``
* ``OptionalLong``
* ``PrimitiveIterator``
* ``PrimitiveIterator.OfDouble``
* ``PrimitiveIterator.OfInt``
* ``PrimitiveIterator.OfLong``
* ``PriorityQueue``
* ``Properties``
* ``Queue``
* ``Random``
* ``RandomAccess``
* ``RandomAccessListIterator``
* ``ServiceConfigurationError``
* ``Set``
* ``SizeChangeEvent``
* ``SortedMap``
* ``SortedSet``
* ``Spliterator``
* ``Spliterators``
* ``StringJoiner``
* ``StringTokenizer``
* ``TooManyListenersException``
* ``TreeSet``
* ``UUID``
* ``UnknownFormatConversionException``
* ``UnknownFormatFlagsException``
* ``Vector``
* ``WeakHashMap``

..
    util.concurrent classes are listed as in JVM documentation.
    This varies in some cases from strictly alphabetical order.
    Yes, mind bending, but that is how JVM does it.
    Classes in sub-directories are at the end of the list.
..

java.util.concurrent
""""""""""""""""""""
* ``AbstractExecutorService``
* ``ArrayBlockingQueue``
* ``BlockingDeque``
* ``BlockingQueue``
* ``BrokenBarrierException``
* ``Callable``
* ``CancellationException``
* ``CompletionService``
* ``ConcurrentHashMap``
* ``ConcurrentHashMap.KeySetView``
* ``ConcurrentLinkedQueue``
* ``ConcurrentMap``
* ``ConcurrentNavigableMap``
* ``CopyOnWriteArrayList``
* ``ConcurrentSkipListSet``
* ``CountDownLatch``
* ``CountedCompleter``
* ``CyclicBarrier``
* ``Delayed``
* ``ExecutionException``
* ``Executor``
* ``ExecutorCompletionService``
* ``Executors``
* ``ExecutorService``
* ``Flow``
* ``Flow.Processor``
* ``Flow.Publisher``
* ``Flow.Subscriber``
* ``Flow.Subscripton``
* ``ForkJoinPool``
* ``ForkJoinPoo.ForkJoinWorkerThreadFactory``
* ``ForkJoinPoo.ManagedBlocker``
* ``ForkJoinTask``
* ``ForkJoinWorkerThread``
* ``Future``
* ``FutureTask``
* ``LinkedBlockingQueue``
* ``PriorityBlockingQueue``
* ``RecursiveAction``
* ``RecursiveTask``
* ``RejectedExecutionException``
* ``RejectedExecutionHandler``
* ``RunnableFuture``
* ``RunnableScheduledFuture``
* ``ScheduledExecutorService``
* ``ScheduledFuture``
* ``ScheduledThreadPoolExecutor``
* ``Semaphore``
* ``SynchronousQueue``
* ``ThreadFactory``
* ``ThreadLocalRandom``
* ``ThreadPoolExecutor``
* ``ThreadPoolExecutor.AbortPolicy``
* ``ThreadPoolExecutor.CallerRunsPolicy``
* ``ThreadPoolExecutor.DiscardOldestPolicy``
* ``ThreadPoolExecutor.DiscardPolicy``
* ``TimeoutException``
* ``TimeUnit``
* ``TransferQueue``
* ``atomic.AtomicBoolean``
* ``atomic.AtomicInteger``
* ``atomic.AtomicLong``
* ``atomic.AtomicLongArray``
* ``atomic.AtomicReference``
* ``atomic.AtomicReferenceArray``
* ``atomic.LongAdder``
* ``locks.AbstractOwnableSynchronizer``
* ``locks.AbstractQueuedSynchronizer``
* ``locks.Lock``
* ``locks.ReentrantLock``

java.util.function
""""""""""""""""""
* ``BiConsumer``
* ``BiFunction``
* ``BiPredicate``
* ``BinaryOperator``
* ``Consumer``
* ``Function``
* ``IntUnaryOperator``
* ``Predicate``
* ``Supplier``
* ``UnaryOperator``

java.util.jar
"""""""""""""
* ``Attributes``
* ``Attributes.Name``
* ``InitManifest``
* ``JarEntry``
* ``JarFile``
* ``JarInputStream``
* ``JarOutputStream``
* ``Manifest``

java.util.regex
"""""""""""""""
* ``MatchResult``
* ``Matcher``
* ``Pattern``
* ``PatternSyntaxException``

java.util.stream
""""""""""""""""
* ``BaseStream``
* ``Collector``
* ``Collector.Characteristics``
* ``Collectors``
* ``DoubleStream``
* ``DoubleStream.Builder``
* ``DoubleStream.DoubleMapMultiConsumer``
* ``Stream``
* ``Stream.Builder``
* ``StreamSupport``

java.util.zip
"""""""""""""
* ``Adler32``
* ``CRC32``
* ``CheckedInputStream``
* ``CheckedOutputStream``
* ``Checksum``
* ``DataFormatException``
* ``Deflater``
* ``DeflaterOutputStream``
* ``GZIPInputStream``
* ``GZIPOutputStream``
* ``Inflater``
* ``InflaterInputStream``
* ``ZipConstants``
* ``ZipEntry``
* ``ZipException``
* ``ZipFile``
* ``ZipInputStream``
* ``ZipOutputStream``


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
to add the ".c" and ".h" files there.

Reasoning for the lack of ``getResource()`` and ``getResources()``:

In Scala Native, the outputted file that can be run is a binary, unlike JVM's
classfiles and jars. For that reason, if ``getResources()`` URI methods would be implemented,
a new URI format using a seperate FileSystem would have to be added (e.g. instead
of obtaining ``jar:file:path.ext`` you would obtain ``embedded:path.ext``). As this still
would provide a meaningful inconsistency between JVM's javalib API and Scala
Native's reimplementation, this remains not implemented for now. The added
``getClass().getResourceAsInputStream()`` however is able to be consistent between
the platforms.


Internet Protocol Version 6 (IPv6) Networking
---------------------------------------------

IPv6 provides network features which are more efficient and gradually
replacing its worthy, but venerable, predecessor IPv4.

The Scala Native Java library now supports IPv6 as it is described in the
original `Java Networking IPv6 User Guide  <https://docs.oracle.com/javase/8/docs/technotes/guides/net/ipv6_guide/index.html/>`_. The design center is that
a Scala Java Virtual Machine (JVM) program using networking
will run almost identically using Scala Native.

IPv6 will be used if any network interface on a system/node/host, other
than the loopback interface, is configured to enable IPv6. Otherwise,
IPv4 is used as before. Java has been using this approach for decades.

Most people will not be able to determine if IPv6 or IPv4 is in use.
Networks experts will, by using specialist tools.

Scala Native checks and honors the two System Properties described in
the ipv6_guide above: ``java.net.preferIPv4Stack`` and
``java.net.preferIPv6Addresses``. This check is done once, when the
network is first used.

* If there is ever a reason to use only IPv4, a program can
  set the ``java.net.preferIPv4Stack``  to ``true`` at runtime
  before the first use of the network.  There is no way to accomplish
  this from the command line or environment.::

      System.setProperty("java.net.preferIPv6Addresses", "true")

Continue to :ref:`libc`.
