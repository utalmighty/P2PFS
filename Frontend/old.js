const config = {
    'iceServers': [{
      'urls': ['stun:stun.l.google.com:19302']
    }]
  };

const peerConnections = {};
contestents = [];
maximumparties = 1; // Means Only two Peer can be connected(Excluding yourself)
mysid = '';
queue = 0;
insfu = false;

const constraints = { // media devices constraints
    audio:true,
    video: {width: {min: 320, max: 640}, height: {min: 240, max: 480}, frameRate: {max: 15}}
};

const localVideo = document.querySelector('.localVideo');
const remoteVideos = document.querySelector('.remoteVideos');
//const instantMeter = document.querySelector('#instant meter');
socket=io.connect();//default domain- starting socket connection between server(flask) and client
knowmysid();// first function to run.
// try {
//     window.AudioContext = window.AudioContext || window.webkitAudioContext;
//     window.audioContext = new AudioContext();b
//     alert('Web Audio API not supported.');
//   }


navigator.mediaDevices.getUserMedia(constraints)
.then(function(stream){ // if success in getting media devices
    localVideo.srcObject = stream;// adding stream to video Element
})
.catch(error => console.log(error));

function handleRemoteStreamAdded(stream, id){
    console.log("handling Remote Stream Added ,Number of Contestents ", contestents.length);
    if(insfu == true){
        console.log("here ALL THE AUDIO Tracks", stream.getAudioTracks(), "length = ", stream.getAudioTracks().length);
        stream.getAudioTracks()[queue].enabled = false;
    }
    document.getElementById('joiningbuttonid').disabled = false;
    document.getElementById('joiningbuttonid').textContent = 'Connect';
    const remoteVideo = document.createElement('video');
    document.getElementById('camera').style.display = 'none';
    document.getElementById('screen').style.display = 'none';
    remoteVideo.srcObject = stream;
    remoteVideo.setAttribute("id", id.replace(/[^a-zA-Z]+/g, "").toLowerCase());
    remoteVideo.setAttribute("playsinline", "true");
    remoteVideo.setAttribute("autoplay", "true");
    remoteVideos.appendChild(remoteVideo);
    localVideo.style.right = '4px';
    localVideo.style.bottom = '4px';
    localVideo.style.width = '100px';
    localVideo.style.margin = '10px';
    localVideo.style.height = '75px';
    localVideo.style.position = 'relative';
    if (remoteVideos.querySelectorAll("video").length === 1) {
      remoteVideos.setAttribute("class", "one remoteVideos");
    } else {
      remoteVideos.setAttribute("class", "remoteVideos");
    }
}


function muteunmute(track, mvalue){// track='a' or 'v', True= Unmute, False = Mute
    if (track == 'a'){
        if(mvalue == true){
        document.getElementById('unmutemicid').style.display = 'block';
        document.getElementById('mutemicid').style.display = 'none';
        }
        else{
            document.getElementById('unmutemicid').style.display = 'none';
            document.getElementById('mutemicid').style.display = 'block';
        }
        localVideo.srcObject.getAudioTracks()[0].enabled = mvalue;
    }
    else{
        if(mvalue == true){
            document.getElementById('unmutevidid').style.display = 'block';
            document.getElementById('mutevidid').style.display = 'none';
            }
            else{
                document.getElementById('unmutevidid').style.display = 'none';
                document.getElementById('mutevidid').style.display = 'block';
                }
        localVideo.srcObject.getVideoTracks()[0].enabled = mvalue;
    }
}

function sendcred(){ // Sign IN Credentials.
    username = document.getElementById('usernameid').value;
    password = document.getElementById('passwordid').value;
    if (username.length > 0 && password.length > 0){
        socket.emit('Credentials', {'creator':true, 'username': username, 'password':password}); // Sending Cred to server
    }
    else{
        alert('Username or Password can not be null!');
    }
}

socket.on('flashing', function(mess){ // Once server checks up for username and password it sends message which is displayed as alert
    if (mess['messageid'] == 3){
        document.getElementById('joiningbuttonid').disabled = false;
        document.getElementById('joiningbuttonid').textContent = 'Connect';
        alert(mess['message']);
    }
    else if (mess['messageid'] == 4){
        document.getElementById('joiningbuttonid').disabled = false;
        document.getElementById('joiningbuttonid').textContent = 'Connect';
        alert(mess['from']+' '+mess['message']);
        dec(mess['id']);
    } 
    else{
        alert(mess['message']);
    }
})


