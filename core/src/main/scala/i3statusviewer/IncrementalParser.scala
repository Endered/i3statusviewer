package i3statusviewer

import cats.Monad
import i3statusviewer.IncrementalParser.State
import i3statusviewer.IncrementalParser.ParseResult

case class IncrementalParser(buffer: String, state: State) {
  def parse1(): Option[(IncrementalParser, Option[ParseResult])] = {
    val result: Option[(String, Option[ParseResult], State)] = state match {
      case State.ReadBeggingArray =>
        i3statusviewer
          .string("[")
          .surroundedBy(i3statusviewer.jsonBlank)
          .parse(buffer)
          .toOption
          .map { case (remind, _) =>
            (remind, None, State.ReadValue)
          }
      case State.ReadComma =>
        i3statusviewer
          .string(",")
          .surroundedBy(i3statusviewer.jsonBlank)
          .parse(buffer)
          .toOption
          .map { case (remind, _) =>
            (remind, None, State.ReadValue)
          }
      case State.ReadHeader =>
        i3statusviewer.json.parse(buffer).toOption.map {
          case (remind, result) =>
            (remind, Some(ParseResult.Header(result)), State.ReadBeggingArray)
        }
      case State.ReadValue =>
        i3statusviewer.json.parse(buffer).toOption.map {
          case (remind, result) =>
            (remind, Some(ParseResult.Value(result)), State.ReadComma)
        }
    }

    result.map { (remind, result, state) =>
      (IncrementalParser(remind, state), result)
    }
  }

  def parse(): (IncrementalParser, List[ParseResult]) = {
    parse1() match {
      case None =>
        (this, List())
      case Some(nextParser, result) =>
        val (lastParser, resulsts) = nextParser.parse()
        (lastParser, result.toList ++ resulsts)
    }
  }

  def append(s: String): IncrementalParser =
    IncrementalParser(buffer + s, state)

  def next(s: String): (IncrementalParser, List[ParseResult]) =
    append(s).parse()

  def bufferSize: Int = buffer.size
}

object IncrementalParser {
  enum State {
    case ReadHeader
    case ReadBeggingArray
    case ReadValue
    case ReadComma
  }

  enum ParseResult {
    case Header(body: String)
    case Value(body: String)
    case Nop
  }
}
