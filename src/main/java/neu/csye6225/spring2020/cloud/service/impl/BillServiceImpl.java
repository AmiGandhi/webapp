package neu.csye6225.spring2020.cloud.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private AmazonS3 s3client;

    @Value("${spring.profiles.active}")
    private String profile;
    @Value("${amazonProperties.bucketName}")
    private String bucketName;

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

            bill.setAttachment(null);
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

    // same service as above with different GET url to check if CI/CD pipeline works fine
    @Override
    public List<Bill> getAllBillsAgain(String authHeader) throws UnAuthorizedLoginException, ValidationException {

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
    public ResponseEntity deleteBill(String authHeader, UUID bill_id) throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException, FileStorageException {

       Bill fetchedBill = getBill(authHeader, bill_id);
        if (fetchedBill != null) {
            if (fetchedBill.getAttachment() != null) {
                if (!profile.equalsIgnoreCase("aws")) {
                    File fileToDelete = fetchedBill.getAttachment();
                    fileStorageService.deleteFile(fileToDelete.getUrl());
                } else {
                    String fileToDelete = null;
                    try {
                        String fileLocation = fetchedBill.getAttachment().getUrl();
                        fileToDelete = fileLocation.substring(fileLocation.lastIndexOf("/") + 1);
                        s3client.deleteObject(
                                new DeleteObjectRequest(bucketName, fileToDelete));
                    } catch (Exception e) {
                        throw new FileStorageException("File not stored in S3 bucket. File name: " + fileToDelete);
                    }
                }

            }
            billRepo.delete(fetchedBill);
        } else {
            throw new ResourceNotFoundException("Bill not found with id: " + bill_id);
        }
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
                if (fetchedBill != null) {
                    if (fetchedBill.getAttachment() == null) {

                        String fileLocation = fileStorageService.storeFile(file);
                        File f = new File(fileLocation);
                        f.setSize(Long.toString(file.getSize()));
                        f.setMd5(commonUtil.computeMD5Hash(file.getBytes()));
                        f.setFile_name(commonUtil.getFileNameFromPath(fileLocation));

                        File newfile = fileRepository.save(f);
                        fetchedBill.setAttachment(f);
                        billRepo.save(fetchedBill);

                        // new file to store in s3
                        String fileNewName = generateFileName(file);

                        if (!profile.equalsIgnoreCase("aws")) {
//                            File newfile = fileRepository.save(f);
//                            fetchedBill.setAttachment(f);
//                            billRepo.save(fetchedBill);
                            Path targetLocation = Paths.get(f.getUrl());
                            byte[] fileBytes = file.getBytes();
                            Files.write(targetLocation, fileBytes);
                        } else {
                            try {
                                s3client = new AmazonS3Client();
                                ObjectMetadata objectMeatadata = new ObjectMetadata();
                                objectMeatadata.setContentType(file.getContentType());
                                fileNewName = generateFileName(file);
                                f.setUrl("https://" + bucketName + ".s3.amazonaws.com" + "/" + fileNewName);
                                s3client.putObject(
                                        new PutObjectRequest(bucketName, fileNewName, file.getInputStream(), objectMeatadata).withCannedAcl(CannedAccessControlList.Private));
                            } catch (Exception e) {

                                throw new FileStorageException("File not stored in S3 bucket. File name: " + fileNewName+""+e);
                            }
                            fileRepository.save(f);
                            fetchedBill.setAttachment(f);
                            billRepo.save(fetchedBill);
                        }

                        return newfile;

                    } else {
                        throw new ValidationException("File already exists!");
                    }
                } else {
                    throw new ValidationException("Bill does not exist!");
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
                    Optional <File> fileObj = fileRepository.findById(file_id);
                    if (fileObj.isPresent()) {
                        File file = fileObj.get();
                        File dbfile = fetchedBill.getAttachment();
                        if (dbfile != null && dbfile.equals(file)) {
                            return file;
                        } else {
                            throw new ResourceNotFoundException("File not found with id: " + file_id);
                        }
                    } else {
                            throw new ResourceNotFoundException("File not found with id: " + file_id);
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
                    Optional<File> fileObj = fileRepository.findById(file_id);
                    if (fileObj.isPresent()) {
                        File file = fileObj.get();
                        File dbfile = fetchedBill.getAttachment();
                        if (dbfile != null && dbfile.equals(file)) {
                            if (!profile.equalsIgnoreCase("aws")) {
                                File fileToDelete = new File(dbfile.getUrl());
                                fileStorageService.deleteFile(fileToDelete.getUrl());
                            } else {
                                String fileToDelete = null;
                                try {
                                    String fileLocation = dbfile.getUrl();
                                    fileToDelete = fileLocation.substring(fileLocation.lastIndexOf("/") + 1);
                                    s3client.deleteObject(
                                            new DeleteObjectRequest(bucketName, fileToDelete));
                                } catch (Exception e) {
                                    throw new FileStorageException("File not stored in S3 bucket. File name: " + fileToDelete);
                                }
                            }

                            fetchedBill.setAttachment(null);
                            billRepo.save(fetchedBill);
                            fileRepository.deleteById(file_id);

                        } else {
                            throw new ResourceNotFoundException("File not found with id: " + file_id);
                        }
                    } else {
                        throw new ResourceNotFoundException("File not found with id: " + file_id);
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

    private String generateFileName(MultipartFile multiPart) {
        return generateUUID() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }

    private String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

}
