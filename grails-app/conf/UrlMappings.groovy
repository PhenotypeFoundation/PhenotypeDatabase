class UrlMappings {
	static mappings = {
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

        // landing page
        "/measurements/"(controller: 'SAMHome', action: 'index')

        // landing page
        "/measurements/transcriptomics"(controller: 'SAMHome', action: 'transcriptomics')

        // landing page
        "/measurements/metabolomics"(controller: 'SAMHome', action: 'metabolomics')

        // landing page
        "/measurements/proteomics"(controller: 'SAMHome', action: 'proteomics')

        // landing page
        "/measurements/questionnaire"(controller: 'SAMHome', action: 'questionnaire')

        // landing page
		"/"(controller: 'home', action: 'index')

		// handle 404
		"404"(controller: 'error', action: 'notFound')

		// handle 500
		"500"(view: '/error')
	}
}