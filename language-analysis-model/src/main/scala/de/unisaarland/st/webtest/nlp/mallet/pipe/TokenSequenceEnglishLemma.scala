package de.unisaarland.st.webtest.nlp.mallet.pipe

import cc.mallet.pipe.Pipe
import cc.mallet.types.Instance
import cc.mallet.types.Token
import cc.mallet.types.TokenSequence
import edu.washington.cs.knowitall.morpha.MorphaStemmer
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import scala.collection.JavaConverters.asScalaIteratorConverter

/**
  * <p>
  * Mallet pipe for retrieving the lemmas of a given text, using
  * <a href="http://www.informatics.sussex.ac.uk/research/groups/nlp/carroll/morph.html"><bold>MORPHA STEMMER</bold></a>.
  * </p>
  * <p>
  * A fast and robust morphological analyser for English based on finite-state
  * techniques that returns the lemma and inflection type of a word, given the word
  * form and its part of speech. (The latter is optional but accuracy is degraded
  * if it is not present).
  * </p>
  */
class TokenSequenceEnglishLemma extends Pipe with Serializable {

  @SerialVersionUID(1L)
  private final val CURRENT_SERIAL_VERSION = 0

  object TokenSequenceEnglishLemma {
  }

  override def pipe(carrier: Instance): Instance = {
    carrier.getData match {
      case ts: TokenSequence =>
        ts.iterator().asScala.foreach( t => t.setText(MorphaStemmer.stem(t.getText)))
        carrier
      case _ =>  throw new IllegalArgumentException(s"${getClass.getSimpleName} expects a Tokensequence, found a ${carrier.getData.getClass}")
    }
  }

  @SerialVersionUID(1L)
  private def writeObject(out: ObjectOutputStream) {
    out.writeInt(0)
  }

  private def readObject(in: ObjectInputStream) {
    val version = in.readInt
  }


}
