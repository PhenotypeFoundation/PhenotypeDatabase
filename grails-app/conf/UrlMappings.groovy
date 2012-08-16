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

		// handle short codes
		"/$shortCode"(controller: 'notFound', action: 'find')

		// handle 500
		"500"(view: '/error')
	}
}