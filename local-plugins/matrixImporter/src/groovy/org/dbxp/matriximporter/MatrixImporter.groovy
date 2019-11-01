package org.dbxp.matriximporter

import org.apache.commons.logging.LogFactory

/**
 * Singleton class to access the import methods for matrix imports. Use the importFile method for reading files.
 *
 *  	MatrixImporter.getInstance().importFile( new File( '/tmp/temporaryfile' ) )
 *
 *  Also available are: 'importString', 'importByteArray', and 'importInputStream'.
 *
 *  Currently, excel files (.xls and .xlsx) and csv files (comma, tab or semicolon delimited) are supported.
 *  You can use the register() and unregister() methods to add or remove parsers for specific file types
 *
 *  	// Create a parser implementing the MatrixParser interface. Also implemented the required methods
 *  	class MyTypeParser implements MatrixParser { ... }
 *
 *  	// Register a parser for your own filetype
 *  	def myTypeParser = new MyTypeParser()
 *  	MatrixImporter.getInstance().register( myTypeParser )
 *
 *  	// Import the file
 *  	MatrixImporter.getInstance().importFile( new File( '/tmp/myTypeFile' ) )
 *
 *  	// If needed
 *  	MatrixImporter.getInstance().unregister( MyTypeParser.class )
 *  	// or
 *  	MatrixImporter.getInstance().unregister( myTypeParser )
 *
 * @author Robert Horlings
 *
 */
class MatrixImporter {

	private static def log = LogFactory.getLog(this)

	// Singleton instance
	private static MatrixImporter _instance = null

	// List of registered parsers
	private List<MatrixParser> parsers = []

	/**
	 *
	 * @param file
	 * @param hints
	 * @return
	 */
	public importFile( File file, Map hints = [:], Boolean returnInfo = false ) {
		// set the fileNae 'hint' if it hasn't been set by the user already
		if (!hints.fileName) {
			hints += [fileName: file.name]
		}
		importInputStream(file.newInputStream(), hints, returnInfo)
	}

	/**
	 *
	 * @param string
	 * @param hints
	 * @return
	 */
	public importString( String string, Map hints = [:], Boolean returnInfo = false, Boolean parseStrings = true ) {
		importInputStream( new ByteArrayInputStream(string.getBytes("UTF-8")) , hints, returnInfo, parseStrings)
	}

	/**
	 *
	 * @param bytes
	 * @param hints
	 * @return
	 */
	public importByteArray( byte[] bytes, Map hints  = [:], Boolean returnInfo = false, Boolean parseStrings = true ) {
		importInputStream(new ByteArrayInputStream(bytes), hints, returnInfo, parseStrings)
	}

	/**
	 * Imports a file using an existing MatrixParser. If no parser is found that
	 * is able to parse the file, null is returned.
	 * @param inputStream	the inputstream to read
	 * @param hints			Map with hints for the parser. The value corresponding to the key 'fileName' might be used
	 *              		by the parsers to determine whether they can parse the file.
	 *              		Might also include keys like 'startRow', 'endRow' and 'sheet'.
	 * 						Parsers implementing this interface may or may not listen to the hints given. See the documentation
	 * 						of different implementing classes.
	 * @param returnInfo	Used to determine return value. If 'true', the return value is a tuple containing the matrix
	 * 						and a hashmap with information about the parsing process. This hashmap can contain keys like
	 * 						parserClassName or delimiter so we can see the parser and parameters used in parsing the
	 * 						input. If returnInfo is false, only the matrix will be returned.
	 * @param parseStrings  If true, try to parse strings into numbers
	 * @return				Two-dimensional data matrix with the contents of the file. The matrix has the structure:
	 * 						[
	 * 							[ 1, 3, 5 ] // First line
	 * 							[ 9, 1, 2 ] // Second line
	 * 						]
	 */
	public importInputStream( InputStream inputStream, Map hints = [:], Boolean returnInfo = false, Boolean parseStrings = true ) {

		def bis = new BufferedInputStream(inputStream)

		bis.mark(MatrixParser.readAheadLimit)

		for( parser in parsers ) {
			if( parser.canParse( hints ) ) {
				def matrix, parseInfo

				try {
					bis.reset()

					(matrix, parseInfo) = parser.parse( bis, hints )

					if (parseStrings) {
						// go through each cell and try to convert string values to numbers
						matrix = matrix.collect { row ->
							row.collect { String cell ->
								cell.isNumber() ? (cell.isInteger() ? cell.toInteger() : cell.toDouble()) : cell
							}
						}
					}
				} catch (e) {
					// we take it an exception means this parser was unable to parse
					// the input.
					log.info('Unable to parse using parser: ' + parser.class + ' with description: ' + parser.description + '.', e)
				}

				// Only return the value if the file has been correctly parsed
				// (i.e. the structure != null). Otherwise, we should try to parse
				// the file using another parser (if applicable)
				if( matrix ) {
					return returnInfo ? [matrix, [parserClassName: parser.class.simpleName] + parseInfo] : matrix
				}
			}
		}

		// If parsing didn't work out, return null
		returnInfo ? [null, null] : null
	}

	/**
	 * Registers a parser with the importer, so the parser can be used to parse files
	 * @param c	Object that implements MatrixParser
	 */
	public void registerParser( MatrixParser c ) {
		if( c instanceof MatrixParser && !( c in parsers ) )
		parsers << c
	}

	/**
	 * Removes a parser from the importer, so the parser will not be used to parse files
	 * @param c		Class that implements MatrixParser
	 */
	public void unregisterParser( MatrixParser c ) {
		parsers = parsers.findAll { !( it.is( c ) ) }
	}

	/**
	 * Removes a parser from the importer, so the parser will not be used to parse files
	 * @param c		Class that implements MatrixParser
	 */
	public void unregisterParser( Class c ) {
		parsers = parsers.findAll { !( it.class == c ) }
	}

	/**
	 * Returns a list of parsers that have been registered
	 * @return	List with MatrixParser objects
	 */
	public List<MatrixParser> getParsers() {
		return [] + parsers
	}

	/**
	 * Returns the singleton instance of the MatrixImporter
	 * @return
	 */
	public static MatrixImporter getInstance() {
		if( _instance == null )
		_instance = new MatrixImporter()

		return _instance
	}

	// Private constructor in order to facilitate the singleton pattern
	private MatrixImporter() {
		registerParser( new ExcelParser() )
		registerParser( new ExcelXParser() )
		registerParser( new CsvParser() )
	}

}
