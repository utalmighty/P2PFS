
var countClient = null;

function onLoadScript() {
    connectToCountSocket();
}

function connectToCountSocket() {
    var socket = new SockJS('/upgrade');
    countClient = Stomp.over(socket);  
    countClient.connect({}, function(frame) { 
        countClient.subscribe('/topic/count', function(messageOutput) {
            updateCount(JSON.parse(messageOutput.body));
        })
        countClient.send("/app/count");
    });    
}

function updateCount(message) {
    $("#count").append("<tr><td>" + message["count"] + "</td></tr>");
}