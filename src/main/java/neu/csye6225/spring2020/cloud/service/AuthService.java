package neu.csye6225.spring2020.cloud.service;

import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.User;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity checkIfUserExists(String header)
            throws UnAuthorizedLoginException, ValidationException;

    User findByEmail_address(String email_address);

}
