package org.renci.rdftotable

import caseapp._
import org.apache.jena.graph.Triple
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.system.StreamRDFBase
import org.apache.jena.sparql.core.Quad
import org.apache.jena.sys.JenaSystem

import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import scala.collection.mutable
import scala.io.Source
import scala.util.Using

case class Options(
                    inputFile: Path,
                    edgesFile: Path,
                    nodesFile: Path,
                    predicatesFile: Path,
                    termLabelsFile: Option[Path]
                  )

object Main extends CaseApp[Options] {

  JenaSystem.init()

  def run(options: Options, arg: RemainingArgs): Unit = {
    val irisToLabels = options.termLabelsFile.flatMap { termLabels =>
      Using(Source.fromFile(termLabels.toFile)) { termLabelsSource =>
        termLabelsSource.getLines()
          .map(_.split("\t").map(_.trim))
          .map(xs => xs(0) -> xs(1))
          .map { case (iri, label) =>
            val updatedIRI = if (iri.startsWith("<") && iri.endsWith(">")) iri.drop(1).dropRight(1) else iri
            val updatedLabel = if (label.startsWith("\"") && label.startsWith("\"")) label.drop(1).dropRight(1) else label
            updatedIRI -> updatedLabel
          }
          .toMap
      }.toOption
    }.getOrElse(Map.empty)
    val edgesWriter = new PrintWriter(options.edgesFile.toFile, StandardCharsets.UTF_8.name())
    val nodesWriter = new PrintWriter(options.nodesFile.toFile, StandardCharsets.UTF_8.name())
    val predicatesWriter = new PrintWriter(options.predicatesFile.toFile, StandardCharsets.UTF_8.name())
    var nextNode = 0
    var nextPredicate = 0
    val nodes = mutable.Map[String, Int]()
    val predicates = mutable.Map[String, Int]()
    val streamWriter = new StreamRDFBase {

      override def triple(triple: Triple): Unit = {
        if (triple.getSubject.isURI && triple.getPredicate.isURI && triple.getObject.isURI) {
          val subjectID = nodes.getOrElseUpdate(triple.getSubject.getURI, {
            val id = nextNode
            nextNode += 1
            val label = if (irisToLabels.isEmpty) "" else s"\t${irisToLabels.getOrElse(triple.getSubject.getURI, "")}"
            nodesWriter.println(s"$id\t${triple.getSubject.getURI}$label")
            id
          })
          val objectID = nodes.getOrElseUpdate(triple.getObject.getURI, {
            val id = nextNode
            nextNode += 1
            val label = if (irisToLabels.isEmpty) "" else s"\t${irisToLabels.getOrElse(triple.getObject.getURI, "")}"
            nodesWriter.println(s"$id\t${triple.getObject.getURI}$label")
            id
          })
          val predicateID = predicates.getOrElseUpdate(triple.getPredicate.getURI, {
            val id = nextPredicate
            nextPredicate += 1
            val label = if (irisToLabels.isEmpty) "" else s"\t${irisToLabels.getOrElse(triple.getPredicate.getURI, "")}"
            predicatesWriter.println(s"$id\t${triple.getPredicate.getURI}$label")
            id
          })
          edgesWriter.println(s"$subjectID\t$predicateID\t$objectID")
        }
      }

      override def quad(quad: Quad): Unit = triple(quad.asTriple())

    }
    RDFDataMgr.parse(streamWriter, options.inputFile.toFile.getPath)
    edgesWriter.close()
    nodesWriter.close()
    predicatesWriter.close()
  }

}
