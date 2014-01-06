var wsUri = "ws://localhost:9000/events";

function init() {
    websocket = new WebSocket(wsUri);
    websocket.onmessage = function(evt) { onMessage(evt) };
}
function onMessage(evt) {
    console.log(evt.data);

    var uiEvent = JSON.parse(evt.data);

    if(uiEvent.type === 'PlaneStatus'){
        onPlaneStatus(uiEvent);
    }
    else if (uiEvent.type === 'Score'){
        onScore(uiEvent);
    }
    else if (uiEvent.type === 'GameOver'){
        onGameOver(uiEvent);
    }
    else if (uiEvent.type === 'GameEnd'){
        onGameEnd(uiEvent);
    }
}

function onGameOver(uiEvent){
    websocket.close();
    alert("Looser");
}

function onGameEnd(uiEvent){
    websocket.close();
    alert("You won");
}

function onScore(newScore){
    $("#score")
    .find("#counter")
    .html(newScore.current + " / " + newScore.objective);

    $("#score").find(".bar").css('width',''+ (100 * newScore.current / newScore.objective) + '%');
}

function onPlaneStatus(uiEvent){
    var strip;

    if(uiEvent.step != ''){
        strip = detachOrCreate(uiEvent.flightName);

        var zone = $("#"+uiEvent.step);

        zone.append(strip);

    }

    if(strip == undefined){
        strip = find(uiEvent.flightName);
    }

    if(uiEvent.error != ''){
        strip
            .addClass('error')
            .removeClass('regular')
            .find(".detail")
            .html(uiEvent.error);

        var done = $("#done");

        done.append(strip.detach());

    }else{
        strip.find(".detail")
            .html(uiEvent.detail);
    }
}

function doSend(message) {
    websocket.send(message);
}

function create(flightName){
   return  $('<li>',{
        html: '</i><p class="id"><i class="icon-plane"></i> '+flightName+'</p><p class="detail"></p>',
        id: flightName,
        class: 'strip'
    }).addClass('regular');
}

function find(flightName){
    return $('#'+flightName);
}

function detach(flightName){
    var strip = find(flightName);

    if(strip[0])
        return strip.detach();
}

function detachOrCreate(flightName){
    var detached = detach(flightName);

    if(detached)
        return detached;
    else
        return create(flightName);
}

function addFlight(flightName){
    $incoming.append(create(flightName));
}

window.addEventListener("load", init, false);
