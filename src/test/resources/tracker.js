'use strict';

console.log("Welcome to the", console);

// An example of how we can script state changing, and through this send notifications to sockets
events.get("DEMO").on("field", function(event) { 
    var parameters = event.data.parameters;
    console.log("Parameters", parameters);
    var study = parameters.get("study").asText();
    var view = parameters.get("view").asText();
    study = repository.getStudy(study);
    view = repository.getStudyView(study, view);
    var c = repository.getStudyCase(study, view, parameters.get("case_id").asInt());
    repository.setStudyCaseState(study, view, c, event.data.user, "pending");
});
