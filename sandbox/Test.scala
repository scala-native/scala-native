/* NSC -- new Scala compiler
 * Copyright 2005-2013 LAMP/EPFL
 * @author  Martin Odersky
 */

package scala.tools.nsc

import java.io.{ FileNotFoundException, PrintWriter, FileOutputStream }
import java.security.SecureRandom
import io.{ File, Path, Directory, Socket }
import scala.tools.util.CompileOutputCommon
import scala.reflect.internal.util.StringOps.splitWhere
import scala.sys.process._

class C {
  def getOrCreateSocket(): Option[Socket] = {
    def getsock(attempts: Int): Option[Socket] =
      attempts match {
        case 0 =>
          None
        case _ =>
          (null: scala.util.Either[Throwable, Socket]) match {
            case Right(socket)  =>
              Some(socket)
            case Left(err)      =>
              getsock(attempts - 1)
          }
      }
    getsock(200)
  }
}
