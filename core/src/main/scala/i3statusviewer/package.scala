import cats.parse.Numbers
import cats.parse.Parser
import cats.parse.Parser.stringIn
import cats.parse.Parser
import cats.parse.strings.Json
import cats.parse.Rfc5234
import cats.parse.Parser0

package object i3statusviewer {

  def string(s: String): Parser[String] = Parser.string(s).as(s)
  def commaSeparated(
      element: Parser[String]
  ): Parser[String] = {
    for {
      first <- element.blankAfter
      others <- (string(",").blankAfter cat element).rep0.string
    } yield first ++ others
  }

  lazy val jsonBlank: Parser0[String] =
    (Rfc5234.wsp | Rfc5234.lf).as("").rep0.string
  lazy val jsonNumber: Parser[String] = Numbers.jsonNumber
  lazy val jsonBoolean: Parser[String] = stringIn(Seq("true", "false"))
  lazy val jsonString: Parser[String] =
    Json.delimited.parser.map("\"" + _ + "\"")
  lazy val jsonValue = Parser.recursive[String] { recurse =>
    val jsonArray: Parser[String] =
      string("[").blankAfter cat
        commaSeparated(recurse) cat
        string("]").blankBefore
    val jsonMapElement =
      jsonString cat string(":").blankSorround cat recurse
    val jsonMap: Parser[String] =
      string("{").blankAfter cat
        commaSeparated(jsonMapElement) cat
        string("}").blankBefore

    Parser.oneOf(List(jsonNumber, jsonBoolean, jsonArray, jsonMap, jsonString))
  }

  lazy val json = jsonValue.blankSorround

  extension (p1: Parser[String]) {
    infix def cat(p2: => Parser[String]): Parser[String] = (p1 ~ p2).string
    def blankSorround: Parser[String] = p1.surroundedBy(jsonBlank)
    def blankBefore = jsonBlank.with1 *> p1
    def blankAfter = p1 <* jsonBlank
  }
}
