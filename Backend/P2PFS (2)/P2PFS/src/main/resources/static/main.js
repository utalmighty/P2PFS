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
let key = ""
let isSender = false

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

peerConnection.ondatachannel =e =>{
    peerConnection.dataChannel = e.channel;
    peerConnection.dataChannel.onmessage = e => onReceiveMessageCallback(e);
    peerConnection.dataChannel.onopen = e => console.log("Connection Opened," + e);
}

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

function copyToClipboard() {
    navigator.clipboard.writeText(keyInput.value);
}

async function makeOffer() {
    const fileInput = document.getElementById("fileupload");
    if (fileInput.files.length == 0) {
        alert("Select file first");
        return;
    }
    isSender = true
    callButton.value = "Copy";
    callButton.onclick = e=> navigator.clipboard.writeText(keyInput.value);
    file = fileInput.files[0];
    console.log(`File is ${[file.name, file.size, file.type, file.lastModified].join(' ')}`);
    // Handle 0 size files.
    //statusMessage.textContent = '';
    //downloadAnchor.textContent = '';
    if (file.size === 0) {
      //bitrateDiv.innerHTML = '';
      //statusMessage.textContent = 'File is empty, please select a non-empty file';
      return;
    }
    peerConnection.dataChannel = peerConnection.createDataChannel("dataChannel");
    console.log(peerConnection.dataChannel.readyState);
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
        peerConnection.addIceCandidate(candidate).then(()=> {
            setTimeout(() => {
                console.log(peerConnection.dataChannel.readyState)
                if (peerConnection.dataChannel.readyState == "open") {
                    sendData();
                }
            }, 1000);
        })
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
    if (isSender){
        key = document.getElementById("messageInput").value;
        peer = "offer";
    }
    console.log("After >"+key+"< peer:" + peer);
    if (!isSender){
        signalingChannel.send("/app/candidate", {"id": key, "peer": peer}, JSON.stringify(candidate));
    }
}

function sendData() {
    //sendProgress.max = file.size;
    //receiveProgress.max = file.size;
    const chunkSize = 16384;
    fileReader = new FileReader();
    let offset = 0;
    fileReader.addEventListener('error', error => console.error('Error reading file:', error));
    fileReader.addEventListener('abort', event => console.log('File reading aborted:', event));
    fileReader.addEventListener('load', e => {
      console.log('FileRead.onload ', e);
      peerConnection.dataChannel.send(e.target.result);
      offset += e.target.result.byteLength;
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
    //receiveProgress.value = receivedSize;

    // we are assuming that our signaling protocol told
    // about the expected file size (and name, hash, etc).
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