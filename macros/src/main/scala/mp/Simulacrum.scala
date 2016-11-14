package mp

import scala.annotation._
import scala.meta._
import scala.meta.transversers._
import scala.collection.immutable.Seq

class typeclass extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    def extractAlias(mods: Seq[Mod]): Option[String] = {
      var alias = Option.empty[String]
      mods.exists {
        case mod"@op(${alias0: Lit})" => alias = Some(alias0.value.toString); true
        case _ => false
      }
      alias
    }

    def filterSimulacrumAnnotations(mods: Seq[Mod]): Seq[Mod] = {
      mods.filter {
        case Mod.Annot(Term.Apply(name: Ctor.Name, _)) =>
          name == "op" || name == "noop"
        case _ => true
      }
    }

    def filterStatSimulacrumAnnotations(stats: Seq[Stat]): Seq[Stat] = stats.map {
      case q"..$mods def $name[..$tparams](...$paramss): $tpe" =>
        q"..${filterSimulacrumAnnotations(mods)} def $name[..$tparams](...$paramss): $tpe"

      case other =>
        other
    }

    val q"""
        trait $tname[$tparam] {
          ..$stats
        }
      """ = defn
    val name = Term.Name(tname.value)
    val toOpsName = Type.Name("To" + tname + "Ops")
    val tparamname = Type.Name(tparam.name.value)

    def adaptMethod(method: Stat, alias: String): Stat = method match {
      case m: Decl.Def =>
        val mods = Nil
        val name = Term.Name(alias)
        val tparams = m.tparams // todo filter first if it belongs only to paramss.head.head
        val decltpe = m.decltpe
        val termparams = m.paramss.head.tail
        val aexprssnel: Seq[Seq[Term.Arg]] = List(arg"self" +: termparams.map(tp => Term.Name(tp.name.value)))
        val paramss = m.paramss.updated(0, termparams)
        val body = q"typeClassInstance.${m.name}(...$aexprssnel)"
        Defn.Def(mods, name, tparams, paramss, Some(decltpe), body)

      case m => m
    }

    val adaptedMethods: Seq[Stat] = stats.map {
      case stat @ q"..$mods def $name[..$tparams](...$paramss): $tpe" =>
        extractAlias(mods) match {
          case Some(alias) => adaptMethod(stat, alias)
          case None => stat
        }

      case other => other
    }

    val res = q"""
       trait $tname[$tparam] {
         ..${filterStatSimulacrumAnnotations(stats)}
       }

       object $name {
         def apply[$tparam](implicit instance: $tname[$tparamname]): $tname[$tparamname] = instance

         trait Ops[$tparam] {
           def typeClassInstance: $tname[$tparamname]
           def self: $tparamname
           ..$adaptedMethods
         }

         trait $toOpsName {
           implicit def ${Term.Name("to" + tname + "Ops")}[$tparam](target: $tparamname)(implicit tc: $tname[$tparamname]): Ops[$tparamname] = new Ops[$tparamname] {
             val self = target
             val typeClassInstance = tc
           }
         }

         object nonInheritedOps extends ${template"${Ctor.Ref.Name(toOpsName.value)}"}

         trait AllOps[$tparam] extends Ops[$tparamname] {
           def typeClassInstance: $tname[$tparamname]
         }

         object ops {
           implicit def ${Term.Name("toAll" + tname + "Ops")}[$tparam](target: $tparamname)(implicit tc: $tname[$tparamname]): AllOps[$tparamname] = new AllOps[$tparamname] {
             val self = target
             val typeClassInstance = tc
           }
         }
       }
      """

    println("----- result is")
    println(res)
    println("-----")

    res
  }
}

class op(name: String) extends StaticAnnotation
