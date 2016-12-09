lazy val commonSettings = Seq(
  organization := "com.michaelpollmeier",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.scalameta" %% "scalameta" % "1.4.0",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  ),
  // New-style macro annotations are under active development.  As a result, in
  // this build we'll be referring to snapshot versions of both scala.meta and
  // macro paradise.
  // https://github.com/scalameta/sbt-macro-example/blob/master/build.sbt#L5-L13
  // https://bintray.com/scalameta/maven
  resolvers += Resolver.bintrayIvyRepo("scalameta", "maven"),
  // A dependency on macro paradise 3.x is required to both write and expand
  // new-style macros.  This is similar to how it works for old-style macro
  // annotations and a dependency on macro paradise 2.x.
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0.140" cross CrossVersion.full),
  licenses +=("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://github.com/mpollmeier/scalameta-serialiser"))
)

lazy val scalameta_serialiser = project.in(file("."))
  .settings(commonSettings: _*)

lazy val examples = project.in(file("examples"))
  .settings(commonSettings: _*)
  .dependsOn(scalameta_serialiser)

releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishTo := {
  val sonatype = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at sonatype + "content/repositories/snapshots")
  else
    Some("releases" at sonatype + "service/local/staging/deploy/maven2")
}
pomExtra :=
  <scm>
    <url>git@github.com:mpollmeier/scalameta-serialiser.git</url>
    <connection>scm:git:git@github.com:mpollmeier/scalameta-serialiser.git</connection>
  </scm>
  <developers>
    <developer>
      <id>mpollmeier</id>
      <name>Michael Pollmeier</name>
      <url>http://www.michaelpollmeier.com</url>
    </developer>
  </developers>