function sendjoin(){ // to send Join request
    username = document.getElementById('joinusernameid').value;
    password = document.getElementById('joinpasswordid').value;
    if(username == document.getElementById('usernameid').value){
        alert('You can not call yourself!');
    }
    else if (username.length == 0 || password.length == 0){
        alert('Username or Password can not be null.');
    }
    else if(contestents.length < maximumparties){
        document.getElementById('joiningbuttonid').disabled = true;
        socket.emit('Credentials', {'creator':false, 'username': username, 'password':password});
    }
    else{
        console.log('Joining SFU beacuse number of contestesnts = ', contestents.length);
        socket.emit('joinsfu', {'to': contestents})// remember to send yourself also
        //alert('Maximum number of candidates limited to: ' + maximumparties+ 'therefore joining SFU');
    }
}

function knowmysid(){
    socket.emit('whatsmysid');
}

socket.on('yoursidis', function(message){
    mysid = message['sid']; // Global or NOT
    console.log('my sid is', mysid);
})

function direct(){//remove this
    socket.emit('joinsfu', {'to': [mysid]})// remember to send yourself also
}


socket.on('Credentials', function(calleesid){// received the cred of callee. once server checks up for the opposite peer on its database server sends back Session ID of other person
    makeoffer(calleesid); // offer make function is called
});

function makeoffer(calleesid){ // offer making
    console.log('Sending Offer to: ',calleesid);
    document.getElementById('joiningbuttonid').textContent = 'Calling.';
    const peerConnection = new RTCPeerConnection(config); //write config in (). //RTCPeerConnection Object is created.
    peerConnections[calleesid] = peerConnection;
    peerConnection.addStream(localVideo.srcObject);
    peerConnection.createOffer() // Offer is Created(Promise based) {'offerToReceiveAudio':true,'offerToReceiveVideo':true}
    .then(sdp => peerConnection.setLocalDescription(sdp))//SDP: Session Discription Protocol: it contains many information of Peer.
    .then(function(){
        console.log('Number of Contestents(to confirm Join sfu or not)', contestents.length);
        if (contestents.length>0){ // if already in  meeting and sending one more request.
            socket.emit('offer+1', {'to': calleesid, 'message': peerConnection.localDescription, 'more': contestents});//sending callee to also add peers in contestents.
        }
        else{
            socket.emit('offer', {'to':calleesid, 'message': peerConnection.localDescription}); // offer is emited through socket to server and server will send to calleesid
        }
    });
    peerConnection.onaddstream = event => handleRemoteStreamAdded(event.stream, calleesid);
    peerConnection.onicecandidate = function(event) {
        if (event.candidate) {
          socket.emit('candidate', {'to': calleesid, 'message':event.candidate});
        }
    };
}

socket.on('offer', function(message){// if offer is received
    if(confirm(message['name']+ " Calling. Accept?")){
        console.log('======Request Received and ACCEPTED======');
        if (contestents.length<maximumparties) {
            const peerConnection = new RTCPeerConnection(config); // write config in ()
            contestents[contestents.length] = message['callerid'];
            console.log('Added: --',message['callerid'], "--to Contestents (OFFER) = one only");
            console.log('Accepted Offer of ', message['callerid']);
            peerConnections[message['callerid']] = peerConnection
            peerConnection.addStream(localVideo.srcObject); 
            peerConnection.setRemoteDescription(message['message']) // Remember Local Description and SDP is send throungh the sockets
            .then(() => peerConnection.createAnswer())
            .then(sdp => peerConnection.setLocalDescription(sdp))
            .then(function(){// if was here initially.
                socket.emit('answer', {'to': message['callerid'], 'message': peerConnection.localDescription});
            });
            peerConnection.onaddstream = event => handleRemoteStreamAdded(event.stream, message['callerid']);
            peerConnection.onicecandidate = function(event) {
                if (event.candidate) {
                    socket.emit('candidate', {'to': message['callerid'], 'message':event.candidate});
                }
            };
            if (contestents.length > 1){
                addmore(message['callerid']);
            }
        }
        else{
            if (insfu == false){// if not already in sfu.
                console.log('Accepted offer of', message['callerid'], "and send him request to join SFU to", contestents.concat(message['callerid'])); //In concat by default inplace = false;
                socket.emit('joinsfu', {'to': contestents.concat(message['callerid'])});//review this. joining sfu to if number is greter than 3.
                offermakertosfu(mysid); // sending join request by myself.
            }
            else{
                socket.emit('joinsfu', {'to': message['callerid']}) // if already in sfu then sending request to only offered client
            }
        }
    }
    else{
        console.warn('DECLINED REQUEST!');
        socket.emit('declined', {'to': message['callerid']});
    }  
})

