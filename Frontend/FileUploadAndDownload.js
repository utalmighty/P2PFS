async function uploadFile() {
    let formData = new FormData();  
    let file = fileupload.files[0];         
    formData.append("file", file);
    printFile(file);
    alert('The file has been uploaded successfully.');
}


function printFile(file) {
    const reader = new FileReader();
    reader.onload = (evt) => createFile(file.name, file.type, evt.target.result);
    reader.readAsArrayBuffer(file);
}

function createFile(fileName, fileType, data) {
    var filename = fileName;
    var blob = new Blob([data], {type: fileType});
    var link = document.createElement("a");
    link.download = filename;
    link.innerHTML = "Download File";
    link.href = window.URL.createObjectURL(blob);
    document.body.appendChild(link);
}
