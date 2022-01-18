package java.nio.file.glob

import java.nio.file.{PathMatcher, Path}
import scala.util.matching.Regex
import scala.annotation.tailrec

class GlobMatcher(pattern: GlobPattern) extends PathMatcher {

  def matches(path: java.nio.file.Path): Boolean = {
    val input = path.toString() // TODO change dividers (?)
    val glob = pattern.globSpecification

    // Finds states reachable after using one inputChar
    def reachableStates(
        inputChar: Char,
        node: GlobNode,
        previous: GlobNode
    ): List[GlobNode] =
      if (!node.isFinal) {
        node.globSpec match {
          case Divider if inputChar == '/' => List(node)
          case NonCrossingName if inputChar != '/' =>
            List(node, previous) ++ node.next.flatMap(
              reachableStates(inputChar, _, node)
            ) // skip
          case NonCrossingName if inputChar == '/' =>
            node.next.flatMap(reachableStates(inputChar, _, node))
          case CrossingName =>
            List(node, previous) ++ node.next.flatMap(
              reachableStates(inputChar, _, node)
            ) // skip
          case AnyChar if inputChar != '/'                      => List(node)
          case GlobChar(char) if char == inputChar              => List(node)
          case Bracket(set) if set.contains(inputChar)          => List(node)
          case NegationBracket(set) if !set.contains(inputChar) => List(node)
          case Groups(_) =>
            node.next.flatMap(reachableStates(inputChar, _, node))
          case _ => Nil
        }
      } else {
        Nil
      }

    // @tailrec
    def isEndReachable(node: GlobNode): Boolean =
      if (!node.isFinal) {
        node.globSpec match {
          case NonCrossingName => node.next.exists(isEndReachable(_))
          case CrossingName    => node.next.exists(isEndReachable(_))
          case Groups(_)       => node.next.exists(isEndReachable(_))
          case _               => false
        }
      } else true

    // Matches against one string
    @tailrec
    def matchesInternal(inputIdx: Int, states: List[GlobNode]): Boolean = { /// return if not found
      val inputChar = input.charAt(inputIdx)
      val newStates = states.flatMap { node =>
        val nextList = node.next.flatMap { nextNode =>
          reachableStates(inputChar, nextNode, node)
        }
        nextList
      }

      if (inputIdx == input.length() - 1)
        newStates.exists(_.next.exists(isEndReachable(_)))
      else matchesInternal(inputIdx + 1, newStates)
    }

    matchesInternal(0, List(glob))

  }
}
