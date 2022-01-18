package java.nio.file.glob

import java.util.regex.PatternSyntaxException

import scala.collection.mutable
import scala.annotation.tailrec
import scala.scalanative.annotation.alwaysinline

class GlobPattern(pattern: String) {

  def matcher(): GlobMatcher = new GlobMatcher(this)

  private[glob] val minDividerAmount = 0 // todo set maybe
  private[glob] val minCharAmount = 0 // TODO set maybe

  private[glob] lazy val globSpecification = { // Map[(Int, Char), Int] = { // (state, char) -> state, produces NFA

    // Parse input
    val length = pattern.length
    val parsingList = mutable.ArrayBuffer[GlobSpecification]()

    var i = 0
    var escaping = false
    var inParentheses = false
    var inBrackets = false

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
            case (startingChar, Some('-'), Some(endingChar)) if endingChar != ']' =>
              for (j <- startingChar to endingChar) set.add(j)
              i += 2
              false
            case (otherChar, _, _) =>
              set.add(otherChar)
              false
          }

        i += 1

        if (!closedBracket && i >= length) throw new PatternSyntaxException("Missing ']'", pattern, i-1)

        !closedBracket
      }) ()

      set.toSet
    }

    def parseGroups() = {
      i += 1
      def parseGroupElement() = {
        val groupBuffer = mutable.ArrayBuffer[GlobSpecification]()
        var toLeave = false
        var toNext = false
        while (!toLeave && !toNext) {
          nextTwo() match {
            case ('?', _) =>
              groupBuffer.append(AnyChar)
            case ('*', Some('*')) =>
              groupBuffer.append(CrossingName)
              i += 1
            case ('*', _) =>
              groupBuffer.append(NonCrossingName)
            case ('[', Some('!')) =>
              i += 1
              groupBuffer.append(NegationBracket(parseBracketSet()))
              i -= 1
            case ('[', _) =>
              groupBuffer.append(Bracket(parseBracketSet()))
              i -= 1
            case ('{', _) =>
              throw new PatternSyntaxException(
                s"Cannot nest groups",
                pattern,
                i
              )
            case ('}', _) =>
              toLeave = true // TODO maybe return/reconsider
            case (',', _) =>
              toNext = true
            case ('\\', Some(char)) =>
              groupBuffer.append(GlobChar(char))
              i += 1
            case (char, _) =>
              groupBuffer.append(GlobChar(char))
          }

          i += 1
          if(i >= length && !toLeave) throw new PatternSyntaxException("Missing '}'", pattern, i)
        }

        (groupBuffer.toList, toLeave)
      }

      val buffer = mutable.ArrayBuffer[List[GlobSpecification]]()
      var leaving = false // todo redo loop
      while (i < length && !leaving) { // todo check incorrect todo redo loop
        val (parsingList, toLeave) = parseGroupElement()
        leaving = toLeave
        buffer.append(parsingList)
      }
      Groups(buffer.toList)
    }

    while (i < length) {
      nextTwo() match {
        case ('?', _) =>
          parsingList.append(AnyChar)
        case ('{', _) =>
          parsingList.append(parseGroups())
          i -= 1
        case ('*', Some('*')) =>
          parsingList.append(CrossingName)
          i += 1
        case ('*', _) =>
          parsingList.append(NonCrossingName)
        case ('[', Some('!')) =>
          i += 2
          parsingList.append(NegationBracket(parseBracketSet()))
          i -= 1
        case ('[', _) =>
          i += 1
          parsingList.append(Bracket(parseBracketSet()))
          i -= 1
        case ('/', _) =>
          parsingList.append(Divider)
        case ('\\', Some(char)) =>
          parsingList.append(GlobChar(char))
          i += 1
        case (char, _) =>
          parsingList.append(GlobChar(char))
      }

      i += 1
    }

    // Parse to NFA(-like)
    // @tailrec todo
    def specListToGlobNodes(
        specList: List[GlobSpecification],
        next: GlobNode
    ): GlobNode =
      specList match {
        case (head @ Groups(lists)) :: tail =>
          val nextGroupNodes =
            lists.map(list => specListToGlobNodes(list.reverse, next))
          val newNext = GlobNode(head, nextGroupNodes, isFinal = false)
          specListToGlobNodes(tail, newNext)
        case head :: Nil => // TODO check if NIL is a list
          val newNext = GlobNode(head, List(next), isFinal = false)
          newNext
        case head :: tail => // TODO reconsider where is specification parsed
          val newNext = GlobNode(head, List(next), isFinal = false)
          specListToGlobNodes(tail, newNext)
        case Nil => next
      }

    val reversedSpecification = parsingList.toList.reverse
    GlobNode(
      null,
      List(
        specListToGlobNodes(
          reversedSpecification,
          GlobNode(null, Nil, isFinal = true)
        )
      ),
      false
    ) // todo rethink final
  }
}

object GlobPattern {
  def compile(pattern: String): GlobPattern = {
    val a = new GlobPattern(pattern)
    a.globSpecification
    a
  }
}
