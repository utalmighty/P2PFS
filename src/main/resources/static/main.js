const config = {
    'iceServers': [{
      'urls': ['stun:stun.l.google.com:19302']
    }]
}

const peerConnection = new RTCPeerConnection(config)
let signalingChannel = null
let receiveBuffer = []
let upcomingFileSize = 0
let upcomingFileName = ""
let receivedSize = 0
let count = 0
let flip = null
let keyInput = document.getElementById("keyInput")
let makeOfferButton = document.getElementById("makeOfferButton")
let callButton = document.getElementById("callButton")
let popup = document.getElementsByClassName("popup")[0];
let about = document.getElementById("about");
let flipCredits = document.getElementsByClassName("tick-credits")[0];
let key = ""
let isSender = false
let sending = false
let aboutStatus = false
let popupStatus = false

makeOfferButton.onclick = function () {
    makeOffer()
}

callButton.onclick = function () {
    sendSearch();
}

keyInput.onchange = function () {
    key = keyInput.value;
}

peerConnection.onicecandidate = function(event) {
    if (event.candidate) {
        console.log("Sending ice candaidate to peer:", event.candidate);
        sendCandidate(event.candidate);
    }
}

function onLoadScript() {
    if (flipCredits != null ) {
        flipCredits.remove();
    }
    connectToSockets();
}

function popupLogic() {
    if (!popupStatus) {
        popup.style.display = "block";
        popupStatus = true
        aboutStatus = true
    }
}

window.addEventListener('click', function(e){  
    if (aboutStatus) {
        aboutStatus = false
    }
    else if(!popup.contains(e.target)){
        popupStatus  = false
        popup.style.display = "none";
    }
});

function copyToClipboard() {
    navigator.clipboard.writeText(keyInput.value);
}

peerConnection.ondatachannel = e =>{
    peerConnection.dataChannel = e.channel;
    peerConnection.dataChannel.onmessage = e => onReceiveMessageCallback(e);
}

function connectToSockets() {
    let socket = new SockJS('/upgrade');
    signalingChannel = Stomp.over(socket);
    signalingChannel.connect({}, function(frame) { 
        signalingChannel.subscribe('/topic/count', function(messageOutput) {
            updateCount(JSON.parse(messageOutput.body));
        })
        signalingChannel.subscribe('/topic/error', function(messageOutput) {
            publicError(JSON.parse(messageOutput.body));
        })
        signalingChannel.subscribe('/user/queue/send', function(messageOutput) {
            privateMessageIncomingLogic(messageOutput.body);
        })
        getUpdatedCount();
    });   
}

async function makeOffer() {
    const fileInput = document.getElementById("fileupload");
    if (fileInput.files.length == 0) {
        alert("Please select a file to share!");
        return;
    }
    isSender = true
    file = fileInput.files[0];
    console.log(`File is ${[file.name, file.size, file.type, file.lastModified].join(' ')}`);
    if (file.size === 0) {
      alert("File is empty, please select a non-empty file");
      return;
    }
    peerConnection.dataChannel = peerConnection.createDataChannel("dataChannel");
    peerConnection.dataChannel.addEventListener('open', function name() {
        if (!sending) {
            sending = true;
            sendData();
        }})
    const offer = await peerConnection.createOffer();
    await peerConnection.setLocalDescription(offer);
    console.log("Offer Created: ", offer);
    sendOffer(offer);
}

async function privateMessageIncomingLogic(messageBody) {
    let message = JSON.parse(messageBody);
    if (message.id) {
        console.log("Received Id",  message.id);
        keyInput.value = message.id;
        callButton.value = "COPY";
        callButton.onclick = e=> navigator.clipboard.writeText(message.id);
    }
    else if (message.offer){
        console.log("Received Offer ", message.offer);
        console.log(message.filesize, message.filename);
        upcomingFileSize = message.filesize;
        upcomingFileName = message.filename;
        const remoteDesc = new RTCSessionDescription(message.offer);
        await peerConnection.setRemoteDescription(remoteDesc);
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
    else if(message.icecandidate) {
        let candidate = JSON.parse(message.icecandidate);
        console.log("Received Candidate of Peer", candidate);
        const remoteDesc = new RTCIceCandidate(candidate);
        peerConnection.addIceCandidate(candidate)
    }
    else if (message.error) {
        alert(message.error)
    }
}

function updateCount(message) {
    count = message["count"];
    flip.value = count;
}

function incrementCount(uniqueId) {
    signalingChannel.send("/app/updateCount/increment", {"id": uniqueId})
}

function getUpdatedCount() {
    signalingChannel.send("/app/count");
}

function sendPrivateMessage(message) {
    signalingChannel.send("/app/private");
}

function sendOffer(offer) {
    signalingChannel.send("/app/offer", {"filename": file.name, "filesize": file.size}, offer.sdp); 
}

function sendAnswer(id, answer) {
    signalingChannel.send("/app/answer", {"id": id}, answer.sdp);
}

function sendSearch() {
    if (key.trim().length <= 0){
        alert("Please enter the unique key")
        return
    }
    signalingChannel.send("/app/search", {"id": key}, "remove me");
}

function sendCandidate(candidate) {
    let peer = "answer";
    if (isSender) peer = "offer";
    else signalingChannel.send("/app/candidate", {"id": key, "peer": peer}, JSON.stringify(candidate));
}

function sendData() {
    const chunkSize = 16384;
    fileReader = new FileReader();
    let offset = 0;
    fileReader.addEventListener('error', error => console.error('Error reading file:', error));
    fileReader.addEventListener('abort', event => console.log('File reading aborted:', event));
    fileReader.addEventListener('load', e => {
      console.log('FileRead.onload ', e);
      peerConnection.dataChannel.send(e.target.result);
      offset += e.target.result.byteLength;
      callButton.value = percent(offset, file.size);
      //sendProgress.value = offset;
      if (offset < file.size) {
        readSlice(offset);
      }
    });
    const readSlice = o => {
      console.log('readSlice ', o);
      const slice = file.slice(offset, o + chunkSize);
      fileReader.readAsArrayBuffer(slice);
    };
    readSlice(0);
}

function onReceiveMessageCallback(event) {
    const downloadAnchor = document.getElementById("download");
    console.log(`Received Message ${event.data.byteLength}`);
    receiveBuffer.push(event.data);
    receivedSize += event.data.byteLength;
    callButton.value = percent(receivedSize, upcomingFileSize);
    if (receivedSize === upcomingFileSize) {
    const received = new Blob(receiveBuffer);
    receiveBuffer = [];

    downloadAnchor.href = URL.createObjectURL(received);
    downloadAnchor.download = upcomingFileName;
    downloadAnchor.textContent =
        `Click to download '${upcomingFileName}' (${upcomingFileSize} bytes)`;
    downloadAnchor.style.display = 'block';
    // TODO: Close data channel
    incrementCount(key);
    closeDataChannels();
    }
}

function setupFlip(tick) {
    flip = tick;
}

function percent(value, max) {
    let per = parseInt((value/max)*100)
    return per+"%";
}