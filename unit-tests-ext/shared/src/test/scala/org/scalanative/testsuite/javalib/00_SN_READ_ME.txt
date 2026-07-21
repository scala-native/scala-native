unit-tests-ext/shared/src/test/.../testsuite/javalib/00_SN_READ_ME.txt

By design and intent, Scala Native javalib does not support some of
the Java API (Application Programming Interface) packages, classes, and
methods. Instead application developers are encouraged to use dedicated
third-party implementations. For example, javalib does not implement
'java.time' where the superior 'scala-java-time' and 'sjavatime'
are both available.

Some generally supported javalib classes, such as
'java.util.concurrent.TimeUnit' have methods, such as
'TimeUnit.convert(Duration)', which use some features which are themselves not
supported.  These files will compile given a suitable
JDK (Java Development Kit) version, but will fail to link with missing symbols
if a third-party dependency is not supplied.

The Scala Native 'unit-tests-ext' project exists to internally test supported
classes which themselves use unsupported features.

The 'javalib-ext-dummies' (sic) project supplies minimal implementations of
otherwise unsupported Java methods used by this project.  This allows
this project to avoid any dependency on a third-party library when
testing methods supported for general use by javalib.
