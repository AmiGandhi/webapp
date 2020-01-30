package neu.csye6225.spring2020.cloud.util;

import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import static neu.csye6225.spring2020.cloud.constants.ApplicationConstants.PASSWORD_INCORRECT;

@Component
public class AuthServiceUtil {

    public String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    public void verifyPassword(String enteredPassword, String passwordFromDb) throws UnAuthorizedLoginException {

        if (!BCrypt.checkpw(enteredPassword, passwordFromDb)) {
            throw new UnAuthorizedLoginException(PASSWORD_INCORRECT);
        }
    }

    public byte[] getDecodedString(String authDetails) {
        return Base64.decodeBase64(authDetails);
    }

}
