
var countClient = null;
var privateClient = null;

function onLoadScript() {
    //connectToCountSocket();
    connectToPrivateSocket();
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

function connectToPrivateSocket() {
    var socket = new SockJS('/upgrade');
    privateClient = Stomp.over(socket);  
    privateClient.connect({}, function(frame) { 
        privateClient.subscribe('/user/queue/send', function(messageOutput) {
            updatePrivateMessage(messageOutput.body);
        })
        privateClient.send("/app/private");
    });    
}

function updatePrivateMessage(message) {
    $("#private").append("<tr><td>" + message + "</td></tr>");
}

function updateCount(message) {
    $("#count").append("<tr><td>" + message["count"] + "</td></tr>");
}

function sendPrivateHello() {
    console.log("Sending private message");
    privateClient.send("/app/private");
}