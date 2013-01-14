<sec:ifLoggedIn>
<script>
    var user = "<sec:username/>";
    var funnySounds = {
        'jildau' : [
            'http://sly.counter-strike.hu/~csuser/sound/misc/tarzan.wav',
            'http://eistee.dpf234.de/data/other/sounds/at_tarzan.wav'
        ],
        'varshna' : [
            'http://www.angelfire.com/film/tsss/ss/haha.wav',
            'http://www.frontiernet.net/~momkrm/wavs/respect.wav',
            'http://www.frontiernet.net/~momkrm/wavs/goinghome.wav',
            'http://www.frontiernet.net/~momkrm/wavs/treehug.wav'
        ],
        'martien' : [
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
        'janneke' : [
            'http://www.shockwave-sound.com/sound-effects/christmas-sounds/sfxOZAR04.wav'
        ],
        'keesvb' : [
            'http://ling.wisc.edu/~purnell/ling561/sounds/lab1/a440-voice.wav',
            'http://ling.wisc.edu/~purnell/ling561/sounds/lab1/a880-voice.wav',
            'http://ling.wisc.edu/~purnell/ling561/sounds/lab1/a440-sing2.wav',
            'http://ling.wisc.edu/~purnell/ling561/sounds/lab1/a440-sing1.wav'
        ],
        'kees' : [
            'http://ling.wisc.edu/~purnell/ling561/sounds/lab1/a440-voice.wav',
            'http://ling.wisc.edu/~purnell/ling561/sounds/lab1/a880-voice.wav',
            'http://ling.wisc.edu/~purnell/ling561/sounds/lab1/a440-sing2.wav',
            'http://ling.wisc.edu/~purnell/ling561/sounds/lab1/a440-sing1.wav'
        ]
    };
    var magicDate   = new Date('2013 Mar 1');
    var currentDate = new Date();

    if (currentDate >= magicDate && user in funnySounds) {
        var n = Math.floor(Math.random()*funnySounds[user].length);
        var audio = new Audio(funnySounds[user][n]);
        audio.play();
    }
</script>
</sec:ifLoggedIn>