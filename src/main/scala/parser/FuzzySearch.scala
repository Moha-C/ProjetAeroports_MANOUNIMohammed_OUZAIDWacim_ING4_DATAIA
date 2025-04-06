package parser

object FuzzySearch {
  def levenshtein(a: String, b: String): Int = {
    val dist = Array.tabulate(b.length + 1, a.length + 1) { (j, i) =>
      if (j == 0) i else if (i == 0) j else 0
    }
    for (j <- 1 to b.length; i <- 1 to a.length)
      dist(j)(i) =
        if (b(j - 1) == a(i - 1)) dist(j - 1)(i - 1)
        else List(
          dist(j - 1)(i) + 1,
          dist(j)(i - 1) + 1,
          dist(j - 1)(i - 1) + 1
        ).min
    dist(b.length)(a.length)
  }

  def findClosest(input: String, options: List[String]): Option[String] =
    options.sortBy(levenshtein(input.toLowerCase, _)).headOption
}