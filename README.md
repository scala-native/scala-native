# Scala Native

[![Join the chat at https://gitter.im/scala-native/scala-native](https://badges.gitter.im/scala-native/scala-native.svg)](https://gitter.im/scala-native/scala-native?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/scala-native/scala-native.png?branch=master)](https://travis-ci.org/scala-native/scala-native)

Scala Native is a new ahead-of-time compiler and lightweight managed runtime
designed specifically for Scala. Project is currently in pre-release stage.
[Follow us on twitter to be first to know about upcoming
updates.](http://twitter.com/scala_native)

## Features

* **Low-level primitives**.

  ```scala
  type Vec = CStruct3[Double, Double, Double]

  val vec = stackalloc[Vec] // allocate c struct on stack
  !vec._1 = 10.0            // initialize fields
  !vec._2 = 20.0
  !vec._3 = 30.0
  length(vec)               // pass by reference
  ```

  Pointers, structs, you name it. Low-level primitives
  let you hand-tune your application to make it work
  exactly as you want it to. You're in control.

* **Extern objects**.

  ```scala
  @extern object stdlib {
    def malloc(size: CSize): Ptr[Byte] = extern
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

## Community

* Have a question?
  Ask it on [Stack Overflow with tag `scala-native`](http://stackoverflow.com/questions/tagged/scala-native).
* Want to chat?
  Join [our Gitter chat channel](https://gitter.im/scala-native/scala-native).
* Found a bug or want to propose a new feature?
  Open [an issue on Github](https://github.com/scala-native/scala-native/issues).

## Documentation

[scala-native.readthedocs.io](http://scala-native.readthedocs.io/en/latest/)

## License

Scala Native is distributed under [the Scala license](
https://github.com/scala-native/scala-native/blob/master/LICENSE).
