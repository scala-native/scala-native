//  Taken from https://github.com/kostya/benchmarks/blob/master/brainfuck2/bf.scala
//
//  Copyright (c) 2014 'Konstantin Makarchev'
//
//    MIT License
//
//    Permission is hereby granted, free of charge, to any person obtaining
//    a copy of this software and associated documentation files (the
//    "Software"), to deal in the Software without restriction, including
//    without limitation the rights to use, copy, modify, merge, publish,
//    distribute, sublicense, and/or sell copies of the Software, and to
//    permit persons to whom the Software is furnished to do so, subject to
//    the following conditions:
//
//    The above copyright notice and this permission notice shall be
//    included in all copies or substantial portions of the Software.
//
//    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
//    EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
//    MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
//    NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
//    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
//    OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
//    WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//

package brainfuck

import benchmarks.{BenchmarkRunningTime, LongRunningTime}

class BrainfuckBenchmark extends benchmarks.Benchmark[String] {
  override val runningTime: BenchmarkRunningTime = LongRunningTime

  override def run(): String = {
    new Program(Program.asText).run
  }

  override def check(result: String) =
    Program.expectedOutput == result.size
}

abstract class Op
case class Inc(v: Int)           extends Op
case class Move(v: Int)          extends Op
case class Print()               extends Op
case class Loop(loop: Array[Op]) extends Op
case class Nop()                 extends Op

class Tape() {
  private var tape = Array(0)
  private var pos  = 0

  def get         = tape(pos)
  def inc(x: Int) = tape(pos) += x
  def move(x: Int) = {
    pos += x
    while (pos >= tape.length) { tape :+= 0 }
  }
}

class Program(text: String) {
  var ops = parse(text.iterator)

  def parse(iterator: Iterator[Char]): Array[Op] = {
    var res = Array[Op]()
    while (iterator.hasNext) {
      val op = iterator.next() match {
        case '+' => new Inc(1)
        case '-' => new Inc(-1)
        case '>' => new Move(1)
        case '<' => new Move(-1)
        case '.' => new Print()
        case '[' => new Loop(parse(iterator))
        case ']' => return res
        case _   => new Nop()
      }

      op match {
        case Nop() => ()
        case _     => res :+= op
      }
    }

    res
  }

  def run = {
    val stringBuilder = new StringBuilder

    _run(ops, new Tape(), stringBuilder)

    stringBuilder.toString
  }

  def _run(program: Array[Op], tape: Tape, output: StringBuilder) {
    for (op <- program) op match {
      case Inc(x)     => tape.inc(x)
      case Move(x)    => tape.move(x)
      case Loop(loop) => while (tape.get > 0) _run(loop, tape, output)
      case Print()    => output.append(tape.get.toChar)
    }
  }

}

