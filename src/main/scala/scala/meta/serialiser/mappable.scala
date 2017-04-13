package scala.meta.serialiser

import scala.annotation.compileTimeOnly
import scala.annotation.StaticAnnotation
import scala.collection.immutable.Seq
import scala.meta._
import scala.util.control.NoStackTrace

// type classes that @mappable will generate for annotated classes
trait ToMap[A] {
  def apply(a: A): Map[String, Any]
  val customMappings: Map[String, String]
}
trait FromMap[A] {
  def apply(keyValues: Map[String, Any]): Option[A]
  val customMappings: Map[String, String]
}

case class SerialiserException(message: String, cause: Option[Throwable] = None)
    extends RuntimeException(message, cause.orNull)
    with NoStackTrace


/** Map a class member to a custom name. Usage example: 
  *  @mappable case class WithCustomMapping(
  *    @mappedTo("iMapped") i: Int,
  *    @mappedTo("jMapped") j: Option[Int],
  *                         s: String)
  */
class mappedTo(name: String) extends StaticAnnotation

/** example usages: see MappableTest.scala */
@compileTimeOnly("@scala.meta.serialiser.mappable not expanded")
class mappable extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {

    // defined class may or may not have a companion object
    val (classDefn: Defn.Class, compDefnOption: Option[Defn.Object]) = defn match {
      case classDefn: Defn.Class => (classDefn, None) //only class
      case Term.Block((classDefn: Defn.Class) :: (compDefn: Defn.Object) :: Nil) => (classDefn, Option(compDefn)) // class + companion
      case _ => abort(defn.pos, "Invalid annottee")
    }

    // get existing companion object statements (if any)
    val compStats: Seq[Stat] = compDefnOption match {
      case None => Nil
      case Some(compDefn) => compDefn.templ.stats.getOrElse(Nil)
    }

    val q"..$mods class $tName[..$tParams] ..$ctorMods (...$paramss) extends $template" = classDefn
    val paramssFlat: Seq[Term.Param] = paramss.flatten

    val typeTermName = Term.Name(tName.value)

    val tParamTypes: Seq[Type] = tParams map Helpers.toType
    val tCompleteTerm: Term =
      if (tParamTypes.isEmpty) q"$typeTermName"
      else q"$typeTermName[..$tParamTypes]"
    val tCompleteType: Type = Helpers.toType(tCompleteTerm)
    val tCompleteTypeOption: Type = Helpers.toType(q"Option[$tCompleteType]")

    val customMappings2: Map[Term.Param, String] = paramssFlat.map { param =>
      param.mods.collect {
        case mod"@mappedTo(${Lit.String(mappedTo)})" => (param -> mappedTo)
      }
    }.flatten.toMap.withDefault(_.name.value)

    object ToMapImpl {
      val instanceName: Term.Name = q"instance"
      def keyValues(instanceName: Term.Name): Seq[Term] = paramssFlat.map { param =>
        val propertyKey: String = customMappings2(param)
        val nameTerm = Term.Name(param.name.value)
        param.decltpe.getOrElse{ throw new SerialiserException(s"type for $nameTerm not defined...") } match {
          case _: Type.Name => // simple type, e.g. String
            q"$propertyKey -> $instanceName.$nameTerm"
          case Type.Apply(Type.Name(tpeName), _) if tpeName == "Option" => // Option[A]
            q"$propertyKey -> $instanceName.$nameTerm.getOrElse(null)"
          case other => throw new SerialiserException(s"unable to map $other (${other.getClass})... not (yet) supported")
        }
      }
    }

    object FromMapImpl {
      val ctorMapWithValues: Term.Name = q"values"

      // get default value and store those value as a map in object
      val defaultValue:  Seq[Term.ApplyInfix] = paramssFlat collect {
        case param if param.default.nonEmpty =>
          q"""${param.name.value} -> ${param.default.get}"""
      }

      val ctorParamsFirst: Seq[Term.Param] = paramss.headOption.getOrElse(Nil)
      def ctorArgs(mapWithValues: Term.Name): Seq[Term] = ctorParamsFirst.map { param =>
        val propertyKey: String = customMappings2(param)
        val nameTerm = Term.Name(param.name.value)
        val fromMapWithExpectedType = param.decltpe.getOrElse{ throw new SerialiserException(s"type for $nameTerm not defined...") } match {
          case tpe: Type.Name => // simple type, e.g. String
            q"""$mapWithValues($propertyKey).asInstanceOf[$tpe]""" 
          case completeTpe @ Type.Apply(Type.Name(tpeName), wrappedTpe :: Nil) if tpeName == "Option" => // Option[A]
            q"""Option($mapWithValues.get($propertyKey).orNull).asInstanceOf[$completeTpe]""" 
          case other => throw new SerialiserException(s"unable to map $other (${other.getClass})... not (yet) supported")
        }
        q""" $nameTerm = $fromMapWithExpectedType"""
      }
    }

