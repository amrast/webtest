package de.unisaarland.st.webtest.csv

sealed trait Quoting extends Product with Serializable
case object QUOTE_ALL extends Quoting
case object QUOTE_MINIMAL extends Quoting
case object QUOTE_NONE extends Quoting
case object QUOTE_NONNUMERIC extends Quoting
