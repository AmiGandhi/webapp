package neu.csye6225.spring2020.cloud.service.impl;

import neu.csye6225.spring2020.cloud.exception.ResourceNotFoundException;
import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.Bill;
import neu.csye6225.spring2020.cloud.model.User;
import neu.csye6225.spring2020.cloud.repository.BillRepository;
import neu.csye6225.spring2020.cloud.service.BillService;
import neu.csye6225.spring2020.cloud.service.ValidationService;
import neu.csye6225.spring2020.cloud.util.AuthServiceUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static neu.csye6225.spring2020.cloud.constants.ApplicationConstants.INVALID_CREDENTIALS;

@Service
public class BillServiceImpl implements BillService {

    @Autowired
    private BillRepository billRepo;

    @Autowired
    private AuthServiceImpl authServiceImpl;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private AuthServiceUtil authServiceUtil;

    @Override
    public Bill createBill(String authHeader, Bill bill) throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException {

        ResponseEntity responseBody = authServiceImpl.checkIfUserExists(authHeader);
        User u = (User) responseBody.getBody();
        if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            validationService.isBillValid(bill);
            bill.setUser(u);
            Bill savedBill = billRepo.save(bill);
            return savedBill;

        } else {
            throw new ResourceNotFoundException(INVALID_CREDENTIALS);
        }
    }

    @Override
    public Bill getAllBills(String authHeader) throws UnAuthorizedLoginException, ResourceNotFoundException, ValidationException {

        ResponseEntity responseBody = authServiceImpl.checkIfUserExists(authHeader);
        User u = (User) responseBody.getBody();
        if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            Bill bill = billRepo.getAllBillsForUser(u);
            return bill;

        } else {
            throw new ResourceNotFoundException(INVALID_CREDENTIALS);
        }
    }

    @Override
    public Bill getBill(String authHeader, UUID bill_id) throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException {

        ResponseEntity responseBody = authServiceImpl.checkIfUserExists(authHeader);
        User u = (User) responseBody.getBody();
        if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            Optional<Bill> billWrapper = billRepo.findById(bill_id);
            if (!billWrapper.isPresent() || billWrapper.get() == null) {
                throw new ResourceNotFoundException("Bill with ID: " + bill_id.toString() + " does not exist");
            }
            Bill bill = billWrapper.get();
            return bill;

        } else {
            throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
        }

    }

    @Override
    public Bill updateBill(String authHeader, UUID bill_id, Bill bill) throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException {
        validationService.isBillValid(bill);
        Bill fetchedBill;
        fetchedBill = getBill(authHeader, bill_id);
        fetchedBill.setBillId(bill.getBillId());
        fetchedBill.setOwnerId(bill.getOwnerId());
        fetchedBill.setVendor(bill.getVendor());
        fetchedBill.setBillDate(bill.getBillDate());
        fetchedBill.setDueDate(bill.getDueDate());
        fetchedBill.setAmountDue(bill.getAmountDue());
        fetchedBill.setCategories(bill.getCategories());
        fetchedBill.setPaymentStatus(bill.getPaymentStatus());

        billRepo.save(fetchedBill);
        return fetchedBill;

    }

    @Override
    public ResponseEntity deleteBill(String authHeader, UUID bill_id) throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException {

        Bill fetchedBill;
        fetchedBill = getBill(authHeader, bill_id);
        billRepo.delete(fetchedBill);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


}
