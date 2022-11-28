
var stompClient = null;

function onLoadScript() {
    connectToCountSocket();
}


function connectToCountSocket() {
    var socket = new SockJS('/upgrade');
    stompClient = Stomp.over(socket);  
    stompClient.connect({}, function(frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/count', function(messageOutput) {
            showCount(JSON.parse(messageOutput.body["count"]));
        });
    });
    stompClient.send("/app/count");
}

function showCount(message) {
    $("#count").append("<tr><td>" + message + "</td></tr>");
}