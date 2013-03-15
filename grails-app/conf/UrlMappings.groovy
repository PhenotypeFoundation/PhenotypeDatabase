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

        // sam mapping for alternative url
        "/measurements/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }

        // landing page for alternative SAM url
        "/measurements/"(controller: 'SAMHome', action: 'index')

        // landing page for alternative SAM Transcriptomics url
        "/measurements/transcriptomics"(controller: 'SAMHome', action: 'transcriptomics')

        // landing page for alternative SAM Metabolomics url
        "/measurements/metabolomics"(controller: 'SAMHome', action: 'metabolomics')

        // landing page for alternative SAM Proteomics url
        "/measurements/proteomics"(controller: 'SAMHome', action: 'proteomics')

        // landing page for alternative SAM Questionnaire url
        "/measurements/questionnaire"(controller: 'SAMHome', action: 'questionnaire')

        // landing page for alternative SAM url
		"/"(controller: 'home', action: 'index')

		// handle 404
		"404"(controller: 'error', action: 'notFound')

		// handle 500
		"500"(view: '/error')
	}
}