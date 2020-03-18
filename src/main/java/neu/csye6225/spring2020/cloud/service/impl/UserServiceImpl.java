package neu.csye6225.spring2020.cloud.service.impl;

import com.timgroup.statsd.StatsDClient;
import neu.csye6225.spring2020.cloud.exception.ResourceNotFoundException;
import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.User;
import neu.csye6225.spring2020.cloud.repository.UserRepository;
import neu.csye6225.spring2020.cloud.service.UserService;
import neu.csye6225.spring2020.cloud.util.CommonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static neu.csye6225.spring2020.cloud.constants.ApplicationConstants.*;
import static neu.csye6225.spring2020.cloud.constants.ApplicationConstants.EXISTING_EMAIL;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepo ;

    @Autowired
    private AuthServiceImpl authServiceImpl;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private StatsDClient statsDClient;

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    @Override
    public User createUser(User userBody) throws ValidationException {

        statsDClient.incrementCounter("endpoint.user.create.http.post");
        long start = System.currentTimeMillis();
        try {
            if(userBody.getEmailAddress()== null|| userBody.getFirst_name()==null|| userBody.getLast_name()==null|| userBody.getPassword()==null)
            {
                throw new ValidationException(MANDATORY_FIELDS_MISSING);
            }

            Pattern ptrn = Pattern.compile(PASSWORD_REGEX);
            Matcher mtch = ptrn.matcher(userBody.getPassword());
            if(!mtch.matches())
            {
                throw new ValidationException(PASSWORD_INCORRECT);
            }

            Pattern emailPattern = Pattern.compile(EMAILID_REGEX);
            Matcher m = emailPattern.matcher(userBody.getEmailAddress());
            if(!m.matches())
            {
                throw new ValidationException(INVALID_EMAIL);
            }

            String email_address = userBody.getEmailAddress();
            User checkUserExists = authServiceImpl.findByEmail_address(email_address);
            if(checkUserExists==null)
            {
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String hashedPassword = passwordEncoder.encode(userBody.getPassword());
                userBody.setPassword(hashedPassword);

                long startTime = System.currentTimeMillis();
                userRepo.save(userBody);
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                statsDClient.recordExecutionTime("CreatedUserInDatabase",duration);

            } else
            {
                throw new ValidationException(EXISTING_EMAIL);
            }
            long end = System.currentTimeMillis();
            long duration = (end - start);
            statsDClient.recordExecutionTime("CreateUserApiCall",duration);
            logger.info("User created successfully!");
            return userBody;

        } catch (Exception e) {
            logger.error(commonUtil.stackTraceString(e));
            throw e;
        }
    }

    public User getUser(String authHeader) throws UnAuthorizedLoginException, ResourceNotFoundException, ValidationException {

        statsDClient.incrementCounter("endpoint.user.get.http.get");
        long start = System.currentTimeMillis();
        try {
            ResponseEntity responseBody = authServiceImpl.checkIfUserExists(authHeader);
            if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT))
            {
                User u = (User) responseBody.getBody();
                long end = System.currentTimeMillis();
                long duration = (end - start);
                statsDClient.recordExecutionTime("GetUserApiCall",duration);
                logger.info("Found user successfully!");
                return u;
            }
            throw new ResourceNotFoundException(INVALID_CREDENTIALS);
        } catch (Exception e) {
            logger.error(commonUtil.stackTraceString(e));
            throw e;
        }
    }

    public ResponseEntity updateUser(String authHeader, User user) throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException {

        statsDClient.incrementCounter("endpoint.user.update.http.put");
        long start = System.currentTimeMillis();
        try {
            ResponseEntity responseBody = authServiceImpl.checkIfUserExists(authHeader);
            User u = (User) responseBody.getBody();
            if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT))
            {

                if(!user.getEmailAddress().equalsIgnoreCase(u.getEmailAddress()))
                {
                    throw new ValidationException(INVALID_EMAIL);
                }

                if(user.getEmailAddress()== null|| user.getFirst_name()==null|| user.getLast_name()==null|| user.getPassword()==null)
                {
                    throw new ValidationException(MANDATORY_FIELDS_MISSING);
                }

                if(user.getPassword()!=null)
                {
                    Pattern ptrn =    Pattern.compile(PASSWORD_REGEX);
                    Matcher mtch = ptrn.matcher(user.getPassword());
                    if(!mtch.matches()) {
                        throw new ValidationException(PASSWORD_INCORRECT);
                    }
                }

                long startTime = System.currentTimeMillis();
                userRepo.findById(u.getId())
                        .map(u1 -> {
                            u1.setFirst_name(user.getFirst_name());
                            u1.setLast_name(user.getLast_name());
                            if(user.getPassword()!=null)
                            {
                                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                                String hashedPassword = passwordEncoder.encode(user.getPassword());
                                u1.setPassword(hashedPassword);

                            }
                            long endTime = System.currentTimeMillis();
                            long duration = (endTime - startTime);
                            statsDClient.recordExecutionTime("UpdatedUserInDatabase",duration);
                            return userRepo.save(u1);
                        })
                        .orElseThrow(()-> new ResourceNotFoundException("User not found with email"+ u.getEmailAddress()));
            } else {
                throw new ResourceNotFoundException(INVALID_CREDENTIALS);
            }
            long end = System.currentTimeMillis();
            long duration = (end - start);
            statsDClient.recordExecutionTime("UpdateUserApiCall",duration);
            logger.info("User updated successfully!");
            return new ResponseEntity(HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            logger.error(commonUtil.stackTraceString(e));
            throw e;
        }
    }

}
