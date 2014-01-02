var wsUri = "ws://localhost:9000/events";

function init() {
    websocket = new WebSocket(wsUri);
    websocket.onmessage = function(evt) { onMessage(evt) };
}
function onMessage(evt) {
    console.log(evt.data);

    var uiEvent = JSON.parse(evt.data);

    var strip;
    if(uiEvent.step != ''){
        strip = detachOrCreate(uiEvent.flightName);

        var zone = $("#"+uiEvent.step);

        zone.append(strip);
    }

    if(strip == undefined){
        strip = find(uiEvent.flightName);
    }

    strip.find(".detail").html(uiEvent.detail);

    if(uiEvent.detail.length >= 5 && uiEvent.detail.substring(0,5) == 'Error'){
       strip
       .addClass('error')
       .removeClass('regular');
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
