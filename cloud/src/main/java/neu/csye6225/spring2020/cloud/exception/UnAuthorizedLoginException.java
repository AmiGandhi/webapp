package neu.csye6225.spring2020.cloud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class UnAuthorizedLoginException extends Exception {

    private static final long serialVersionUID = 3253030044129825830L;

    public UnAuthorizedLoginException() {
        super();
    }

    public UnAuthorizedLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnAuthorizedLoginException(String message) {
        super(message);
    }
}
