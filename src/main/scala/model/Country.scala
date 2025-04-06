package model

case class Country(code: String, name: String)

object Country {
  def fromCSV(line: Array[String]): Country =
    Country(line(1), line(2))
}
