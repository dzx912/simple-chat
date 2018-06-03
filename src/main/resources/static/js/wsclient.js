document.addEventListener("DOMContentLoaded", function() {

    var buttonSend = document.getElementById("buttonSend");
    var inputTextMessage = document.getElementById("inputMessage");
    var outputTextMessage = document.getElementById("outputMessage");

    var buttonConnect = document.getElementById("buttonConnect");
    var inputToken = document.getElementById("inputToken");
    var receiverToken = document.getElementById("receiverToken");

    var socket;
    var token;
    var isConnect;

    buttonConnect.addEventListener("click", onConnect);

    buttonSend.addEventListener("click", sendMessage);
    inputToken.addEventListener("keypress", onEnterInputToken);
    inputTextMessage.addEventListener("keypress", onEnterInputTextMessage);

    init();

    function init() {
        isConnect = false;

        buttonConnect.innerText = "Connect";
        inputToken.disabled = false;

        buttonSend.disabled = true;
        receiverToken.disabled = true;
        inputTextMessage.disabled = true;
    }

    function holdConnection() {
        isConnect = true;

        buttonConnect.innerText = "Close";
        inputToken.disabled = true;

        buttonSend.disabled = false;
        receiverToken.disabled = false;
        inputTextMessage.disabled = false;
    }

    function onEnterInputToken(event) {
        if (event.keyCode == 13) {
            onConnect();
        }
    }

    function onConnect() {
        token = inputToken.value;
        if(!isConnect) {
            if(token) {
                createWsConnect();
            }
        } else {
            socket.close();
        }
    }

    function createWsConnect() {
        socket = new WebSocket("ws://localhost:8080/token/" + token);

        socket.addEventListener('open', wsConnect);
        socket.addEventListener('message', wsGetMessage);
        socket.addEventListener('close', wsClose);
        socket.addEventListener('error', wsError);

        clearHistory();
        addTextMessage("SERVER: ", "Connect like " + token);
    }

    function clearHistory() {
        outputTextMessage.value = "";
    }

    function wsConnect() {
        holdConnection();
    }

    function wsGetMessage(event) {
        console.log("message: " + event.data);

        if(event.data) {
            var json = JSON.parse(event.data);
            routerWsMessage(json);
        }
    }

    function routerWsMessage(json) {
        switch (json.type) {
            case "history":
                showHistory(json.content.history);
                break;
            case "text":
                showMessage(json.content);
                break;
            default:
                console.log("Неизвестный тип сообщения");
                break;
        }
    }

    function showHistory(history) {
        history.forEach(function(message) {
            showMessage(message);
        });
    }

    function showMessage(message) {
        var author = message.author.id + ": ";
        addTextMessage(author, message.text);
    }

    function onEnterInputTextMessage(event) {
        if (event.keyCode == 13) {
            sendMessage();
        }
    }

    function sendMessage() {
        var text = inputTextMessage.value;
        var receiverTokenText = receiverToken.value;
        if(text && receiverTokenText) {
            sendMessageAndPutToConversation(text, receiverTokenText)
        }
    }

    function sendMessageAndPutToConversation(text, receiverTokenText) {
        socket.send(
            '{"clientId":1,"text":"'
            + text + '","chat":{"id":'
            + receiverTokenText + '}}'
        );
        inputTextMessage.value = '';

        addTextMessage("Me: ", text);
    }

    function addTextMessage(author, message) {
        var oldText = outputTextMessage.value;

        outputTextMessage.value = author + message + "\n" + oldText;
    }

    function wsClose(event) {
        init();
        if (event.wasClean) {
            console.log('close');
        } else {
            console.log('Alarm close');
        }
        console.log('Code: ' + event.code + ' cause: ' + event.reason);
        addTextMessage("SERVER: ", "Disconnect")
    }

    function wsError() {
        console.log("Error: " + error.message);
    }

});