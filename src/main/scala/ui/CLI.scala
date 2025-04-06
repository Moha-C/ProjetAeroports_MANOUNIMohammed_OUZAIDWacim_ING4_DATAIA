package ui

import parser.FuzzySearch
import database.Database
import scala.io.StdIn

object CLI {
  def ui(args: Array[String]): Unit = {
    var continue = true
    while (continue) {
      println("\n=== MAIN MENU ===")
      println("1. Query")
      println("2. Reports")
      println("3. Quit")
      print("Choose an option: ")

      StdIn.readLine() match {
        case "1" => query()
        case "2" => reports()
        case "3" =>
          println("Goodbye!")
          continue = false
        case _ =>
          println("Invalid choice. Please try again.")
      }
    }
  }

  def query(): Unit = {
    println("Enter country name or code:")
    val input = StdIn.readLine()
    println(s"[DEBUG] Recherche de : $input")

    val matched = Database.findCountryByNameOrCode(input).orElse {
      val allCountries = Database.getAllCountryNames()
      println(s"[DEBUG] Pays disponibles (extrait) : ${allCountries.take(5).mkString(", ")}...")
      FuzzySearch.findClosest(input, allCountries).flatMap(Database.findCountryByNameOrCode)
    }

    matched match {
      case Some(code) =>
        println(s"[DEBUG] Pays trouvé, code : $code")
        val data = Database.getAirportsAndRunways(code)
        println(s"[DEBUG] ${data.length} aéroports trouvés")
        data.foreach { case (airport, runways) =>
          println(s"\nAirport: $airport")
          runways.foreach(r => println(s"  Runway surface: $r"))
        }
      case None => println("No country matched.")
    }
  }

  def reports(): Unit = {
    println("\n1. 10 countries with most airports")
    println("2. 10 countries with least airports")
    println("3. Runway types per country")
    println("4. Top 10 most common runway identifiers")
    print("Choose a report: ")
    StdIn.readLine() match {
      case "1" =>
        println("\nTop 10 countries with most airports:")
        Database.getTopCountries(10, ascending = false).foreach {
          case (name, code, count) => println(s"$name ($code): $count airports")
        }
      case "2" =>
        println("\nTop 10 countries with least airports:")
        Database.getTopCountries(10, ascending = true).foreach {
          case (name, code, count) => println(s"$name ($code): $count airports")
        }
      case "3" =>
  	println("\nRunway types per country:")
  	Database.getRunwaySurfacesPerCountry().foreach {
    	  case (name, code, surfaces) =>
      		println(s"$name ($code): ${surfaces.mkString(", ")}")
  	}
      case "4" =>
        println("\nTop 10 most common runway identifiers:")
        Database.getTopRunwayIdentifiers().foreach {
          case (id, count) => println(s"$id: $count occurrences")
        }
      case _ => println("Invalid report choice")
    }
  }
}
