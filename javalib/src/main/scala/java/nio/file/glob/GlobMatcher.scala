package java.nio.file.glob

import scala.annotation.tailrec
import scala.scalanative.meta.LinktimeInfo.isWindows

class GlobMatcher(glob: GlobNode, inputPath: String) {

  def matches(): Boolean = {
    val input =
      if (isWindows) inputPath.replace("\\", "/")
      else inputPath

    // Finds states reachable after using one input character
    def reachableStates(
        inputChar: Char,
        node: GlobNode,
        previous: GlobNode
    ): List[GlobNode] =
      node match {
        case EndNode      => Nil
        case StartNode(_) => Nil
        case TransitionNode(globSpec, next, _, _) =>
          globSpec match {
            case Separator if inputChar == '/' => List(node)
            case Asterisk if inputChar != '/' =>
              List(node, previous) ++
                next.flatMap(reachableStates(inputChar, _, node))
            case Asterisk if inputChar == '/' =>
              next.flatMap(reachableStates(inputChar, _, node))
            case DoubleAsterisk =>
              List(node, previous) ++
                next.flatMap(reachableStates(inputChar, _, node))
            case AnyChar if inputChar != '/'                      => List(node)
            case GlobChar(char) if char == inputChar              => List(node)
            case Bracket(set) if set.contains(inputChar)          => List(node)
            case NegationBracket(set) if !set.contains(inputChar) => List(node)
            case Groups(_) =>
              next.flatMap(reachableStates(inputChar, _, node))
            case _ => Nil
          }
      }

    def isEndReachable(node: GlobNode): Boolean =
      node match {
        case StartNode(nextNode) => isEndReachable(nextNode)
        case EndNode             => true
        case TransitionNode(globSpec, next, _, _) =>
          globSpec match {
            case Asterisk | DoubleAsterisk | Groups(_) =>
              next.exists(isEndReachable(_))
            case _ => false
          }
      }

    // Matches against one string
    @tailrec
    def matchesInternal(
        inputIdx: Int,
        states: List[GlobNode],
        charsLeft: Int,
        sepsLeft: Int
    ): Boolean = {
      if (inputIdx < input.length()) {
        val inputChar = input.charAt(inputIdx)
        val newStates = states.flatMap {
          case EndNode => Nil
          case node @ (StartNode(nextNode)) =>
            reachableStates(inputChar, nextNode, node)
          case node @ (TransitionNode(_, next, _, _)) =>
            val nextList = next.flatMap { nextNode =>
              reachableStates(inputChar, nextNode, node)
            }
            nextList
        }

        val newCharsLeft = charsLeft - 1
        val newSepsLeft = sepsLeft - (if (inputChar == '/') 1 else 0)

        if (inputIdx == input.length() - 1) {
          newStates.collectFirst {
            case TransitionNode(_, next, _, _)
                if next.exists(isEndReachable(_)) =>
              true
          }.isDefined
        } else {
          matchesInternal(inputIdx + 1, newStates, newCharsLeft, newSepsLeft)
        }
      } else {
        states.exists(isEndReachable(_))
      }
    }

    val charsLeft = input.length()
    val sepsLeft = input.count(_ == '/')

    matchesInternal(0, List(glob), charsLeft, sepsLeft)
  }
}
