package neu.csye6225.spring2020.cloud.service;

import neu.csye6225.spring2020.cloud.exception.FileStorageException;
import neu.csye6225.spring2020.cloud.exception.ResourceNotFoundException;
import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.Bill;
import neu.csye6225.spring2020.cloud.model.File;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
            throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException, FileStorageException;

    // file for bill
    public File saveAttachment(String auth, UUID bill_id, MultipartFile file)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException, FileStorageException, IOException, NoSuchAlgorithmException;

    public File getAttachment(String auth, UUID bill_id, UUID file_id)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException;

    public void deleteAttachment(String auth, UUID bill_id, UUID file_id)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException, FileStorageException;

}
