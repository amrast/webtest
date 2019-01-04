package de.unisaarland.st.webtest.api

import play.api.libs.json.Json

/**
  * Using a probabilistic topic model, a topic description label describes the underlying unknown topic.
  * @param word a label describing the topic
  * @param weight the weight this label has
  */
case class TopicDescription(word: String, weight: Double) {
  override def toString: String = s"$word ($weight)"
}

object TopicDescription {
  implicit val topicDescriptionFormat = Json.format[TopicDescription]
}

case class Topic(descriptions: Seq[TopicDescription], weight: Double) {
  override def toString: String = "%.3f\t%s".format(weight, descriptions.mkString(" "))
}

object Topic {

  implicit val topicOrdering = new Ordering[Topic] {
    override def compare(x: Topic, y: Topic): Int = y.weight.compare(x.weight)
  }

  implicit val topicFormat = Json.format[Topic]
}



/**
  * A sorted list of topics describing the content of a document.
  */
case class TopicModel(topics: Seq[Topic]) {

  val topicList: Seq[Topic] = topics.sorted

  override def toString: String = topicList.zipWithIndex.map{case (t, index) => s"$index\t${t.toString}"}.mkString(System.lineSeparator())
}

object TopicModel {
  implicit val topicModelFormat = Json.format[TopicModel]
}