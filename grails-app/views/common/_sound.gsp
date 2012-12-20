<sec:ifLoggedIn>
<script>
    var user = "<sec:username/>";
    var funnySounds = {
        'ferry' : 'http://www.blotto-online.com/blotto/snd/callotc.wav'
    };
    if (user in funnySounds) {
        var audio = new Audio(funnySounds[user]);
        audio.play();
    }
</script>
</sec:ifLoggedIn>