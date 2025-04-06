package parser

import scala.io.Source
import model.{Country, Airport, Runway}

object CSVParser {

  def parseCountries(path: String): List[Country] =
    Source.fromFile(path)
      .getLines()
      .drop(1)
      .map(_.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"))
      .filter(_.length > 2)
      .map(Country.fromCSV)
      .toList

  def parseAirports(path: String): List[Airport] =
    Source.fromFile(path)
      .getLines()
      .drop(1)
      .map(_.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"))
      .filter(_.length > 3)
      .map(Airport.fromCSV)
      .toList

  def parseRunways(path: String): List[Runway] =
    Source.fromFile(path)
      .getLines()
      .drop(1)
      .map(_.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"))
      .filter(_.length > 8)
      .map(Runway.fromCSV)
      .toList
}
