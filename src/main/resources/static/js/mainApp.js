document.addEventListener("DOMContentLoaded", function() {
    var buttonSend = document.getElementById("buttonSend");
    var inputTextMessage = document.getElementById("inputMessage");
    var outputTextMessage = document.getElementById("outputMessage");

    var buttonLogout = document.getElementById("buttonLogout");

    var registrationPanel = document.getElementById("registrationPanel");
    var openChatPanel = document.getElementById("openChatPanel");
    var chatPanel = document.getElementById("chatPanel");

    var buttonSignUp = document.getElementById("buttonSignUp");
    var inputLogin = document.getElementById("inputLogin");
    var inputFirstName = document.getElementById("inputFirstName");
    var inputLastName = document.getElementById("inputLastName");

    var inputLoginReceiver = document.getElementById("inputLoginReceiver");

    buttonSignUp.addEventListener("click", onSignUp);

    buttonLogout.addEventListener("click", onLogout);
    buttonSend.addEventListener("click", sendMessage);
    inputTextMessage.addEventListener("keypress", onEnterInputTextMessage);

    buttonOpenChat.addEventListener("click", openChat);
    inputLoginReceiver.addEventListener("keypress", onEnterInputLoginReceiver);

    buttonCloseChat.addEventListener("click", closeChat);

    var chatStorage = new ChatStorage();
    var chatClient = new ChatClient(outputTextMessage, init, clearInput, showChatPanel, chatStorage.clearToken);
    var registration = new Registration(responseRegistration);

    init();

    function init() {
        var token = chatStorage.getChatToken()
        if(token) {
            chatClient.connect(token);
            showPanels('openChat');
        } else {
            showPanels('registration')
        }
    }

    function clearInput() {
        inputTextMessage.value = '';
    }

    function responseRegistration(token) {
        chatStorage.saveChatToken(token);
        chatClient.connect(token);
        showPanels('openChat');
    }

    function showChatPanel() {
        showPanels("chat");
    }

    function showPanels(panel) {
        registrationPanel.style.display="none";
        openChatPanel.style.display="none";
        chatPanel.style.display="none";

        if(panel === 'chat') {
            chatPanel.style.display="block";
        }
        if(panel === 'openChat') {
            openChatPanel.style.display="block";
        }
        if(panel === 'registration') {
            registrationPanel.style.display="block";
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
        if(text) {
            chatClient.send(text)
        }
    }

    function onLogout() {
        chatClient.logout();
    }

    function onEnterInputLoginReceiver(event) {
        if (event.keyCode == 13) {
            openChat();
        }
    }

    function openChat() {
        var loginReceiver = inputLoginReceiver.value;
        if(loginReceiver) {
            chatClient.createChat(loginReceiver);
        }
    }

    function closeChat() {
        showPanels('openChat');
        chatClient.closeChat();
    }
});