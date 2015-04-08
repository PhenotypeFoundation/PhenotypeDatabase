
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
