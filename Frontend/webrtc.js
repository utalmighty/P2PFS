const config = {
    'iceServers': [{
      'urls': ['stun:stun.l.google.com:19302']
    }]
  };

async function makeCall() {
  const peerConnection = new RTCPeerConnection(config);
  signalingChannel.addEventListener('message', async message => {
      if (message.answer) {
          const remoteDesc = new RTCSessionDescription(message.answer);
          await peerConnection.setRemoteDescription(remoteDesc);
      }
  });
  const offer = await peerConnection.createOffer();
  await peerConnection.setLocalDescription(offer);
  signalingChannel.send({'offer': offer});
}

// // on call
// const peerConnection = new RTCPeerConnection(configuration);
// signalingChannel.addEventListener('message', async message => {
//     if (message.offer) {
//         peerConnection.setRemoteDescription(new RTCSessionDescription(message.offer));
//         const answer = await peerConnection.createAnswer();
//         await peerConnection.setLocalDescription(answer);
//         signalingChannel.send({'answer': answer});
//     }
// });

const Http = new XMLHttpRequest();
const url='http://localhost.com/hello';
Http.open("GET", url);
Http.send();

Http.onreadystatechange = (e) => {
  console.log(Http.responseText)
}

