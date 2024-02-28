# Appendix A: Finding main methods in .ll files

[Name mangling](./mangling.md) describes the precise, low level details Scala Native uses to transform names when generating code into `.ll` files.

This section shows how that information might be used to find a given
method in those files. The `main` method is used as an example.

## Scala 3 style main methods

The following code:

``` scala
package example
@main def run(): Unit = ???
```

creates a fully qualified class name `example.run`, with length 11.

``` text
C := example.run - fully qualified of the main class
N := 11          - length of fully qualified class name C
```

The entry point for this name has the form:

    _SM<N><C>$D4mainLAL16java.lang.String_uEo

yielding:

    _SM11example.run$D4mainLAL16java.lang.String_uEo

`LAL16java.lang.String_uEo` is the return type.

## Scala 2 style main methods

The following code

``` scala
package example

object Test {
    def main(args: Array[String]): Unit = ()
}
```

creates a fully qualified class name `example.Test`, with length 12.

``` text
C := example.Test - fully qualified of the main class
N := 12           - length of fully qualified class name C
```

A static main method forwarder is defined in a companion class to
implement the companion and has the form:

    _SM<N><C>D4mainLAL16java.lang.String_uEo

yielding:

    _SM12example.TestD4mainLAL16java.lang.String_uEo.

The actual main method defined in the companion object has the form:

    _SM<N+1><C>$D4mainLAL16java.lang.String_uEo

yielding:

    _SM13example.Test$D4mainLAL16java.lang.String_uEo

`LAL16java.lang.String_uEo` is the return type.
