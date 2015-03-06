/**
 * attach tooltips to tooltip icons
 *
 * example:
 * <div>
 *   <div class="helpIcon"></div>
 * </div>
 * <div class="helpContent">Lorem ipsum...</div>
 *
 * @author  Jeroen Wesbeek
 * @since   20100115
 * @package wizard
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

//attach help tooltips
function attachHelpTooltips() {
    // attach help action on all wizard help icons
    $('div.helpIcon').each(function() {
        helpIcon = $(this);
        helpContent = helpIcon.parent().find('div.helpContent');
        if (!helpContent.html()) {
            helpContent = helpIcon.parent().parent().find('div.helpContent');
        }

        // handle special content
        var html = (helpContent.html()) ? helpContent.html() : '';
        if (html) {
            var specialContent = html.match(/\[([^:]+)\:([^\]]+)\]/);
            if (specialContent) {
                // replace content by calling a helper function
                eval(specialContent[1] + "('" + specialContent[2] + "',helpContent)");
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
            });

            // remove helpcontent div as we don't need it anymore
            helpContent.remove();
        }
    });
}

// insert a youtube player in a certain element
function youtube(video, element) {
    // insert a div we will replace with a youtube player
    element.html("<div id='" + video + "'></div>");

    // insert youtube player
    var params = { allowScriptAccess: "always" };
    var atts = { id: 'myytplayer_' + video };
    swfobject.embedSWF("http://www.youtube.com/v/" + video + "?enablejsapi=1&playerapiid=ytplayer_" + video,
            video, "200", "150", "8", null, null, params, atts);
}

// when a youtube player is ready, play the video
function onYouTubePlayerReady(playerId) {
    ytplayer = document.getElementById("my" + playerId);
    ytplayer.playVideo();
}