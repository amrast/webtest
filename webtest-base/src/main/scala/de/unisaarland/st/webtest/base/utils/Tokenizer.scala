package de.unisaarland.st.webtest.base.utils

class Tokenizer {

  private val regex = "(\\d+\\.\\d+)|(\\d+,\\d+)|[a-zA-Z0-9 ]".r
  private val regexWithOutNumbers = "[^a-zA-Z ]"

  def sanitizeString(str: String): String = str.replaceAll(regexWithOutNumbers, "").replaceAll("\\s+", " ").trim

  def tokenize(str: String): Seq[String] = sanitizeString(str).split("\\s+")

}

object Tokenizer extends Tokenizer
