package i3statusviewer.translator

import mainargs.main
import mainargs.arg
import scala.io.StdIn
import i3statusviewer.IncrementalParser
import i3statusviewer.IncrementalParser.ParseResult

@main
case class Config(
    @arg(positional = true) mode: String
)

def encode() = {

  def parse(buf: String): (String, List[String]) = {
    i3statusviewer.json.parse(buf).toOption.match {
      case None                   => (buf, List())
      case Some((remind, result)) =>
        val (finalRemind, results) = parse(remind)
        (finalRemind, result :: results)
    }
  }

  var buffer = ""
  var printedHeader = false
  while (true) {
    val line = StdIn.readLine()
    buffer += line

    val (nextBuffer, results) = parse(buffer)
    buffer = nextBuffer
    results.foreach { json =>
      if (printedHeader) {
        println(s"${json},")
      } else {
        println(json)
        println("[")
        printedHeader = true
      }
    }
  }
}

def decode() = {
  var parser = IncrementalParser.default
  while (true) {
    val line = StdIn.readLine()
    val (nextParser, results) = parser.next(line)

    parser = nextParser
    results.foreach {
      case ParseResult.Header(body) => println(body)
      case ParseResult.Value(body)  => println(body)
    }
  }
}

def entrypoint(args: Array[String]) = {
  args.lift(0) match {
    case Some("encode") => encode()
    case Some("decode") => decode()
    case Some(mode)     =>
      System.err.println(s"Unknown mode specified: ${mode}")
      sys.exit(1)
    case None =>
      System.err.println("Please specify mode (decode / decode)")
      sys.exit(1)
  }
}
