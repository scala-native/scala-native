package scala.scalanative.reporter

class ExternSyntaxReporterTest extends NirErrorReporterSpec {
  verifyThat("fields in extern objects must have extern body") {
    reportsErrors {
      s"""|@scala.scalanative.unsafe.extern
		  |object test {
		  |  val t = 1
		  |}""".stripMargin
    }
  }

  verifyThat("methods in extern objects must have extern body") {
    reportsErrors {
      s"""|@scala.scalanative.unsafe.extern
		  |object test {
		  |  def t = 1
		  |}""".stripMargin
    }
  }

  verifyThat("extern objects may only have extern parents",
             "when extending trait") {
    reportsErrors {
      s"""|@scala.scalanative.unsafe.extern
		  |object bar extends foo {
		  |  var z: Int = scala.scalanative.unsafe.extern
		  |}
		  |trait foo {
		  |}  """.stripMargin
    }
  }

  verifyThat("extern classes may only have extern parents") {
    reportsErrors {
      s"""|@scala.scalanative.unsafe.extern
		  |class bar extends foo {
		  |  var z: Int = scala.scalanative.unsafe.extern
		  |}
		  |trait foo {
		  |}  """.stripMargin
    }
  }

  it should "allows to compile correct extern definition" in {
    allows {
      s"""|@scala.scalanative.unsafe.extern
		  |object one extends two {
		  |  var z: Int = scala.scalanative.unsafe.extern
		  |}
		  |@scala.scalanative.unsafe.extern
		  |trait two {
		  |}  """.stripMargin
    }
  }

  verifyThat("fields in extern traits must have extern body") {
    reportsErrors {
      s"""|@scala.scalanative.unsafe.extern
		  |object bar extends foo{
		  |  var z: Int = scala.scalanative.unsafe.extern
		  |}
		  |@scala.scalanative.unsafe.extern
		  |trait foo {
		  |  val y: Int = 1
		  |}  """.stripMargin
    }
  }

  it should "allow to compile object extending extern trait" in {
    allows {
      s"""|@scala.scalanative.unsafe.extern
		  |object bar extends foo {
		  |  var z: Int = scala.scalanative.unsafe.extern
		  |}
		  |@scala.scalanative.unsafe.extern
		  |trait foo {
		  |  val y: Int = scala.scalanative.unsafe.extern
		  |}  """.stripMargin
    }
  }

  it should "verify that field in extern object must not be lazy" in {
    reportsErrors {
      s"""|@scala.scalanative.unsafe.extern
		  |object test {
		  |  lazy val t: Int = scala.scalanative.unsafe.extern
		  |}""".stripMargin
    }("(limitation) fields in extern objects must not be lazy")
  }

  verifyThat("extern objects may only have extern parents",
             "when extending class") {
    reportsErrors {
      s"""|class Foo(val x: Int)
		  |@scala.scalanative.unsafe.extern
		  |object Bar extends Foo(10)""".stripMargin
    }
  }

  it should "verify that not allow parameter in extern classes" in {
    // Previously, this would compile and execute but wouldn't
    // return the incorrect result (0) for `Bar.x`
    reportsErrors {
      s"""
		 |@scala.scalanative.unsafe.extern
		 |class Foo(val x: Int)
		 |@scala.scalanative.unsafe.extern
		 |object Bar extends Foo(10)""".stripMargin
    }("parameters in extern classes are not allowed - only extern fields and methods are allowed")
  }

  verifyThat("extern members must have an explicit type annotation") {
    reportsErrors {
      s"""|@scala.scalanative.unsafe.extern
		  |object test {
		  |  val t = scala.scalanative.unsafe.extern
		  |}""".stripMargin
    }
  }
}
