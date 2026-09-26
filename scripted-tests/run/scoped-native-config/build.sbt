enablePlugins(ScalaNativePlugin)

scalaVersion := {
  val scalaVersion = System.getProperty("scala.version")
  if (scalaVersion == null)
    throw new RuntimeException(
      """|The system property 'scala.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  else scalaVersion
}

import scala.scalanative.build._

def baseNativeConfig(config: NativeConfig): NativeConfig =
  config
    .withMode(Mode.debug)
    .withLTO(LTO.none)
    .withGC(GC.default)
    .withOptimize(true)

// Distinct override at every scope level to verify the delegation chain:
//   Compile / nativeLinkReleaseX / nativeConfig
//     -> Compile / nativeConfig
//     -> nativeConfig (project)
//     -> ThisBuild / nativeConfig
//     -> Global / nativeConfig
//
// Note: in sbt, Test extends Compile on the configuration axis, so when no
// `Test / nativeConfig` is provided, `Test / nativeConfig` would inherit from
// `Compile / nativeConfig` rather than from the project-scoped `nativeConfig`.
// This test sets every scope explicitly to make the chain unambiguous.
Global / nativeConfig ~= { baseNativeConfig(_).withBaseName("level-global") }
ThisBuild / nativeConfig ~= { _.withBaseName("level-thisbuild") }
nativeConfig ~= { _.withBaseName("level-project") }
Compile / nativeConfig ~= { _.withBaseName("level-compile") }
Test / nativeConfig ~= { _.withBaseName("level-test") }

// Task-scoped overlay: ~= preserves the plugin-installed task default
// (Mode.releaseFast / Mode.releaseFull) and lets the user add fields on top.
Compile / nativeLink / nativeConfig ~= {
  _.withBaseName("level-task-link")
}
Compile / nativeLinkReleaseFast / nativeConfig ~= {
  _.withBaseName("level-task-fast").withLTO(LTO.thin)
}
// Task-scoped replacement: := drops the plugin task default, so the user
// is fully responsible for the resulting NativeConfig (mode included).
Compile / nativeLinkReleaseFull / nativeConfig := (Compile / nativeConfig).value
  .withBaseName("level-task-full")

lazy val checkScopedNativeConfig = taskKey[Unit](
  "verify nativeConfig delegates and overrides across every scope"
)

checkScopedNativeConfig := {
  val global = (Global / nativeConfig).value
  val thisBuild = (ThisBuild / nativeConfig).value
  val proj = nativeConfig.value
  val compileCfg = (Compile / nativeConfig).value
  val testCfg = (Test / nativeConfig).value

  val compileLink = (Compile / nativeLink / nativeConfig).value
  val compileFast = (Compile / nativeLinkReleaseFast / nativeConfig).value
  val compileFull = (Compile / nativeLinkReleaseFull / nativeConfig).value

  val testLink = (Test / nativeLink / nativeConfig).value
  val testFast = (Test / nativeLinkReleaseFast / nativeConfig).value
  val testFull = (Test / nativeLinkReleaseFull / nativeConfig).value

  // -- Each scope holds the value defined at that level --------------------
  assert(
    global.baseName == "level-global",
    s"Global / nativeConfig baseName: ${global.baseName}"
  )
  assert(
    thisBuild.baseName == "level-thisbuild",
    s"ThisBuild / nativeConfig baseName: ${thisBuild.baseName}"
  )
  assert(
    proj.baseName == "level-project",
    s"<project> / nativeConfig baseName: ${proj.baseName}"
  )
  assert(
    compileCfg.baseName == "level-compile",
    s"Compile / nativeConfig baseName: ${compileCfg.baseName}"
  )
  assert(
    testCfg.baseName == "level-test",
    s"Test / nativeConfig baseName: ${testCfg.baseName}"
  )
  // The plugin's Test settings overlay buildTarget = application on top of
  // whatever the user provides for Test / nativeConfig.
  assert(
    testCfg.buildTarget == BuildTarget.application,
    s"Test / nativeConfig buildTarget: ${testCfg.buildTarget}"
  )

  // -- Compile / nativeLink: ~= overlay preserves the no-mode-change default
  //    and lets the user override baseName. ---------------------------------
  assert(
    compileLink.baseName == "level-task-link",
    s"Compile / nativeLink / nativeConfig baseName: ${compileLink.baseName}"
  )
  assert(
    compileLink.mode == Mode.debug,
    s"Compile / nativeLink / nativeConfig mode: ${compileLink.mode}"
  )

  // -- ~= overlay preserves the Mode.releaseFast task default and lets the
  //    user add fields (baseName + lto). -----------------------------------
  assert(
    compileFast.baseName == "level-task-fast",
    s"Compile / nativeLinkReleaseFast / nativeConfig baseName: ${compileFast.baseName}"
  )
  assert(
    compileFast.mode == Mode.releaseFast,
    s"Compile / nativeLinkReleaseFast / nativeConfig mode: ${compileFast.mode}"
  )
  assert(
    compileFast.lto == LTO.thin,
    s"Compile / nativeLinkReleaseFast / nativeConfig lto: ${compileFast.lto}"
  )

  // -- := replacement on a task-scoped nativeConfig drops the plugin default,
  //    so mode falls back to whatever the user-built RHS carries (debug from
  //    Compile / nativeConfig in this case). -------------------------------
  assert(
    compileFull.baseName == "level-task-full",
    s"Compile / nativeLinkReleaseFull / nativeConfig baseName: ${compileFull.baseName}"
  )
  assert(
    compileFull.mode == Mode.debug,
    s"Compile / nativeLinkReleaseFull / nativeConfig mode: ${compileFull.mode}"
  )

  // -- Test task-scoped configs delegate to Test / nativeConfig (which carries
  //    "level-test" + buildTarget=application) and pick up the appropriate
  //    plugin task defaults for the release modes. -------------------------
  assert(
    testLink.baseName == "level-test",
    s"Test / nativeLink / nativeConfig baseName: ${testLink.baseName}"
  )
  assert(
    testLink.buildTarget == BuildTarget.application,
    s"Test / nativeLink / nativeConfig buildTarget: ${testLink.buildTarget}"
  )
  assert(
    testLink.mode == Mode.debug,
    s"Test / nativeLink / nativeConfig mode: ${testLink.mode}"
  )

  assert(
    testFast.baseName == "level-test",
    s"Test / nativeLinkReleaseFast / nativeConfig baseName: ${testFast.baseName}"
  )
  assert(
    testFast.mode == Mode.releaseFast,
    s"Test / nativeLinkReleaseFast / nativeConfig mode: ${testFast.mode}"
  )
  assert(
    testFast.buildTarget == BuildTarget.application,
    s"Test / nativeLinkReleaseFast / nativeConfig buildTarget: ${testFast.buildTarget}"
  )

  assert(
    testFull.baseName == "level-test",
    s"Test / nativeLinkReleaseFull / nativeConfig baseName: ${testFull.baseName}"
  )
  assert(
    testFull.mode == Mode.releaseFull,
    s"Test / nativeLinkReleaseFull / nativeConfig mode: ${testFull.mode}"
  )
  assert(
    testFull.buildTarget == BuildTarget.application,
    s"Test / nativeLinkReleaseFull / nativeConfig buildTarget: ${testFull.buildTarget}"
  )
}
