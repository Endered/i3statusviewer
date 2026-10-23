import scala.io.StdIn
import scala.util.chaining.given

enum I3StatusState {
  case ReadHeader
  case ReadBeggingArray
  case ReadValue
  case ReadComma
}

object Main {
  def main(args: Array[String]): Unit = {
    var state = I3StatusState.ReadHeader
    var contents = ""

    def parse(): Boolean = {
      val parsed = state match {
        case I3StatusState.ReadHeader =>
          i3statusviewer.json.parse(contents).tap {
            case Right(_) => state = I3StatusState.ReadBeggingArray
            case _        => ()
          }

        case I3StatusState.ReadBeggingArray =>
          i3statusviewer
            .string("[")
            .surroundedBy(i3statusviewer.jsonBlank)
            .parse(contents)
            .tap {
              case Right(_) => state = I3StatusState.ReadValue
              case _        => ()
            }
        case I3StatusState.ReadValue =>
          i3statusviewer.json.parse(contents).tap {
            case Right(_) => state = I3StatusState.ReadComma
            case _        => ()
          }
        case I3StatusState.ReadComma =>
          i3statusviewer
            .string(",")
            .surroundedBy(i3statusviewer.jsonBlank)
            .parse(contents)
            .tap {
              case Right(_) => state = I3StatusState.ReadValue
              case _        => ()
            }
      }

      parsed match {
        case Left(_) =>
          false
        case Right((remind, parsed)) =>
          println(parsed)
          contents = remind
          true
      }
    }

    while (true) {
      val line = StdIn.readLine()
      contents += line
      while (parse()) {}
    }
  }
}
