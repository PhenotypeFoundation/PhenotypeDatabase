sourcePubMed = function( chooserObject, searchterm, response ) {
    // Search for every term in both title and author fields
    var searchFor = [];
    var terms = searchterm.split( " " );
    for( var i = 0; i < terms.length; i++ ) {
        searchFor[ searchFor.length ] = "(" + $.trim( terms[i] ) + "[title]" + " OR " + $.trim( terms[i] ) + "[author]" + ")";
    }

    var url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=" + searchFor.join( " AND " ) + "&usehistory=y";

    // Fetch it from Pubmed
    // This has to be done in two steps: first find IDs, and second, find information
    $.ajax({
            url: url,
            dataType: "xml",
            success: function(xmlResponse) {
                var xmlDoc = $(xmlResponse);
                var WebEnv = $("WebEnv", xmlDoc ).text();
                var query_key = $("QueryKey", xmlDoc ).text();

                $.ajax({
                    url: "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&retmax=" + chooserObject.maxResults + "&query_key=" + query_key + "&WebEnv=" + WebEnv,
                    dataType: "xml",
                    success: function(summaryResponse) {
                        // Parse the response
                        var parsedData = parsePubmedData( summaryResponse )
                        
                        // Save in cache
                        chooserObject.cache[ chooserObject.database ][ searchterm ] = parsedData;

                        // Return it to jquery
                        response( parsedData );
                    }
                })
            }
    })

};

// Handler that handles the select of a publication
selectPubMedAdd = function( chooserObject, inputElement, event, ui ) {

    // option selected, set hidden fields
    var element = inputElement;

    // set hidden fields
    chooserObject.setInputValue(element, 'title', ui.item.title);
    chooserObject.setInputValue(element, 'authorsList', ui.item.authors.join( ', ' ));
    chooserObject.setInputValue(element, 'pubMedID', ui.item.id);
    chooserObject.setInputValue(element, 'doi', ui.item.doi);

    // remove error class (if present)
    element.removeClass('error');
};

// Handler that handles the closing of the autocomplete
closePubMedAdd  = function( chooserObject, inputElement, event, ui ) {
    // no he didn't, clear the field(s)
    var element = inputElement;

    // set fields
    inputElement.val('');
    chooserObject.setInputValue(element, 'title', '');
    chooserObject.setInputValue(element, 'authorsList', '');
    chooserObject.setInputValue(element, 'pubMedID', '');
    chooserObject.setInputValue(element, 'doi', '');

    // add error class
    element.addClass('error');
};

//renderPubMed = function( chooserObject, ul, item ) {};

/**
 * Parse the result data from pubmed
 *
 * Data is in the format described on
 * http://www.ncbi.nlm.nih.gov/bookshelf/br.fcgi?book=helpeutils&part=chapter1#chapter1.Downloading_Document_Summaries
 *
 * @param data
 * @return array
 */
function parsePubmedData(responseData) {
    var data = $("DocSum", responseData).map(function() {
            var title = $("Item[Name=Title]", this).text();
            var authors = buildAuthorList( $("Item[Name=AuthorList]", this ) );

            // Also search for the DOI
            var doi = '';
            var idsXML = $("Item[Name=ArticleIds]", this );
            var doiXML = idsXML.find("Item[Name=doi]");
            if( doiXML ) {
                doi = doiXML.text();
            }
            return {
                  value: title + " (" + authors.join( ', ' ) + ")",
                  title: title,
                  authors: authors,
                  doi: doi,
                  id: $("Id", this).text()
            };
    }).get();

    return data;
}

/**
 * Builds an array of authors from the XML source.
 *
 * @param   xmlAuthors  XML element from esearch with Item[Name=AuthorList]
 * @return  Array       Names of authors
 */
function buildAuthorList( xmlAuthors ) {
    var authorList = xmlAuthors.find( "Item" ).map(function() {
          return $(this).text();
    }).get();

    return authorList;
}

// Only the source method should be used for all pubmed autocompletes
// The select and close handlers that are defined here, should only be used
// on the add publication page. They can be overridden in the initialization
// of the publication chooser class
PublicationChooser.prototype.availableDBs[ "pubmed" ] = { 
    'source': sourcePubMed,
    'select': selectPubMedAdd,
    'close':  closePubMedAdd
};

