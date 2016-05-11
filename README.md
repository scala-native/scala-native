# Scala Native

Scala Native is a new ahead-of-time compiler and lightweight managed runtime
designed specifically for Scala. Project is current in pre-release stage.
[Follow us on twitter to be first to know about upcoming
updates.](http://twitter.com/scala_native)

## Features

* **Low-level primitives**.

  ```scala
  @struct class Vec(
    val x: Double,
    val y: Double,
    val z: Double
  )

  val vec = stackalloc[Vec] // pointer to stack allocation
  !vec = new Vec(1, 2, 3)   // store value to stack
  length(vec)               // pass by reference
  ```

  Pointers, structs, you name it. Low-level primitives
  let you hand-tune your application to make it work
  exactly as you want it to. You're in control.

* **Extern objects**.

  Calling C code has never been easier.
  With the help of extern objects you can
  seamlessly call native code without any
  runtime overhead.

  ```scala
  @extern object stdlib {
    def malloc(size: CSize): Ptr[_] = extern
  }

  val ptr = stdlib.malloc(32)
  ```

  Calling C code has never been easier.
  With the help of extern objects you can
  seamlessly call native code without any
  runtime overhead.

* **Instant startup**.

  ```
  > time hello-native
  hello, native!

  real    0m0.005s
  user    0m0.002s
  sys     0m0.002s
  ```

  Scala Native is compiled ahead-of-time via LLVM.
  This means that there is no sluggish warm-up
  phase that's common for just-in-time compilers.
  Your code is immediately fast and ready for action.

## Documentation

Documentation is available in [docs subfolder](/docs/00_toc.md).

## How to contribute

1. Check the list of [open issues](https://github.com/scala-native/scala-native/issues) and see
   if you are interested in fixing any of them. If you have encountered a problem or have
   a feature suggestion feel free to open a new issue.
1. Fork the [main repo](https://github.com/scala-native/scala-native) and start hacking up
   the fix. If you have problems with getting started, contact
   [@densh](https://github.com/densh) to help you out.
1. Whenever you fix an issue, add a test that shows that it was indeed fixed. If you
   introduce a new feature, add a new test suite with a bunch of tests that cover common
   use cases. If you propose a performance enhancement, include before & after results of
   a corresponding jmh performance benchmark run in the commit message.
1. Fire up a pull request. Don't forget to sign the
   [Scala CLA](http://typesafe.com/contribute/cla/scala).