    object CustomMappings {
      val debugKey = "_debug" //if set to `true`, we will print the generated code

      def forMember(memberName: String): String =
        customMappings.get(memberName).getOrElse(memberName)

      /* not having named arguments to @mappable limits extensibility, but this is currently the only way to
      * pass arguments to the macro annotation */
      val (mappingsAsTerms: Seq[Term], customMappings: Map[String, String], debugEnabled: Boolean) = {
        def illegalDefinition(unsupported: Tree) = throw new SerialiserException(
          "illegal definition of @mappable annotation. Valid examples are e.g.:" +
          " `@mappable` and `@mappable(List(\"memberName\" -> \"mappedName\")`. See MappableTest.scala for more examples. " +
          s"Unsupported Tree: $unsupported of type ${unsupported.getClass}")

        val mappings: Seq[(Lit, Lit)] = this match {
          case q"new $_()" => Nil // no mappings defined
          case q"new $_(${Term.Apply(_, mappings)})" => mappings.map { // I'd rather match on a refinement type, but that's unchecked :(
            case Term.ApplyInfix(memberName: Lit, _, _, (mappedName: Lit) :: Nil) => (memberName, mappedName)
            case unsupported => illegalDefinition(unsupported)
          }
          case unsupported => illegalDefinition(unsupported)
        }

        val mappingsAsTerms: Seq[Term] = mappings.map {
          case (memberName: Lit, mappedName: Lit) => q"""$memberName -> $mappedName"""
        }
        val mappingsAsStrings: Map[String, String] = mappings.map {
          case (memberName: Lit, mappedName: Lit) => (memberName.value.toString, mappedName.value.toString)
        }.toMap
        val debugEnabled: Boolean = mappingsAsStrings.getOrElse(debugKey, "false").toBoolean
        val customMappings: Map[String, String] = mappingsAsStrings - debugKey // debugKey is in internal detail
        (mappingsAsTerms, customMappings, debugEnabled)
      }

      def validateCustomMappings(): Unit = {
        val memberNames: Set[String] = paramssFlat.map(_.name.value).toSet
        val mappedMembers = customMappings.keys.filterNot(_.startsWith("_"))
        mappedMembers.foreach { key =>
          if (!memberNames.contains(key))
            throw new SerialiserException(s"mapped member '$key' is not a member of class '$tName'")
        }
      }
    }

    CustomMappings.validateCustomMappings()

    val res = q"""
      ..$mods class $tName[..$tParams](...$paramss) extends $template

      object $typeTermName {
        import scala.meta.serialiser.{ToMap, FromMap}

        val defaultValueMap: Map[String, Any] = Map(..${FromMapImpl.defaultValue})

        implicit def toMap[..$tParams] = new scala.meta.serialiser.ToMap[$tCompleteType] {
          override def apply(${ToMapImpl.instanceName}: ${Option(tCompleteType)}): Map[String, Any] =
            Map[String, Any](..${ToMapImpl.keyValues(ToMapImpl.instanceName)})

          override val customMappings = Map[String, String](..${CustomMappings.mappingsAsTerms})
        }

        implicit class ToMapOps[..$tParams](instance: $tCompleteType) {
          def toMap(implicit toMap: ToMap[$tCompleteType]): Map[String, Any] = toMap(instance)
        }

        implicit def fromMap[..$tParams] = new scala.meta.serialiser.FromMap[$tCompleteType] {
          override def apply(v: Map[String, Any]): ${Option(tCompleteTypeOption)} = {
              val values = defaultValueMap ++ v
              scala.util.Try {
                ${tCompleteTerm}(..${FromMapImpl.ctorArgs(FromMapImpl.ctorMapWithValues)})
              }.toOption
            }
          
          override val customMappings = Map[String, String](..${CustomMappings.mappingsAsTerms})
        }

        ..$compStats
      }
    """

    if (CustomMappings.debugEnabled) println(res)
    res
  }
}

object Helpers {
  def toType(term: Term): Type = term match {
    case name: Term.Name => Type.Name(name.value)
    case applyType: Term.ApplyType => Type.Apply(toType(applyType.fun), applyType.targs)
  }

  def toType(tparam: Type.Param): Type = Type.Name(tparam.name.value)
}
