import munit.FunSuite
import i3statusviewer.IncrementalParser
import i3statusviewer.IncrementalParser.State
import i3statusviewer.IncrementalParser.ParseResult

class IncrementalParserTest extends FunSuite {
  test("parse header") {
    val parser = IncrementalParser("{}", State.ReadHeader)

    val (lastParser, results) = parser.parse()

    assertEquals(lastParser.state, State.ReadBeggingArray)
    assertEquals(lastParser.buffer, "")
    assertEquals(results, List(ParseResult.Header("{}")))
  }

  test("parse begging array") {
    val parser = IncrementalParser("[", State.ReadBeggingArray)

    val (lastParser, results) = parser.parse()

    assertEquals(lastParser.state, State.ReadValue)
    assertEquals(lastParser.buffer, "")
    assertEquals(results, List())
  }

  test("parse value") {
    val parser = IncrementalParser("{}", State.ReadValue)

    val (lastParser, results) = parser.parse()

    assertEquals(lastParser.state, State.ReadComma)
    assertEquals(lastParser.buffer, "")
    assertEquals(results, List(ParseResult.Value("{}")))
  }

  test("parse comma") {
    val parser = IncrementalParser(",", State.ReadComma)

    val (lastParser, results) = parser.parse()

    assertEquals(lastParser.state, State.ReadValue)
    assertEquals(lastParser.buffer, "")
    assertEquals(results, List())
  }

  test("read fullset") {
    val parser = IncrementalParser("""{"a":1}[{"b":42},""", State.ReadHeader)

    val (lastParser, results) = parser.parse()

    assertEquals(lastParser.state, State.ReadValue)
    assertEquals(lastParser.buffer, "")
    assertEquals(
      results,
      List(
        ParseResult.Header("""{"a":1}"""),
        ParseResult.Value("""{"b":42}""")
      )
    )
  }

  test("read uncompleted value") {
    val parser = IncrementalParser("""{"a":1}[{"b":""", State.ReadHeader)

    val (lastParser, results) = parser.parse()

    assertEquals(lastParser.state, State.ReadValue)
    assertEquals(lastParser.buffer, """{"b":""")
    assertEquals(
      results,
      List(
        ParseResult.Header("""{"a":1}""")
      )
    )
  }

  test("next") {
    val parser0 = IncrementalParser("", State.ReadHeader)

    val (parser1, results1) = parser0.next("""{"a":""")
    assertEquals(parser1.state, State.ReadHeader)
    assertEquals(parser1.buffer, """{"a":""")
    assertEquals(results1, List())

    val (parser2, results2) = parser1.next("""1""")
    assertEquals(parser2.state, State.ReadHeader)
    assertEquals(parser2.buffer, """{"a":1""")
    assertEquals(results2, List())

    val (parser3, results3) = parser2.next(""","b":2} """)
    assertEquals(parser3.state, State.ReadBeggingArray)
    assertEquals(parser3.buffer, "")
    assertEquals(results3, List(ParseResult.Header("""{"a":1,"b":2}""")))
  }
}
