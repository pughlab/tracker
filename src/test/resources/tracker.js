'use strict';

console.log("Welcome to the", console);

// An example of how we can script state changing, and through this send notifications to sockets
// We should probably have a slightly easier way to look up state changes, and only apply them if 
// needed. But there's a lot to be said for using JS, as it allows study customizations and 
// external logic. 

//events.get("DEMO").on("field", function(event) { 
//    var parameters = event.data.parameters;
//    console.log("Parameters", parameters);
//    var study = parameters.get("study").asText();
//    var field = parameters.get("field").asText();
//    var newValue = parameters.get("new").getValue();
//    if (field == "returnRequested") {
//        study = repository.getStudy(study);
//        var c = repository.getStudyCase(study, parameters.get("case_id").asInt());
//        var oldState = c.getState();
//        var newState = (newValue.toString() == "true") ? "pending" : null;
//        if (oldState != newState) {
//            repository.setStudyCaseState(study, c, event.data.user, newState);
//        }
//    }
//});

function netWorkingDaysInternal(d1, d2) {
    // getUTCDay() returns 0 for Sunday and 6 for Saturday, helpfully (not)
    var startDay = d1.getUTCDay();
    var elapsed = d2.getTime() - d1.getTime();  // In milliseconds
    var elapsedDays = elapsed / (1000 * 60 * 60 * 24);
    
    var netDays = 0;
    
    // Calculate the number of weeks, and then use this to calculate full weekends
    // Then we can subtract these from the elapsed days, so elapsed days must then
    // be less then 7.
    var weeks = Math.floor(elapsedDays / 7);
    netDays = netDays + weeks * 5;
    elapsedDays = elapsedDays - weeks * 7;
    
    // Adjust to make 0 Saturday, so we can use a simple boundary to detect weekends
    startDay = (startDay + 1) % 7;
    
    // Now we can calculate from the starting day and the number of elapsedDays.
    var endDay = startDay + elapsedDays;
    for(var i = startDay; i <= endDay; i++) {
        if ((i % 7) >= 2) {
            netDays = netDays + 1;
        }
    }
    
    return netDays;
}

// NOTE: We assume whole days only here, not times
function netWorkingDays(d1, d2) {
    var pd1 = Date.parse(d1);
    var pd2 = Date.parse(d2);
    if (isNaN(pd1) || isNaN(pd2)) {
        return null;
    } else {
        return netWorkingDaysInternal(new Date(pd1), new Date(pd2));
    }
}

