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
    var d = ''
    d += '<input type="button" class="prevnext" value="reload CSS" onclick="reloadCSS();return false;"/>'

    // insert into footer
    $('#footer').html(d + "<br/>"+$('#footer').html())
});

function reloadCSS() {
    console.log('reloading css...');

    var i,a,s;
    a=document.getElementsByTagName('link');
    for(i=0;i<a.length;i++) {
        s=a[i];
        if(s.rel.toLowerCase().indexOf('stylesheet')>=0&&s.href) {
            var h=s.href.replace(/(&|\\?)forceReload=d /,'');
            s.href=h+(h.indexOf('?')>=0?'&':'?')+'forceReload='+(new Date().valueOf());
        }
    }
}
