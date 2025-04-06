package model

case class Airport(id: Int, name: String, countryCode: String)

object Airport {
  def fromCSV(line: Array[String]): Airport =
    Airport(line(0).toInt, line(3), line(8)) 
}
