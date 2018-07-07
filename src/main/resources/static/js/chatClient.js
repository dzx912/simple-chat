function ChatClient(output, callbackInit, callbackClearInput, callbackClearToken) {
    var socket;
    var outputTextMessage = output;
    this.callbackInit = callbackInit;

    function init() {
        checkAndRun(callbackInit);
    }

    function clearInput() {
        checkAndRun(callbackClearInput);
    }

    function clearToken() {
        checkAndRun(callbackClearToken);
    }

    function checkAndRun(func) {
        if(func && typeof func === "function") {
            func();
        }
    }

    this.connect = function (token) {
        socket = new WebSocket("ws://localhost:8081/token/" + token);

        socket.addEventListener('message', wsGetMessage);
        socket.addEventListener('close', wsClose);
        socket.addEventListener('error', wsError);

        clearHistory();
        addTextMessage("SERVER: ", "Connect like " + token);
    }

    function clearHistory() {
        outputTextMessage.value = "";
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
        var author = message.author.login + ": ";
        addTextMessage(author, message.text);
    }

    function wsClose(event) {
        if (event.wasClean) {
            console.log('close');
        } else {
            console.log('Alarm close');
        }
        console.log('Code: ' + event.code + ' cause: ' + event.reason);
        addTextMessage("SERVER: ", "Disconnect");

        init();
    }

    function wsError() {
        console.log("Error: " + error.message);
    }

    this.logout = function () {
        clearToken();

        if(socket) {
            socket.close();
        }

        init();
    }

    this.send = function(textMessage, receiverToken) {
        var json = JSON.stringify({
            method: "sendTextMessage",
            content: {
                clientId: 1,
                text: textMessage,
                chatId: receiverToken
            }
        });

        socket.send(json);

        addTextMessage("Me: ", textMessage);

        clearInput();
    }

    function addTextMessage(author, message) {
        var oldText = outputTextMessage.value;

        outputTextMessage.value = author + message + "\n" + oldText;
    }
}