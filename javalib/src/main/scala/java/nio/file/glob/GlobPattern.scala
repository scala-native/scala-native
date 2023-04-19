package java.nio.file.glob

import java.util.regex.PatternSyntaxException

import scala.collection.mutable
import scala.scalanative.annotation.alwaysinline

class GlobPattern(pattern: String) {

  def matcher(pathString: String): GlobMatcher =
    new GlobMatcher(globNFA, pathString)

  private val globNFA = compileGlobNFA()

  private def compileGlobNFA(): GlobNode = {

    val length = pattern.length
    val parsingList = mutable.ArrayBuffer[GlobSpecification]()

    var i = 0

    @alwaysinline def nextTwo(): (Char, Option[Char]) = {
      val first = pattern.charAt(i)
      val second =
        if (i + 1 < length) Some(pattern.charAt(i + 1))
        else None
      (first, second)
    }

    @alwaysinline def nextThree(): (Char, Option[Char], Option[Char]) = {
      val (first, second) = nextTwo()
      val third =
        if (i + 2 < length) Some(pattern.charAt(i + 2))
        else None

      (first, second, third)
    }

    def parseBracketSet() = {
      val set = mutable.TreeSet[Char]()
      while ({
        val closedBracket =
          nextThree() match {
            case (']', _, _) =>
              i += 1
              true
            case ('/', _, _) =>
              throw new PatternSyntaxException(
                "Explicit 'name separator' in class",
                pattern,
                i
              )
            case (startingChar, Some('-'), Some('/')) =>
              throw new PatternSyntaxException("Invalid range", pattern, i)
            case (startingChar, Some('-'), Some(endingChar))
                if (endingChar < startingChar && endingChar != ']') =>
              throw new PatternSyntaxException("Invalid range", pattern, i)
            case (startingChar, Some('-'), Some(endingChar))
                if endingChar != ']' =>
              for (j <- startingChar to endingChar) set.add(j)
              i += 3
              false
            case (otherChar, _, _) =>
              set.add(otherChar)
              i += 1
              false
          }

        if (!closedBracket && i >= length)
          throw new PatternSyntaxException("Missing ']'", pattern, i - 1)

        !closedBracket
      }) ()

      set.toSet
    }

    def parseGroups() = {
      def parseGroupElement() = {
        val groupBuffer = mutable.ArrayBuffer[GlobSpecification]()
        var toLeaveGroups = false
        var toNextGroup = false
        while (!toLeaveGroups && !toNextGroup) {
          nextTwo() match {
            case ('?', _) =>
              groupBuffer.append(AnyChar)
              i += 1
            case ('*', Some('*')) =>
              groupBuffer.append(DoubleAsterisk)
              i += 2
            case ('*', _) =>
              groupBuffer.append(Asterisk)
              i += 1
            case ('[', Some('!')) =>
              i += 1
              groupBuffer.append(NegationBracket(parseBracketSet()))
            case ('[', _) =>
              groupBuffer.append(Bracket(parseBracketSet()))
            case ('{', _) =>
              throw new PatternSyntaxException(
                s"Cannot nest groups",
                pattern,
                i
              )
            case ('}', _) =>
              toLeaveGroups = true
              i += 1
            case (',', _) =>
              toNextGroup = true
              i += 1
            case ('\\', Some(char)) =>
              groupBuffer.append(GlobChar(char))
              i += 2
            case (char, _) =>
              groupBuffer.append(GlobChar(char))
              i += 1
          }

          if (i >= length && !toLeaveGroups)
            throw new PatternSyntaxException("Missing '}'", pattern, i)
        }

        (groupBuffer.toList, toLeaveGroups)
      }

      val buffer = mutable.ArrayBuffer[List[GlobSpecification]]()
      while ({
        val (parsingList, toLeaveGroups) = parseGroupElement()
        buffer.append(parsingList)

        i < length && !toLeaveGroups
      }) ()
      Groups(buffer.toList)
    }

    while (i < length) {
      nextTwo() match {
        case ('?', _) =>
          parsingList.append(AnyChar)
          i += 1
        case ('{', _) =>
          i += 1
          parsingList.append(parseGroups())
        case ('*', Some('*')) =>
          parsingList.append(DoubleAsterisk)
          i += 2
        case ('*', _) =>
          parsingList.append(Asterisk)
          i += 1
        case ('[', Some('!')) =>
          i += 2
          parsingList.append(NegationBracket(parseBracketSet()))
        case ('[', _) =>
          i += 1
          parsingList.append(Bracket(parseBracketSet()))
        case ('/', _) =>
          parsingList.append(Separator)
          i += 1
        case ('\\', Some(char)) =>
          parsingList.append(GlobChar(char))
          i += 2
        case (char, _) =>
          parsingList.append(GlobChar(char))
          i += 1
      }
    }

    def charsTaken(spec: GlobSpecification): (Int, Int) =
      spec match {
        case Separator                                               => (1, 1)
        case AnyChar | GlobChar(_) | Bracket(_) | NegationBracket(_) => (1, 0)
        case Groups(_) | Asterisk | DoubleAsterisk                   => (0, 0)
      }

    def specListToGlobNodes(
        specList: List[GlobSpecification],
        next: GlobNode
    ): GlobNode =
      specList match {
        case (head @ Groups(lists)) :: tail =>
          val nextGroupNodes =
            lists.map(list => specListToGlobNodes(list.reverse, next))
          val minChars = nextGroupNodes.minBy(_.minSepsLeft).minSepsLeft
          val minSeps = nextGroupNodes.minBy(_.minCharsLeft).minCharsLeft
          val newNext = TransitionNode(head, nextGroupNodes, minChars, minSeps)
          specListToGlobNodes(tail, newNext)
        case head :: tail =>
          val (chars, seps) = charsTaken(head)
          val newNext = TransitionNode(
            head,
            List(next),
            next.minCharsLeft + chars,
            next.minSepsLeft + seps
          )
          specListToGlobNodes(tail, newNext)
        case Nil => next
      }

    val reversedSpecification = parsingList.toList.reverse
    StartNode(
      specListToGlobNodes(
        reversedSpecification,
        EndNode
      )
    )
  }
}

object GlobPattern {
  def compile(pattern: String): GlobPattern =
    new GlobPattern(pattern)
}
