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

    var compoundCounter = 2;
    var transcriptomeCounter = 2;

    $("#accordion").accordion({collapsible: true,
        autoHeight: false,
        active: false});

    $('#addCompound').click(function() {
        var compoundGroup = document.getElementById('compoundGroup');

        var newCompoundDiv = document.createElement('div');
        newCompoundDiv.setAttribute('id', 'compoundRow' + compoundCounter);

        newCompoundDiv.innerHTML = '<div class="description">Compound</div> <div class="input"><input type="text" name="compound" value=""></div> <div class="description">Value</div> <div class="input"><input id="compoundValue' + compoundCounter + '" type="text"></div>';

        compoundGroup.appendChild(newCompoundDiv);

        compoundCounter++;
        // alert('Handler for adding compound called: ' + compoundCounter);
        return true;
    });

      $('#addTranscriptome').click(function() {
          var transcriptomeGroup = document.getElementById('transcriptomeGroup');
          var newTranscriptomeDiv = document.createElement('div');

          newTranscriptomeDiv.setAttribute('id', 'transcriptomeRow' + transcriptomeCounter);

          var newTranscriptomeRowDiv1 = document.createElement('div');
          newTranscriptomeRowDiv1.setAttribute('class', 'description');
          newTranscriptomeRowDiv1.innerHTML = "Gene/pathway";
          newTranscriptomeDiv.appendChild(newTranscriptomeRowDiv1);

          var newTranscriptomeRowDiv2 = document.createElement('div');
          newTranscriptomeRowDiv2.setAttribute('class', 'input');
          newTranscriptomeRowDiv2.innerHTML = '<input type="text" name="genepath" value="">';
          newTranscriptomeDiv.appendChild(newTranscriptomeRowDiv2);

          var newTranscriptomeRowDiv3 = document.createElement('div');
          newTranscriptomeRowDiv3.setAttribute('class', 'description');
          newTranscriptomeRowDiv3.innerHTML = "Type of regulations";
          newTranscriptomeDiv.appendChild(newTranscriptomeRowDiv3);

          var newTranscriptomeRowDiv4 = document.createElement('div');
          newTranscriptomeRowDiv4.setAttribute('class', 'input');
          var newSelectBox = document.getElementById('regulation');
          newTranscriptomeRowDiv4 = newSelectBox.cloneNode(true);
          newTranscriptomeRowDiv4.setAttribute('id', 'regulation' + transcriptomeCounter);
          newTranscriptomeDiv.appendChild(newTranscriptomeRowDiv4);

          transcriptomeGroup.appendChild(newTranscriptomeDiv);

          transcriptomeCounter++;
          // alert('Handler for adding transcriptome called: ' + transcriptomeCounter);
          return true;
      });

  });

