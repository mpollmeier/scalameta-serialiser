[![Build Status](https://secure.travis-ci.org/mpollmeier/scalameta-serialiser.png?branch=master)](http://travis-ci.org/mpollmeier/scalameta-serialiser)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.michaelpollmeier/scalameta_serialiser_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.michaelpollmeier/scalameta_serialiser_2.11)

# Setup with sbt
Get the latest version of [scalameta-serialiser](https://maven-badges.herokuapp.com/maven-central/com.michaelpollmeier/scalameta_serialiser_2.11) and the [scalameta compiler plugin](https://maven-badges.herokuapp.com/maven-central/org.scalameta/paradise_2.11.8)

```
resolvers += Resolver.bintrayIvyRepo("scalameta", "maven")
libraryDependencies += "com.michaelpollmeier" %% "scalameta_serialiser" % "LATEST_VERSION"
addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0.132" cross CrossVersion.full)
```

# Usage

## @mappable

```scala
import scala.meta.serialiser.mappable
@mappable case class SimpleCaseClass(i: Int, s: String)

val testInstance = SimpleCaseClass(i = 42, s = "something")
val keyValues: Map[String, Any] = testInstance.toMap
SimpleCaseClass.fromMap(keyValues) // result: Some(testInstance)
```

For working examples have a look at [MappableTest.scala](examples/src/test/scala/scala/meta/serialiser/MappableTest.scala).

## Understand what's going on
Annotating any case class with `mappable` will generate typeclass instances `FromMap` and `ToMap` that let you serialise and deserialise that specific case class. 

```scala
trait ToMap[A] {
  def apply(a: A): Map[String, Any]
}

trait FromMap[A] {
  def apply(keyValues: Map[String, Any]): Option[A]
}
```

These typeclass instances end up in the companion object.

## current limitations (a.k.a. TODOs) 
- no support for multiple constructor parameter lists
- [get maps of specific types](https://github.com/mpollmeier/scalameta-serialiser/issues/1)

# sbt command to compile and test this project
;clean;examples/clean;examples/test
