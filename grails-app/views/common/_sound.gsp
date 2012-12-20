<sec:ifLoggedIn>
<script>
    var user = "<sec:username/>";
    var funnySounds = {
        'ferry' : [
            'http://www.blotto-online.com/blotto/snd/callotc.wav',
            'http://www.reocities.com/hollywood/studio/5352/sounds/show.wav',
            'http://www.jiskefet.net/jiskefet/geluiden/09_raar.wav'
        ],
        'jordy' : [
            'http://www.heptune.com/fartmachine.wav',
            'http://antifart.com/sounds/fart88.wav',
            'http://soundmachine.gooddogie.com/sounds/wav/burp2.WAV',
            'http://cd.textfiles.com/10000soundssongs/WAV/BURP.WAV',
            'http://www.newexpression.com/lara/web/sounds/body/chewburp.wav',
            'http://owling.com/ggo1a.wav'
        ],
        'duh'   : ['http://www.shockwave-sound.com/sound-effects/christmas-sounds/Emotisound-hoho.wav'],
        'janneke' : [
            'http://www.shockwave-sound.com/sound-effects/christmas-sounds/sfxOZAR04.wav'
        ],
        'keesvb' : [
            'http://mixonline.com/ai/sounds/hollywood_edge/ComputerHighWhir.wav',
            'http://www.mediacollege.com/downloads/sound-effects/household/fan-02.wav'
        ]
    };
    if (user in funnySounds) {
        var n = Math.floor(Math.random()*funnySounds[user].length);
        var audio = new Audio(funnySounds[user][n]);
        audio.play();
    }
</script>
</sec:ifLoggedIn>