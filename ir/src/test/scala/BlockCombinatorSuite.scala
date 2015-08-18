import org.scalatest.FunSuite
import salty.ir._

class BlockCombinatorSuite extends FunSuite {
  implicit val fresh = new Fresh()

  def i(k: Int) = Instr.Assign(n(k), v(k))
  def n(k: Int) = Name.Local(k.toString)
  def v(k: Int) = Val.Number(k.toString, Type.I64)

  val I0 = i(0)
  val I1 = i(1)
  val I2 = i(2)
  val I3 = i(3)
  val I4 = i(4)
  val I5 = i(5)

  val V0 = n(0)
  val V1 = n(1)
  val V2 = n(2)
  val V3 = n(3)
  val V4 = n(4)
  val V5 = n(5)

  /*test("chain return") {
    val block = Block(Seq(I1), Termn.Return(V1))
    val res = block chain { (is, v) =>
      Block(is :+ I2, Termn.Return(V2))
    }
    val Block(Seq(I1, I2), Termn.Return(V2)) = res
  }

  test("chain jump-return") {
    val block = Block(Seq(I1), Termn.Jump(Block(Seq(I2), Termn.Return(V2))))
    val res = block chain { (instrs, v) =>
      Block(instrs :+ I3, Termn.Return(V3))
    }
    val Block(Seq(I1), Termn.Jump(
      Block(Seq(I2, I3), Termn.Return(V3)))) = res
  }

  test("chain if-return") {
    val block =
      Block(Seq(I1), Termn.If(V1,
        Block(Seq(I2), Termn.Return(V2)),
        Block(Seq(I3), Termn.Return(V3))))
    val res = block chain { (instrs, v) =>
      Block(instrs :+ I4, Termn.Return(V4))
    }
    val Block(Seq(I1), Termn.If(V1,
      b2 @ Block(Seq(I2), Termn.Jump(j1)),
      b3 @ Block(Seq(I3), Termn.Jump(j2)))) = res
    assert(j1 eq j2)
    val Block(Seq(
      Instr.Assign(_, Expr.Phi(Seq(Branch(V2, `b2`), Branch(V3, `b3`)))),
      I4),
      Termn.Return(V4)) = j1
  }

  test("initialize by-name") {
    lazy val block: Block =
      Block.byName(Seq(I1), Termn.If(V1, block, Block(Seq(I2), Termn.Return(V2))))
    val Block(_, Termn.If(_, b, _)) = block
    assert(b eq block)
  }
  */

  test("meet if") {
    val b =
      Block(Seq(I1), Termn.If(V1,
        Block(Seq(I2), Termn.Return(V2)),
        Block(Seq(I3), Termn.Return(V3))))

    println(b.show.build)
    Block.meet(b) { e =>
      val name = fresh()
      Block(
        Seq(Instr.Assign(name, e)),
        Termn.Return(name))
    }
    println()
    println(b.show.build)
  }
}
