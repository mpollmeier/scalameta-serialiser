package scala.meta.serialiser

import scala.annotation.compileTimeOnly
import scala.annotation.StaticAnnotation
import scala.collection.immutable.Seq
import scala.meta._

@compileTimeOnly("@scala.meta.serialiser.entity not expanded")
class entity extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"..$mods class $tName[..$tParams] ..$ctorMods (...$paramss) extends $template" = defn
    val typeTermName = Term.Name(tName.value)

    val tCompleteType: Type = {
      val tParamTypes: Seq[Type] = tParams map Helpers.toType
      val tCompleteTerm: Term =
        if (tParamTypes.isEmpty) q"$typeTermName"
        else q"$typeTermName[..$tParamTypes]"
      Helpers.toType(tCompleteTerm)
    }

    object ToMap {
      val entityName: Term.Name = q"entity"
      val paramssFlat: Seq[Term.Param] = paramss.flatten
      def keyValues(entityName: Term.Name): Seq[Term] = paramssFlat.map { param =>
        val memberName = Term.Name(param.name.value)
        q"${param.name.value} -> $entityName.$memberName"
      }
    }

    object FromMap {
      val ctorValuesName: Term.Name = q"values"

    // TODO: support multiple constructor params lists
      val ctorParamsFirst: Seq[Term.Param] = paramss.headOption.getOrElse(Nil)
      def ctorArgs(valuesName: Term.Name): Seq[Term] = ctorParamsFirst.map { param =>
        val nameTerm = Term.Name(param.name.value)
        val tpe: Type = param.decltpe.get.asInstanceOf[Type.Name] // TODO: don't do option.get, don't cast
        q""" $nameTerm = $valuesName(${param.name.value}).asInstanceOf[$tpe] """
      }
    }

    val res = q"""
      ..$mods class $tName[..$tParams](...$paramss) extends $template

      object $typeTermName {
        def toMap[..$tParams](${ToMap.entityName}: ${Option(tCompleteType)}): Map[String, Any] =
          Map[String, Any](..${ToMap.keyValues(ToMap.entityName)})

        def fromMap[..$tParams](values: Map[String, Any]): ${Option(tCompleteType)} =
          ${typeTermName}(..${FromMap.ctorArgs(FromMap.ctorValuesName)})
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
