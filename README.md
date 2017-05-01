# Scala Native Windows Clang Port

Hello guys,
I don't know if it's worth to say but I just was able to compile sandbox.exe with clang 5.0 from sbt command-line from master branch (unfortunately without unwind).
It's 4.7Mb executable file though :)
I used Visual Studio Code as IDE for Scala (built-in terminal for sbt),
clang/llvm/re2/boehm from git repositories compiled with cmake and Visual Studio 2017 Community Edition.
I had to implement a few methods in native library because there were missing and I modified a sbt-plugin to support different slash-direction for filesystem, but I believe it could be implemented inside a scala filesystem code.
I'm trying to learn how to submit pull request correctly just in case if anyone interested.
I made this fork to learn how to contribute to the scala native.

[![Join the chat at https://gitter.im/scala-native/scala-native](https://badges.gitter.im/scala-native/scala-native.svg)](https://gitter.im/scala-native/scala-native?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/scala-native/scala-native.png?branch=master)](https://travis-ci.org/scala-native/scala-native)

[http://www.scala-native.org/](http://www.scala-native.org/)

## License

Scala Native is distributed under the Scala license.
[See LICENSE.md for details](https://github.com/scala-native/scala-native/blob/master/LICENSE.md)
