package scala.scalanative
package nir
package parser

import fastparse.all._

object Conv extends Base[nir.Conv] {

  val Trunc    = P("trunc".! map (_ => nir.Conv.Trunc))
  val Zext     = P("zext".! map (_ => nir.Conv.Zext))
  val Sext     = P("sext".! map (_ => nir.Conv.Sext))
  val Fptrunc  = P("fptrunc".! map (_ => nir.Conv.Fptrunc))
  val Fpext    = P("fpext".! map (_ => nir.Conv.Fpext))
  val Fptoui   = P("fptoui".! map (_ => nir.Conv.Fptoui))
  val Fptosi   = P("fptosi".! map (_ => nir.Conv.Fptosi))
  val Uitofp   = P("uitofp".! map (_ => nir.Conv.Uitofp))
  val Sitofp   = P("sitofp".! map (_ => nir.Conv.Sitofp))
  val Ptrtoint = P("ptrtoint".! map (_ => nir.Conv.Ptrtoint))
  val Inttoptr = P("inttoptr".! map (_ => nir.Conv.Inttoptr))
  val Bitcast  = P("bitcast".! map (_ => nir.Conv.Bitcast))
  override val parser: P[nir.Conv] =
    Trunc | Zext | Sext | Fptrunc | Fpext | Fptoui | Fptosi | Uitofp | Sitofp | Ptrtoint | Inttoptr | Bitcast
}
