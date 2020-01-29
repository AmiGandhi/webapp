package neu.csye6225.spring2020.cloud.controller;

import javax.validation.Valid;

import neu.csye6225.spring2020.cloud.exception.ResourceNotFoundException;
import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.User;
import neu.csye6225.spring2020.cloud.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static neu.csye6225.spring2020.cloud.constants.ApplicationConstants.*;

@RestController
public class EntryController {

    @Autowired
    private UserService userService;

    @RequestMapping( method = RequestMethod.POST, value=REGISTER, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> createUser(@Valid @RequestBody User userBody)
            throws ValidationException {
        return new ResponseEntity<User>(userService.createUser(userBody), HttpStatus.CREATED);
    }


    @RequestMapping(method = RequestMethod.GET, value = LOGIN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUser(@RequestHeader(AUTHORIZATION) String header)
            throws UnAuthorizedLoginException, ResourceNotFoundException {
        return new ResponseEntity<User>(userService.getUser(header), HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.PUT, value = LOGIN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity updateUser(@RequestHeader(AUTHORIZATION) String header, @RequestBody  User user)
            throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException {
        return new ResponseEntity(userService.updateUser(header, user), HttpStatus.NO_CONTENT);
    }

}
