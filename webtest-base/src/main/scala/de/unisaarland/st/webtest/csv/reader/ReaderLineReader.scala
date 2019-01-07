package de.unisaarland.st.webtest.csv.reader

import java.io.{BufferedReader, Reader}

class ReaderLineReader(reader: Reader) extends LineReader {

  private val bufferedReader = new BufferedReader(reader)

//  @throws[IOException]
//  def readLineWithTerminator: String = {
//    val sb = new StringBuilder
//
//    var c = bufferedReader.read()
//
//    while (c != -1) {
//      sb.append(c.toChar)
//      if (c == '\n' || c == '\u2028' || c == '\u2029' || c == '\u0085') {
//        c = -1
//      }
//      else if (c == '\r') {
//        bufferedReader.mark(1)
//        c = bufferedReader.read()
//
//        if ( c == '\n') {
//          sb.append('\n')
//        } else {
//          bufferedReader.reset()
//        }
//
//      }
//    }
//
//    sb.toString()
//
//  }

  import java.io.IOException

  @throws[IOException]
  def readLineWithTerminator: String = {
    val sb = new StringBuilder
    var continue = true
    do {
      var c = bufferedReader.read
      if (c == -1) if (sb.isEmpty) return null
      else continue = false //todo: break is not supported
      sb.append(c.toChar)
      if (c == '\n' || c == '\u2028' || c == '\u2029' || c == '\u0085') continue = false //todo: break is not supported
      if (c == '\r') {
        bufferedReader.mark(1)
        c = bufferedReader.read
        if (c == -1) continue = false //todo: break is not supported
        else if (c == '\n') sb.append('\n')
        else bufferedReader.reset
        continue = false //todo: break is not supported
      }
    } while ( {
      continue
    })
    sb.toString
  }


  @throws[IOException]
  def close(): Unit = {
    bufferedReader.close()
    reader.close()
  }

}
