/**
  * Created by akshayd on 10/26/2016.
  */

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

/**
  * Created by Akshay Dubey.
  */
object CsvDataInput {
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)
  val sparkSession = SparkSession.builder
    .master("local")
    .appName("CSV Dta Import")
    .config("spark.sql.warehouse.dir", "file:///C:/Users/akshayd/Downloads/Scala_Project/scala-ssh-master/LunatechAssign/spark-warehouse")
    .getOrCreate()

  def main(args: Array[String]): Unit = {
    val dfCountries = sparkSession.
      read.
      format("com.databricks.spark.csv").
      option("header", "true").
      load("file:///C:/Users/akshayd/Downloads/ScalaProject/countries.csv").createOrReplaceTempView("Countries")
    val dfAirports = sparkSession.
      read.
      format("csv").
      option("header", "true").
      csv("file:///C:/Users/akshayd/Downloads/ScalaProject/airports.csv").createOrReplaceTempView("Airports")
    //dfAirports.groupBy("iso_country").count().show(10)
    // val SqlDF = sparkSession.sql("SELECT iso_country As Country, COUNT(*) AS Count FROM Airports WHERE iso_country <> '' GROUP BY iso_country ORDER BY cnt DESC LIMIT 10").show()
    val dfRunways = sparkSession.
      read.option("header", "true").
      csv("file:///C:/Users/akshayd/Downloads/ScalaProject/runways.csv").toDF()
    //println("*****************************************")
    // dfRunways.select("name").show()

    UserInputQuest()

    //val sqlDFC = sparkSession.sql("SELECT "+line+"(*) FROM Countries")
    //sqlDFC.show()
  }

  def UserInput(n: Int): Unit = n match {
    case 1 => DataSearch()
    case 2 => DataReport()
    case _ => UserInputQuest()
  }

  def DataSearch(): Unit = {
    val line = scala.io.StdIn.readLine("Please provide the country Code or Country Name:- ")
    // val SearchRS = sparkSession.sql("SELECT code from Countries WHERE name like concat('%',"+line+ ",'%') OR CODE like concat('%',"+line+",'%')").show()
    val SearchRS = sparkSession.sql("SELECT Distinct(A.name, R.le_ident) from Runways R, Airports A WHERE A.iso_country = (SELECT code from Countries " +
      "WHERE name like concat('%',\'" + line + "\','%') OR " +
      "CODE like concat('%',\'" + line + "\','%'))").collect().foreach(println)
  }

  def DataReport(): Any = {
    println("10 countries with highest number of airports (with count) and countries with lowest number of airports")
    val Rep1 = sparkSession.sql("SELECT iso_country As Country, COUNT(*) AS Count FROM Airports " +
      "WHERE iso_country <> '' GROUP BY iso_country ORDER BY COUNT DESC LIMIT 10").show()
    val line = scala.io.StdIn.readLine("Provide Your Input and Press Enter:- ")

    println("Type of runways (as indicated in \"surface\" column) per country")
    val Rep2 = sparkSession.sql("SELECT Distinct(r.surface), a.iso_country from Runways r inner join" +
      " Airports a on r.airport_ident = a.ident order by a.iso_country, r.surface ").toDF("surface", "iso_country").collect().foreach(println)

    val line1 = scala.io.StdIn.readLine("Provide Your Input and Press Enter:- ")
    println("Print the top 10 most common runway identifications (indicated in \"le_ident\" column) ")
    val Rep3 = sparkSession.sql("SELECT le_ident, count(*) from Runways " +
      " GROUP BY le_ident ORDER BY count(*) DESC LIMIT 10 ").collect().foreach(println)

  }

  def UserInputQuest() = {
    println("Please provide your input:-")
    println("1. Query")
    println("2. Report")
    val line = scala.io.StdIn.readLine("Provide Your Input and Press Enter:- ")
    UserInput(line.toInt)
  }
}
