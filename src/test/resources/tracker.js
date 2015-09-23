'use strict';

console.log("Welcome to the", console);

events.get("DEMO").on("field", function(event) { 
    console.log(event.data.parameters);
});