function dec(id){
    peerConnections[id] && peerConnections[id].close();
    delete peerConnections[id];
    console.log('Declined function called');
}

socket.on('offerer', function(message){ // receiced offer with more than one candidate.(having more than one contestents) make join sfu also
    if((confirm(message['name']+ " Calling (with more than one participants). Accept? ")) && (contestents.length<maximumparties)){//also add here to join sfu.
        const peerConnection = new RTCPeerConnection(config);
        console.log('Accepted Offer of', message['callerid'], "with offer with multiple candidates");
        contestents[contestents.length] = message['callerid'];
        console.log('Added: --',message['callerid'], "--to Contestents (OFFERER)");
        console.log('Answering to', message['callerid']);
        peerConnections[message['callerid']] = peerConnection // write config in ()
        peerConnection.addStream(localVideo.srcObject);
        peerConnection.setRemoteDescription(message['message']) // Remember Local Description and SDP is send throungh the sockets
        .then(() => peerConnection.createAnswer())
        .then(sdp => peerConnection.setLocalDescription(sdp))
        .then(function(){// if was here initially.
            socket.emit('answer', {'to': message['callerid'], 'message': peerConnection.localDescription});
        });
        peerConnection.onaddstream = event => handleRemoteStreamAdded(event.stream, message['callerid']);
        peerConnection.onicecandidate = function(event) {
            if (event.candidate) {
                socket.emit('candidate', {'to': message['callerid'], 'message':event.candidate});
            }
        };
        offeringtothem(message['more']);// also sending offer to more candidates.
    }
    else{
        socket.emit('declined', {'to': message['callerid']});
    }
})


socket.on('alsoadd', function(message){
    offeringtothem(message);
});

function offeringtothem(message){// sending offer to got list toadd them without answer button. to make grp call.
    for (i=0; i<message.length; i++){
        xp = message[i];
        const peerConnection = new RTCPeerConnection(config); //write config in (). //RTCPeerConnection Object is created.
        console.log('Offering to(with iteration)' ,i);
        peerConnections[message[i]] = peerConnection;
        peerConnection.addStream(localVideo.srcObject);
        peerConnection.createOffer() // Offer is Created(Promise based)
        .then(sdp => peerConnection.setLocalDescription(sdp))//SDP: Session Discription Protocol: it contains many information of Peer.
        .then(function(){
            socket.emit('specialofferaddthem', {'to': xp, 'message': peerConnection.localDescription}); // offer is emited through socket to server and server will send to calleesid
        });
        peerConnection.onaddstream = event => handleRemoteStreamAdded(event.stream, xp);
        peerConnection.onicecandidate = function(event) {
            if (event.candidate) {
            socket.emit('candidate', {'to': message[i], 'message':event.candidate});
            }
        };
    }
}

socket.on('specialofferfromflask', function(message){ // if received offer to accept it without answer or decline confirmation.
    const peerConnection = new RTCPeerConnection(config);
    contestents[contestents.length] = message['callerid'];
    console.log('Added: --',message['callerid'], "--to Contestents (SPECIALOFFERFROMFLASK)");
    peerConnections[message['callerid']] = peerConnection // write config in ()
    peerConnection.addStream(localVideo.srcObject);
    console.log('Answering without permission', message['callerid'])
    peerConnection.setRemoteDescription(message['message']) // Remember Local Description and SDP is send throungh the sockets
    .then(() => peerConnection.createAnswer())
    .then(sdp => peerConnection.setLocalDescription(sdp))
    .then(function(){// if was here initially.
        socket.emit('answer', {'to': message['callerid'], 'message': peerConnection.localDescription});
    });
    peerConnection.onaddstream = event => handleRemoteStreamAdded(event.stream, message['callerid']);
    peerConnection.onicecandidate = function(event) {
        if (event.candidate) {
            socket.emit('candidate', {'to': message['callerid'], 'message':event.candidate});
        }
    };
})

socket.on('candidate', function(mess){
    console.log('Candidate Part' ,mess['from']);
    peerConnections[mess['from']].addIceCandidate(new RTCIceCandidate(mess['message']))
    .catch(e => console.error(e));
});