object Program {
  // From https://copy.sh/brainfuck/
  //   99 bottles in 1752 brainfuck instructions
  //   by jim crawford (http://www (dot) goombas (dot) org/)
  val asText: String =
    """+++++++++>+++++++++>>>++++++[<+++>-]+++++++++>+++++++++>>>>>>+++++++
      |++++++[<++++++>-]>+++++++++++[<++++++++++>-]<+>>++++++++[<++++>-]>++
      |+++++++++[<++++++++++>-]<->>+++++++++++[<++++++++++>-]<+>>>+++++[<++
      |++>-]<-[<++++++>-]>++++++++++[<++++++++++>-]<+>>>+++++++[<+++++++>-]
      |>>>++++++++[<++++>-]>++++++++[<++++>-]>+++++++++++[<+++++++++>-]<->>
      |++++++++[<++++>-]>+++++++++++[<++++++++++>-]<+>>++++++++[<++++>-]>>+
      |++++++[<++++>-]<+[<++++>-]>++++++++[<++++>-]>>+++++++[<++++>-]<+[<++
      |++>-]>>++++++++++++[<+++++++++>-]++++++++++>>++++++++++[<++++++++++>
      |-]<+>>++++++++++++[<+++++++++>-]>>++++++++++++[<+++++++++>-]>>++++++
      |[<++++>-]<-[<+++++>-]>++++++++++++[<++++++++>-]<+>>>>++++[<++++>-]<+
      |[<+++++++>-]>++++++++[<++++>-]>++++++++[<++++>-]>+++++++++++[<++++++
      |++++>-]<+>>++++++++++[<++++++++++>-]<+>>>++++[<++++>-]<+[<++++++>-]>
      |+++++++++++++[<++++++++>-]>++++++++[<++++>-]>>+++++++[<++++>-]<+[<++
      |++>-]>+++++++++++[<+++++++++>-]<->>++++++++[<++++>-]>++++++++++[<+++
      |+++++++>-]<+>>+++++++++++[<++++++++++>-]>++++++++++[<++++++++++>-]<+
      |>>+++++++++++[<++++++++++>-]<+>>>+++++[<++++>-]<-[<++++++>-]>+++++++
      |+[<++++>-]>++++++++++>>>>++++++++++++[<+++++++>-]++++++++++>>+++++++
      |+++++[<++++++++>-]<+>>++++++++++[<++++++++++>-]>++++++++++++[<++++++
      |+++>-]<->>+++++++++++[<++++++++++>-]>++++++++++[<++++++++++>-]<+>>++
      |+++++++++++[<+++++++++>-]>++++++++[<++++>-]>+++++++++++[<++++++++++>
      |-]<+>>>>+++++[<++++>-]<-[<++++++>-]>+++++++++++[<++++++++++>-]<+>>++
      |++++++++++[<++++++++>-]<+>>+++++++++++[<++++++++++>-]>++++++++[<++++
      |>-]>++++++++++[<++++++++++>-]<+>>>+++++++[<++++>-]<+[<++++>-]>>>++++
      |+[<+++>-]<[<+++++++>-]>>+++++[<+++>-]<[<+++++++>-]>++++++++[<++++>-]
      |>>+++++++[<++++>-]<+[<++++>-]>>++++++[<++++>-]<-[<+++++>-]>>>++++++[
      |<++++>-]<-[<+++++>-]>++++++++[<++++>-]>++++++++++++[<++++++++>-]<+>>
      |++++++++++[<++++++++++>-]>>++++[<++++>-]<[<+++++++>-]>+++++++++++[<+
      |+++++++++>-]<+>>++++++++[<++++>-]>>++++[<++++>-]<+[<+++++++>-]>+++++
      |+++++[<++++++++++>-]>+++++++++++[<++++++++++>-]>+++++++++++[<+++++++
      |+++>-]>++++++++[<++++>-]>++++++++++++[<++++++++>-]<+[<]<[<]<[<]<[<]<
      |<<<[<]<[<]<[<]<[<]<<<<[<]<<<<<<[>>[<<[>>>+>+<<<<-]>>>[<<<+>>>-]<[>+<
      |-]>>-[>-<[-]]>+[<+>-]<<<]>>[<<<<[-]>>>>>>>[>]>.>>>[.>>]>[>]>>[.>>]<[
      |.<<]<[<]<<.>>>[.>>]>[>]>>[.>>]>.>>>[.>>]>[>]>>[.>>]>>[.>>]<[.<<]<<<<
      |[<]<[<]<[<]<[<]<<<<[<]>[.>]>>>>[.>>]>>[.>>]>>[.>>]<[.<<]<[<]<<<<[<]<
      |<-]<<<<[[-]>>>[<+>-]<<[>>+>+<<<-]>>>[<<<+>>>-]<[<<<++++++++[>++++++<
      |-]>>>[-]]++++++++[<++++++>-]<<[.>.>]>[.>>]>>>[>]>>>>[.>>]>>[.>>]>>[.
      |>>]<[.<<]<[<]<<<<[<]<<<<<[.>.>]>[.>>]<<<[-]>[-]>>>>>[>]>>>>[.>>]>>[.
      |>>]>>[.>>]>.>>>[.>>]>>[.>>]>[>]>>[.>>]<[.<<]<<<<[<]<[<]<[<]<[<]<<<<[
      |<]<<<<<<]<<[>+>+<<-]>>[<<+>>-]<[>-<[-]]>+[<+>-]<[<++++++++++<->>-]<<
      |[>>+>+>+<<<<-]>>[<<+>>-]<-[>+>+>>+<<<<-]>[<+>-]>>>[<<[>>>+>+<<<<-]>>
      |>[<<<+>>>-]<[>+<-]>>-[>-<[-]]>+[<+>-]<<<]>>[<<<<[-]>>>>>>>[>]>.>>>[.
      |>>]>[>]>>[.>>]<[.<<]<[<]<<<<[<]<<-]<<<<[[-]>>>[<+>-]<<[>>+>+<<<-]>>>
      |[<<<+>>>-]<[<<<++++++++[>++++++<-]>>>[-]]++++++++[<++++++>-]<<[.>.>]
      |>[.>>]<<<[-]>[-]>>>>>[>]>>>>[.>>]>>[.>>]>>[.>>]<[.<<]<[<]<<<<[<]<<<<
      |<<]++++++++++.[-]<<<[>>+>+>+<<<<-]>>[<<+>>-]<[>+>+>>+<<<<-]>[<+>-]>]
      |""".stripMargin

  val expectedOutput = 11359
}
