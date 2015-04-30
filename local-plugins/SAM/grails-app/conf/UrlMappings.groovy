class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(controller:"SAMHome")
		"/home"(controller:"SAMHome")
		"500"(view:'/error')
	}
}
