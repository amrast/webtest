package de.unisaarland.st.webtest.nlp.mallet.pipe

import java.io.{ObjectInputStream, ObjectOutputStream}

import cc.mallet.pipe.Pipe
import cc.mallet.types.Instance

class LineBreakRemover extends Pipe with Serializable {

  override def pipe(inst: Instance): Instance = {
    inst.getData match {
      case ch: CharSequence =>
        inst.setData(ch.toString.trim.replaceAll("\\r\\n|\\r|\\n| +", " "))
        inst
      case _ => throw new IllegalArgumentException(s"${getClass.getSimpleName} expects a CharSequence, found a ${inst.getData.getClass}")
    }
  }

  private def writeObject(out: ObjectOutputStream): Unit =  out.writeInt(LineBreakRemover.CURRENT_SERIAL_VERSION)
  private def readObject(in: ObjectInputStream): Unit = { val version = in.readInt}

}

object LineBreakRemover {

  private final val serialVersionUID = 42L
  private final val CURRENT_SERIAL_VERSION = 0

}