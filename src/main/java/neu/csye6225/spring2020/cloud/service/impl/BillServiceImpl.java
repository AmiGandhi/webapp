package neu.csye6225.spring2020.cloud.service.impl;

import neu.csye6225.spring2020.cloud.exception.ResourceNotFoundException;
import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.Bill;
import neu.csye6225.spring2020.cloud.model.PaymentStatusType;
import neu.csye6225.spring2020.cloud.model.User;
import neu.csye6225.spring2020.cloud.repository.BillRepository;
import neu.csye6225.spring2020.cloud.service.BillService;
import neu.csye6225.spring2020.cloud.service.ValidationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
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

    @Override
    public Bill createBill(String authHeader, Bill bill) throws ValidationException, UnAuthorizedLoginException {

        ResponseEntity responseBody = authServiceImpl.checkIfUserExists(authHeader);
        if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            User u = (User) responseBody.getBody();
            validationService.isBillValid(bill);
            bill.setUser(u);
            PaymentStatusType pay =  bill.getPaymentStatus();
            bill.setPaymentStatus(pay);
            Bill savedBill = billRepo.save(bill);
            return savedBill;

        } else {
            throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
        }
    }

    @Override
    public List<Bill> getAllBills(String authHeader) throws UnAuthorizedLoginException, ValidationException {

        ResponseEntity responseBody = authServiceImpl.checkIfUserExists(authHeader);
        User u = (User) responseBody.getBody();
        if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            List<Bill> bill_list = billRepo.findBillsForAUser(u.getId());
            return bill_list;

        } else {
            throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
        }
    }

    @Override
    public Bill getBill(String authHeader, UUID bill_id) throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException {

        ResponseEntity responseBody = authServiceImpl.checkIfUserExists(authHeader);
        if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            User u = (User) responseBody.getBody();
            Optional<Bill> billWrapper = billRepo.findById(bill_id);
            Bill b = billRepo.findBillById(u.getId(), bill_id);
            if (!billWrapper.isPresent() || b == null) {
                throw new ResourceNotFoundException("Bill with ID: " + bill_id.toString() + " does not exist");
            }
            if (billWrapper.isPresent() && b==null) {
                throw new UnAuthorizedLoginException("Unauthorized to access the bill!");
            }
            return b;

        } else {
            throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
        }

    }

    @Override
    public Bill updateBill(String authHeader, UUID bill_id, Bill bill) throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException {

        ResponseEntity responseBody = authServiceImpl.checkIfUserExists(authHeader);
        if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            validationService.isBillValid(bill);
            Optional<Bill> billWrapper = billRepo.findById(bill_id);

            if (!billWrapper.isPresent()) {
                throw new ResourceNotFoundException("Bill with ID: " + bill_id.toString() + " does not exist");
            }
            if (billWrapper.isPresent() && billWrapper.get().getUser()!=responseBody.getBody()) {
                throw new UnAuthorizedLoginException("Unauthorized to access the bill!");
            }
            Bill savedBill=billWrapper.get();
            savedBill.setAmountDue(bill.getAmountDue());
            savedBill.setBillDate(bill.getBillDate());
            savedBill.setCategories(bill.getCategories());
            savedBill.setDueDate(bill.getDueDate());
            savedBill.setPaymentStatus(bill.getPaymentStatus());
            savedBill.setVendor(bill.getVendor());
            billRepo.save(savedBill);
            return savedBill;

        } else {
            throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
        }

    }

    @Override
    public ResponseEntity deleteBill(String authHeader, UUID bill_id) throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException {

        Bill fetchedBill = getBill(authHeader, bill_id);
        billRepo.delete(fetchedBill);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


}
