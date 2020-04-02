package neu.csye6225.spring2020.cloud.controller;

import javax.validation.Valid;

import neu.csye6225.spring2020.cloud.exception.FileStorageException;
import neu.csye6225.spring2020.cloud.exception.ResourceNotFoundException;
import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.Bill;
import neu.csye6225.spring2020.cloud.model.File;
import neu.csye6225.spring2020.cloud.model.User;
import neu.csye6225.spring2020.cloud.service.BillService;
import neu.csye6225.spring2020.cloud.service.FileStorageService;
import neu.csye6225.spring2020.cloud.service.UserService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.rmi.ServerException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import static neu.csye6225.spring2020.cloud.constants.ApplicationConstants.*;

@RestController
public class EntryController {

    @Autowired
    private UserService userService;

    @Autowired
    private BillService billService;

    @Autowired
    private FileStorageService fileStorageService;

    private static final Logger logger = LogManager.getLogger(EntryController.class);

    @RequestMapping(value = HEALTH_CHECK, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public String healthCheck() {
        logger.info("Health check successful!!");
        return "***************Welcome to my cloud project!!***************";
    }

    //endpoints for user
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


    //endpoints for bills
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
            throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException, FileStorageException {
        return new ResponseEntity(billService.deleteBill(header, id), HttpStatus.NO_CONTENT);
    }

    //endpoints for file attachment to bills
    @RequestMapping(value = ATTACH_FILE, method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public File attachFile(@RequestHeader(value = AUTHORIZATION) String auth,
                           @PathVariable(value = "bill_id") UUID bill_id,
                           @RequestParam("file") MultipartFile file)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException, FileStorageException, IOException, NoSuchAlgorithmException {
        return billService.saveAttachment(auth, bill_id, file);
    }


    @RequestMapping(value = GET_ATTACHMENT, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public File getAttachment(@RequestHeader(value = AUTHORIZATION) String auth,
                                        @PathVariable(value = "bill_id") UUID bill_id,
                                        @PathVariable(value = "file_id") UUID file_id)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException {
        return billService.getAttachment(auth, bill_id, file_id);
    }


    @RequestMapping(value = DELETE_ATTACHMENT, method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttachment(@RequestHeader(value = AUTHORIZATION) String auth,
                                 @PathVariable(value = "bill_id") UUID bill_id,
                                 @PathVariable(value = "file_id") UUID file_id)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException, FileStorageException {
        billService.deleteAttachment(auth, bill_id, file_id);
    }


    //endpoint for triggering lambda function
    @RequestMapping(value = GET_DUE_BILLS, method = RequestMethod.GET)
    public ResponseEntity<List<Bill>> getDueBills(@RequestHeader(value = AUTHORIZATION) String authHeader,
                                                  @PathVariable(value = "x_days") Integer x_days)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException, ServerException {
        return new ResponseEntity<List<Bill>>(billService.getDueBills(authHeader, x_days), HttpStatus.OK);
    }
}
