class UrlMappings {
	static mappings = {
		// default mappings
		"/$controller/$action?/$id?" {
			constraints {
				// apply constraints here
			}
		}

		// landing page
		"/"(controller: 'home', action: 'index')

		// handle 404
		"404"(controller: 'error', action: 'notFound')

		// handle 500
		"500"(view: '/error')
	}
}