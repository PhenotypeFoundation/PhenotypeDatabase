/**
 * simpleQuery javascript functions
 *
 * @author  Vincent Ludden
 * @since   20100526
 * @package query
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

  $(document).ready(function() {
    $("#accordion").accordion({collapsible: true,
        autoHeight: false,
        active: false});

    $('#addCompound').submit(function() {
        alert('Handler for adding compound called');
        return true;
    });

    $('#addTransciptome').submit(function() {
        alert('Handler for adding transcriptome called');
        return true;
    });
  });
