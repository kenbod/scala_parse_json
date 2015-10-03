package edu.colorado.cs.epic

import java.nio.file.{Files,Path,Paths}
import java.io.{File,PrintWriter}

import net.liftweb.json._
import net.liftweb.json.Serialization.write

import scala.sys.process._

// input format
case class Parts(first: String, last: String)

// output format
case class SparkStudent(name: String)

object ParseJson {

  private def error(msg: String) = {
    println(msg)
    System.exit(1)
  }

  private def exists(file : Path) = {
    Files.exists(file)
  }

  private def check(file: Path) = {
    if (!exists(file)) {
      error(s"File: <$file> does not exist.")
    }
  }

  private def readInput(input: Path) = {

    val handle = io.Source.fromFile(input.toFile)
    val data   = handle.mkString
    handle.close // close input file

    // create an array of containing each line of the input file
    val records = data.split("\n")

    // convert each line of the input file into a case class
    // this call to map uses the lift framework to parse the json
    // the first line is required by lift to parse json structures
    // the second line actually does the parsing
    // the third line converts the json into the input case class
    val names   = records.map {
      line =>
        implicit val formats = DefaultFormats
        val jsonData = parse(line) 
        jsonData.extract[Parts]
    }

    // if you want to see the output of the map, uncomment this line
    // names.foreach { println }

    // return names to main program
    names

  }

  private def writeOutput(output: Path, students: Array[SparkStudent]) = {

    // need to convert SparkStudent data into JSON strings
    // the first line is still required by lift
    // the second line converts a SparkStudent into a JSON string
    // uncomment third line to see the string produced by lift
    // the fourth line returns the string, so map can do its job
    val jsonData = students.map {
      student =>
        implicit val formats = DefaultFormats
        val data = write(student)
        // println(data)
        data
    }

    // create a printwriter
    val pw = new PrintWriter(new File(output.toString))
    
    // write each line of JSON data to the output file
    jsonData.foreach { line => pw.println(line) }

    // close the printwriter
    pw.close
  }

  def main(args: Array[String]) {
    if (args.length != 1) {
      error("Usage: ParseJson <input.json>")
    }

    var inputPath  = Paths.get(args(0)).toAbsolutePath.normalize
    var outputPath = Paths.get("output.json").toAbsolutePath.normalize

    check(inputPath)

    println(s"Input : $inputPath")
    println(s"Output: $outputPath")

    // read the input file to create array of Parts objects
    val names = readInput(inputPath)

    // convert the input data from Parts objects to SparkStudent objects
    val students = names.map {
      part => SparkStudent(part.first + " " + part.last)
    }

    // write the SparkStudent data to an output file
    writeOutput(outputPath, students)

    // all done
    println("Conversion from input to output complete.")

  }

}
