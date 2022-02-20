inThisBuild(
  Seq(
    scalaVersion := "3.1.1",
    crossScalaVersions := Seq("3.1.1", "2.13.8"),
    version := "0.1.0-SNAPSHOT",
    organization := "org.scala-native.test",
    publishMavenStyle := true
  )
)

def commonScala213Settigns = Def.settings(
  scalacOptions ++= {
    if ((scalaVersion.value).startsWith("2.13."))
      Seq(
        // Needed to use Scala 3 dependencies from Scala 2.13
        "-Ytasty-reader"
      )
    else Nil
  }
)

lazy val base = project
  .in(file("base"))
  .enablePlugins(ScalaNativePlugin)

lazy val projectA = project
  .in(file("project-A"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    libraryDependencies += organization.value %%% (base / normalizedName).value % version.value
  )

lazy val projectB = project
  .in(file("project-B"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonScala213Settigns,
    libraryDependencies += (organization.value %%% (base / normalizedName).value % version.value)
      .cross(CrossVersion.for3Use2_13)
  )

lazy val projectC = project
  .in(file("project-C"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonScala213Settigns,
    libraryDependencies += (organization.value %%% (base / normalizedName).value % version.value)
      .cross(CrossVersion.for2_13Use3)
  )

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
