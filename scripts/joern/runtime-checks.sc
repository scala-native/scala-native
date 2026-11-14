// Custom Joern queries for the Scala Native runtime C sources.
// Invoked by scripts/joern/scan.sh; heuristics, so expect false positives.

import io.shiftleft.semanticcpg.language.*
import io.shiftleft.codepropertygraph.generated.nodes.*
import java.io.PrintWriter

val allocators = "malloc|calloc|realloc|strdup|strndup|aligned_alloc|valloc"
val handleOpeners = Map(
  "fopen" -> "fclose",
  "fdopen" -> "fclose",
  "opendir" -> "closedir",
  "open" -> "close",
  "socket" -> "close",
  "dup" -> "close",
  "popen" -> "pclose"
)

def location(n: CfgNode): String =
  s"${n.method.filename}:${n.lineNumber.getOrElse(-1)} ${n.method.name}"

// `x = alloc(...)` where x is a plain local/parameter identifier.
def assignedLocal(call: Call): Option[String] =
  call.inAssignment.target.isIdentifier.name.headOption

def usedInCondition(m: Method, name: String): Boolean =
  m.controlStructure.condition.ast.isIdentifier.name(name).nonEmpty ||
    m.call.nameExact("<operator>.conditional").argument(1).ast.isIdentifier
      .name(name)
      .nonEmpty

// The value escapes the function if it is returned, stored somewhere other
// than a plain local, or handed to any call that is not the releasing one.
def escapes(m: Method, name: String, release: Set[String]): Boolean =
  returned(m, name) ||
    m.assignment.exists(a =>
      a.target.code != name && a.source.ast.isIdentifier.name(name).nonEmpty
    ) ||
    m.call
      .filterNot(_.name.startsWith("<operator>"))
      .filterNot(c => release.contains(c.name))
      .argument
      .ast
      .isIdentifier
      .name(name)
      .nonEmpty

def returned(m: Method, name: String): Boolean =
  m.ast.isReturn.ast.isIdentifier.name(name).nonEmpty

def findings(using cpg: io.shiftleft.codepropertygraph.generated.Cpg)
    : List[String] = {
  val allocs = cpg.call.name(allocators).l

  val unchecked = allocs.flatMap { c =>
    assignedLocal(c)
      .filterNot(usedInCondition(c.method, _))
      .filterNot(returned(c.method, _)) // the caller is expected to check
      .map(v => s"unchecked-alloc: `$v = ${c.code}` never NULL-checked: ${location(c)}")
  }

  val reallocSelf = cpg.call
    .name("realloc")
    .filter { c =>
      val target = c.inAssignment.target.code.headOption
      target.isDefined && c.argument(1).code == target.get
    }
    .map(c => s"realloc-self-assign: `${c.inAssignment.code.head}` leaks on failure: ${location(c)}")
    .l

  val freeNames = Set("free")
  val leakedAllocs = allocs
    .filterNot(_.name == "realloc")
    .flatMap { c =>
      assignedLocal(c)
        .filter(_ => c.method.local.name.l.nonEmpty)
        .filter(v => c.method.local.name(v).nonEmpty)
        .filterNot(escapes(c.method, _, freeNames))
        .filter(v => c.method.call.name("free").argument.code(v).isEmpty)
        .map(v => s"possible-leak: `$v = ${c.code}` is never freed: ${location(c)}")
    }

  val leakedHandles = handleOpeners.toList.flatMap { case (open, close) =>
    cpg.call.nameExact(open).l.flatMap { c =>
      assignedLocal(c)
        .filter(v => c.method.local.name(v).nonEmpty)
        .filterNot(escapes(c.method, _, Set(close)))
        .filter(v => c.method.call.nameExact(close).argument.code(v).isEmpty)
        .map(v => s"possible-handle-leak: `$v = ${c.code}` never passed to $close: ${location(c)}")
    }
  }

  (unchecked ++ reallocSelf ++ leakedAllocs ++ leakedHandles).distinct.sorted
}

@main def main(cpgFile: String, outFile: String, srcRoot: String = "") = {
  importCpg(cpgFile)
  val results = findings(using cpg).map(_.replace(srcRoot, ""))
  val out = new PrintWriter(outFile)
  try results.foreach(out.println)
  finally out.close()
  results.foreach(println)
  println(s"${results.size} findings written to $outFile")
}
