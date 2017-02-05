package java.lang

class DummyNoStackTraceException extends scala.util.control.NoStackTrace

object ExceptionSuite extends tests.Suite {
  test("printStackTrace") {
    val sw = new java.io.StringWriter
    val pw = new java.io.PrintWriter(sw)
    (new Exception).printStackTrace(pw)
    val expected = Seq(
      "java.lang.Exception",
      "\tat java.lang.Throwable::fillInStackTrace_class.java.lang.Throwable",
      "\tat java.lang.Throwable::init_class.java.lang.String_class.java.lang.Throwable",
      "\tat java.lang.Exception::init_class.java.lang.String_class.java.lang.Throwable",
      "\tat java.lang.Exception::init",
      "\tat java.lang.ExceptionSuite$$anonfun$1::apply$mcV$sp_unit",
      "\tat tests.Suite$$anonfun$test$1::apply$mcZ$sp_bool",
      "\tat tests.Suite$$anonfun$run$1::apply_class.tests.Test_unit",
      "\tat tests.Suite$$anonfun$run$1::apply_class.java.lang.Object_class.java.lang.Object",
      "\tat scala.collection.mutable.UnrolledBuffer$Unrolled::foreach_trait.scala.Function1_unit",
      "\tat scala.collection.mutable.UnrolledBuffer::foreach_trait.scala.Function1_unit",
      "\tat tests.Suite::run_bool",
      "\tat tests.Main$$anonfun$main$1::apply_class.tests.Suite_unit",
      "\tat tests.Main$$anonfun$main$1::apply_class.java.lang.Object_class.java.lang.Object",
      "\tat scala.collection.immutable.List::foreach_trait.scala.Function1_unit",
      "\tat tests.Main$::main_class.ssnr.ObjectArray_unit",
      "\tat main"
    ).mkString("\n")
    // It's startsWith and not == as there could be additional stack
    // activations before main in the stack trace. For example on linux
    // there would be additional __libc_start_main and _start frames.

    // fails on nix https://github.com/scala-native/scala-native/issues/516
    assert(sw.toString.startsWith(expected))
  }

  test("printStackTrace <no stack trace available>") {
    val sw = new java.io.StringWriter
    val pw = new java.io.PrintWriter(sw)
    (new DummyNoStackTraceException).printStackTrace(pw)
    val expected = Seq(
      "java.lang.DummyNoStackTraceException",
      "\t<no stack trace available>"
    ).mkString("\n")
    assert(sw.toString.startsWith(expected))
  }
}
