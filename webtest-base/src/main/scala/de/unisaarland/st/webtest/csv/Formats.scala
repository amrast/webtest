package de.unisaarland.st.webtest.csv

trait DefaultCSVFormat extends CSVFormat {

  val delimiter: Char = ','

  val quoteChar: Char = '"'

  val escapeChar: Char = '\\'

  val lineTerminator: String = "\r\n"

  val quoting: Quoting = QUOTE_MINIMAL

  val treatEmptyLineAsNil: Boolean = false

}

trait TSVFormat extends CSVFormat {

  val delimiter: Char = '\t'

  val quoteChar: Char = '"'

  val escapeChar: Char = '\\'

  val lineTerminator: String = "\r\n"

  val quoting: Quoting = QUOTE_NONE

  val treatEmptyLineAsNil: Boolean = false

}

