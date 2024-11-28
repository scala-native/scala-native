object Test {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
    println(SomeClass.get)
  }
}

import java.util.ServiceLoader
class SomeClass
object SomeClass {
  def get =
    try {
      if(this.## == 0) throw new RuntimeException("foo")
      ServiceLoader.load(classOf[SomeClass])
    }
    catch {
      case ex: java.util.ServiceConfigurationError => throw ex
      case ex: Exception                           => ()
      // case ex: Throwable => throw ex
    } finally { println("done") }
}

// uses-intrinsics def @"M10SomeClass$D3getL23java.util.ServiceLoaderEO" : (@"T10SomeClass$") => @"T23java.util.ServiceLoader" {
// %2(%1 <this> : @"T10SomeClass$"):
//   jump %4
// %4:
//   1: %51 = module @"T24java.util.ServiceLoader$" unwind %8 : @"T19java.lang.Throwable" to %3(%8 : @"T19java.lang.Throwable")
//   1: %52 = arrayalloc[@"T32java.util.ServiceLoader$Provider"] arrayvalue @"T32java.util.ServiceLoader$Provider" {} unwind %8 : @"T19java.lang.Throwable" to %3(%8 : @"T19java.lang.Throwable")
//   1: %9 = classalloc @"T23java.util.ServiceLoader" unwind %8 : @"T19java.lang.Throwable" to %3(%8 : @"T19java.lang.Throwable")
//   1: %53 = call[(@"T23java.util.ServiceLoader", @"T15java.lang.Class", array[@"T9SomeClass"]) => unit] @"M23java.util.ServiceLoaderRL15java.lang.ClassLAL32java.util.ServiceLoader$Provider_E" : ptr(%9 : @"T23java.util.ServiceLoader", classOf[@"T9SomeClass"], %52 : ?array[@"T32java.util.ServiceLoader$Provider"]) unwind %8 : @"T19java.lang.Throwable" to %3(%8 : @"T19java.lang.Throwable")
//   jump %31 // (0)
// %3(%6 : @"T19java.lang.Throwable"): // exception handle
//   2: %10 = is[@"T35java.util.ServiceConfigurationError"] %6 : @"T19java.lang.Throwable"
//   if %10 : bool then %11 else %12
// %11:
//   3: %15 <ex> = as[@"T35java.util.ServiceConfigurationError"] %6 : @"T19java.lang.Throwable"
//   jump %31 // (1)
// %12:
//   2: %18 = is[@"T19java.lang.Exception"] %6 : @"T19java.lang.Throwable"
//   if %18 : bool then %19 else %20
// %19:
//   5: %23 <ex> = as[@"T19java.lang.Exception"] %6 : @"T19java.lang.Throwable"
//   jump %31 // (2)
// %20:
//   jump %31 // (-1)
// %31(key: Int): // finally block
//   10: %32 = module @"T13scala.Predef$"
//   10: %33 = method %32 : !?@"T13scala.Predef$", "D7printlnL16java.lang.ObjectuEO"
//   10: %34 = call[(@"T13scala.Predef$", @"T16java.lang.Object") => unit] %33 : ptr(%32 : !?@"T13scala.Predef$", "done")
//   switch %key, label %onUcaught [ i32 0 %continue
//                                 i32 1 %onServiceConfigurationError
//                                 i32 2 %onException
//                               ]
// %onServiceConfigurationError:
//   throw %15 <ex> : @"T35java.util.ServiceConfigurationError"
// %onException:
//   throw %23 <ex> : @"T19java.lang.Exception"
// %onUcaught:
//   throw %6 : @"T19java.lang.Throwable"
// %continue:
//   jump %5(%9 : @"T23java.util.ServiceLoader")
// %5(%7 : @"T23java.util.ServiceLoader"):
//   ret %7 : @"T23java.util.ServiceLoader"
// }
