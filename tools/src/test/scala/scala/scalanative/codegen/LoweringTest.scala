package scala.scalanative.codegen

import java.nio.file.Files

import org.junit.Assert._
import org.junit.Test

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
  @Test def blockerCallsPassModuleReceiver(): Unit = lowered(
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
}
