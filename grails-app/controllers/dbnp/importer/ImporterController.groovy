/**
 * Importer controller
 *
 * The importer controller handles the uploading of tabular, comma delimited and Excel format
 * based files. When uploaded a preview is shown of the data and the user can adjust the column
 * type. Data in cells which don't correspond to the specified column type will be represented as "#error".
 *
 * The importer controller catches the actions and consecutively performs the
 * logic behind it.
 *
 * @package	importer
 * @author	t.w.abma@umcutrecht.nl
 * @since	20100126
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

package dbnp.importer

class ImporterController {
    def ImporterService

    def index = { }

    /**
    * This method will move the uploaded file to a temporary path and send the header
    * and the first n rows to the preview
    */
    def upload = {
	def downloadedfile = request.getFile('importfile');
        def tempfile = new File("/tmp/" + System.currentTimeMillis() + ".nmcdsp")

        downloadedfile.transferTo(tempfile)
        
	def wb = ImporterService.getWorkbook(new FileInputStream(tempfile))
        
	def header = ImporterService.getHeader(wb)
        render (view:"preview", model:[header:header])
    }
}
