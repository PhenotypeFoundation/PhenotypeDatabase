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

$(document).ready(function(){
    // attach help action on all wizard help icons
    $('div#wizard').find('div.help')
            .bind('mouseenter',function() {
        $(this).find('div.content').animate({
            opacity: 0.95
        }, 200 );
    }).bind('mouseleave',function() {
        $(this).find('div.content').animate({
            opacity: 0
        }, 200 );
    });
});