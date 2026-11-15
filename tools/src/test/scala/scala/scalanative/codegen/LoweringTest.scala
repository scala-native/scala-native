package scala.scalanative.codegen

import java.nio.file.Files

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.buildinfo.ScalaNativeBuildInfo
import scala.scalanative.{build, nir}

class LoweringTest extends CodeGenSpec {

  @Test def issue4010(): Unit = codegen(
    entry = "Main",
    sources = Map(
      "Main.scala" ->
        """|
           |object Main {
           |  def main(args: Array[String]): Unit = {
           |    val foo = 42
           |    println(Option.empty[Any].getOrElse(throw new RuntimeException(s"foo=$foo")))
           |  }
           |}""".stripMargin
    ),
    setupConfig = _.withOptimize(false)
  ) {
    case (config, result, _) =>
      assertFalse(config.compilerConfig.optimize)
      assertTrue(result.isSuccessful)
  }

  // With opaque pointers, `bitcast ptr %x to ptr` is a no-op and should not be
  // emitted by the code generator. Lower rewrites such redundant bitcasts as
  // copies which are elided at codegen time.
  @Test def noRedundantPtrBitcast(): Unit = codegen(
    entry = "Main",
    sources = Map(
      "Main.scala" ->
        """|
           |object Main {
           |  def main(args: Array[String]): Unit = {
           |    val s = Set(1, 2, 3) ++ Set(3, 4, 5)
           |    println(s.size)
           |  }
           |}""".stripMargin
    )
  ) {
    case (_, _, outfiles) =>
      val pattern = """bitcast ptr %\S+ to ptr\b""".r
      val offending =
        outfiles.iterator.flatMap { path =>
          val content = new String(Files.readAllBytes(path))
          pattern
            .findAllMatchIn(content)
            .map(m => s"${path.getFileName}: ${m.matched}")
        }.toList
      assertTrue(
        s"Found redundant `bitcast ptr %x to ptr` in generated IR:\n${offending.mkString("\n")}",
        offending.isEmpty
      )
  }

  // Blocker calls must pass the module receiver before `attempted`.
  @Test def blockerCallsPassModuleReceiver(): Unit = {
    // Virtual threads exist only in the Scala 3 javalib.
    assumeTrue(ScalaNativeBuildInfo.scalaVersion.startsWith("3"))
    blockerCallsPassModuleReceiverImpl()
  }

  private def blockerCallsPassModuleReceiverImpl(): Unit = lowered(
    entry = "Main",
    sources = Map(
      "Main.scala" ->
        """|import scala.scalanative.unsafe._
           |
           |@extern object Native {
           |  @blocking def usleep(usec: CInt): CInt = extern
           |}
           |
           |object Main {
           |  def main(args: Array[String]): Unit = {
           |    val vt = Thread.ofVirtual().start(() => Native.usleep(1))
           |    vt.join()
           |  }
           |}""".stripMargin
    ),
    // Virtual threads need a target with continuations.
    setupConfig =
      _.withMultithreading(true).withTargetTriple("x86_64-unknown-linux-gnu")
  ) { defns =>
    import scala.scalanative.nir
    def definedType(name: nir.Global) = defns.collectFirst {
      case defn: nir.Defn.Define if defn.name == name => defn.ty
    }
    assertEquals(
      Some(Lower.RuntimeBlockerBeginTy),
      definedType(Lower.RuntimeBlockerBeginName)
    )
    assertEquals(
      Some(Lower.RuntimeBlockerEndTy),
      definedType(Lower.RuntimeBlockerEndName)
    )

    val insts = defns.collect { case defn: nir.Defn.Define => defn.insts }
    def callsTo(name: nir.Global) = insts.flatten.collect {
      case nir.Inst.Let(
            id,
            nir.Op.Call(_, nir.Val.Global(`name`, _), args),
            _
          ) =>
        (id, args)
    }
    val begins = callsTo(Lower.RuntimeBlockerBeginName)
    val ends = callsTo(Lower.RuntimeBlockerEndName)
    assertTrue("no Blocker.begin call generated", begins.nonEmpty)
    assertEquals(begins.size, ends.size)
    begins.foreach { case (_, args) => assertEquals(Seq(nir.Val.Null), args) }
    val beginIds = begins.map(_._1).toSet
    ends.foreach {
      case (_, Seq(nir.Val.Null, nir.Val.Local(attempted, nir.Type.Bool))) =>
        assertTrue(beginIds.contains(attempted))
      case (_, args) => fail(s"unexpected Blocker.end arguments: $args")
    }
  }

