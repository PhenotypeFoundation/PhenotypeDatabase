import org.apache.commons.lang.StringEscapeUtils

class SimpleXMLCodec {
    
    static encode = { theTarget ->
        StringEscapeUtils.escapeXml(theTarget.toString())
    }
    
    static decode = { theTarget ->
        StringEscapeUtils.unescapeXml(theTarget.toString())
    }
}
