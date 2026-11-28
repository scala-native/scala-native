package org.scalanative.testsuite.javalib.nio.channels

import java.io._
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.Platform

import scala.scalanative.junit.utils.AssumesHelper._

/* A child JVM that holds a lock on a file, driven through stdin:
 * "lock <pos> <size> <shared>", "trylock <pos> <size> <shared>", "release".
 * It exits on stdin EOF or after a minute, so no test can hang on it.
 */
final class LockHolder private (proc: Process) extends AutoCloseable {
  private val lines = new LinkedBlockingQueue[String]()
  private val in = new PrintWriter(
    new OutputStreamWriter(proc.getOutputStream(), UTF_8),
    true
  )

  FileChannelInterruptTest.daemon { () =>
    val out = new BufferedReader(
      new InputStreamReader(proc.getInputStream(), UTF_8)
    )
    try {
      var line = out.readLine()
      while (line != null) {
        lines.put(line)
        line = out.readLine()
      }
    } catch { case _: IOException => () }
    lines.put("EOF")
  }

  def line(): String = {
    val l = lines.poll(LockHolder.ReplyMillis, TimeUnit.MILLISECONDS)
    assertNotNull("lock holder did not reply", l)
    l
  }

  def send(cmd: String): String = {
    in.println(cmd)
    line()
  }

  def lock(pos: Long, size: Long, shared: Boolean = false): Unit =
    assertEquals("child lock", "locked", send(s"lock $pos $size $shared"))

  def tryLock(pos: Long, size: Long, shared: Boolean = false): String =
    send(s"trylock $pos $size $shared")

  def release(): Unit =
    assertEquals("child release", "released", send("release"))

  def close(): Unit = {
    in.close()
    if (!proc.waitFor(5, TimeUnit.SECONDS)) proc.destroyForcibly()
  }
}

object LockHolder {
  final val ReplyMillis = 30000L

  private val source =
    """import java.io.*;
      |import java.nio.channels.*;
      |import java.nio.file.*;
      |import static java.nio.file.StandardOpenOption.*;
      |
      |public class LockHolderMain {
      |  public static void main(String[] a) throws Exception {
      |    Thread timer = new Thread(() -> {
      |      try { Thread.sleep(60000); } catch (InterruptedException e) {}
      |      System.exit(2);
      |    });
      |    timer.setDaemon(true);
      |    timer.start();
      |    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
      |    try (FileChannel ch = FileChannel.open(Paths.get(a[0]), READ, WRITE)) {
      |      FileLock held = null;
      |      System.out.println("READY");
      |      String cmd;
      |      while ((cmd = stdin.readLine()) != null) {
      |        String[] c = cmd.split(" ");
      |        String r;
      |        try {
      |          switch (c[0]) {
      |            case "lock":
      |              held = ch.lock(Long.parseLong(c[1]), Long.parseLong(c[2]), Boolean.parseBoolean(c[3]));
      |              r = "locked";
      |              break;
      |            case "trylock": {
      |              FileLock l = ch.tryLock(Long.parseLong(c[1]), Long.parseLong(c[2]), Boolean.parseBoolean(c[3]));
      |              r = l == null ? "null" : "locked";
      |              if (l != null) l.release();
      |              break;
      |            }
      |            case "release":
      |              held.release();
      |              r = "released";
      |              break;
      |            default:
      |              r = "?";
      |          }
      |        } catch (Throwable e) {
      |          r = "threw " + e;
      |        }
      |        System.out.println(r);
      |      }
      |    }
      |    System.exit(0);
      |  }
      |}
      |""".stripMargin

  private def javaCommand: Option[String] = {
    val exe = if (Platform.isWindows) "java.exe" else "java"
    val home =
      if (Platform.executingInJVM) Option(System.getProperty("java.home"))
      else Option(System.getenv("JAVA_HOME"))
    home
      .map(h => Paths.get(h, "bin", exe))
      .filter(Files.isExecutable(_))
      .map(_.toString())
      .orElse(onPath(exe))
  }

  private def onPath(exe: String): Option[String] =
    Option(System.getenv("PATH")).flatMap { path =>
      path
        .split(File.pathSeparator)
        .iterator
        .map(Paths.get(_, exe))
        .find(Files.isExecutable(_))
        .map(_.toString())
    }

  private def assumeAvailable(): Unit = {
    assumeMultithreadingIsEnabled()
    assumeSupportsSubprocesses()
    assumeTrue("no java executable for the lock holder", javaCommand.isDefined)
  }

  /* Compiled once per run with the javac next to java; without one, every
   * holder runs the source file (Java 11+) and compiles it again.
   */
  private lazy val launcher: Option[Seq[String]] = {
    val java = javaCommand.get
    val dir = Files.createTempDirectory("lock-holder")
    val src = dir.resolve("LockHolderMain.java")
    val cls = dir.resolve("LockHolderMain.class")
    Seq(dir, src, cls).foreach(_.toFile().deleteOnExit())
    Files.write(src, source.getBytes(UTF_8))
    val javac = Paths
      .get(java)
      .resolveSibling(if (Platform.isWindows) "javac.exe" else "javac")
    val compiled = Files.isExecutable(javac) && {
      val p = new ProcessBuilder(
        javac.toString(),
        "-d",
        dir.toString(),
        src.toString()
      )
        .inheritIO()
        .start()
      if (!p.waitFor(ReplyMillis, TimeUnit.MILLISECONDS)) p.destroyForcibly()
      Files.exists(cls)
    }
    if (compiled) Some(Seq(java, "-cp", dir.toString(), "LockHolderMain"))
    else if (javaMajorVersion(java) >= 11) Some(Seq(java, src.toString()))
    else None
  }

  // From `java -version`, e.g. "1.8.0_402" is 8 and "25.0.1" is 25.
  private def javaMajorVersion(java: String): Int = {
    val p =
      new ProcessBuilder(java, "-version").redirectErrorStream(true).start()
    val out = new String(p.getInputStream().readAllBytes(), UTF_8)
    p.waitFor(ReplyMillis, TimeUnit.MILLISECONDS)
    val m = "version \"(?:1\\.)?(\\d+)".r.findFirstMatchIn(out)
    m.fold(0)(_.group(1).toInt)
  }

  // Starts a holder on `file`.
  def start(file: Path): LockHolder = {
    assumeAvailable()
    assumeTrue(
      "lock holder needs javac next to java, or Java 11+",
      launcher.isDefined
    )
    val cmd = new java.util.ArrayList[String]()
    launcher.get.foreach(cmd.add(_))
    cmd.add(file.toString())
    val proc = new ProcessBuilder(cmd)
      .redirectError(ProcessBuilder.Redirect.INHERIT)
      .start()
    val holder = new LockHolder(proc)
    try assertEquals("lock holder start", "READY", holder.line())
    catch {
      case e: Throwable =>
        proc.destroyForcibly()
        throw e
    }
    holder
  }
}
