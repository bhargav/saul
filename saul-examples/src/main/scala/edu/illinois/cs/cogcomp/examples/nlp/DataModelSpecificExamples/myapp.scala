package edu.illinois.cs.cogcomp.examples.nlp.FeatureExamples.DataModelSpecificExamples

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{TextAnnotation, Sentence, Constituent}
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager
import edu.illinois.cs.cogcomp.curator.CuratorFactory
import edu.illinois.cs.cogcomp.examples.nlp.FeatureExamples.{EdisonDataModel, CogcompGiantSensor}
import edu.illinois.cs.cogcomp.tutorial_related.{DocumentReader, Document}
import scala.collection.JavaConversions._
import edu.illinois.cs.cogcomp.examples.util


object myapp {

  def main(args: Array[String]): Unit = {
    val corpus: String = "20-NewsGroup"
    val x: Constituent = null
    val config = "./saul-examples/config/caching-curator.properties"
    val rm = new ResourceManager(config)
    val annotatorService = CuratorFactory.buildCuratorClient(rm)
    val dat: List[Document] = new DocumentReader("./saul-examples/src/test/resources/20newsToy/train").docs.toList.slice(1, 3)

    val a = util.textCollection(dat) zip dat.map(x => x.getGUID) // this generates a list of strings each member is a textual content of a document
    val taList = a.map(x => CogcompGiantSensor.processDocumentWith(annotatorService, corpus, x._2, x._1))
    EdisonDataModel.++(taList)
    //The below line uses a generator sensor
    EdisonDataModel.populateWith(util.f, 'dTos)
    //TODO: make the below line work, to just use the edge name and depending on the type of sensor a generator or matching edge will be called.
    //EdisonDataModel.populateWith(EdisonDataModel.DocTosen)

    val taa = EdisonDataModel.document.getAllInstances
    val sen = EdisonDataModel.sentence.getAllInstances
    val x1 = EdisonDataModel.getFromRelation[Sentence, TextAnnotation](sen.head)
    val x2 = EdisonDataModel.getFromRelation[TextAnnotation, Sentence](taa.head)

    println(s"x1.size = ${x1.size}")
    println(s"x2.size = ${x2.size}")

    annotatorService.closeCache()
    print("finished")

  }
}
