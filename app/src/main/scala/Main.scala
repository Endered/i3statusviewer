import scala.io.StdIn
import scala.util.chaining.given
import i3statusviewer.IncrementalParser
import i3statusviewer.IncrementalParser.ParseResult
import scala.scalanative.runtime.filename
import java.nio.file.Paths

enum I3StatusState {
  case ReadHeader
  case ReadBeggingArray
  case ReadValue
  case ReadComma
}

object Main {
  def main(args: Array[String]): Unit = {
    val entries = Map(
      "i3statusviewer-translator" -> i3statusviewer.translator.entrypoint,
      "i3statusviewer-viewer" -> i3statusviewer.viewer.entrypoint
    )
    val appName = Paths.get(filename).getFileName().toString()

    entries.get(appName).match {
      case None =>
        System.err.println(s"Unknown argv[0] = ${appName}")
        System.err.println("Please rename it to one of the below")
        System.err.println(entries.keys.toList.sorted.mkString(", "))
      case Some(entry) => entry(args)
    }
  }
}
