package scala.scalanative.compat

private[scalanative] object ParserCompat {
  val parser = {
    import Compat._
    {
      import scala.sys.process._
      Parser
    }
  }

  object Compat {
    val Parser = {
      import Compat2._
      {
        import scala.tools._
        import cmd._
        CommandLineParser
      }

    }

    object Compat2 {
      object cmd {
        object CommandLineParser
      }
    }
  }
}
