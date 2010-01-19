/**
 * wizard javascript functions
 *
 * @author  Jeroen Wesbeek
 * @since   20100115
 * @package wizard
 * @see     dbnp.studycapturing.WizardTagLib
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
$(document).ready(function() {
    // attach help action on all wizard help icons
    $('div#wizard').find('div.help').each(function() {
        $(this).find('div.icon').qtip({
            content: 'leftMiddle',
            position: {
               corner: {
                  tooltip: 'leftMiddle',
                  target: 'rightMiddle'
               }
            },
            style: {
               border: {
                  width: 5,
                  radius: 10
               },
               padding: 10,
               textAlign: 'center',
               tip: true, // Give it a speech bubble tip with automatic corner detection
               name: 'blue' // Style it according to the preset 'cream' style
            },
            content: $(this).find('div.content').html(),
            show: 'mouseover',
            hide: 'mouseout'
        })
    })
});