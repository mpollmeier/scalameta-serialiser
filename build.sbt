lazy val commonSettings = Seq(
  organization := "com.michaelpollmeier",
  version := "1.0.0-SNAPSHOT",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.scalameta" %% "scalameta" % "1.3.0",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  ),
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M5" cross CrossVersion.full)
)

lazy val scalameta_serialiser = project.in(file("."))
  .settings(commonSettings: _*)

lazy val examples = project.in(file("examples"))
  .settings(commonSettings: _*)
  .dependsOn(scalameta_serialiser)

