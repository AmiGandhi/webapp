package neu.csye6225.spring2020.cloud.service.impl;

import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.Bill;
import neu.csye6225.spring2020.cloud.service.ValidationService;
import org.springframework.stereotype.Service;

import static neu.csye6225.spring2020.cloud.constants.ApplicationConstants.MANDATORY_FIELDS_MISSING;

@Service
public class ValidationServiceImpl implements ValidationService {
    @Override
    public void isBillValid(Bill bill) throws ValidationException {

        if(bill.getBillId()== null|| bill.getOwnerId()==null|| bill.getVendor()==null||
                bill.getAmountDue() ==null || bill.getBillDate()==null || bill.getDueDate()==null ||
                bill.getCategories()==null || bill.getPaymentStatus() == null)
        {
            throw new ValidationException(MANDATORY_FIELDS_MISSING);
        }
    }
}
