package de.unisaarland.st.webtest.csv.reader

import scala.io.Source

class SourceLineReader(source: Source) extends LineReader {


  override def readLineWithTerminator: String = {
    val sb = new StringBuilder

    var noEscape = true

    while(source.hasNext && noEscape) {
      val c = source.next()

      sb.append(c)

      if ((c == '\n') || (c == '\u2028') || (c == '\u2029') || (c == '\u0085')) {
        noEscape = false
      } else if (c == '\r') {
        if (!source.hasNext) {
          noEscape = false
        } else {
          val c1 = source.next()
          sb.append(c1)
          if (c1 == '\n') {
            noEscape = false
          }
        }
      }

    }

    sb.toString

  }

  override def close() = source.close()
}
