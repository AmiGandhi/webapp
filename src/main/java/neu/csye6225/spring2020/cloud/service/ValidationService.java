package neu.csye6225.spring2020.cloud.service;

import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.Bill;

public interface ValidationService {

    void isBillValid (Bill bill) throws ValidationException;
}