socket.on('answer', function(message){
    console.log('Received Answer from', message['calleeid']);
    document.getElementById('joiningbuttonid').textContent = 'Connect';
    document.getElementById('joiningbuttonid').disabled = false;
    document.getElementById('joinusernameid').value = '';
    document.getElementById('joinpasswordid').value = '';
    contestents[contestents.length] = message['calleeid'];
    console.log('Added: --',message['calleeid'], "--to Contestents (ANSWER)");
    peerConnections[message['calleeid']].setRemoteDescription(message['message']);
});

function addmore(receiver){
    x = contestents;
    socket.emit('specialoffer', {'to': receiver, 'message': x.slice(0,x.length-1)})
}

  socket.on('close', function(id){
      handleRemoteHangup(id)
  })

  function hangup(){ // called from HTML
    console.log('Hanging Up with button press');
    if (contestents.length>0){
        socket.emit('hangup', contestents);
        for(i=0; i<contestents.length;i++){
        console.log('Removing', contestents[i], "Video Element");
        document.querySelector("#" + contestents[i].replace(/[^a-zA-Z]+/g, "").toLowerCase()).remove();
        }
        if (remoteVideos.querySelectorAll("video").length === 1) {
        remoteVideos.setAttribute("class", "one remoteVideos");
        } else {
        remoteVideos.setAttribute("class", "remoteVideos");
        }
        contestents = [];
        console.log('Contestents after Hangup: ',contestents);
    }
}

socket.on('hangupreceived', function(from){
    handleRemoteHangup(from);
})

  function handleRemoteHangup(id) {
    peerConnections[id] && peerConnections[id].close();
    delete peerConnections[id];
    console.log('Received Hangup from', id);
    console.log('Contestents before', contestents);
    contestents.splice(contestents.indexOf(id), 1);
    console.log('Contestents after', contestents);
    try{
        document.querySelector("#" + id.replace(/[^a-zA-Z]+/g, "").toLowerCase()).remove();
    }
    catch{
        console.error('Kuch Nahi Hua areh kuch nahi hua.');
    }
    if (remoteVideos.querySelectorAll("video").length === 1) {
      remoteVideos.setAttribute("class", "one remoteVideos");
    } else {
      remoteVideos.setAttribute("class", "remoteVideos");
    }
  }

  function screenshare(){
    document.getElementById('camera').style.display = 'block';
    document.getElementById('screen').style.display = 'none';
    navigator.mediaDevices.getDisplayMedia({video: true, audio: true})
    .then(function(stream){ // if success in getting media devices
        localVideo.srcObject = stream;// adding stream to video Element
    })
    .catch(error => console.log(error));
}

function camera(){
    document.getElementById('camera').style.display = 'none';
    document.getElementById('screen').style.display = 'block';
    navigator.mediaDevices.getUserMedia(constraints)
    .then(function(stream){ // if success in getting media devices
        localVideo.srcObject = stream;// adding stream to video Element
    })
    .catch(error => console.log(error));
}

  window.onunload = window.onbeforeunload = function() {
    socket.close();
  };
//------------------------------------------------------------------------------------------------SFU THING--------------------------
  socket.on('pleasejoinsfu', function(message){
      //alert('joiningsfu'); // received request to join sfu.
    offermakertosfu(message);
  })

  function offermakertosfu(message){ // sending offer to sfu.
    insfu = true;
    console.log('I need to join SFU because someone in meeting send me request to do that therfore, sending Offer to SFU');
    if (contestents.length > 0){ // to make sure no redundant request is made to server.
        socket.emit('hangup', contestents);
        console.warn('Hangup Send to join SFU');
    }
    
    //for(i=0;i<contestents.length;i++){
        //document.querySelector("#" + contestents[i].replace(/[^a-zA-Z]+/g, "").toLowerCase()).style.display='none';
    //}
    const peerConnection = new RTCPeerConnection(config);
    peerConnections[message['sfu']] = peerConnection;
    peerConnection.addStream(localVideo.srcObject);
    peerConnection.createOffer()
    .then(sdp => peerConnection.setLocalDescription(sdp))
    .then(function(){
        socket.emit('offeringtosfu', {'to': message['sfu'], 'message': peerConnection.localDescription});
    });
    //muteunmute(track = 'a', mvalue = false);// on joining sfu You are automatically muted.
    peerConnection.onaddstream = event => handleRemoteStreamAdded(event.stream, message['sfu']);
    peerConnection.onicecandidate = function(event) {
        if (event.candidate) {
          socket.emit('candidate', {'to': message['sfu'], 'message': event.candidate});
        }
    };
}

socket.on('yourserial', function(message){
    console.log('Got Queue', message['my']);
    queue  = message['my'];
})