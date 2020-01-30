package neu.csye6225.spring2020.cloud.service;

import neu.csye6225.spring2020.cloud.exception.ResourceNotFoundException;
import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.Bill;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

public interface BillService {

    Bill createBill(String authHeader, @Valid Bill bill)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException;

    List<Bill> getAllBills(String authHeader)
            throws UnAuthorizedLoginException, ResourceNotFoundException, ValidationException;

    Bill getBill(String authHeader, UUID bill_id)
            throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException;

    Bill updateBill(String authHeader, UUID bill_id, Bill bill)
            throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException;

    ResponseEntity deleteBill(String authHeader, UUID bill_id)
            throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException;

}
