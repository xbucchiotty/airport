var wsUri = "ws://localhost:9000/events";
var $incoming;
var $runway;
var $taxiway;
var $gate;
var $done;

function init() {
    $incoming = $('#incoming');
    $runway = $('#runway');
    $taxiway = $('#taxiway');
    $gate = $('#gate');
    $done = $('#done');
    websocket = new WebSocket(wsUri);
    websocket.onmessage = function(evt) { onMessage(evt) };
}
function onMessage(evt) {
    console.log(evt.data);
    if(evt.data.substring(0,4) === 'add:'){
        addFlight(evt.data.substring(4));
    } else
    if(evt.data.substring(0,6) === 'crash:'){
        crash(evt.data.substring(6));
    } else
    if(evt.data.substring(0,7) === 'landed:'){
        land(evt.data.substring(7));
    } else
    if(evt.data.substring(0,5) === 'taxi:'){
        taxi(evt.data.substring(5));
    } else
    if(evt.data.substring(0,5) === 'park:'){
        park(evt.data.substring(5));
    } else
    if(evt.data.substring(0,6) === 'leave:'){
        leave(evt.data.substring(6));
    } else
    if(evt.data.substring(0,10) === 'collision:'){
        collision(evt.data.substring(10));
    }
}

function doSend(message) {
    websocket.send(message);
}

function create(flightName){
   return  $('<li>',{
        html: '</i><p class="id"><i class="icon-plane"></i> '+flightName+'</p>',
        id: flightName,
        class: 'strip'
    }).addClass('regular');
}

function find(flightName){
    return $('#'+flightName);
}

function detach(flightName){
    return find(flightName).detach();
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

function crash(flightName){
     var newItem = $('<p>',{
        html : 'OUT OF KEROZEN'
    });

    find(flightName)
        .addClass('error')
        .removeClass('regular')
        .append(newItem);
}

function collision(flightName){
     var newItem = $('<p>',{
        html : 'COLLISION'
    });

    find(flightName)
        .addClass('error')
        .removeClass('regular')
        .append(newItem);
}

function land(flightName){
    var strip = detachOrCreate(flightName);

    $runway.append(strip);
}

function taxi(flightName){
    var strip = detachOrCreate(flightName);

    $taxiway.append(strip);
}

function park(flightName){
    var strip = detachOrCreate(flightName);

    $gate.append(strip);
}

function leave(flightName){
    var strip = detachOrCreate(flightName);

    $done.append(strip);
}

window.addEventListener("load", init, false);
