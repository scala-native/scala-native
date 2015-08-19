import org.scalatest.FunSuite
import salty.ir._, Termn._, Instr._

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
  val I6 = i(6)

  val V0 = n(0)
  val V1 = n(1)
  val V2 = n(2)
  val V3 = n(3)
  val V4 = n(4)
  val V5 = n(5)
  val V6 = n(6)

  test("meet if") {
    val block =
      Block(Seq(I1), If(V1,
        Block(Seq(I2), Return(V2)),
        Block(Seq(I3), Return(V3))))

    block.join { e =>
      val name = fresh()
      Block(
        Seq(Assign(name, e)),
        Return(name))
    }
  }

  test("meet switch") {
    val block =
      Block(Seq(I1), Switch(V1,
        Block(Seq(I2), Return(V2)),
        Seq(Branch(V3, Block(Seq(I3), Return(V3))),
            Branch(V4, Block(Seq(I4), Return(V4))))))

    block.join { e =>
      val name = fresh()
      Block(
        Seq(Assign(name, e)),
        Return(name))
    }
  }
}
