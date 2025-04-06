import database.Database
import model.{Airport, Runway, Country}
import parser.CSVParser
import ui.CLI

object Main {
  def main(args: Array[String]): Unit = {
    val countries = CSVParser.parseCountries("countries.csv")
    val airports = CSVParser.parseAirports("airports.csv")
    val runways = CSVParser.parseRunways("runways.csv")

    Database.initialize(countries, airports, runways)

    CLI.ui(args)
  }
}
