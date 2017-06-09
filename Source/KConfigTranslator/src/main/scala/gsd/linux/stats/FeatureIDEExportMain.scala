package gsd.linux.stats

import java.io.PrintStream

import gsd.linux.Hierarchy.{mkConfigMap, toStringMap}
import gsd.linux._
import gsd.linux.KType

/**
  * Created by aknueppel on 12.02.2017.
  */
object FeatureIDEExportMain {

  object VisitState  {
    var visited : List[String] = Nil
    var processing : List[String] = Nil
  }

  def getStructure2(hier: Map[String, String], mutex:List[List[String]], xor:List[List[String]], xorC:List[List[String]], or:List[List[String]], feature: String, model: ASEStatistics): Any = {
    val children = hier.filter((dep: (String, String)) => dep._2 == feature).keys.toList
    val m = if (model.allConfigs.filter(_.name == feature).filter(c => c.ktype == KStringType || c.ktype == KIntType).size > 0) "true" else "false"
    if(m == "true") {
     // println(feature + " is mandatory!")
    }

    if (VisitState.visited.contains(feature)) {
      //do nothing
    } else if (xor.flatten.contains(feature) && !VisitState.processing.contains(feature)) {
      val xor_list = xor.filter(_.contains(feature)).head
      xor_list.foreach(elem => VisitState.processing ::= elem)
      <alt name={feature + "_alt"} mandatory="true" abstract="true">
        {xor_list.map(elem => getStructure2(hier,  mutex, xor.filterNot(_.contains(feature)), xorC, or, elem, model))}
      </alt>
    } else if (mutex.flatten.contains(feature) && !VisitState.processing.contains(feature)) {
      //mutex --> optional alternative group
      val mutex_list = mutex.filter(_.contains(feature)).head
      mutex_list.foreach(elem => VisitState.processing ::= elem)
      <alt name={feature + "_alt"} mandatory="false" abstract="true">
        {mutex_list.map(elem => getStructure2(hier,  mutex, xor.filterNot(_.contains(feature)), xorC, or, elem, model))}
      </alt>
    } else if (or.flatten.contains(feature) && !VisitState.processing.contains(feature)) {
      //or
      val or_list = or.filter(_.contains(feature)).head
      or_list.foreach(elem => VisitState.processing ::= elem)
      <or name={feature + "_alt"} mandatory="true" abstract="true">
        {or_list.map(elem => getStructure2(hier,  mutex, xor.filterNot(_.contains(feature)), xorC, or, elem, model))}
      </or>
    } else if (children == Nil) {
      VisitState.visited ::= feature
        <feature name={feature} mandatory={m}/>
    } else {
      <and name={feature} mandatory={m}> {
        VisitState.visited ::= feature
        children.map(getStructure2(hier, mutex, xor, xorC, or, _, model))
        }
      </and>
    }
  }

  def transformRule(c : BExpr): Any = c match {
    case BOr( a, b ) => (<disj>{transformRule(a)}{transformRule(b)}</disj>).filterNot(_ == <disj></disj>)
    case BNot( a ) => (<not>{transformRule(a)}</not>).filterNot(_ == <not></not>)
    case BImplies( a, b ) => (<imp>{transformRule(a)}{transformRule(b)}</imp>).filterNot(_ == <imp></imp>)
    case BIff( a, b ) => (<eq>{transformRule(a)}{transformRule(b)}</eq>).filterNot(_ == <eq></eq>)
    case BAnd( a, b ) => (<conj>{transformRule(a)}{transformRule(b)}</conj>).filterNot(_ == <conj></conj>)
    case BId( id ) => (<var>{id}</var>).filterNot(_ == <var></var>)
    case _ =>
  }

  //def main(args: Array[String]): Unit = {
  //  val list: List[String] = List("axTLS", "busybox-1.18.0", "embtoolkit", "uClibc", "uClinux-base", "uClinux-distribution", "linux-2.6.33.3")
  //  list.foreach(c => execute(c))
  //}

  def main(args: Array[String]): Unit = {
    println("Converting "+ args(0) + "...")
    val k = KConfigParser.parseKConfigFile(args(0))
    val es = new ASEStatistics(k)

    val mutex = k.choices collect {
      case x@CChoice(_,_,true,false,_,_) => x.cs.map(_.name)
    }

    val xor   = k.choices collect {
      case x@CChoice(_,Prompt(_,Yes),true,true,_,_) => x.cs.map(_.name)
    }

    val xorC  = (k.choices collect {
      case x@CChoice(_,_,true,true,_,_) => x.cs.map(_.name)
    }) filterNot (xor contains)

    val or    = k.choices collect {
      case x@CChoice(_,_,false,true,_,_) => x.cs.map(_.name)
    }

    //val out = new PrintStream("output2/" + name + ".xml")
      //else System.out

    //val out2 = new PrintStream("debug.dat")

    //get hierarchy
    val hierarchy = toStringMap(mkConfigMap(k), k.allConfigs, "root")

    val res = BooleanTranslation.mkBooleanTranslation(k.toAbstractKConfig).exprs
    val CTC = res.filter(x => x.identifiers.intersect(hierarchy.keySet).equals(x.identifiers))
    //es.allConfigs.foreach(out2.println(_))

    // list of all cross-tree constraints (CDLExpression) without hierarchy implications
    //val CTC = BooleanTranslation.mkChoice(k).filter(x => x.identifiers.intersect(hierarchy.keySet).equals(x.identifiers))
    //CTC.foreach(out2.println(_))

    val xml =
      <featureModel>
        <properties/>
        <struct>{
           getStructure2(hierarchy, mutex, xor.union(xorC), xorC, or, "root", es)
          }</struct>
        <constraints>
          {CTC.map(c => <rule>{transformRule(c.simplify)}</rule>).filterNot(_ == <rule></rule>)}
        </constraints>
      </featureModel>

    scala.xml.XML.save( args(1), xml )
  }
}
