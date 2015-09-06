package salty.tools.linker

import salty.ir

class Linker(val classpath: Classpath, entry: ir.Name) {
  def link: Option[ir.Defn] = {
    val scope = new ClasspathScope(classpath)
    val defn = entry.resolve(entry)
    (new ResolveExtern(scope)).run(defn)
  }
}
