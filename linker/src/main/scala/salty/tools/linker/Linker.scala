package salty.tools.linker

import salty.ir

class Linker(val classpath: Classpath, entry: ir.Name) {
  def link: Option[ir.Defn] =
    new ClasspathScope(classpath).resolve(entry)
}
