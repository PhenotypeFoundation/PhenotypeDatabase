package org.dbxp.sam

import grails.util.Holders

class FuzzySearchService {

    static transactional = false
	
	/**
	 * Matches the patterns with the candidates, and returns the best candidates for all patterns, but returning
	 * each candidate at most once.
	 * 
	 * @param patterns		List with patterns to search for
	 * @param candidates	List with candidates to search in
	 * @param threshold		Threshold the matches have to be above. This input variable is either 'default', or a map whose keys can be used to look up the requested threshold value. The 'retrieveThresholdFromConfig' function will look for a value associated to this key in a map located in the configurationHolder, at 'config.fuzzyMatching.threshold'.
	 * @return				A list with each element being a map with three elements:
	 * 							pattern:	the pattern that has been matched
	 * 							candidate:	the best matching candidate for this pattern or null if no match has been found
	 * 							index:		the index of the candidate in the original list				
	 */
	static def mostSimilarUnique( patterns, candidates, thresholdInput='default' ) {
        def threshold = retrieveThresholdFromConfig(thresholdInput)
		def matches = []
		
		// Find the best matching candidate for each pattern
		patterns.findAll { it }.each { pattern ->
			def topScore = 0
			def bestFit = null
			
			candidates.eachWithIndex { candidate, idx ->
				def score = stringSimilarity(pattern, candidate);
				if( !score.isNaN() && score >= threshold )
					matches << [ 'pattern': pattern, 'candidate': candidate, 'score': score, 'index': idx ];
			}
		}
		
		// Sort the list on descending score
		matches = matches.sort( { a, b -> b.score <=> a.score } as Comparator )
		
		// Loop through the scores and select the best matching for every candidate
		def results = patterns.collect { [ 'pattern': it, 'candidate': null, 'index': null ] }
		def selectedCandidates = [];
		def filledPatterns = [];
		
		matches.each { match ->
			if( !filledPatterns.contains( match.pattern ) && !selectedCandidates.contains( match.candidate ) ) {
				def foundMatch = results.find { result -> result.pattern == match.pattern };
                foundMatch?.candidate = match.candidate;
                foundMatch?.index = match.index;

				
				selectedCandidates << match.candidate;
				filledPatterns << match.pattern;
			}
		}
		
		return results
	}

	// classes for fuzzy string matching
	// <FUZZY MATCHING>
	static def similarity(l_seq, r_seq, degree=2) {
		def l_histo = countNgramFrequency(l_seq, degree)
		def r_histo = countNgramFrequency(r_seq, degree)

		dotProduct(l_histo, r_histo) /
				Math.sqrt(dotProduct(l_histo, l_histo) *
				dotProduct(r_histo, r_histo))
	}

	static def countNgramFrequency(sequence, degree) {
		def histo = [:]
		def items = sequence.size()

		for (int i = 0; i + degree <= items; i++)
		{
			def gram = sequence[i..<(i + degree)]
			histo[gram] = 1 + histo.get(gram, 0)
		}
		histo
	}

	static def dotProduct(l_histo, r_histo) {
		def sum = 0
		l_histo.each { key, value ->
			sum = sum + l_histo[key] * r_histo.get(key, 0)
		}
		sum
	}

	static def stringSimilarity (l_str, r_str, degree=2) {

		similarity(l_str.toString().toLowerCase().toCharArray(),
				r_str.toString().toLowerCase().toCharArray(),
				degree)
	}

	static def mostSimilar(pattern, candidates, thresholdInput='default' ) {
        def threshold = retrieveThresholdFromConfig(thresholdInput)
		def topScore = 0
		def bestFit = null

		candidates.each { candidate ->
			def score = stringSimilarity(pattern, candidate)
			
			if (score > topScore) {
				topScore = score
				bestFit = candidate
			}
		}

		if (topScore < threshold)
			bestFit = null

		bestFit
	}
	
	static def mostSimilarWithIndex(pattern, candidates, thresholdInput='default' ) {
        def threshold = retrieveThresholdFromConfig(thresholdInput)
		def topScore = 0
		def bestFit = null

		candidates.eachWithIndex { candidate, idx ->
			def score = stringSimilarity(pattern, candidate)

			if (score != java.lang.Double.NaN && score > topScore) {
				topScore = score
				bestFit = idx
			}
		}

		if (topScore < threshold)
			bestFit = null

		bestFit
	}
	
	// </FUZZY MATCHING>

    /** The 'retrieveThresholdFromConfig' function will look for a value located in the configurationHolder, at 'config.fuzzyMatching.threshold'. For example: input map '['controller': 'measurementImporter', 'item': 'subjectName']' would grab the value for 'fuzzyMatching.threshold.measurementImporter.subjectName'
     * @param thresholdInput   Either consists of 'default', or consists of a map, with a 'controller' and an 'item' key.
     * @return  A double
     */
    static double retrieveThresholdFromConfig(thresholdInput){
        Double defaultValue = Double.valueOf(Holders.config.fuzzyMatching.threshold.default)
        if(thresholdInput=='default'){
            return defaultValue
        }

        // Apparently it was not the default value that was requested so try to find the requested custom value
        String requestedValueString = Holders.config.fuzzyMatching.threshold.get(thresholdInput['controller']).get(thresholdInput['item'])
        if(requestedValueString!=null && requestedValueString!=''){
            return Double.valueOf(requestedValueString)
        } else {
            return defaultValue
        }
    }
}
