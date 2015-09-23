'use strict';

console.log("Welcome to the", console);

events.get("DEMO").on("state", function() { console.log(arguments); });
