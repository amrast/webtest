package de.unisaarland.st.webtest.base.dom

import de.unisaarland.st.webtest.base.{Logging, TryThat}
import org.openqa.selenium.By
import org.openqa.selenium.By.ByXPath
import play.api.libs.json._

import scala.collection.mutable
import scalaz.{Failure, Success}


class ElementDataWrites extends Writes[ElementData] {
  implicit val elementDataSeqWrites = Writes.traversableWrites[ElementData](this)

  override def writes(o: ElementData): JsValue = Json.obj(
    "nodeName" -> Json.toJson(o.nodeName),
    "attributeMap" -> Json.toJson(o.attributes),
    "eventHandlers" -> Json.toJson(o.eventHandlers),
    "children" -> Json.toJson[Seq[ElementData]](o.children.toSeq)(elementDataSeqWrites),
    "relativeXPath" -> Json.toJson(o.getRelativeXPath.expression),
    "fullXPath" -> Json.toJson(o.getFullXpath.expression),
    "readOnly" -> Json.toJson(o.readOnly),
    "disabled" -> Json.toJson(o.disabled),
    "zindexInput" -> Json.toJson(o.zIndexInput),

    // position
    "relX" -> Json.toJson(o.x),
    "relY" -> Json.toJson(o.y),
    "height" -> Json.toJson(o.height),
    "width" -> Json.toJson(o.width),
    "realX1" -> Json.toJson(o.x1real),
    "realY1" -> Json.toJson(o.y1real),
    "realX2" -> Json.toJson(o.x2real),
    "realY2" -> Json.toJson(o.y2real),
    "visibleDimensions" -> Json.toJson(o.visibleDimensions),

    // CSS
    "overflowX" -> Json.toJson(o.overflowX),
    "overflowY" -> Json.toJson(o.overflowY),
    "zindex" -> Json.toJson(o.getEffectiveZIndex),
    "position" -> Json.toJson(o.cssPosition),
    "display" -> Json.toJson(o.display),
    "backgroundColor" -> Json.toJson(o.backgroundColor),
    "backgroundImage" -> Json.toJson(o.backgroundImage),
    "visibility" -> Json.toJson(o.visibility),
    "cssFloat" -> Json.toJson(o.cssFloat),
    "cssFloatEffective" -> Json.toJson(o.effectiveFloat),
    "cssClip" -> Json.toJson(o.cssClip),
    "color" -> Json.toJson(o.color),
    "opacity" -> Json.toJson(o.opacity),
    "opacityEffective" -> Json.toJson(o.getEffectiveOpacity()),
    "text" -> Json.toJson[String](o.getTextContent()),
    "content" -> Json.toJson(o.content),
    "outlineStyle" -> Json.toJson(o.outlineStyle),
    "boxShadow" -> Json.toJson(o.boxShadow),
    "textIndent" -> Json.toJson(o.textIndent),

    "borderColor" -> Json.toJson(o.borderColor),
    "borderStyle" -> Json.toJson(o.borderStyle),
    "borderWidth" -> Json.toJson(o.borderWidth),
    "borderImage" -> Json.toJson(o.borderImage),

    // methods
    "isElementVisible" -> Json.toJson(o.isElementVisible()),
    "hasNonZeroArea" -> Json.toJson(o.hasNonZeroArea),
    "isMultilineInlineElement" -> Json.toJson(o.isMultilineInlineElement),
    "isPseudoElement" -> Json.toJson(o.isPseudoElement),
    "hasTextContent" -> Json.toJson(o.hasTextContent),
    "hasTextContentVisible" -> Json.toJson(o.hasVisibleText(None)),
    "hasVisibleStylingProperties" -> Json.toJson(o.hasVisibleStylingProperties(None)),
    "borderHasVisibleStyling" -> Json.toJson(o.hasVisibleBorder(None))
  )
}

class ElementDataReads extends Reads[ElementData] {

  implicit lazy val elementDataSeqReads = Reads.seq(this)


