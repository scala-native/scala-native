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
* ``java.lang.Character$CaseFolding``
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
* ``java.lang.PipeIO``
* ``java.lang.PipeIO$NullInput``
* ``java.lang.PipeIO$NullOutput``
* ``java.lang.PipeIO$Stream``
* ``java.lang.PipeIO$Stream$class``
* ``java.lang.PipeIO$StreamImpl``
* ``java.lang.Process``
* ``java.lang.ProcessBuilder``
* ``java.lang.ProcessBuilder$Redirect``
* ``java.lang.ProcessBuilder$Redirect$RedirectImpl``
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
* ``java.lang.UnixProcess``
* ``java.lang.UnixProcess$ProcessMonitor``
* ``java.lang.UnknownError``
* ``java.lang.UnsatisfiedLinkError``
* ``java.lang.UnsupportedClassVersionError``
* ``java.lang.UnsupportedOperationException``
* ``java.lang.VerifyError``
* ``java.lang.VirtualMachineError``
* ``java.lang.Void``
* ``java.lang._String$CaseInsensitiveComparator``
* ``java.lang.annotation.Retention``
* ``java.lang.annotation.RetentionPolicy``
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
* ``java.math.BigDecimal$QuotAndRem``
* ``java.math.BigDecimal$StringOps``
* ``java.math.BigInteger``
* ``java.math.BigInteger$QuotAndRem``
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
* ``java.net.InetAddressBase``
* ``java.net.InetAddressBase$class``
* ``java.net.InetSocketAddress``
* ``java.net.MalformedURLException``
* ``java.net.NoRouteToHostException``
* ``java.net.PlainSocketImpl``
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
* ``java.net.URLEncoder``
* ``java.net.UnknownHostException``
* ``java.net.UnknownServiceException``
* ``java.nio.Buffer``
* ``java.nio.BufferOverflowException``
* ``java.nio.BufferUnderflowException``
* ``java.nio.ByteArrayBits``
* ``java.nio.ByteBuffer``
* ``java.nio.ByteOrder``
* ``java.nio.CharBuffer``
* ``java.nio.DoubleBuffer``
* ``java.nio.FloatBuffer``
* ``java.nio.GenBuffer``
* ``java.nio.GenHeapBuffer``
* ``java.nio.GenHeapBuffer$NewHeapBuffer``
* ``java.nio.GenHeapBufferView``
* ``java.nio.GenHeapBufferView$NewHeapBufferView``
* ``java.nio.HeapByteBuffer``
* ``java.nio.HeapByteBuffer$NewHeapByteBuffer``
* ``java.nio.HeapByteBufferCharView``
* ``java.nio.HeapByteBufferCharView$NewHeapByteBufferCharView``
* ``java.nio.HeapByteBufferDoubleView``
* ``java.nio.HeapByteBufferDoubleView$NewHeapByteBufferDoubleView``
* ``java.nio.HeapByteBufferFloatView``
* ``java.nio.HeapByteBufferFloatView$NewHeapByteBufferFloatView``
* ``java.nio.HeapByteBufferIntView``
* ``java.nio.HeapByteBufferIntView$NewHeapByteBufferIntView``
* ``java.nio.HeapByteBufferLongView``
* ``java.nio.HeapByteBufferLongView$NewHeapByteBufferLongView``
* ``java.nio.HeapByteBufferShortView``
* ``java.nio.HeapByteBufferShortView$NewHeapByteBufferShortView``
* ``java.nio.HeapCharBuffer``
* ``java.nio.HeapCharBuffer$NewHeapCharBuffer``
* ``java.nio.HeapDoubleBuffer``
* ``java.nio.HeapDoubleBuffer$NewHeapDoubleBuffer``
* ``java.nio.HeapFloatBuffer``
* ``java.nio.HeapFloatBuffer$NewHeapFloatBuffer``
* ``java.nio.HeapIntBuffer``
* ``java.nio.HeapIntBuffer$NewHeapIntBuffer``
* ``java.nio.HeapLongBuffer``
* ``java.nio.HeapLongBuffer$NewHeapLongBuffer``
* ``java.nio.HeapShortBuffer``
* ``java.nio.HeapShortBuffer$NewHeapShortBuffer``
* ``java.nio.IntBuffer``
* ``java.nio.InvalidMarkException``
* ``java.nio.LongBuffer``
* ``java.nio.MappedByteBuffer``
* ``java.nio.ReadOnlyBufferException``
* ``java.nio.ShortBuffer``
* ``java.nio.StringCharBuffer``
* ``java.nio.channels.ByteChannel``
* ``java.nio.channels.Channel``
* ``java.nio.channels.Channels``
* ``java.nio.channels.ClosedChannelException``
* ``java.nio.channels.FileChannel``
* ``java.nio.channels.FileChannel$MapMode``
* ``java.nio.channels.FileChannelImpl``
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
* ``java.nio.file.LinkOption``
* ``java.nio.file.NoSuchFileException``
* ``java.nio.file.NotDirectoryException``
* ``java.nio.file.NotLinkException``
* ``java.nio.file.OpenOption``
* ``java.nio.file.Path``
* ``java.nio.file.PathMatcher``
* ``java.nio.file.PathMatcherImpl``
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
* ``java.nio.file.attribute.FileAttributeView$class``
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
* ``java.text.DateFormatSymbols``
* ``java.text.DecimalFormat``
* ``java.text.DecimalFormat$BigDecimalFormatting``
* ``java.text.DecimalFormat$BigIntegerFormatting``
* ``java.text.DecimalFormat$DoubleFormatting``
* ``java.text.DecimalFormat$DoubleFormatting$DoubleDigits``
* ``java.text.DecimalFormat$Formatting``
* ``java.text.DecimalFormat$Formatting$Digits``
* ``java.text.DecimalFormat$Formatting$class``
* ``java.text.DecimalFormat$LongFormatting``
* ``java.text.DecimalFormat$PatternSyntax``
* ``java.text.DecimalFormat$PatternSyntax$Affix``
* ``java.text.DecimalFormat$PatternSyntax$Exponent``
* ``java.text.DecimalFormat$PatternSyntax$Fraction``
* ``java.text.DecimalFormat$PatternSyntax$Fraction$$plus$plus``
* ``java.text.DecimalFormat$PatternSyntax$Integer``
* ``java.text.DecimalFormat$PatternSyntax$MinimumExponent``
* ``java.text.DecimalFormat$PatternSyntax$MinimumFraction``
* ``java.text.DecimalFormat$PatternSyntax$MinimumInteger``
* ``java.text.DecimalFormat$PatternSyntax$Number``
* ``java.text.DecimalFormat$PatternSyntax$Number$Fraction_$plus$plus``
* ``java.text.DecimalFormat$PatternSyntax$Number$Integer_$plus$plus``
* ``java.text.DecimalFormat$PatternSyntax$OptionalFraction``
* ``java.text.DecimalFormat$PatternSyntax$Pattern``
* ``java.text.DecimalFormat$PatternSyntax$Pattern$$plus$plus``
* ``java.text.DecimalFormat$PatternSyntax$SignedPattern``
* ``java.text.DecimalFormat$PatternSyntax$SignedPattern$Number_$plus$plus``
* ``java.text.DecimalFormat$PatternSyntax$SignedPattern$Prefix_$plus$plus``
* ``java.text.DecimalFormatSymbols``
* ``java.text.FieldPosition``
* ``java.text.Format``
* ``java.text.Format$Field``
* ``java.text.NumberFormat``
* ``java.text.ParseException``
* ``java.time.Duration``
* ``java.time.Instant``
* ``java.time.temporal.TemporalAmount``
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
* ``java.util.Calendar``
* ``java.util.Collection``
* ``java.util.Collections``
* ``java.util.Collections$BasicSynchronizedList$1``
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
* ``java.util.Collections$WrappedCollection$class``
* ``java.util.Collections$WrappedEquals``
* ``java.util.Collections$WrappedEquals$class``
* ``java.util.Collections$WrappedIterator``
* ``java.util.Collections$WrappedIterator$class``
* ``java.util.Collections$WrappedList``
* ``java.util.Collections$WrappedList$class``
* ``java.util.Collections$WrappedListIterator``
* ``java.util.Collections$WrappedListIterator$class``
* ``java.util.Collections$WrappedMap``
* ``java.util.Collections$WrappedMap$class``
* ``java.util.Collections$WrappedSet``
* ``java.util.Collections$WrappedSortedMap``
* ``java.util.Collections$WrappedSortedMap$class``
* ``java.util.Collections$WrappedSortedSet``
* ``java.util.Collections$WrappedSortedSet$class``
* ``java.util.Comparator``
* ``java.util.Comparator$class``
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
* ``java.util.Formatter$DateTimeUtil``
* ``java.util.Formatter$FloatUtil``
* ``java.util.Formatter$FormatToken``
* ``java.util.Formatter$ParserStateMachine``
* ``java.util.Formatter$Transformer``
* ``java.util.FormatterClosedException``
* ``java.util.GregorianCalendar``
* ``java.util.HashMap``
* ``java.util.HashMap$AbstractMapView``
* ``java.util.HashMap$AbstractMapView$class``
* ``java.util.HashMap$AbstractMapViewIterator``
* ``java.util.HashMap$EntrySet``
* ``java.util.HashMap$KeySet``
* ``java.util.HashMap$ValuesView``
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
* ``java.util.LinkedList$Node``
* ``java.util.List``
* ``java.util.ListIterator``
* ``java.util.Locale``
* ``java.util.Map``
* ``java.util.Map$Entry``
* ``java.util.MissingFormatArgumentException``
* ``java.util.MissingFormatWidthException``
* ``java.util.MissingResourceException``
* ``java.util.NavigableSet``
* ``java.util.NavigableView``
* ``java.util.NoSuchElementException``
* ``java.util.Objects``
* ``java.util.PriorityQueue``
* ``java.util.PriorityQueue$BoxOrdering``
* ``java.util.Properties``
* ``java.util.Queue``
* ``java.util.Random``
* ``java.util.RandomAccess``
* ``java.util.RandomAccessListIterator``
* ``java.util.ServiceConfigurationError``
* ``java.util.Set``
* ``java.util.SizeChangeEvent``
* ``java.util.SizeChangeEvent$class``
* ``java.util.SortedMap``
* ``java.util.SortedSet``
* ``java.util.StringTokenizer``
* ``java.util.TimeZone``
* ``java.util.TooManyListenersException``
* ``java.util.TreeSet``
* ``java.util.TreeSet$BoxOrdering``
* ``java.util.UUID``
* ``java.util.UnknownFormatConversionException``
* ``java.util.UnknownFormatFlagsException``
* ``java.util.WeakHashMap``
* ``java.util.WeakHashMap$AbstractMapView``
* ``java.util.WeakHashMap$AbstractMapView$class``
* ``java.util.WeakHashMap$AbstractMapViewIterator``
* ``java.util.WeakHashMap$EntrySet``
* ``java.util.WeakHashMap$KeySet``
* ``java.util.WeakHashMap$ValuesView``
* ``java.util.concurrent.Callable``
* ``java.util.concurrent.CancellationException``
* ``java.util.concurrent.ExecutionException``
* ``java.util.concurrent.Executor``
* ``java.util.concurrent.RejectedExecutionException``
* ``java.util.concurrent.TimeUnit``
* ``java.util.concurrent.TimeoutException``
* ``java.util.concurrent.atomic.AtomicBoolean``
* ``java.util.concurrent.atomic.AtomicInteger``
* ``java.util.concurrent.atomic.AtomicLong``
* ``java.util.concurrent.atomic.AtomicLongArray``
* ``java.util.concurrent.atomic.AtomicReference``
* ``java.util.concurrent.atomic.AtomicReferenceArray``
* ``java.util.concurrent.locks.AbstractOwnableSynchronizer``
* ``java.util.concurrent.locks.AbstractQueuedSynchronizer``
* ``java.util.function.BiConsumer``
* ``java.util.function.BiConsumer$class``
* ``java.util.function.BiFunction``
* ``java.util.function.BiFunction$class``
* ``java.util.function.BiPredicate``
* ``java.util.function.BiPredicate$class``
* ``java.util.function.BinaryOperator``
* ``java.util.function.Consumer``
* ``java.util.function.Consumer$class``
* ``java.util.function.Function``
* ``java.util.function.Function$class``
* ``java.util.function.Predicate``
* ``java.util.function.Predicate$class``
* ``java.util.function.Supplier``
* ``java.util.function.UnaryOperator``
* ``java.util.jar.Attributes``
* ``java.util.jar.Attributes$Name``
* ``java.util.jar.InitManifest``
* ``java.util.jar.JarEntry``
* ``java.util.jar.JarFile``
* ``java.util.jar.JarFile$JarFileEnumerator$1``
* ``java.util.jar.JarFile$JarFileInputStream``
* ``java.util.jar.JarInputStream``
* ``java.util.jar.JarOutputStream``
* ``java.util.jar.JarVerifier``
* ``java.util.jar.JarVerifier$VerifierEntry``
* ``java.util.jar.Manifest``
* ``java.util.jar.Manifest$Chunk``
* ``java.util.package``
* ``java.util.package$Box``
* ``java.util.package$CompareNullablesOps``
* ``java.util.package$IdentityBox``
* ``java.util.regex.MatchResult``
* ``java.util.regex.Matcher``
* ``java.util.regex.Pattern``
* ``java.util.regex.PatternSyntaxException``
* ``java.util.stream.BaseStream``
* ``java.util.stream.CompositeStream``
* ``java.util.stream.EmptyIterator``
* ``java.util.stream.Stream``
* ``java.util.stream.Stream$Builder``
* ``java.util.stream.Stream$Builder$class``
* ``java.util.stream.WrappedScalaStream``
* ``java.util.stream.WrappedScalaStream$Builder``
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
* ``java.util.zip.ZipConstants$class``
* ``java.util.zip.ZipEntry``
* ``java.util.zip.ZipEntry$LittleEndianReader``
* ``java.util.zip.ZipException``
* ``java.util.zip.ZipFile``
* ``java.util.zip.ZipFile$RAFStream``
* ``java.util.zip.ZipFile$ZipInflaterInputStream``
* ``java.util.zip.ZipInputStream``
* ``java.util.zip.ZipOutputStream``

**Note:** This is an ongoing effort, some of the classes listed here might
be partially implemented. Please consult `javalib sources
<https://github.com/scala-native/scala-native/tree/master/javalib/src/main/scala/java>`_
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

3. The following Matcher methods have a minimal implementation:

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

4. Scala Native 0.3.8 required POSIX patterns to have the form
   ``[[:alpha:]]``.
   Now the Java standard form ``\p{Alpha}`` is accepted and the former variant
   pattern is not. This improves compatibility with Java but,
   regrettably, may require code changes when upgrading from Scala Native
   0.3.8.

Continue to :ref:`libc`.
