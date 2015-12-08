package edu.illinois.cs.cogcomp.saulexamples.datamodel

import edu.illinois.cs.cogcomp.saul.datamodel.DataModel
import org.scalatest.{ Matchers, FlatSpec }

class GraphQueriesTest extends FlatSpec with Matchers {

  object TestGraph extends DataModel {
    val firstNames = node[String]
    val lastNames = node[String]
    val name = edge(firstNames, lastNames, 'names)
    val prefix = property[String]("prefix")((s: String) => s.charAt(1).toString)

    firstNames.populate(Seq("Dave", "John", "Mark", "Michael"))
    lastNames.populate(List("Dell", "Jacobs", "Maron", "Mario"))

    name.populateWith(_.charAt(0) == _.charAt(0))
  }

  "finding neighbors of a link" should "find the neighbors" in {
    import TestGraph._
    name.forward.neighborsOf("Dave").toSet should be(Set("Dell"))
    name.forward.neighborsOf("John").toSet should be(Set("Jacobs"))
  }

  "finding neighbors of a reverse link" should "find the reverse neighbors" in {
    import TestGraph._
    name.backward.neighborsOf("Jacobs").toSet should be(Set("John"))
    name.backward.neighborsOf("Maron").toSet should be(Set("Mark", "Michael"))
  }

  "atomic queries" should "return themselves" in {
    import TestGraph._
    firstNames().toSet should be(Set("Dave", "John", "Mark", "Michael"))
    firstNames("Jim").toSet should be(Set("Jim"))
  }

  "single hop with all instances" should "return their neighbors" in {
    import TestGraph._
    val query = firstNames() ~> name
    query.toSet should be(Set("Dell", "Jacobs", "Maron", "Mario"))
  }

  "single hop with custom instances" should "return their neighbors" in {
    import TestGraph._

    val query1 = firstNames("John") ~> name
    query1.toSet should be(Set("Jacobs"))

    val query2 = firstNames("Mark") ~> name
    query2.toSet should be(Set("Maron", "Mario"))
  }

  "single reverse hop with custom instances" should "return their neighbors" in {
    import TestGraph._

    val query = lastNames() ~> -name
    query.toSet should be(firstNames.getAllInstances.toSet)

    val query1 = lastNames("Jacobs") ~> -name
    query1.toSet should be(Set("John"))

    val query2 = lastNames("Maron") ~> -name
    query2.toSet should be(Set("Mark", "Michael"))
  }

  "reverse hop with custom instances" should "return similar ones" in {
    import TestGraph._

    val query1 = firstNames("John") ~> name ~> -name
    query1.toSet should be(Set("John"))

    val query2 = firstNames("Mark") ~> name ~> -name
    query2.toSet should be(Set("Mark", "Michael"))
  }

  "prop on single node, single instance" should "return the correct value" in {
    import TestGraph._

    val query1 = firstNames("John") prop prefix
    query1.toSet should be(Set("o"))

    val query2 = lastNames("Maron") prop prefix
    query2.toSet should be(Set("a"))
  }

  "prop on single node, multiple instances" should "return the correct set" in {
    import TestGraph._

    val query1 = firstNames(Seq("John", "Dave", "Mark")) prop prefix
    query1.toSet should be(Set("o", "a"))

    val query2 = lastNames() prop prefix
    query2.toSet should be(Set("a", "e"))
  }

  "prop on query, multiple instances" should "return the correct values" in {
    import TestGraph._

    val query1 = firstNames("John") ~> name prop prefix
    query1.toSet should be(Set("a"))
    query1.counts.size should be(1)
    query1.counts("a") should be(1)

    val query2 = firstNames("Mark") ~> name prop prefix
    query2.toSet should be(Set("a"))
    query2.counts.size should be(1)
    query2.counts("a") should be(2)

    val query3 = firstNames() ~> name prop prefix
    query3.toSet should be(Set("a", "e"))
    query3.counts.size should be(2)
    query3.counts("a") should be(3)
    query3.counts("e") should be(1)

    //  "finding the nodes in a window in the neighbohood" should "find the neighbors in a window" in { 
    //     firstNames.getWithWindow(firstNames.getAllInstances.head, -2, 2).toSet should be(Set(None, Some("Dave"), Some("John"), Some("Mark"))) 
    //    lastNames.getWithWindow(lastNames.getAllInstances.head, -2, 2).toSet should be(Set(None, Some("Dave"), Some("John"), Some("Mark")))  }
  }
}