  override def reads(json: JsValue): JsResult[ElementData] = TryThat.protect {
    val nodeName = (json \ "nodeName").as[String]
    val attributeMap = (json \ "attributeMap").as[Map[String, Attribute]].map(x => x._2.name -> x._2.value)
    val handlers = (json \ "eventHandlers").as[Seq[EventHandler]]
    val relativeXPath = new XPathExpression((json \ "relativeXPath").as[String])
    val fullXPath = new XPathExpression((json \ "fullXPath").as[String])

    val readOnly = (json \ "readOnly").as[String]
    val disabled = (json \ "disabled").as[String]

    // position
    val relX = (json \ "relX").as[Int]
    val relY = (json \ "relY").as[Int]
    val height = (json \ "height").as[Int]
    val width = (json \ "width").as[Int]
    val realX1 = (json \ "realX1").asOpt[Int]
    val realY1 = (json \ "realY1").asOpt[Int]
    val realX2 = (json \ "realX2").asOpt[Int]
    val realY2 = (json \ "realY2").asOpt[Int]
    val visibleDimensions = (json \ "visibleDimensions").asOpt[Rectangle]

    // CSS
    val overflowX = (json \ "overflowX").as[String]
    val overflowY = (json \ "overflowY").as[String]
    val zindex = (json \ "zindex").asOpt[Int]
    val zindexInput = (json \ "zindexInput").as[String]
    val position = (json \ "position").as[String]
    val display = (json \ "display").as[String]
    val backgroundColor = (json \ "backgroundColor").as[String]
    val bgImage = (json \ "backgroundImage").as[String]
    val visibility = (json \ "visibility").as[String]
    val cssFloat = (json \ "cssFloat").as[String]
    val cssFloatEff = (json \ "cssFloatEffective").as[String]
    val cssClip = (json \ "cssClip").asOpt[String]
    val color = (json \ "color").as[String]
    val opacity = (json \ "opacity").as[String]
    val text = (json \ "text").as[String]
    val content = (json \ "content").as[String]
    val outlineStyle = (json \ "outlineStyle").asOpt[String]
    val boxShadow = (json \ "boxShadow").asOpt[String]
    val textIndent = (json \ "textIndent").asOpt[String]

    val borderColor = (json \ "borderColor").as[String]
    val borderStyle = (json \ "borderStyle").as[String]
    val borderWidth = (json \ "borderWidth").as[String]
    val borderImage = (json \ "borderImage").as[String]
    val children = (json \ "children").as[Seq[ElementData]]

    val ele = new ElementData(None, nodeName, attributeMap, relX, relY, width, height, realX1, realY1, realX2, realY2, zindexInput,
      display, visibility, position, cssFloat, readOnly, disabled, overflowX, overflowY, backgroundColor, bgImage, borderColor,
      borderImage, borderStyle, borderWidth, color, content, opacity, outlineStyle, cssClip, boxShadow, textIndent,
      handlers, visibleDimensions)
    ele.appendTextContent(text)

    ele.xpath = Some(fullXPath)

    children.foreach(c => {
      c.parent = Some(ele)
      ele.addChild(c)
    })

    Success(ele)
  } match {
    case Success(elementData) => JsSuccess(elementData)
    case Failure(e) => JsError(e.getMessage)
  }
}

object ElementData {
  implicit val elementDataWrites = new ElementDataWrites
  implicit val elementDataReads = new ElementDataReads

  /**
    * Find the nearest ancestor of an ElementData matching the given predicate.
    * @param elem
    * @param pred
    * @return
    */
  def findMatchingAncestor(elem: ElementData, pred: (ElementData => Boolean)): Option[ElementData] = {
    var parent = elem.parent
    while (parent.isDefined && !pred(parent.get)) {
      parent = parent.get.parent
    }
    parent
  }

  def apply(json: JsValue): ElementData = {
    Json.fromJson[ElementData](json)(elementDataReads).get
  }

}

