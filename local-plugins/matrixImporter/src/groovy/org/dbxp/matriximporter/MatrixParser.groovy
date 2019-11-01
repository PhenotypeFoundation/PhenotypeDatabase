package org.dbxp.matriximporter

/**
 * This interface describes the methods needed
 * to import data from a specific filetype.
 * 
 * Classes that implement this interface can be registered
 * to function by calling MatrixImporter.register(class)
 * 
 * @author robert
 *
 */
public abstract class MatrixParser {

    public static final int readAheadLimit = 16384

	/**
	 * Returns true if this class is able to parse an object with given filename supplied via 'hints'.
     * Should also return true for empty input.
     *
	 * @param hints a map containing hints. Right now, only 'fileName' is used
	 * @return	True if this class can parse the given object or empty input, false otherwise
	 */
	public abstract boolean canParse ( Map hints )

    /**
     *
     * @param file
     * @param hints
     * @return
     */
	public parse( File file, Map hints = [:] ) {
       parse(file.newInputStream(), hints)
    }

    /**
     *
     * @param string
     * @param hints
     * @return
     */
    public parse( String string, Map hints = [:] ) {
        parse(new ByteArrayInputStream(string.getBytes("UTF-8")), hints)
    }

    /**
     *
     * @param bytes
     * @param hints
     * @return
     */
    public parse( byte[] bytes, Map hints = [:] ) {
        parse(new ByteArrayInputStream(bytes), hints)
    }

    /**
	 * Parses the given input stream and returns the matrix in that file
     *
	 * @param file	File object to read
	 * @param hints	Map with hints for the parser. Might include keys like 'startRow', 'endRow' and 'sheet'.
	 * 				Parsers implementing this interface may or may not listen to the hints given. See the documentation
	 * 				of different implementing classes.
	 * @return		Two-dimensional data matrix of structure:
	 * 				[
	 * 					[ 1, 3, 5 ] // First line
	 * 					[ 9, 1, 2 ] // Second line
	 * 				]
	 * 				The matrix must be rectangular, so all lines should contain
	 * 				the same number of values. All values must be String objects (or null).
	 */
    public abstract ArrayList parse( InputStream inputStream, Map hints )

    protected forceValueInRange(Integer suggested, Integer min, Integer max) {
        suggested < min ? min : Math.min(suggested, max)
    }

	/**
	 * Returns a description for this parser
	 * @return	Human readable description
	 */
	public abstract String getDescription()
}
