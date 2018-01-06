document.addEventListener("DOMContentLoaded", function() {

    var buttonSend = document.getElementById("buttonSend");
    var inputTextMessage = document.getElementById("inputMessage");
    var outputTextMessage = document.getElementById("outputMessage");

    var socket = new WebSocket("ws://localhost:8080/token/3");

    buttonSend.addEventListener("click", sendMessage);
    socket.addEventListener('open', wsConnect);
    socket.addEventListener('message', wsGetMessage);
    socket.addEventListener('close', wsClose);
    socket.addEventListener('error', wsError);

    function wsConnect() {
        console.log("open");
        buttonSend.disabled = false;
    }

    function wsGetMessage(event) {
        var data = event.data;
        console.log("message: " + data);
        if(data) {
            var oldText = outputTextMessage.value;

            var jsonText = JSON.parse(data);
            outputTextMessage.value = jsonText.text + "\n" + oldText;
        }
    }

    function wsClose() {
        if (event.wasClean) {
            console.log('close');
        } else {
            console.log('Alarm close');
        }
        console.log('Code: ' + event.code + ' cause: ' + event.reason);
    }

    function wsError() {
        console.log("Error: " + error.message);
    }

    function sendMessage() {
        var text = inputTextMessage.value;
        if(text) {
            socket.send(
                '{"id":1,"text":"' + text + '","author":{"id":2},"chat":{"id":3}}'
            );
        }
    }

});