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
    // check if user is using Firefox 3.6 and warm the user
    // about the XMLHttpRequest bug that causes the wizard to break...
    re = /Firefox\/3\.6/gi;
    if (navigator.userAgent.match(re)) {
        // http://code.google.com/p/fbug/issues/detail?id=1899
        var wizard = $('div#wizard')
        wizard.html('<span style="color:red;font-size:8px;">Firefox 3.6 contains <a href="http://code.google.com/p/fbug/issues/detail?id=2746" target="_new">a bug</a> in combination with Firebug\'s XMLHttpRequest spy which causes the wizard to not function anymore. Please make sure you have firebug\'s XMLHttpRequest spy disabled use use Firefox 3.5.7 instead...</span>' + wizard.html())
    }

    // attach Tooltips
    onWizardPage();
});

// runs when document is ready or a wizard action has been performs
// @see _wizard.gsp, _navigation.gsp, _subjects.gsp
function onWizardPage() {
    // attach help tooltips
    //insertYoutubePlayers();
    attachHelpTooltips();
    attachDatePickers();
    attachTableEvents();
    attachGroupingEvents();
}

// attach help tooltips
function attachHelpTooltips() {
    // attach help action on all wizard help icons
    $('div#wizard').find('div.helpIcon').each(function() {
        helpIcon = $(this)
        helpContent = helpIcon.parent().parent().find('div.helpContent')

        // handle special content
        var specialContent = helpContent.html().match(/\[([^:]+)\:([^\]]+)\]/)
        if (specialContent) {
            // replace content by calling a helper function
            eval(specialContent[1]+"('"+specialContent[2]+"',helpContent)");
        }

        // attach tooltip
        helpIcon.qtip({
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
                tip: true,
                name: 'blue'
            },
            content: helpContent.html(),
            show: 'mouseover',
            hide: 'mouseout',
            api: {
                beforeShow: function() {
                    // not used at this moment
                }
            }
        })

        // remove helpcontent div as we don't need it anymore
        helpContent.remove();
    });
}

// insert a youtube player in a certain element
function youtube(video,element) {
    // insert a div we will replace with a youtube player
    element.html("<div id='"+video+"'></div>")

    // insert youtube player
    var params = { allowScriptAccess: "always" };
    var atts = { id: 'myytplayer_'+video };
    swfobject.embedSWF("http://www.youtube.com/v/"+video+"?enablejsapi=1&playerapiid=ytplayer_"+video,
                       video, "200", "150", "8", null, null, params, atts)
}

// when a youtube player is ready, play the video
function onYouTubePlayerReady(playerId) {
    ytplayer = document.getElementById("my"+playerId);
    ytplayer.playVideo();
}

// add datepickers to date fields
function attachDatePickers() {
    $('div#wizard').find("input[type=text][name$='Date']").each(function() {
        $(this).datepicker({
            dateFormat  : 'dd/mm/yy',
            altField    : '#' + $(this).attr('name') + 'Example',
            altFormat   : 'DD, d MM, yy'
        });
    })
}

// attach subject events
function attachTableEvents() {
    $('div#wizard').find('div.row').each(function() {
        $(this).hover(
            function() {
                $(this).addClass('highlight');
            },
            function() {
                $(this).removeClass('highlight');
            }
        )
    });
}

function attachGroupingEvents() {
    console.log('attach drag and drop events')

    $(".groups").find('div.droppable').droppable({
		drop: function(event, ui) {
			$(this).addClass('ui-state-highlight').find('p').html('Dropped!');
		}
	});


    $(".subjects").find(".selectable").selectable({
        stop: function() {
            // remove draggable from unselected items
            $('.ui-selectee:not(.ui-selected)', this).each(function() {
                $(this).draggable('destroy')   
            })

            // attach draggable to selected items
            var subjects = $('.ui-selected', this);
            subjects.each(function() {
                var d = this
                var D = $(this)
                var content = D.html()
                var offset = D.offset()

                D.draggable({
                    revert: 'invalid',
                    start: function(event, ui) {
                        // change dragged item's content to summarize selected items
                        D.html(subjects.length + ' subjects');

                        // hide the other items
                        subjects.each(function() {
                            if (this != d) {
                                $(this).animate(
                                    {
                                        opacity: 0
                                    },
                                    200
                                );
                            }
                        });
                    },                  
                    stop: function(event, ui) {
                        // restore original content
                        D.html(content);

                        // make selected items visible
                        subjects.each(function() {
                            if (this != d) {
                                $(this).animate(
                                    {
                                        opacity: 100
                                    },
                                    200
                                );
                            }
                        });
                    }
                });
            });
        }
    });
}

