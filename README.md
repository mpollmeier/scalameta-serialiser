[![Build Status](https://secure.travis-ci.org/mpollmeier/scalameta-serialiser.png?branch=master)](http://travis-ci.org/mpollmeier/scalameta-serialiser)
[![Scaladex](https://index.scala-lang.org/mpollmeier/scalameta-serialiser/scalameta-serialiser/latest.svg)](https://index.scala-lang.org/mpollmeier/scalameta-serialiser/scalameta-serialiser/)

## Setup with sbt
Get the latest version of [scalameta-serialiser](https://maven-badges.herokuapp.com/maven-central/com.michaelpollmeier/scalameta-serialiser_2.12) and the [scalameta compiler plugin](https://maven-badges.herokuapp.com/maven-central/org.scalameta/paradise_2.12.3)

```
libraryDependencies += "com.michaelpollmeier" %% "scalameta-serialiser" % "LATEST_VERSION"
addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full)
```

## Usage

Just annotate your case class with @mappable to wire in this macro. It will create a serialiser and deserialiser for the annotated class. 

```scala
import scala.meta.serialiser._

@mappable case class SimpleCaseClass(i: Int, s: String)

val testInstance = SimpleCaseClass(i = 42, s = "something")
val keyValues: Map[String, Any] = testInstance.toMap
SimpleCaseClass.fromMap(keyValues) // result: Some(testInstance)
```

## Features

* map class members to different names: `@mappedTo("iMapped") i: Int`
* mark class members as nullable: `@nullable nullableValue: String`
* Option types
* default values
* keeps existing body of the annotated class
* keeps existing companion object, injects the generated typeclasses
* inject your own parameters for later use, useful when using this in a library: `@mappable(Map("param1" -> "value1"))`

All of the above are covered in [MappableTest.scala](examples/src/test/scala/scala/meta/serialiser/MappableTest.scala).

## Understand what's going on
Annotating any case class with `mappable` will generate typeclass instances `FromMap` and `ToMap` that let you serialise and deserialise that specific case class. These typeclass instances end up in the companion object.

```scala
trait ToMap[A] {
  def apply(a: A): Map[String, Any]
}

trait FromMap[A] {
  def apply(keyValues: Map[String, Any]): Option[A]
}
```

If you want to see what that means specifically for your class, you can turn on debug mode - this will print the generated code:

```scala
@mappable(Map("_debug" -> "true"))
case class WithDebugEnabled(i: Int)
```


## TODOs
* [get maps of specific types](https://github.com/mpollmeier/scalameta-serialiser/issues/1)
* support for multiple constructor parameter lists
* allow to switch between `null` entry and missing entry in Map when dealing with an Option type (currently maps to `null`)

## sbt command to compile and test this project
;clean;examples/clean;examples/test

## release a new version from sbt
* release  #will do a cross release
* sonatypeReleaseAll

## Talk at ScalaDays 2017 Chicago code generation with scala.meta
[Video](https://www.youtube.com/watch?v=l88-ljjtLO0) and [slides](http://www.michaelpollmeier.com/presentations/2017-04-22-scalameta-scaladays)
