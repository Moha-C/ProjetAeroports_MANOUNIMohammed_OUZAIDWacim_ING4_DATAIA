package database

import java.sql.{Connection, DriverManager}
import model.{Airport, Country, Runway}

object Database {
  Class.forName("org.sqlite.JDBC")
  val conn: Connection = DriverManager.getConnection("jdbc:sqlite::memory:")

  def initialize(countries: List[Country], airports: List[Airport], runways: List[Runway]): Unit = {
    val stmt = conn.createStatement()
    stmt.execute("CREATE TABLE countries (code TEXT PRIMARY KEY, name TEXT)")
    stmt.execute("CREATE TABLE airports (id INTEGER PRIMARY KEY, name TEXT, country TEXT)")
    stmt.execute("CREATE TABLE runways (id INTEGER PRIMARY KEY, airport_id INTEGER, surface TEXT, le_ident TEXT)")

    countries.foreach { c =>
      val ps = conn.prepareStatement("INSERT INTO countries VALUES (?, ?)")
      ps.setString(1, c.code)
      ps.setString(2, c.name)
      ps.executeUpdate()
    }

    airports.foreach { a =>
      val ps = conn.prepareStatement("INSERT INTO airports VALUES (?, ?, ?)")
      ps.setInt(1, a.id)
      ps.setString(2, a.name)
      ps.setString(3, a.countryCode)
      ps.executeUpdate()
    }

    runways.foreach { r =>
      val ps = conn.prepareStatement("INSERT INTO runways VALUES (?, ?, ?, ?)")
      ps.setInt(1, r.id)
      ps.setInt(2, r.airportId)
      ps.setString(3, r.surface)
      ps.setString(4, r.leIdent)
      ps.executeUpdate()
    }
  }

  def findCountryByNameOrCode(input: String): Option[String] = {
    val stmt = conn.prepareStatement("SELECT code FROM countries WHERE LOWER(name) LIKE ? OR LOWER(code) = ?")
    stmt.setString(1, s"%${input.toLowerCase}%")
    stmt.setString(2, input.toLowerCase)
    val rs = stmt.executeQuery()
    if (rs.next()) Some(rs.getString("code")) else None
  }

  def getAllCountryNames(): List[String] = {
    val stmt = conn.createStatement()
    val rs = stmt.executeQuery("SELECT name FROM countries")
    Iterator.continually((rs, rs.next())).takeWhile(_._2).map(_._1.getString("name")).toList
  }

  def getAirportsAndRunways(countryCode: String): List[(String, List[String])] = {
    val stmt = conn.prepareStatement("SELECT id, name FROM airports WHERE country = ?")
    stmt.setString(1, countryCode)
    val rs = stmt.executeQuery()
    Iterator.continually((rs, rs.next())).takeWhile(_._2).map(_._1).map { r =>
      val airportId = r.getInt("id")
      val name = r.getString("name")
      val runwayStmt = conn.prepareStatement("SELECT surface FROM runways WHERE airport_id = ?")
      runwayStmt.setInt(1, airportId)
      val rrs = runwayStmt.executeQuery()
      val surfaces = Iterator.continually((rrs, rrs.next())).takeWhile(_._2).map(_._1.getString("surface")).toList
      (name, surfaces)
    }.toList
  }

  def getTopCountries(limit: Int, ascending: Boolean): List[(String, String, Int)] = {
    val order = if (ascending) "ASC" else "DESC"
    val stmt = conn.createStatement()
    val rs = stmt.executeQuery(
      s"""
         |SELECT c.name, c.code, COUNT(a.id) as count
         |FROM airports a
         |JOIN countries c ON a.country = c.code
         |GROUP BY c.code
         |ORDER BY count $order
         |LIMIT $limit
         |""".stripMargin)

    Iterator
      .continually((rs, rs.next()))
      .takeWhile(_._2)
      .map(_._1)
      .map(r => (r.getString("name"), r.getString("code"), r.getInt("count")))
      .toList
  }

  def getRunwaySurfacesPerCountry(): List[(String, String, List[String])] = {
    val stmt = conn.createStatement()
    val rs = stmt.executeQuery(
      """
        SELECT c.name, c.code, GROUP_CONCAT(DISTINCT r.surface) AS surfaces
        FROM countries c
        JOIN airports a ON c.code = a.country
        JOIN runways r ON a.id = r.airport_id
        GROUP BY c.code
      """)

    Iterator
      .continually((rs, rs.next()))
      .takeWhile(_._2)
      .map(_._1)
      .map { r =>
        val name = r.getString("name")
        val code = r.getString("code")
        val surfaces = r.getString("surfaces").split(",").toList
        (name, code, surfaces)
      }.toList
  }

  def getTopRunwayIdentifiers(): List[(String, Int)] = {
    val stmt = conn.createStatement()
    val rs = stmt.executeQuery("SELECT le_ident, COUNT(*) as count FROM runways GROUP BY le_ident ORDER BY count DESC LIMIT 10")
    Iterator.continually((rs, rs.next())).takeWhile(_._2).map(_._1).map(r => (r.getString("le_ident"), r.getInt("count"))).toList
  }
}
