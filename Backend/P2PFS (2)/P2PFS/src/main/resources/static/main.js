
var socketClient = null;

function onLoadScript() {
    connectToSockets();
}

function connectToSockets() {
    var socket = new SockJS('/upgrade');
    socketClient = Stomp.over(socket);  
    socketClient.connect({}, function(frame) { 
        socketClient.subscribe('/topic/count', function(messageOutput) {
            updateCount(JSON.parse(messageOutput.body));
        })
        socketClient.subscribe('/user/queue/send', function(messageOutput) {
            console.log("First Me");
            updatePrivateMessage(messageOutput.body);
        })
        socketClient.send("/app/count");
        socketClient.send("/app/private");
    });   
}

function updatePrivateMessage(message) {
    document.getElementById("messageInput").value = message;
}

function updateCount(message) {
    document.getElementById("countInput").value = message["count"];
}

function sendPrivateHello() {
    console.log("Sending private message");
    socketClient.send("/app/private");
}