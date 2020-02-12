package neu.csye6225.spring2020.cloud.service.impl;

import neu.csye6225.spring2020.cloud.exception.FileStorageException;
import neu.csye6225.spring2020.cloud.exception.ResourceNotFoundException;
import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.Bill;
import neu.csye6225.spring2020.cloud.model.File;
import neu.csye6225.spring2020.cloud.model.PaymentStatusType;
import neu.csye6225.spring2020.cloud.model.User;
import neu.csye6225.spring2020.cloud.repository.BillRepository;
import neu.csye6225.spring2020.cloud.repository.FileRepository;
import neu.csye6225.spring2020.cloud.service.BillService;
import neu.csye6225.spring2020.cloud.service.FileStorageService;
import neu.csye6225.spring2020.cloud.service.ValidationService;
import neu.csye6225.spring2020.cloud.util.CommonUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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
    private FileRepository fileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CommonUtil commonUtil;

    private static final Logger logger = LogManager.getLogger(BillServiceImpl.class);

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


    //implementation for bill attachment
    @Override
    public File saveAttachment(String auth, UUID bill_id, MultipartFile file)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException, FileStorageException, IOException, NoSuchAlgorithmException {

        ResponseEntity responseBody = authServiceImpl.checkIfUserExists(auth);
        if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            try {
                Bill fetchedBill = getBill(auth, bill_id);
                if (fetchedBill.getAttachment() == null) {
                    String fileLocation = fileStorageService.storeFile(file);
                    File f = new File(fileLocation, fetchedBill);
                    f.setSize(Long.toString(file.getSize()));
                    f.setMd5(computeMD5Hash(file.getBytes()));
                    f.setFile_name(commonUtil.getFileNameFromPath(fileLocation));
                    return fileRepository.save(f);
                } else {
                    throw new ValidationException("Cannot attach the file!");
                }

            } catch (Exception e) {
                logger.error(commonUtil.stackTraceString(e));
                throw e;
            }
        } else {
                throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
            }
    }

    @Override
    public File getAttachment(String auth, UUID bill_id, UUID file_id)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException {

        ResponseEntity responseBody = authServiceImpl.checkIfUserExists(auth);
        if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            try {
                Bill fetchedBill = getBill(auth, bill_id);
                if (fetchedBill != null) {
                    if (fetchedBill.getAttachment().getId().equals(file_id)) {
                        return fetchedBill.getAttachment();
                    } else {
                        throw new ResourceNotFoundException("File Id not found with id: " + file_id);
                    }
                } else {
                    throw new ResourceNotFoundException("Bill not found with id: " + bill_id);
                }

            } catch (Exception e) {
                logger.error(commonUtil.stackTraceString(e));
                throw e;
            }
        } else {
            throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
            }
    }

    @Override
    public void deleteAttachment(String auth, UUID bill_id, UUID file_id)
            throws ValidationException, UnAuthorizedLoginException, FileStorageException, ResourceNotFoundException {

        ResponseEntity responseBody = authServiceImpl.checkIfUserExists(auth);
        if (responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            try {
                Bill fetchedBill = getBill(auth, bill_id);
                if (fetchedBill != null) {
                    if (fetchedBill.getAttachment().getId().equals(file_id)) {
                        fileStorageService.deleteFile(fetchedBill.getAttachment().getUrl());
                        fileRepository.delete(fetchedBill.getAttachment());
                    } else {
                        throw new ResourceNotFoundException("File Id not found with id: " + file_id);
                    }
                } else {
                    throw new ResourceNotFoundException("Bill not found with id: " + bill_id);
                }

            } catch (Exception e) {
                logger.error(commonUtil.stackTraceString(e));
                throw e;
            }
        } else {
            throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
        }

/*        try {
            Bill fetchedBill = getBill(auth, bill_id);
            File file = getAttachmentForBill(file_id, fetchedBill);
            fileStorageService.deleteFile(file.getUrl());
            fileRepository.delete(file);
        } catch (Exception e) {
            logger.error(commonUtil.stackTraceString(e));
            throw e;
        }*/

    }

/*    private File getAttachmentForBill(UUID file_id, Bill fetchedBill)
            throws ValidationException, UnAuthorizedLoginException {
        Optional<File> attachmentWrapper = fileRepository.findById(file_id);
        if (!attachmentWrapper.isPresent() || attachmentWrapper.get() == null) {
            throw new ValidationException("Attachment with ID: " + file_id.toString() + " does not exist");
        }
        File f = attachmentWrapper.get();
        if (fetchedBill.getAttachment().equals(f)) {
            return f;
        }
        throw new UnAuthorizedLoginException(
                "Attachment with ID: " + file_id.toString() + " is not one of the attachments of your note");

    }*/

    public String computeMD5Hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        byte[] digest = messageDigest.digest(data);

        StringBuffer sb = new StringBuffer();
        for (byte b : digest) {
            sb.append(Integer.toHexString((int) (b & 0xff)));
        }
        return sb.toString();
    }

}
