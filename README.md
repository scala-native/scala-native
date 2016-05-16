# Scala Native

[![Join the chat at https://gitter.im/scala-native/scala-native](https://badges.gitter.im/scala-native/scala-native.svg)](https://gitter.im/scala-native/scala-native?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/scala-native/scala-native.png?branch=master)](https://travis-ci.org/scala-native/scala-native)

Scala Native is a new ahead-of-time compiler and lightweight managed runtime
designed specifically for Scala. Project is currently in pre-release stage.
[Follow us on twitter to be first to know about upcoming
updates.](http://twitter.com/scala_native)

## How to build?

This requires SBT to be installed on your machine. Refer to [this link](http://www.scala-sbt.org/release/docs/Setup.html) for instructions.

```
$ git clone https://github.com/scala-native/scala-native.git --recursive
$ cd scala-native/
$ sbt rtlib/publishLocal nscplugin/publishLocal
$ sbt package
```

If you face any issues with your development environment setup, refer to [developer guide](docs/develop.md)

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

* [Contributing guidelines.](/docs/contributing.md)

## License

Scala Native is distributed under [the Scala license](
https://github.com/scala-native/scala-native/blob/master/LICENSE).
