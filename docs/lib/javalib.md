# Java Standard Library

Scala Native supports a subset of the JDK core libraries reimplemented
in Scala.

## Supported classes

For list of currently supported Java Standard Library types and methods refer to [scaladoc package](https://www.javadoc.io/doc/org.scala-native/javalib_native0.5_2.13/latest/index.html) or consult [javalib sources](https://github.com/scala-native/scala-native/tree/main/javalib/src/main/scala/java) for details.

## Regular expressions (java.util.regex)

Scala Native implements `java.util.regex`-compatible API
using [Google's RE2 library](https://github.com/google/re2). RE2 is not
a drop-in replacement for `java.util.regex` but handles most
common cases well.

Some notes on the implementation:

1.  The included RE2 implements a Unicode version lower than the version
    used in the Scala Native Character class (>= 7.0.0). The RE2
    Unicode version is in the 6.n range. For reference, Java 8 released
    with Unicode 6.2.0.

    The RE2 implemented may not match codepoints added or changed in
    later Unicode versions. Similarly, there may be slight differences
    for Unicode codepoints with high numeric value between values used
    by RE2 and those used by the Character class.

2.  This implementation of RE2 does not support:

    -   Character classes:
        -   Unions: `[a-d[m-p]]`
        -   Intersections: `[a-z&&[^aeiou]]`
    -   Predefined character classes: `\h`, `\H`, `\v`, `\V`
    -   Patterns:
        -   Octal: `\0100` - use decimal or hexadecimal instead.
        -   Two character Hexadecimal: `\xFF` - use `\x00FF` instead.
        -   All alphabetic Unicode: `\uBEEF` - use hex `\xBEEF` instead.
        -   Escape: `\e` - use `\u001B` instead.
    -   Java character function classes:
        -   `\p{javaLowerCase}`
        -   `\p{javaUpperCase}`
        -   `\p{javaWhitespace}`
        -   `\p{javaMirrored}`
    -   Boundary matchers: `\G`, `\R`, `\Z`
    -   Possessive quantifiers: `X?+`, `X*+`, `X++`, `X{n}+`, `X{n,}+`,
        `X{n,m}+`
    -   Lookaheads: `(?=X)`, `(?!X)`, `(?<=X)`, `(?<!X)`, `(?>X)`
    -   Options
        -   CANON_EQ
        -   COMMENTS
        -   LITERAL
        -   UNICODE_CASE
        -   UNICODE_CHARACTER_CLASS
        -   UNIX_LINES
    -   Patterns to match a Unicode binary property, such as
        `\p{isAlphabetic}` for a codepoint with the \'alphabetic\'
        property, are not supported. Often another pattern `\p{isAlpha}`
        may be used instead, `\p{isAlpha}` in this case.

3.  The reference Java 8 regex package does not support certain commonly
    used Perl expressions supported by this implementation of RE2. For
    example, for named capture groups Java uses the expression
    `(?<foo>)` while Perl uses the expression `(?P<foo>)`.

    Scala Native java.util.regex methods accept both forms. This
    extension is intended to useful but is not strictly Java 8
    compliant. Not all RE2 Perl expressions may be exposed in this way.

4.  The following Matcher methods have a minimal implementation:

    -   `Matcher.hasAnchoringBounds()` - always return true.

    -   `Matcher.hasTransparentBounds()` - always throws
        `UnsupportedOperationException` because RE2 does not support
        lookaheads.

    -   `Matcher.hitEnd()` - always throws `UnsupportedOperationException`.

    -   `Matcher.region(int, int)`

    -   `Matcher.regionEnd()`

    -   `Matcher.regionStart()`

    -   `Matcher.requireEnd()` - always throws
        `UnsupportedOperationException`.

    -   `Matcher.useAnchoringBounds(boolean)` - always throws `UnsupportedOperationException`

    -   `Matcher.useTransparentBounds(boolean)` - always throws
        `UnsupportedOperationException` because RE2 does not support
        lookaheads.

5.  Scala Native 0.3.8 required POSIX patterns to have the form
    `[[:alpha:]]`. Now the Java standard form `\p{Alpha}` is accepted
    and the former variant pattern is not. This improves compatibility
    with Java but, regrettably, may require code changes when upgrading
    from Scala Native 0.3.8.

## Embedding Resources

In Scala Native, resources are implemented via embedding a resource in a
resulting binary file. Only `getClass().getResourceAsInputStream()` is
implemented. For that to work, you have to specify an additional
NativeConfig option:

``` scala
nativeConfig ~= {
  _.withEmbedResources(true)
}
```

This will include the resource files found on the classpath in the
resulting binary file.

Also, you can specify which resources would be embedded in an executable
using include or exclude glob pattern. By default, scala-native will
include all the files in the classpath, and exclude none (there're some
exceptions for files such as `.class`, see below). By specifying the
include patterns, only the files matching the include patterns will be
included. This can be useful for reducing the size of your executables.

The example below will include all the text and png files in the
classpath, while excluding the rootdoc.txt file.

``` scala
nativeConfig ~= {
  _.withEmbedResources(true)
    .withResourceIncludePatterns(Seq("**.txt", "**.png"))
    .withResourceExcludePatterns(Seq("rootdoc.txt"))
}
```

Also, note that this featuer is using Java's PathMatcher, which behave
a bit different from the posix glob.
<https://docs.oracle.com/javase/tutorial/essential/io/find.html>

Please note that files with following extensions cannot be embedded and
used as a resource:

`".class", ".tasty", ".nir", ".scala", ".java", ".jar"`

This is to avoid unnecesarily embedding source files. If necessary,
please consider using a different file extension for embedding. Files
found in the `resources/scala-native` directory will not be embedded as
well. It is recommended to add the ".c" and ".h" files there.

Reasoning for the lack of `getResource()` and `getResources()`:

In Scala Native, the outputted file that can be run is a binary, unlike
JVM's classfiles and jars. For that reason, if `getResources()` URI
methods would be implemented, a new URI format using a seperate
FileSystem would have to be added (e.g. instead of obtaining
`jar:file:path.ext` you would obtain `embedded:path.ext`). As this still
would provide a meaningful inconsistency between JVM's javalib API and
Scala Native's reimplementation, this remains not implemented for now.
The added `getClass().getResourceAsInputStream()` however is able to be
consistent between the platforms.

## Internet Protocol Version 6 (IPv6) Networking

IPv6 provides network features which are more efficient and gradually
replacing its worthy, but venerable, predecessor IPv4.

The Scala Native Java library now supports IPv6 as it is described in
the original [Java Networking IPv6 User
Guide](https://docs.oracle.com/javase/8/docs/technotes/guides/net/ipv6_guide/index.html/).
The design center is that a Scala Java Virtual Machine (JVM) program
using networking will run almost identically using Scala Native.

IPv6 will be used if any network interface on a system/node/host, other
than the loopback interface, is configured to enable IPv6. Otherwise,
IPv4 is used as before. Java has been using this approach for decades.

Most people will not be able to determine if IPv6 or IPv4 is in use.
Networks experts will, by using specialist tools.

Scala Native checks and honors the two System Properties described in
the ipv6_guide above: `java.net.preferIPv4Stack` and
`java.net.preferIPv6Addresses`. This check is done once, when the
network is first used.

-   If there is ever a reason to use only IPv4, a program can set the
    `java.net.preferIPv4Stack` to `true` at runtime before the first use
    of the network. There is no way to accomplish this from the command
    line or environment.:

        System.setProperty("java.net.preferIPv6Addresses", "true")

(service_providers)=
## Support for discovering service providers

Scala Native implements partial support for using the Java service
providers pattern. This includes using `java.util.ServiceLoader` to load
available implementations of a given interface.

### Step 1: Configure META-INF/services

Similarly to the JVM toolchain, the Scala Native toolchain will try to
discover implementations of services using
`META-INF/services/<fully-qualified-class-name>` files found in
resources of dependencies.

The example in "Step 2" below provides a custom
`java.nio.file.spi.FileSystemProvider` implementation called
`my.lib.MyCustomFileSystem`.

To use the custom implementation, the project's
`<projectRoot>/src/main/resources/META-INF/services/` directory must
contain a file called `java.nio.file.spi.FileSystemProvider`.

That file contains the line: `my.lib.MyCustomFileSystem`

### Step 2: Configure Scala Native

Scala Native uses an ahead of time compilation model and requires
additional configuration. This allows loading only implementations
requested in the provided configuration.

A snippet to configure one local implementation for
"java.nio.file.spi.FileSystemProvider" and two for "MyServiceName"
looks like:

``` scala
/* The project defines a Map in a .sbt or .scala file. The Map
 * is then used to configure the available providers.
 *
 * The entries of this map have the general form:
 *  "<ServiceName>" -> Seq("<ServiceProviderClassName>")
 *
 * If additional implementations are to be defined, this becomes:
 *  "<ServiceName>" -> Seq("<ServiceProviderClassName_1>",
 *                         "<ServiceProviderClassName_2>")
 *
 * The first entry below is a better model than the second.
 * The names in the second entry are simplified for demonstration.
 * More fully qualified names would be used in real world code.
 */

nativeConfig ~= { _.withServiceProviders(
  Map(
    "java.nio.file.spi.FileSystemProvider" -> Seq(
    "my.lib.MyCustomFileSystem"),
    "MyServiceName" -> Seq(
    "MyImplementation1",
    "foo.bar.MyOtherImplementation")
 )
)}
```

When linking the project, all providers of service referenced by
`java.util.ServiceLoader.load` that were reached from any entrypoint
will be enlisted.

These providers will report one of the five status values:

-   `Loaded` - this provider was allowed by the config and found on the
    classpath. It would be available at runtime.
-   `Available` - this provider was found on classpath, but it was not
    enlisted in the config. It would not be available at runtime.
-   `UnknownConfigEntry` - provider enlisted in config was not found on
    classpath. It might suggest typo in configuration or in
    `META-INF/servies` file.
-   `NotFoundOnClasspath` - given provider was found both in config and
    in `META-INF/services` file, but it was not found on classpath. It
    might suggest that given provider was not cross-compiled for Scala
    Native.
-   `NoProviders` - status assigned for services without any available
    implementations found on classpath and without config entries

Continue to [libc](./libc.md).
