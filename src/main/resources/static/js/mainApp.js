document.addEventListener("DOMContentLoaded", function() {
    var buttonSend = document.getElementById("buttonSend");
    var inputTextMessage = document.getElementById("inputMessage");
    var outputTextMessage = document.getElementById("outputMessage");

    var buttonLogout = document.getElementById("buttonLogout");
    var receiverToken = document.getElementById("receiverToken");

    var registrationPanel = document.getElementById("registrationPanel");
    var chatPanel = document.getElementById("chatPanel");
    var buttonSignUp = document.getElementById("buttonSignUp");
    var inputLogin = document.getElementById("inputLogin");
    var inputFirstName = document.getElementById("inputFirstName");
    var inputLastName = document.getElementById("inputLastName");
    var tokenOutput = document.getElementById("tokenOutput");

    buttonSignUp.addEventListener("click", onSignUp);

    buttonLogout.addEventListener("click", onLogout);
    buttonSend.addEventListener("click", sendMessage);
    inputTextMessage.addEventListener("keypress", onEnterInputTextMessage);

    var chatStorage = new ChatStorage();
    var chatClient = new ChatClient(outputTextMessage, init, clearInput, chatStorage.clearToken);
    var registration = new Registration(responseRegistration);

    init();

    function init() {
        var token = chatStorage.getChatToken()
        if(token) {
            chatClient.connect(token);
            tokenOutput.innerHTML = token;
            showPanels('chat');
        } else {
            tokenOutput.innerHTML = '';
            showPanels('registration')
        }
    }

    function clearInput() {
        inputTextMessage.value = '';
    }

    function responseRegistration(token) {
        chatStorage.saveChatToken(token);
        chatClient.connect(token);
        tokenOutput.innerHTML = token;
        showPanels('chat');
    }

    function showPanels(panel) {
        if(panel === 'chat') {
            registrationPanel.style.display="none";
            chatPanel.style.display="block";
        }
        if(panel === 'registration') {
            registrationPanel.style.display="block";
            chatPanel.style.display="none";
        }
    }

    function onSignUp() {
        registration.signUp(inputLogin.value, inputFirstName.value, inputLastName.value);
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
            chatClient.send(text, receiverTokenText)
        }
    }

    function onLogout() {
        chatClient.logout();
    }
});