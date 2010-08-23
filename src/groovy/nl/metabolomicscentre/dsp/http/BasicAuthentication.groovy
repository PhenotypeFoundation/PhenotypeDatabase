package nl.metabolomicscentre.dsp.http

class BasicAuthentication {

	static public credentialsFromRequest(req){

		// default response
		def credFromRequest = [u: "", p: ""] // by default u(sername) and p(assword) are empty

		// u and p from Basic HTTP
		def httpAuthenticationUsername
		def httpAuthenticationPassword

		if (req){

			// get the authorization header from the request
			def authString = req.getHeader('Authorization')

			if(authString){ // a authorization string was found in the request header, now decode en retrieve the u and p
				def encodedPair = authString - 'Basic '
				def decodedPair =  new String(new sun.misc.BASE64Decoder().decodeBuffer(encodedPair));
				def credentials = decodedPair.split(':')

				httpAuthenticationUsername = credentials[0] // u from Basic HTTP Auth
				httpAuthenticationPassword = credentials[1] // p from Basic HTTP Auth

				credFromRequest = [ u: httpAuthenticationUsername, p: httpAuthenticationPassword ]
			}
		}

		return credFromRequest
	}

	static public callSecure(String username, String password, String url){

		def authString = "${username}:${password}".getBytes().encodeBase64().toString()
		def conn = url.toURL().openConnection()
		conn.setRequestProperty("Authorization", "Basic ${authString}")
		return conn.content.text
	}
}
