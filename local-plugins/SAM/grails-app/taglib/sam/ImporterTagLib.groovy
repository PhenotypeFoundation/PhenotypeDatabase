package sam

class ImporterTagLib {
    static namespace = 'imp';

    def importerHeader = {attrs, body ->
        def pages = attrs.get('pages') ? attrs.get('pages') : [:];
        def page  = attrs.get('page') ? attrs.get('page') : "";
        def iPageNum = 1;

        out << "<div class=\"tabs\">"
		out << " <ul>"
		pages.each { key, value ->
            if(key.equals(page)) {
                out << "  <li class=\"active\">"
            } else {
			    out << "  <li>"
            }
			out << "   <span class=\"content\">"
            out << "   ${iPageNum}. ${value}"
            out << "   </span>"
			out << "  </li>"
            iPageNum++
		}
		out << " </ul>"
		out << "</div>"
    }

    def importerFooter = {attrs, body ->
        out << "<div class=\"navigation buttons\">"
        out << body{}
        out << "</div>"
    }
}
