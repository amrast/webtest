package de.unisaarland.st.webtest.csv.reader

import java.io.Closeable

trait LineReader extends Closeable {

  def readLineWithTerminator: String

}