/**
  * Represents a DOM element as found by and at the time of state extraction. All properties are "computed" properties in the sense that
  * they represent the state of the DOM element after CSS styling has finished (i.e. rendering is complete).
  *
  * @param parent DOM parent
  * @param nodeName DOM node name, e.g. "div" or "a"
  * @param attributeMap map of all attribute set to the DOM node
  * @param x absolute X position in px (i.e. absolute to the document, i.e. window or frame)
  * @param y absolute Y position in px (i.e. absolute to the document, i.e. window or frame)
  * @param width width in px
  * @param height height in px
  * @param x1real this and four following fields represent real coordinates of the element. They only make sense for inline elements.
  *               real coordinates differ from coordinates above when the element is inline and it wraps the line.
  *               Which means that the bounding box reported by values above make no sense (it's bigger than the real bounding box,
  *               which is actually not a box any more). WMKNBN-1754
  * @param y1real
  * @param x2real
  * @param y2real
  * @param zIndexInput see also: effectiveZIndex
  * @param display value of display property returned by the browser. see also: effectiveDisplay
  * @param visibility
  * @param cssPosition
  * @param cssFloat value of float property returned by the browser. see also: effectiveFloat
  * @param readOnly
  * @param disabled
  * @param overflowX
  * @param overflowY
  * @param backgroundColor color value normalized to rbga
  * @param backgroundImage
  * @param borderColor a 4 values of color rgba format: (top, right, bottom, left)
  * @param borderImage
  * @param borderStyle a 4 values of style in format: (top, right, bottom, left)
  * @param borderWidth a 4 values of width in px: (top, right, bottom, left)
  * @param color color of the text in the element
  * @param content elemen't content assigned in CSS. Only makes sense for pseudo elements such as :before or :after
  * @param opacity opacity of current element, a value for 0.0 to 1.0 in string. see also: effectiveOpacity
  * @param outlineStyle
  * @param cssClip 4 values of clip: (top, right, bottom, left)
  * @param boxShadow a string with box shadow, as it is returned by the browser
  * @param textIndent indentation of first line of the text. Normally in px.
  * @param eventHandlers
  * @param visibleDimensions Dimensions after clipping
  */
