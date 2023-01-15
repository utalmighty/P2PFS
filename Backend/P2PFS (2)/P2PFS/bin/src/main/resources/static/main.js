const config = {
    'iceServers': [{
      'urls': ['stun:stun.l.google.com:19302']
    }]
  };

const peerConnection = new RTCPeerConnection(config);

let signalingChannel = null;


function onLoadScript() {
    connectToSockets();
}


function connectToSockets() {
    let socket = new SockJS('/upgrade');
    signalingChannel = Stomp.over(socket);  
    signalingChannel.connect({}, function(frame) { 
        signalingChannel.subscribe('/topic/count', function(messageOutput) {
            updateCount(JSON.parse(messageOutput.body));
        })
        signalingChannel.subscribe('/user/queue/send', function(messageOutput) {
            privateMessageIncomingLogic(messageOutput.body);
        })
        getUpdatedCount();
        //sendPrivateMessage("eg");
    });   
}

async function makeOffer() {
    const offer = await peerConnection.createOffer();
    await peerConnection.setLocalDescription(offer);
    console.log("Offer Created: ", offer);
    sendOffer(offer);
}

async function search() {
    let key = document.getElementById("keyInput").value;
    sendSearch(key);
}

async function privateMessageIncomingLogic(messageBody) {
    let message = JSON.parse(messageBody);
    if (message.id) {
        console.log("Received Id",  message.id);
        document.getElementById("messageInput").value = message.id;
    }
    else if (message.offer){
        console.log("Received Offer ", message.offer);
        const remoteDesc = new RTCSessionDescription(message.offer);
        await peerConnection.setRemoteDescription(remoteDesc);
        let key = document.getElementById("keyInput").value;
        const answer = await peerConnection.createAnswer();
        await peerConnection.setLocalDescription(answer);
        console.log("Answer Created: ", answer);
        sendAnswer(key, answer);
    }
    else if(message.answer) {
        console.log("Received Answer ", message.answer);
        const remoteDesc = new RTCSessionDescription(message.answer);
        await peerConnection.setRemoteDescription(remoteDesc);
    }
}

function updateCount(message) {
    document.getElementById("countInput").value = message["count"];
}

function getUpdatedCount() {
    signalingChannel.send("/app/count");
}

function sendPrivateMessage(message) {
    signalingChannel.send("/app/private");
}

function sendOffer(offer) {
    signalingChannel.send("/app/offer", {}, offer.sdp); 
}

function sendAnswer(id, answer) {
    signalingChannel.send("/app/answer", {"id": id}, answer.sdp);
}

function sendSearch(id) {
    signalingChannel.send("/app/search", {"id": id}, "remove me");
}