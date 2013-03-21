class UrlMappings {
	static mappings = {

        "/"(controller: 'home', action: 'index')

        // default mappings
		"/$controller/$action?/$id?" {
			constraints {
				// apply constraints here
			}
		}

        // sam mapping
        "/sam/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }

        // sam mapping for alternative url
        "/measurements/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }

        // landing page for alternative SAM url
        "/measurements/"(controller: 'SAMHome', action: 'index')

		// handle 404
		"404"(controller: 'error', action: 'notFound')

		// handle 500
		"500"(view: '/error')
	}
}