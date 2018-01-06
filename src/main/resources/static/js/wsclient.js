console.log("hello");

var socket = new WebSocket("ws://localhost:8080/token/3");

socket.onopen = function() {
  console.log("open");
  socket.send('{"id":1,"text":"hello","author":{"id":2},"chat":{"id":3}}');
};

socket.onclose = function(event) {
  if (event.wasClean) {
    console.log('close');
  } else {
    console.log('Alarm close');
  }
  console.log('Code: ' + event.code + ' cause: ' + event.reason);
};

socket.onmessage = function(event) {
  console.log("message: " + event.data);
};

socket.onerror = function(error) {
  console.log("Error: " + error.message);
};