/**
 * development functions
 *
 * @author  Jeroen Wesbeek
 * @since   20100128
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
$(document).ready(function() {
    var d = '<b>development:</b> '
    d += '<input type="button" class="prevnext" value="reload CSS" onclick="reloadCSS();return false;"/>'
    d += '<input type="button" class="prevnext" value="reload JS" onclick="reloadJS();return false;"/>'
    d += '<input type="button" class="prevnext" value="reload Both" onclick="reloadBoth();return false;"/>'

    // insert into footer
    $('#footer').html(d + "<br/>" + $('#footer').html())
});

function reloadBoth() {
    reloadCSS();
    reloadJS();
}

function reloadCSS() {
    console.log('reloading css...');

    var i,a,s;
    a = document.getElementsByTagName('link');
    for (i = 0; i < a.length; i++) {
        s = a[i];
        if (s.rel.toLowerCase().indexOf('stylesheet') >= 0 && s.href) {
            var h = s.href.replace(/(&|\\?)forceReload=d /, '');
            s.href = h + (h.indexOf('?') >= 0 ? '&' : '?') + 'forceReload=' + (new Date().valueOf());
        }
    }
}

function reloadJS() {
    console.log('reloading js...');

    var i,j,a,s;
    var head = document.getElementsByTagName('head')[0];
    var ignore = [
        /development.js/,
        /topnav.js/,
        /jquery/,
        /login_panel.js/
    ];

    a = document.getElementsByTagName('script');
    for (i = a.length - 1; i >= 0; i--) {
        s = a[i];

        // check if this script should be skipped
        var deleteIt = true;
        for (j = 0; j < ignore.length; j++) {
            if (ignore[j].test(s.src)) {
                deleteIt = false;
                break;
            }
        }

        // delete this script and reload it?
        if (deleteIt) {
            // delete it
            head.removeChild(s)

            // and load it again
            var script = document.createElement('script');
            script.type = 'text/javascript';
            script.src = s.src
            head.appendChild(script);
        }
    }
}
