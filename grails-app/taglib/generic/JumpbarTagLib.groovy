package generic

class JumpbarTagLib {
    static namespace = "jumpbar"

    def link = {attr, body ->
        out << """
        <a href="${createLink(action:'jump', controller: 'jumpbar', plugin:'jumpbar', params:[frameSource: "$attr.frameSource", pageTitle: "$attr.pageTitle"])} ">${body()}</a>
        """
    }

}
