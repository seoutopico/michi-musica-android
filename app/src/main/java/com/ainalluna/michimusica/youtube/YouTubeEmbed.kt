package com.ainalluna.michimusica.youtube

/** Only validated IDs and numeric values enter this local page; result titles never become HTML. */
internal fun youtubeEmbedHtml(id: String, appId: String, seconds: Double, autoplay: Boolean): String {
    require(Regex("[A-Za-z0-9_-]{11}").matches(id))
    require(Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+").matches(appId))
    val start = if (seconds.isFinite()) seconds.coerceIn(0.0, Int.MAX_VALUE.toDouble()).toInt() else 0
    return """
        <!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1">
        <meta name="referrer" content="strict-origin-when-cross-origin">
        <style>html,body{margin:0;background:#000;overflow:hidden}#player{display:block;border:0}</style>
        </head><body><div id="player"></div><script>
        var player, allowed=${autoplay}, ready=false;
        function report(kind,value){MichiVideo.event(kind,String(value));}
        function pauseMichiVideo(){allowed=false;if(ready){player.pauseVideo();report('time',player.getCurrentTime());}}
        // WebView can resolve both percentage and vh heights to zero on its first Compose layout.
        // Use the measured viewport in CSS pixels, and resize the same iframe without reloading it.
        function sizeMichiVideo(){
          if(document.fullscreenElement)return;
          var w=Math.max(1,window.innerWidth),h=Math.max(1,window.innerHeight);
          document.documentElement.style.width=w+'px';document.documentElement.style.height=h+'px';
          document.body.style.width=w+'px';document.body.style.height=h+'px';
          var frame=document.getElementById('player');
          if(frame){frame.style.width=w+'px';frame.style.height=h+'px';}
        }
        window.addEventListener('resize',sizeMichiVideo);
        if(window.visualViewport)window.visualViewport.addEventListener('resize',sizeMichiVideo);
        document.addEventListener('fullscreenchange',sizeMichiVideo);
        function onYouTubeIframeAPIReady(){
          sizeMichiVideo();
          player=new YT.Player('player',{width:Math.max(1,window.innerWidth),height:Math.max(1,window.innerHeight),videoId:'$id',
            playerVars:{playsinline:1,controls:1,fs:1,autoplay:0,start:$start,origin:'https://$appId'},
            events:{onReady:function(e){ready=true;sizeMichiVideo();report('ready',0);if(allowed)e.target.playVideo();},
              onStateChange:function(e){report('state',e.data);report('time',e.target.getCurrentTime());},
              onError:function(e){report('error',e.data);}}});
        }
        setInterval(function(){if(ready)report('time',player.getCurrentTime());},1000);
        </script><script src="https://www.youtube.com/iframe_api" onerror="report('error',0)"></script></body></html>
    """.trimIndent()
}

internal fun youtubeEmbedError(code: Int): String = when (code) {
    100 -> "Este vídeo ya no está disponible o es privado."
    101, 150 -> "Este vídeo solo se puede reproducir en YouTube."
    153 -> "YouTube no ha podido abrir su reproductor aquí. Puedes verlo en YouTube."
    else -> "No se pudo abrir el vídeo. Comprueba la conexión o prueba en YouTube."
}
