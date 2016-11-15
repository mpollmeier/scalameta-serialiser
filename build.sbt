name := "scalameta-examples"
version := "1.0.0-SNAPSHOT"
scalaVersion := "2.11.8"

val commonSettings = Seq(
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.scalameta" %% "scalameta" % "1.3.0",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  ),
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M5" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise"
)

lazy val root = project.in(file(".")).aggregate(examples, macros)

lazy val examples = project.in(file("examples"))
  .settings(commonSettings: _*)
  .dependsOn(macros)

lazy val macros = project.in(file("macros"))
  .settings(commonSettings: _*)
