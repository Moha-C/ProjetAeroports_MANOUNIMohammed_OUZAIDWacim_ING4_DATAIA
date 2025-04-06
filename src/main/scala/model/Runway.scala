package model

case class Runway(id: Int, airportId: Int, surface: String, leIdent: String)

object Runway {
  def fromCSV(line: Array[String]): Runway =
    Runway(line(0).toInt, line(1).toInt, line(5), line(8))
}