class ElementData(var parent: Option[ElementData], val nodeName: String, attributeMap: Map[String, String],
                  val x: Int, val y: Int, val width: Int, val height: Int, val x1real: Option[Int], val y1real: Option[Int], val x2real: Option[Int], val y2real: Option[Int],
                  val zIndexInput: String, val display: String,
                  val visibility: String, val cssPosition: String, val cssFloat: String, val readOnly: String, val disabled: String,
                  val overflowX: String, val overflowY: String, val backgroundColor: String, val backgroundImage: String, val borderColor: String, val borderImage: String,
                  val borderStyle: String, val borderWidth: String, val color: String, val content: String,
                  val opacity: String, val outlineStyle: Option[String], val cssClip: Option[String], val boxShadow: Option[String], val textIndent: Option[String],
                  val eventHandlers: Seq[EventHandler], var visibleDimensions: Option[Rectangle] = None) extends Iterable[ElementData] with Logging {

  @transient private var effectiveZIndex : Option[Int] = None
  @transient private var parsedEffectiveZIndex = false

  @transient private var effectiveOpacity : Option[Double] = None
  @transient private var parsedEffectiveOpacity = false

  @transient private var clip: Option[Rectangle] = None
  @transient private var parsedCssClip = false

  @transient private var relXPath: Option[XPathExpression] = None

  @transient private var elementVisible = 0
  val dimensions = Rectangle(x, y, width, height)
  private val textContentBuffer = mutable.ListBuffer.empty[String]
  private var textContent: Option[String] = None
  val zIndex: Option[String] = Option(zIndexInput)
  val attributes: Map[String, Attribute] = for ((name, value) <- attributeMap) yield name -> Attribute(name, value)
  val children = new scala.collection.mutable.ListBuffer[ElementData]()
  val eventHandlersByEventType: Map[String, Seq[EventHandler]] = eventHandlers.groupBy(_.eventType)

  // relations between position, display and float property is reported differently by different browsers. See: WMKNBN-1363
  def effectiveDisplay = if ((List("absolute", "fixed").contains(cssPosition) || (cssFloat != "none" && cssFloat != "undefined" && cssFloat != null)) && display != "none" && display != null) "block" else display
  def effectiveFloat = if (List("absolute", "fixed").contains(cssPosition)) "none" else cssFloat

  var xpath : Option[XPathExpression] = None

  def addChild(child : ElementData) : Unit = children += child

  def appendTextContent(text : String) : Unit = {
    textContentBuffer += text
    textContent = None
  }

  def containsAttribute(name : String) : Boolean = attributes.contains(name)

  def containsEventHandler(eventHandlerName : String) : Boolean = eventHandlers.map(_.eventType).contains(eventHandlerName)

  def getAttributeValue(name : String) : Option[String] = attributes.get(name).map(_.value)

  def getEffectiveZIndex: Option[Int] = {
    if (!parsedEffectiveZIndex) {
      parsedEffectiveZIndex = true
      effectiveZIndex = if (zIndex.isEmpty || zIndex.get.length == 0 || zIndex.get.equalsIgnoreCase("null") || zIndex.get.equalsIgnoreCase("undefined")) {
        Some(0)
      } else if (zIndex.get.equalsIgnoreCase("auto")) {
        Some(0)
      } else if (zIndex.get.equalsIgnoreCase("inherit")) {
        if (parent.isEmpty) {
          if (nodeName.equalsIgnoreCase("html")) {
            Some(0)
          } else {
            None
          }
        } else {
          parent.get.getEffectiveZIndex
        }
      } else {
        try {
          Some(zIndex.get.toInt)
        } catch  {
          case ex: NumberFormatException =>
            try {
              val value = zIndex.get.toDouble
              Some(value.toInt)
            } catch {
              case ex2: NumberFormatException =>
                // fall back: z-index auto => return 0
                Some(0)
            }
        }
      }
    }
    effectiveZIndex
  }

  def getCssClip(): Option[Rectangle] = {
    if (!parsedCssClip) {
      parsedCssClip = true
      if (cssClip.isDefined) {
        val regExp = "rect\\((-?\\d+)px,? (-?\\d+)px,? (-?\\d+)px,? (-?\\d+)px\\)".r
        val rect = regExp.findFirstMatchIn(cssClip.get.replaceAll("auto", "0px"))

        if (rect.isDefined) {
          val top = math.max(rect.get.group(1).toInt, 0)
          val right = math.max(rect.get.group(2).toInt, 0)
          val bottom = math.max(rect.get.group(3).toInt, 0)
          val left = math.max(rect.get.group(4).toInt, 0)
          clip = Some(Rectangle(left, top, right - left, bottom - top))
        } else {
          clip = None
        }
      } else {
        clip = None
      }
    }
    clip
  }

  def getEffectiveOpacity(): Option[Double] = {
    if (!parsedEffectiveOpacity) {
      parsedEffectiveOpacity = true

      val ownOpacity = try {
        opacity.toDouble
      }  catch {
        case e: NumberFormatException =>
          1
      }

      val parentOpacity = if (parent.isEmpty) {
        1.0
      } else {
        val peo = parent.get.getEffectiveOpacity()
        if (peo.isDefined)
          peo.get
        else
          1.0
      }

      effectiveOpacity = Some(parentOpacity * ownOpacity)
    }

    effectiveOpacity
  }

  def getEventHandler(eventType: String) : Option[Seq[EventHandler]] = eventHandlersByEventType.get(eventType)

  def getTextContent(): String = {
    if (isPseudoElement) {
      val pattern = """["'].*["']"""
      val empty = content == null || content == "none" || content.length == 0 || (content.matches(pattern) && content.substring(1, content.length - 1).trim.length == 0)

      if (!empty)
        content
      else
        ""

    } else {
      if (textContent.isEmpty) {
        textContent = Some(textContentBuffer.reverse.mkString)
      }

      textContent.get match {
        case c if c.nonEmpty && c.matches(textMatchingRegex) => c
        case _ if nodeName.toLowerCase.contains("input") =>
          (getAttributeValue("value"), getAttributeValue("placeholder")) match {
            case (Some(r), None) =>
              r
            case (None, Some(ph)) => ph
            case (Some(value), Some(_)) if value.matches(textMatchingRegex) && value.nonEmpty => value
            case (Some(_), Some(ph)) => ph
            case _ => ""
          }
        case _ => ""

      }
    }
  }

  def hasChildren() : Boolean = children.nonEmpty

  /**
    * Checks if the element has zero area. It also considers two corner cases:
    * <ul>
    * <li> Chrome reports width zero for buttons without labels. To include them nonetheless we use logical or in case
    * the element is a button</li>
    * <li> if an element has zero area and it has text content, the text is still shown, which means that area should be ignored</li>
    * </ul>
    */
  def hasNonZeroArea: Boolean = {
    if (isPseudoElement)
    // TODO @avi
      return true

    val c = getCssClip()
    if (nodeName.equals("button") || nodeName.equals("input") && getAttributeValue("type").orNull == "button") {
      dimensions.width > 0 || dimensions.height > 0
    } else if (hasVisibleText() && allowsOverflow) {
      c.isEmpty || c.get.width > 0 && c.get.height > 0
    } else {
      dimensions.width > 0 && dimensions.height > 0 && (c.isEmpty || c.get.width > 0 && c.get.height > 0)
    }
  }

  def isMultilineInlineElement = (display == "inline" || display == "inline-block") && (
    (x1real.isDefined && x != x1real.get) ||
      (y1real.isDefined && y != y1real.get) ||
      (x2real.isDefined && x + width != x2real.get) ||
      (y2real.isDefined && y + height != y2real.get))

  def hasPositiveCoordinates: Boolean = dimensions.x2 > 0 && dimensions.y2 > 0

  def isDisabled() : Boolean = return disabled != null && (disabled.equalsIgnoreCase("true") || disabled.equalsIgnoreCase("disabled"))

  def allowsOverflow: Boolean = !Set("scroll", "hidden").contains(overflowX) && !Set("scroll", "hidden").contains(overflowY)


  def isDisplayed() : Boolean = {
    var result = true
    if (parent.isDefined) {
      result = parent.get.isDisplayed()
    }
    result = result && (effectiveDisplay == null || !effectiveDisplay.equalsIgnoreCase("none"))
    result
  }

  /**
    * This method returns true if the element (i.e. the element itself, not its children) has any influence on the visual appearance of the document.
    * The method is supposed to represent the intuition of "actual visibility" in the sense of "Can the user see this element"?
    *
    * That "actual visibility" is clearly influenced by many factors. For instance, an element with no area (including margins) can
    * usually be seen (exceptions to this are shown later) as well as it is completely located outside of the visible page.
    * Besides its dimensions, there are CSS properties that determine the visibility of an element, be it "opacity", "background*", "border*", "text content",
    * which must also be taken into account. In addition to this, text nodes within block-level boxes are rendered as "anonymous inline-boxes", i.e.
    * depending on the dimensions and the overflow property, non-empty text content makes an element visible. (TODO: maybe our logic would become
    * simpler, if we enclosed text content into "artificial element data"?).
    *
    * Result of this function is cached unless background is provided.
    *
    * @param bg color of the element's background Some properties are ignored if they are of the same color as
    * the background (consider white element on top of another white element).
    */
  def isElementVisible(bg: Option[String] = None): Boolean = {
    if (elementVisible != 0 && bg.isEmpty) {
      elementVisible == 1
    } else {
      val visible = hasNonZeroArea && isDisplayed() && isVisible() && !isTransparent && !isInputHidden && !isNoScript &&
        (isImage || isInput || isIFrame || hasVisibleText(bg) || hasVisibleStylingProperties(bg))

      if (bg.isEmpty) {
        elementVisible = if (visible) 1 else 2
      }
      visible
    }
  }

  def isPseudoElement: Boolean = nodeName.startsWith("pe:")

  def isDimensionsValid: Boolean = !(isPseudoElement && (width == -1 || height == -1 || x == -1 || y == -1))


  // checks if the color is actually visible (it is not defined as transparent or as rgba(*, *, *, 0) where alpha is 0)
  def colorIsVisible(color: String): Boolean = color != "transparent" && !color.matches("rgba\\((\\d+,\\s){3}0\\)")

  private val textMatchingRegex = "(?s).*[^\\s\\p{Z}]+.*"

  def hasTextContent: Boolean = {
    val text = getTextContent()
    !text.isEmpty && text.matches(textMatchingRegex)


    //    logger.debug("has textcontent (" + text + ") is " + result)
  }


  /**
    * We perform three checks here: (1) the element has text (2) this text is written in a visible color both by itself and
    * on a background (if provided) and (3) text indentation is in the reasonable limits. Latter is necessary because
    * developers may use this property to hide something from viewport (eg for SEO).
    * @param bg
    * @return
    */
  def hasVisibleText(bg: Option[String] = None): Boolean = {
    val regex = """^-?\d+""".r
    val indentOpt = if (textIndent.isDefined) regex.findFirstIn(textIndent.get) else None
    val result = hasTextContent &&
      (bg.isEmpty || bg.get != color) && color != backgroundColor && colorIsVisible(color) &&
      (indentOpt.isEmpty || indentOpt.get.toInt > -1000)
    //      logger.debug("hasTextContentVisibleStyling (" + color + ") is " + result)
    result
  }

  def isImage: Boolean = nodeName == "img"

  def isIFrame: Boolean = nodeName == "iframe"

  def isInput: Boolean = nodeName == "input"

  /**
    * No Script elements are shown only in the case when Browsers have JS disabled.
    * Since we know, that it's not the case for us, we report them as invisible by default.
    * WMKNBN-1741 describes why it is necessary
    * @return
    */
  def isNoScript: Boolean = nodeName == "noscript"

  def isInputHidden: Boolean = isInput && attributeMap.getOrElse("type", "text") == "hidden"

  /**
    * Check if the element has either background (image or color), border or shadow
    * @param bg optional background color in rgba notation
    * @return
    */
  def hasVisibleStylingProperties(bg: Option[String] = None): Boolean = hasBackgroundImage || hasVisibleBackground(bg) || hasVisibleBorder(bg) || hasShadow

  /**
    *
    * @return if the shadow definition is present
    */
  def hasShadow = boxShadow.isDefined && boxShadow.get != "none" && boxShadow.get != "undefined" && !fallbackScenarioForBoxShadowInOldIE

  /**
    *
    * @return if the background image is defined
    */
  def hasBackgroundImage = !backgroundImage.isEmpty && backgroundImage != "none"

  /**
    * check if element has background color and if this colour is distinct on the given background
    */
  def hasVisibleBackground(bg: Option[String] = None) = (bg.isEmpty || bg.get != backgroundColor) && !backgroundColor.isEmpty && colorIsVisible(backgroundColor)

  /**
    * JS API of IE <=10 return _color_ property if box shadow is set to _none_; regular expression here hence
    * checks if what we have in boxShadow is a color definition alone
    */
  def fallbackScenarioForBoxShadowInOldIE: Boolean = boxShadow.isDefined && boxShadow.get.matches( """^(((#\w{3,6}))|(rgba?\([\d,%\.\s]+\))|(hsla?\([\d,%\.\s]+\))|(\w+))$""")

  /**
    * check if element has border, and this border is visible on the given background
    */
  def hasVisibleBorder(bg: Option[String] = None): Boolean = {
    if (!borderStyle.isEmpty) {
      val borderColorArray = if (borderColor.contains("rgb")) {
        // the case when browser normalizes every color definition to rgb notation
        val regExp = "(rgba?\\([\\d,\\s\\.]+\\))".r
        regExp.findAllIn(borderColor).toArray
      } else {
        // this covers the case when color is 'auto auto auto auto' or 'transparent ... '
        // IE7 and IE8 do not normalize the color. So it can be that borderColor='black black black black'
        borderColor.split(" ")
      }

      val borderStyleArray = borderStyle.split(" ")
      val borderWidthArray = borderWidth.split(" ")

      if (borderColorArray.length == 4 && borderStyleArray.length == 4 && borderWidthArray.length == 4) {
        // expect every aspect of border to be defined as a quartet
        for (i <- 0 until 4) {
          if (((bg.isEmpty || bg.get != borderColorArray(i)) && colorIsVisible(borderColorArray(i))) &&
            borderStyleArray(i) != "none" && !borderWidthArray(i).matches("0\\w*")) {
            return true
          }
        }
      }
      false
    } else {
      false
    }
  }

  /**
    * Returns the topmost element (root parent) of this element
    */
  def getRootElement: ElementData = if (parent.isDefined) parent.get.getRootElement else this

  def isReadOnly() : Boolean = readOnly != null && readOnly.equalsIgnoreCase("true")

  def isTransparent: Boolean = getEffectiveOpacity().exists(_ < 0.001)

  def isVisible() : Boolean = {
    val isParentVisible = parent.isEmpty || parent.get.isVisible()
    if (isParentVisible) {
      return visibility == null || !visibility.equalsIgnoreCase("hidden")
    }
    if (visibility == null) {
      return false
    }
    return visibility.equalsIgnoreCase("visible")
  }

  override def iterator() : Iterator[ElementData] = new TreeIterator(this)

  override def toString() : String =
    ("ElementData [effectiveZIndex=" + effectiveZIndex + ", isElementVisible=" + isElementVisible() + ", eventHandlers=" + eventHandlers
      + ", attributeMap=" + attributeMap + ", nodeName=" + nodeName + ", visibility=" + visibility + ", display=" + display + ", zIndex=" + zIndex
      + ", disabled=" + disabled + ", readOnly=" + readOnly + ", dimensions=" + dimensions + ", xpath=" + xpath + "]")

  def computeXPathToElementMappingDescending(): Map[XPathExpression, ElementData] = {
    val mapping = new collection.mutable.HashMap[XPathExpression, ElementData]()
    for (element <- iterator(); if element.xpath.isDefined){
      mapping += (element.xpath.get -> element)
    }
    mapping.toMap
  }

  def computeXPathToElementMappingComplete(): Map[XPathExpression, ElementData] = {
    var node = this
    while (node.parent.isDefined){
      node = node.parent.get
    }
    node.computeXPathToElementMappingDescending()
  }

  def getRelativeXPath: XPathExpression = {
    if (relXPath.isEmpty) {
      val r = recursiveSearchForIDrelations(Seq("id")) match {
        case Some((idTag: String, name: String, distance: Int)) =>
          val idString: String = "//*[@%s=\"%s\"]".format(idTag, name)
          if (distance == 0) {
            new XPathExpression(idString)
          } else {
            val split = xpath.get.expression.split("/")
            new XPathExpression(idString + split.slice(split.length - distance,split.length).mkString("/","/", ""))
          }
        case None => xpath.get
      }

      relXPath = Some(r)
    }

    relXPath.get
  }

  def getFullXpath: XPathExpression = {
    xpath.get
  }

  /**
    * Parses a given XML-node element recursively for a given id identifier, as specified by the idIdentifiers. Per default
    * <b>id</b> and <b>class</b> are reported. If such an identifier exists, the found identifier tag, the content and the distance
    * in the DOM tree are reported.
    * @param idIdentifiers
    * @return
    */
  def recursiveSearchForIDrelations(idIdentifiers: Seq[String] = Seq("id", "class")): Option[(String, String, Int)] = {
    var distance = 0
    var current = Option.apply(this)

    while (current.isDefined) {
      for (idTag <- idIdentifiers){
        current.get.getAttributeValue(idTag) match {
          case Some(name) => return Some(idTag, name, distance)
          case None =>
        }
      }
      distance+= 1
      current = current.get.parent
    }
    None
  }

  /**
    * Traverses the Element Data DOM structure to find the elements identified by the xpath. Typically this
    * identifier should be unique.
    * @param xPath the element identifier we search for
    * @param strict if false, element tags like 'button[1]' are treated as 'button'
    * @return elements identified by the xpath
    */
  def findElementsByXPath(xPath: XPathExpression, strict: Boolean = true): Seq[ElementData] = {
    (for (i <- iterator(); if {
      val eval = Seq(i.xpath, Some(i.getRelativeXPath)).flatten ++ (if (strict) Seq() else Seq(i.xpath, i.relXPath).flatten.map( xpath => XPathExpression(xpath.expression.replace("[1]", ""))) )
      eval.contains(xPath)
    }) yield i).toSeq
  }

  def findElementByXPath(xPath: XPathExpression, strict: Boolean = true): Option[ElementData] = findElementsByXPath(xPath, strict).headOption

  def findElement(by: By): Option[ElementData] = {
    by match {
      case x : ByXPath =>
        val xpath = x.toString.replaceFirst("By.xpath: ", "")
        findElementByXPath(XPathExpression(xpath), false)
    }
  }

  /**
    * Determines if this element is a parent the other one.
    * @param other
    */
  def isParentOf(other: ElementData): Boolean = {
    if (this == other) {
      false
    } else {
      var current = other
      while(current.parent.isDefined){
        current = current.parent.get
        if (current == this) return true
      }
      false
    }
  }

  def hasSameParentAs(other: ElementData): Boolean = this.parent.isDefined && other.parent.isDefined && this.parent.get.xpath == other.parent.get.xpath


  def getVisualDistance(other: ElementData): Double = {
    val othermiddle = other.dimensions.middle
    getVisualDistance(othermiddle)
  }

  def getVisualDistance(point: (Double, Double)): Double = {
    val thismiddle = dimensions.middle
    math.sqrt(math.pow(thismiddle._1 - point._1, 2) + math.pow(thismiddle._2 - point._2, 2))
  }

  //  override def equals(obj: Any) = throw new UnsupportedOperationException("ElementData has different identity semantics depending on context")
  //  override def hashCode = throw new UnsupportedOperationException("ElementData has different identity semantics depending on context")

}

class TreeIterator(rootNode: ElementData) extends Iterator[ElementData] {


  val stack = scala.collection.mutable.Stack[ElementData]()
  stack.push(rootNode)


  override def hasNext() : Boolean = stack.nonEmpty

  override def next() : ElementData = {
    if (hasNext()) {
      val next = stack.pop()
      for (child <- next.children.reverseIterator) stack.push(child)
      next
    } else {
      throw new NoSuchElementException()
    }
  }


}



