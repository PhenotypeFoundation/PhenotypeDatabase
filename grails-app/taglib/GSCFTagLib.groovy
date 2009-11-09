class GSCFTagLib {

     static namespace = "my"

     /**
     * Imports JQuery Javascript to make the JQuery library available to the current page
     */
    def jquery = {attrs, body ->
        out << render(template: "/common/jquerysetup")
    }

    def jqueryui = {attrs, body ->
        out << render(template: "/common/jqueryuisetup")
    }

}
