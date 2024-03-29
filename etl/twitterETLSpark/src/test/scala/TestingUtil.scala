import java.io.File

import scala.io.Source

import org.scalactic.{Equality, TolerantNumerics}
import org.scalatest.MustMatchers

trait TestingUtil extends MustMatchers {

  /**
    * Uses Spark to load the page ranks from the provided path into a HashMap.
    * @param inputPath Path of the file containing the page ranks.
    * @return HashMap of {node: rank}
    */
  def loadETL(inputPath: String): List[(Int, Double)] = {
    val files = listFiles(inputPath)

    files.flatMap { file =>
      val source = Source.fromFile(file)
      val parsedLines = source.getLines.map { line =>
        val nodes = line.split("\\s+")
        (nodes(0).toInt, nodes(1).toDouble)
      }.toList
      source.close()
      parsedLines
    }.toList
  }

  /**
    * Checks if the input file is a directory.
    * If it is, returns the list of files, else, it returns the single file.
    * @param inputPath the path to the results.
    * @return the list of files.
    */
  def listFiles(inputPath: String): Seq[File] = {
    val file = new File(inputPath)

    if (file.exists() && file.isDirectory)
      file.listFiles.filter(
        file =>
          file.isFile && !file.getName.contains("crc") && !file.getName
            .contains("_SUCCESS") && !file.isHidden)
    else
      Seq(file)
  }

  /**
    * Compares the page ranks of the output and reference files.
    * @param outputPath Path of the output file/directory.
    * @param referencePath Path of the reference file.
    * @return True if page ranks match, false if they don't.
    */
  def compareETL(outputPath: String, referencePath: String): Unit = {
    // Verify if it matches with the reference solution
    val referenceOutput = loadETL(referencePath)
    val outputRDD = loadETL(outputPath)

    referenceOutput.size mustEqual outputRDD.size

    val referenceMap = referenceOutput.toMap
    val outputMap = outputRDD.toMap

    // Check if there are any differences in the reference output and output
    referenceMap.map { case (key, value) =>
      outputMap.contains(key) mustEqual true
      value mustEqual outputMap(key)
    }
  }
}
