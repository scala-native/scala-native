val scala3Version = sys.props.getOrElse(
  "scala.version",
  throw new RuntimeException(
    """The system property 'scala.version' is not defined.
      |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
  )
)
val scala213Version = sys.props.getOrElse(
  "scala213.version",
  throw new RuntimeException(
    """The system property 'scala213.version' is not defined.
      |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
  )
)

val usesUnstableScala3 = scala3Version.contains("NIGHTLY")

inThisBuild(
  Seq(
    scalaVersion := scala3Version,
    crossScalaVersions := Seq(scala3Version, scala213Version),
    version := "0.1.0-SNAPSHOT",
    organization := "org.scala-native.test",
    publishMavenStyle := true
  )
)

// Fix to allow skipping execution of this scripted test in Nightly versions of Scala
// Tasty produced by nightly versions cannot be consumed by Scala 2.13
def NoOpInUnstableScala = if (usesUnstableScala3)
  Def.settings(
    run := {},
    Test / test := {},
    publishLocal := {},
    Compile / sources := Nil,
    Test / sources := Nil,
    libraryDependencies := Nil
  )
else Def.settings()

def commonScala213Settigns = Def.settings(
  scalacOptions ++= {
    if ((scalaVersion.value).startsWith("2.13."))
      Seq(
        // Needed to use Scala 3 dependencies from Scala 2.13
        "-Ytasty-reader"
      )
    else Nil
  },
  // Scala 2.13.13 regression https://github.com/scala/bug/issues/12955
  // Not important for our tests
  Compile / doc := { new File("not-existing") }
)

lazy val base = project
  .in(file("base"))
  .enablePlugins(ScalaNativePlugin)
  .settings(NoOpInUnstableScala)

lazy val projectA = project
  .in(file("project-A"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    libraryDependencies += organization.value %%% (base / normalizedName).value % version.value
  )
  .settings(NoOpInUnstableScala)

lazy val projectB = project
  .in(file("project-B"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonScala213Settigns,
    libraryDependencies += (organization.value %%% (base / normalizedName).value % version.value)
      .cross(CrossVersion.for3Use2_13)
  )
  .settings(NoOpInUnstableScala)

lazy val projectC = project
  .in(file("project-C"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonScala213Settigns,
    libraryDependencies += (organization.value %%% (base / normalizedName).value % version.value)
      .cross(CrossVersion.for2_13Use3)
  )
  .settings(NoOpInUnstableScala)

lazy val projectD = project
  .in(file("project-D"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonScala213Settigns,
    libraryDependencies ++= Seq(
      organization.value %%% (projectA / normalizedName).value % version.value,
      organization.value %%% (projectB / normalizedName).value % version.value,
      organization.value %%% (projectC / normalizedName).value % version.value
    ),
    excludeDependencies += ExclusionRule(
      organization.value,
      s"${(base / normalizedName).value}_native${ScalaNativeCrossVersion.currentBinaryVersion}_2.13"
    )
  )
  .settings(NoOpInUnstableScala)

lazy val projectE = project
  .in(file("project-E"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonScala213Settigns,
    libraryDependencies ++= Seq(
      organization.value %%% (projectA / normalizedName).value % version.value,
      organization.value %%% (projectB / normalizedName).value % version.value,
      organization.value %%% (projectC / normalizedName).value % version.value
    ).map(_.cross(CrossVersion.for3Use2_13)),
    excludeDependencies += ExclusionRule(
      organization.value,
      s"${(base / normalizedName).value}_native${ScalaNativeCrossVersion.currentBinaryVersion}_3"
    )
  )
  .settings(NoOpInUnstableScala)

lazy val projectF = project
  .in(file("project-F"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonScala213Settigns,
    libraryDependencies ++= Seq(
      organization.value %%% (projectA / normalizedName).value % version.value,
      organization.value %%% (projectB / normalizedName).value % version.value,
      organization.value %%% (projectC / normalizedName).value % version.value
    ).map(_.cross(CrossVersion.for2_13Use3)),
    excludeDependencies += ExclusionRule(
      organization.value,
      s"${(base / normalizedName).value}_native${ScalaNativeCrossVersion.currentBinaryVersion}_2.13"
    )
  )
  .settings(NoOpInUnstableScala)