  // Issue #122
  @Test def safepointPollDoesNotBreakTailCalls(): Unit =
    for (gc <- Seq(build.GC.immix, build.GC.commix))
      lowered(
        entry = "Main",
        sources = Map(
          "Main.scala" ->
            """|object Main {
               |  @noinline def isEven(n: Int): Boolean = if (n == 0) true else isOdd(n - 1)
               |  @noinline def isOdd(n: Int): Boolean = if (n == 0) false else isEven(n - 1)
               |  def main(args: Array[String]): Unit = println(isEven(args.length))
               |}""".stripMargin
        ),
        setupConfig = _.withGC(gc).withMultithreading(true)
      ) { defns =>
        def isCallTo(inst: nir.Inst)(p: nir.Global.Member => Boolean) =
          inst match {
            case nir.Inst.Let(
                  _,
                  nir.Op.Call(_, nir.Val.Global(name: nir.Global.Member, _), _),
                  _
                ) =>
              p(name)
            case _ => false
          }
        def isPoll(inst: nir.Inst) = isCallTo(inst)(_ == Lower.GCYieldName)
        def isMethod(id: String)(name: nir.Global.Member) =
          name.owner == nir.Global.Top("Main$") && (name.sig.unmangled match {
            case nir.Sig.Method(`id`, _, _) => true
            case _                          => false
          })

        for ((caller, callee) <- Seq(
              "isEven" -> "isOdd",
              "isOdd" -> "isEven"
            )) {
          val insts = defns.collectFirst {
            case defn: nir.Defn.Define if isMethod(caller)(defn.name) =>
              defn.insts
          }.get
          val call = insts.indexWhere(isCallTo(_)(isMethod(callee)))
          assertTrue(s"$caller does not call $callee", call >= 0)
          val polls = insts.zipWithIndex.collect {
            case (inst, idx) if isPoll(inst) => idx
          }
          assertTrue(s"$caller does not poll for GC", polls.nonEmpty)
          assertTrue(
            s"$caller polls for GC after the call to $callee",
            polls.forall(_ < call)
          )
        }
      }

  // Issue #122
  @Test def mutualCallsStayInTailPosition(): Unit = codegen(
    entry = "Main",
    sources = Map(
      "Main.scala" ->
        """|object Main {
           |  @noinline def isEven(n: Int): Boolean = if (n == 0) true else isOdd(n - 1)
           |  @noinline def isOdd(n: Int): Boolean = if (n == 0) false else isEven(n - 1)
           |  def main(args: Array[String]): Unit = println(isEven(args.length))
           |}""".stripMargin
    ),
    setupConfig = _.withMultithreading(true)
  ) {
    case (config, _, outfiles) =>
      def defines(line: String, method: String) =
        line.startsWith("define") && line.contains(
          s"Main$$D${method.length}$method"
        )
      val source = outfiles
        .find { path =>
          new String(Files.readAllBytes(path)).linesIterator
            .exists(defines(_, "isEven"))
        }
        .getOrElse(throw new AssertionError("Missing definition of isEven"))
      val output = Files.createTempFile("tailcalls", ".ll")
      try {
        val command = Seq(
          config.clang.toAbsolutePath.toString,
          "-O2",
          "-S",
          "-emit-llvm",
          "-Wno-override-module",
          source.toAbsolutePath.toString,
          "-o",
          output.toAbsolutePath.toString
        )
        assertEquals(0, new ProcessBuilder(command: _*).start().waitFor())
        val optimized =
          new String(Files.readAllBytes(output)).linesIterator.toIndexedSeq
        for ((caller, callee) <- Seq(
              "isEven" -> "isOdd",
              "isOdd" -> "isEven"
            )) {
          val body = optimized
            .dropWhile(!defines(_, caller))
            .takeWhile(_ != "}")
            .map(_.trim)
            .filter(_.nonEmpty)
          // Instructions executed from the given position until the function returns
          def untilReturn(from: Int): Seq[String] = body(from) match {
            case inst if inst.startsWith("ret ")       => Nil
            case inst if inst.startsWith("br label %") =>
              val label = inst.stripPrefix("br label %").split(",").head + ":"
              untilReturn(body.indexWhere(_.startsWith(label)) + 1)
            case inst => inst +: untilReturn(from + 1)
          }
          val call = body.indexWhere { inst =>
            inst.contains("call ") && inst.contains(
              s"Main$$D${callee.length}$callee"
            )
          }
          assertTrue(s"$caller does not call $callee", call >= 0)
          assertTrue(
            s"$caller does not tail call $callee: ${body(call)}",
            body(call).contains("tail call ")
          )
          val trailing = untilReturn(call + 1).filterNot(_.contains(" = phi "))
          assertTrue(
            s"$caller executes after calling $callee: ${trailing.mkString("; ")}",
            trailing.isEmpty
          )
        }
      } finally Files.deleteIfExists(output)
  }
}
