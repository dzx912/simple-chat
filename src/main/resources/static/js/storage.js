function ChatStorage() {

    this.clearToken = function () {
        localStorage.setItem("isAuthorization", false);
        localStorage.removeItem("token");
    }

    this.getChatToken = function() {
        var isAuthorization = localStorage.getItem("isAuthorization") === 'true';
        if(isAuthorization) {
            var localToken = localStorage.getItem("token")
            if(localToken) {
                return localToken
            }
        }
        return null;
    }

    this.saveChatToken = function(token) {
        localStorage.setItem("isAuthorization", true)
        localStorage.setItem("token", token)
    }
}