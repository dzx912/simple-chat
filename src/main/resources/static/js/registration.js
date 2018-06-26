function Registration(callbackResponse) {
    var httpRequest = new XMLHttpRequest();
    httpRequest.addEventListener("readystatechange", onResponse);

    this.signUp = function (loginValue, firstNameValue, lastNameValue) {
        var json = JSON.stringify({
          login: loginValue,
          firstName: firstNameValue,
          lastName: lastNameValue
        });

        httpRequest.open("POST", '/sign-up', true);
        httpRequest.setRequestHeader('Content-Type', 'application/json; charset=utf-8');

        httpRequest.send(json);
    }

    function onResponse() {
        if(httpRequest.readyState == XMLHttpRequest.DONE && httpRequest.status == 200) {
            var token = httpRequest.responseText
            console.log("Server's token: " + token);

            responseRegistration(token);
        }

    }

    function responseRegistration(token) {
        if(callbackResponse && typeof callbackResponse === "function") {
            callbackResponse(token);
        }
    }
}