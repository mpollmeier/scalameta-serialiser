package scala.meta.serialiser

import scala.annotation.compileTimeOnly
import scala.annotation.StaticAnnotation
import scala.collection.immutable.Seq
import scala.meta._

// type classes that @mappable will generate for annotated classes
trait ToMap[A] { def apply(a: A): Map[String, Any] }
trait FromMap[A] { def apply(keyValues: Map[String, Any]): Option[A] }

case class SerialiserException(message: String, cause: Option[Throwable] = None) extends RuntimeException(message, cause.orNull)

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

    val typeTermName = Term.Name(tName.value)

    val tParamTypes: Seq[Type] = tParams map Helpers.toType
    val tCompleteTerm: Term =
      if (tParamTypes.isEmpty) q"$typeTermName"
      else q"$typeTermName[..$tParamTypes]"
    val tCompleteType: Type = Helpers.toType(tCompleteTerm)
    val tCompleteTypeOption: Type = Helpers.toType(q"Option[$tCompleteType]")

    object ToMapImpl {
      val instanceName: Term.Name = q"instance"
      val paramssFlat: Seq[Term.Param] = paramss.flatten
      def keyValues(instanceName: Term.Name): Seq[Term] = paramssFlat.map { param =>
        val paramName: String = param.name.value
        val nameTerm = Term.Name(paramName)
        param.decltpe.getOrElse{ throw new SerialiserException(s"type for $nameTerm not defined...") } match {
          case _: Type.Name => // simple type, e.g. String
            q"$paramName -> $instanceName.$nameTerm"
          case Type.Apply(Type.Name(tpeName), _) if tpeName == "Option" => // Option[A]
            q"$paramName -> $instanceName.$nameTerm.getOrElse(null)"
          case other => throw new SerialiserException(s"unable to map $other (${other.getClass})... not (yet) supported")
        }
      }
    }

    object FromMapImpl {
      val ctorMapWithValues: Term.Name = q"values"

      // get default value and store those value as a map in object
      val defaultValue:  Seq[Term.ApplyInfix] = paramss.flatten collect {
        case param if param.default.nonEmpty =>
          q"""${param.name.value} -> ${param.default.get}"""
      }

      val ctorParamsFirst: Seq[Term.Param] = paramss.headOption.getOrElse(Nil)
      def ctorArgs(mapWithValues: Term.Name): Seq[Term] = ctorParamsFirst.map { param =>
        val paramName: String = param.name.value
        val nameTerm = Term.Name(paramName)
        val fromMapWithExpectedType = param.decltpe.getOrElse{ throw new SerialiserException(s"type for $nameTerm not defined...") } match {
          case tpe: Type.Name => // simple type, e.g. String
            q"""$mapWithValues($paramName).asInstanceOf[$tpe]""" 
          case completeTpe @ Type.Apply(Type.Name(tpeName), wrappedTpe :: Nil) if tpeName == "Option" => // Option[A]
            q"""Option($mapWithValues.get($paramName).orNull).asInstanceOf[$completeTpe]""" 
          case other => throw new SerialiserException(s"unable to map $other (${other.getClass})... not (yet) supported")
        }
        q""" $nameTerm = $fromMapWithExpectedType"""
      }
    }

    val res = q"""
      ..$mods class $tName[..$tParams](...$paramss) extends $template

      object $typeTermName {
        import scala.meta.serialiser.{ToMap, FromMap}

        val defaultValueMap: Map[String, Any] = Map(..${FromMapImpl.defaultValue})

        implicit def toMap[..$tParams] = new scala.meta.serialiser.ToMap[$tCompleteType] {
          override def apply(${ToMapImpl.instanceName}: ${Option(tCompleteType)}): Map[String, Any] =
            Map[String, Any](..${ToMapImpl.keyValues(ToMapImpl.instanceName)})
        }

        implicit class ToMapOps[..$tParams](instance: $tCompleteType) {
          def toMap(implicit toMap: ToMap[$tCompleteType]): Map[String, Any] = toMap(instance)
        }

        def fromMap[..$tParams] = new scala.meta.serialiser.FromMap[$tCompleteType] {
          override def apply(v: Map[String, Any]): ${Option(tCompleteTypeOption)} = {
              val values = defaultValueMap ++ v
              scala.util.Try {
                ${tCompleteTerm}(..${FromMapImpl.ctorArgs(FromMapImpl.ctorMapWithValues)})
              }.toOption
            }
        }

        ..$compStats
      }
    """

    // println(res)
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
