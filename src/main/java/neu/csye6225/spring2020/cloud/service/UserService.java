package neu.csye6225.spring2020.cloud.service;

import neu.csye6225.spring2020.cloud.exception.ResourceNotFoundException;
import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.User;
import org.springframework.http.ResponseEntity;

public interface UserService {

    User createUser(User user) throws ValidationException;

    User getUser(String authHeader) throws UnAuthorizedLoginException, ResourceNotFoundException;

    User findByEmail_address(String email_address);

    ResponseEntity updateUser(String authHeader, User user) throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException;

}
