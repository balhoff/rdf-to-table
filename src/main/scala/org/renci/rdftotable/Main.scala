package org.renci.rdftotable

import caseapp._
import org.apache.jena.graph.Triple
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.system.StreamRDF
import org.apache.jena.sparql.core.Quad
import org.apache.jena.sys.JenaSystem

import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import scala.collection.mutable

case class Options(
                    inputFile: Path,
                    edgesFile: Path,
                    nodesFile: Path,
                    predicatesFile: Path
                  )

object Main extends CaseApp[Options] {

  JenaSystem.init()

  def run(options: Options, arg: RemainingArgs): Unit = {
    val edgesWriter = new PrintWriter(options.edgesFile.toFile, StandardCharsets.UTF_8.name())
    val nodesWriter = new PrintWriter(options.nodesFile.toFile, StandardCharsets.UTF_8.name())
    val predicatesWriter = new PrintWriter(options.predicatesFile.toFile, StandardCharsets.UTF_8.name())
    var nextNode = 0
    var nextPredicate = 0
    val nodes = mutable.Map[String, Int]()
    val predicates = mutable.Map[String, Int]()
    val streamWriter = new StreamRDF {

      override def triple(triple: Triple): Unit = {
        if (triple.getSubject.isURI && triple.getPredicate.isURI && triple.getObject.isURI) {
          val subjectID = nodes.getOrElseUpdate(triple.getSubject.getURI, {
            val id = nextNode
            nextNode += 1
            nodesWriter.println(s"$id\t${triple.getSubject.getURI}")
            id
          })
          val objectID = nodes.getOrElseUpdate(triple.getObject.getURI, {
            val id = nextNode
            nextNode += 1
            nodesWriter.println(s"$id\t${triple.getObject.getURI}")
            id
          })
          val predicateID = predicates.getOrElseUpdate(triple.getPredicate.getURI, {
            val id = nextPredicate
            nextPredicate += 1
            predicatesWriter.println(s"$id\t${triple.getPredicate.getURI}")
            id
          })
          edgesWriter.println(s"$subjectID\t$predicateID\t$objectID")
        }
      }

      override def quad(quad: Quad): Unit = triple(quad.asTriple())

      override def base(base: String): Unit = ()

      override def prefix(prefix: String, iri: String): Unit = ()

      override def start(): Unit = ()

      override def finish(): Unit = ()
    }
    RDFDataMgr.parse(streamWriter, options.inputFile.toFile.getPath)
    edgesWriter.close()
    nodesWriter.close()
    predicatesWriter.close()
  }

}
