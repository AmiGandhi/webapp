package neu.csye6225.spring2020.cloud.service.impl;

import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.User;
import neu.csye6225.spring2020.cloud.repository.UserRepository;
import neu.csye6225.spring2020.cloud.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;

import static neu.csye6225.spring2020.cloud.constants.ApplicationConstants.*;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepo;

    public ResponseEntity checkIfUserExists(String header) throws UnAuthorizedLoginException, ValidationException {

        if(header!=null && header.contains(BASIC))
        {
            String [] userPassArr = new String(Base64.getDecoder().decode(header.substring(6).getBytes())).split(":", 2);
            if (userPassArr.length != 2) {
                throw new ValidationException(EMAILID_PASSWORD_MISSING);
            }

            User user = findByEmail_address(userPassArr[0]);

            if(user == null)
            {
                throw new UnAuthorizedLoginException(NULL_EMAIL);
            } else
            {
                if(new BCryptPasswordEncoder().matches(userPassArr[1], user.getPassword()))
                {
                    return new ResponseEntity(user, HttpStatus.NO_CONTENT);
                } else
                {
                    throw new UnAuthorizedLoginException(PASSWORD_INCORRECT);
                }
            }

        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Override
    public User findByEmail_address(String email_address) {
        // TODO Auto-generated method stub
        User user = userRepo.findByEmailAddress(email_address);

        return user;
    }


}
