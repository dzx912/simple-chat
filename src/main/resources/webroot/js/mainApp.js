document.addEventListener("DOMContentLoaded", function() {
    const buttonSend = document.getElementById("buttonSend");
    const inputTextMessage = document.getElementById("inputMessage");
    const outputTextMessage = document.getElementById("outputMessage");

    const buttonLogout = document.getElementById("buttonLogout");

    const registrationPanel = document.getElementById("registrationPanel");
    const openChatPanel = document.getElementById("openChatPanel");
    const chatPanel = document.getElementById("chatPanel");

    const buttonSignUp = document.getElementById("buttonSignUp");
    const inputLogin = document.getElementById("inputLogin");
    const inputFirstName = document.getElementById("inputFirstName");
    const inputLastName = document.getElementById("inputLastName");

    const inputLoginReceiver = document.getElementById("inputLoginReceiver");

    buttonSignUp.addEventListener("click", onSignUp);

    buttonLogout.addEventListener("click", onLogout);
    buttonSend.addEventListener("click", sendMessage);
    inputTextMessage.addEventListener("keypress", onEnterInputTextMessage);

    buttonOpenChat.addEventListener("click", openChat);
    inputLoginReceiver.addEventListener("keypress", onEnterInputLoginReceiver);

    buttonCloseChat.addEventListener("click", closeChat);

    const chatStorage = new ChatStorage();
    const chatClient = new ChatClient(outputTextMessage, init, clearInput, showChatPanel, chatStorage.clearToken);
    const registration = new Registration(responseRegistration);

    init();

    function init() {
        const token = chatStorage.getChatToken()
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
        if (event.keyCode === 13) {
            sendMessage();
        }
    }

    function sendMessage() {
        const text = inputTextMessage.value;
        if(text) {
            chatClient.send(text)
        }
    }

    function onLogout() {
        chatClient.logout();
    }

    function onEnterInputLoginReceiver(event) {
        if (event.keyCode === 13) {
            openChat();
        }
    }

    function openChat() {
        const loginReceiver = inputLoginReceiver.value;
        if(loginReceiver) {
            chatClient.createChat(loginReceiver);
        }
    }

    function closeChat() {
        showPanels('openChat');
        chatClient.closeChat();
    }
});