'use strict';

console.log("Welcome to the", console);

// An example of how we can script state changing, and through this send notifications to sockets
// We should probably have a slightly easier way to look up state changes, and only apply them if 
// needed. But there's a lot to be said for using JS, as it allows study customizations and 
// external logic. 

events.get("DEMO").on("field", function(event) { 
    var parameters = event.data.parameters;
    console.log("Parameters", parameters);
    var study = parameters.get("study").asText();
    var view = parameters.get("view").asText();
    var field = parameters.get("field").asText();
    var newValue = parameters.get("new").getValue();
    if (field == "returnRequested") {
        study = repository.getStudy(study);
        view = repository.getStudyView(study, view);
        var c = repository.getStudyCase(study, view, parameters.get("case_id").asInt());
        var oldState = c.getState();
        var newState = (newValue.toString() == "true") ? "returnPending" : null;
        if (oldState != newState) {
            repository.setStudyCaseState(study, view, c, event.data.user, newState);
        }
    }
});
