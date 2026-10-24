package i3statusviewer.viewer

import io.circe.Decoder
import i3statusviewer.IncrementalParser
import i3statusviewer.IncrementalParser.ParseResult
import layoutz.LayoutzApp
import layoutz.Cmd
import layoutz.Sub
import layoutz.Element
import layoutz.Key
import scala.io.StdIn
import scala.sys.process.ProcessLogger
import scala.util.Using
import scala.util.Try
import mainargs.main
import mainargs.arg
import mainargs.Parser

@main
case class Config(
    @arg(
      name = "i3status-command",
      short = 'c',
      doc = "i3status like command to spawn (default `i3status`)"
    ) i3statusCommand: String = "i3status"
)

case class Status(
    name: String,
    fullText: String
)

object parser {

  private case class StatusForParser(
      name: Option[String],
      full_text: String
  ) derives Decoder

  private var innerParser = IncrementalParser.default
  private def next(str: String): List[ParseResult] = {
    val (nextParser, results) = innerParser.next(str)
    innerParser = nextParser
    results
  }

  private var currentStatuses_ : Option[Seq[Status]] = None
  def currentStatuses = currentStatuses_

  def startUpdating(commandName: String): Unit = {
    def stdout(s: String) = {
      val results = next(s)
      results.foreach {
        case ParseResult.Value(body) =>
          val parsed = io.circe.parser.decode[Seq[StatusForParser]](body)
          parsed.foreach { statuses =>
            val trimmedStatus = statuses.collect {
              case StatusForParser(name = Some(name), full_text = full_text) =>
                Status(name = name, fullText = full_text)
            }
            currentStatuses_ = Some(trimmedStatus)
          }
        case _ => ()
      }
    }
    def stderr(s: String) = ()

    new Thread {
      override def run(): Unit = {
        sys.process.Process(commandName).!(ProcessLogger(stdout, stderr))
      }
    }.start
  }
}

enum State {
  case WaitValue
  case Top(statuses: Seq[Status])
}

enum Message {
  case Tick
}

object App extends LayoutzApp[State, Message] {
  import layoutz.*

  override def init: (State, Cmd[Message]) = State.WaitValue
  override def subscriptions(state: State): Sub[Message] =
    Sub.time.everyMs(100, Message.Tick)
  override def update(msg: Message, state: State): (State, Cmd[Message]) = {
    msg.match { case Message.Tick =>
      parser.currentStatuses.fold(state)(statuses => State.Top(statuses))
    }
  }
  override def view(state: State): Element = {
    state.match {
      case State.WaitValue =>
        layout(
          "Waiting for output"
        )
      case State.Top(statuses) =>
        table(
          headers = Seq("name", "full_text"),
          rows = statuses.map { status =>
            Seq(status.name, status.fullText)
          }
        )
    }
  }
}

def entrypoint(args: Array[String]): Unit = {
  val config = Parser[Config].constructOrExit(args)
  parser.startUpdating(config.i3statusCommand)
  App.run(
    quitKey = Key.Char('q'),
  )
  sys.exit()
}
