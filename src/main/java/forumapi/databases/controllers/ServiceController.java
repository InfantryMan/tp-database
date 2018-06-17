package forumapi.databases.controllers;


import forumapi.databases.messages.Message;
import forumapi.databases.messages.MessageStates;
import forumapi.databases.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/service")
public class ServiceController {

    final private ApplicationService serviceService;

    @Autowired
    public ServiceController(ApplicationService serviceService){
        this.serviceService = serviceService;
    }

    // Done
    @RequestMapping(path = "/clear", method = RequestMethod.POST)
    public ResponseEntity clear(){
        serviceService.truncateTables();
        return ResponseEntity.status(HttpStatus.OK).body(new Message(MessageStates.CLEAR_SUCCESSFUL.getMessage()));
    }

    // Done
    @RequestMapping(path = "/status", method = RequestMethod.GET)
    public ResponseEntity getStatus(){
        return ResponseEntity.ok(serviceService.getStatus());
    }
}
