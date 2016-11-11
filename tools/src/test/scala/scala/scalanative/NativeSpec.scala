package scala.scalanative

import nir.parser.NirParser

import scala.io.Source

import fastparse.all.Parsed

import org.scalatest.FlatSpec

abstract class NativeSpec extends FlatSpec {
  def withDefinitions[T](code: String)(fn: Seq[nir.Defn] => T): T =
    withDefinitions(Map("source.scala" -> code))(fn)

  def withDefinitions[T](sources: Map[String, String])(fn: Seq[nir.Defn] => T): T =
    NIRCompiler.withSources(sources) {
      case (sourcesDir, compiler) =>
        val nirFiles = compiler.getNIR(sourcesDir)
        val definitions =
          for { file                     <- nirFiles
                hnir                     =  Source.fromFile(file)
                Parsed.Success(defns, _) =  NirParser(hnir.mkString)
                defn                     <- defns } yield defn

        fn(definitions)
    }

}
