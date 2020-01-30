package neu.csye6225.spring2020.cloud.controller;

import javax.validation.Valid;

import neu.csye6225.spring2020.cloud.exception.ResourceNotFoundException;
import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.Bill;
import neu.csye6225.spring2020.cloud.model.User;
import neu.csye6225.spring2020.cloud.service.BillService;
import neu.csye6225.spring2020.cloud.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static neu.csye6225.spring2020.cloud.constants.ApplicationConstants.*;

@RestController
public class EntryController {

    @Autowired
    private UserService userService;

    @Autowired
    private BillService billService;

    @RequestMapping( method = RequestMethod.POST, value=REGISTER, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> createUser(@Valid @RequestBody User userBody)
            throws ValidationException {
        return new ResponseEntity<User>(userService.createUser(userBody), HttpStatus.CREATED);
    }


    @RequestMapping(method = RequestMethod.GET, value = LOGIN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUser(@RequestHeader(AUTHORIZATION) String header)
            throws UnAuthorizedLoginException, ResourceNotFoundException, ValidationException {
        return new ResponseEntity<User>(userService.getUser(header), HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.PUT, value = LOGIN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity updateUser(@RequestHeader(AUTHORIZATION) String header,
                                     @RequestBody  User user)
            throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException {
        return new ResponseEntity(userService.updateUser(header, user), HttpStatus.NO_CONTENT);
    }


    @RequestMapping(method = RequestMethod.POST, value=CREATE_BILL, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Bill> createBill(@RequestHeader(AUTHORIZATION) String header,
                                           @Valid @RequestBody Bill bill)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException {
        return new ResponseEntity<Bill>(billService.createBill(header, bill), HttpStatus.CREATED);
    }


    @RequestMapping(method = RequestMethod.GET, value = GET_ALL_BILLS, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Bill>> getAllBills(@RequestHeader(AUTHORIZATION) String header)
            throws UnAuthorizedLoginException, ResourceNotFoundException, ValidationException {
        return new ResponseEntity<List<Bill>>(billService.getAllBills(header), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = GET_BILL, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Bill> getBill(@RequestHeader(AUTHORIZATION) String header,
                                        @PathVariable(value = "bill_id") UUID bill_id)
            throws UnAuthorizedLoginException, ResourceNotFoundException, ValidationException {
        return new ResponseEntity<Bill>(billService.getBill(header, bill_id), HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.PUT, value = UPDATE_BILL, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Bill> updateBill(@RequestHeader(AUTHORIZATION) String header,
                                           @PathVariable(value = "bill_id") UUID bill_id,
                                           @RequestBody Bill bill)
            throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException {
        return new ResponseEntity<Bill>(billService.updateBill(header, bill_id, bill), HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.DELETE, value = DELETE_BILL, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity deleteBill(@RequestHeader(AUTHORIZATION) String header,
                                     @PathVariable(value = "bill_id") UUID id)
            throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException {
        return new ResponseEntity(billService.deleteBill(header, id), HttpStatus.NO_CONTENT);
    }

}
