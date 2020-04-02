package neu.csye6225.spring2020.cloud.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import neu.csye6225.spring2020.cloud.aws.SNSClient;
import com.timgroup.statsd.StatsDClient;
import neu.csye6225.spring2020.cloud.aws.SQSClient;
import neu.csye6225.spring2020.cloud.aws.SQSPolling;
import neu.csye6225.spring2020.cloud.exception.FileStorageException;
import neu.csye6225.spring2020.cloud.exception.ResourceNotFoundException;
import neu.csye6225.spring2020.cloud.exception.UnAuthorizedLoginException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.*;
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
import java.rmi.ServerException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;

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

    @Autowired
    private StatsDClient statsDClient;

    @Autowired
    private SNSClient awssnsClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private SQSClient awssqsClient;

    @Value("${spring.profiles.active}")
    private String profile;
    @Value("${amazonProperties.bucketName}")
    private String bucketName;

    private static final Logger logger = LogManager.getLogger(BillServiceImpl.class);

    @Override
    public Bill createBill(String authHeader, Bill bill) throws ValidationException, UnAuthorizedLoginException {

        statsDClient.incrementCounter("endpoint.bill.create.http.post");
        long start = System.currentTimeMillis();
        try {
            ResponseEntity responseBody = authServiceImpl.checkIfUserExists(authHeader);
            if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT))
            {
                User u = (User) responseBody.getBody();
                validationService.isBillValid(bill);
                bill.setUser(u);
                PaymentStatusType pay =  bill.getPaymentStatus();
                bill.setPaymentStatus(pay);
                bill.setAttachment(null);

                long startTime = System.currentTimeMillis();
                Bill savedBill = billRepo.save(bill);
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                statsDClient.recordExecutionTime("CreatedBillInDatabase:",duration);
                long end = System.currentTimeMillis();
                long time = (end - start);
                statsDClient.recordExecutionTime("CreateBillApiCall",time);
                logger.info("Bill created successfully!");
                return savedBill;
            } else {
                throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
            }
        } catch (Exception e) {
            logger.error(commonUtil.stackTraceString(e));
            throw e;
        }
    }

    @Override
    public List<Bill> getAllBills(String authHeader) throws UnAuthorizedLoginException, ValidationException {

        statsDClient.incrementCounter("endpoint.bill.getAll.http.get");
        long start = System.currentTimeMillis();
        try {
            ResponseEntity responseBody = authServiceImpl.checkIfUserExists(authHeader);
            User u = (User) responseBody.getBody();
            if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT))
            {
                long startTime = System.currentTimeMillis();
                List<Bill> bill_list = billRepo.findBillsForAUser(u.getId());
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                statsDClient.recordExecutionTime("GetBillFromDatabase",duration);

                long end = System.currentTimeMillis();
                long time = (end - start);
                statsDClient.recordExecutionTime("GetAllBillsApiCall",time);
                logger.info("Found all bills successfully!");
                return bill_list;
            } else {
                throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, commonUtil.stackTraceString(e));
            throw e;
        }
    }

    @Override
    public Bill getBill(String authHeader, UUID bill_id) throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException {

        statsDClient.incrementCounter("endpoint.bill.getOne.http.get");
        long start = System.currentTimeMillis();
        try {
            ResponseEntity responseBody = authServiceImpl.checkIfUserExists(authHeader);
            if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT))
            {
                long startTime = System.currentTimeMillis();
                User u = (User) responseBody.getBody();
                Optional<Bill> billWrapper = billRepo.findById(bill_id);
                Bill b = billRepo.findBillById(u.getId(), bill_id);
                if (!billWrapper.isPresent() || b == null) {
                    throw new ResourceNotFoundException("Bill with ID: " + bill_id.toString() + " does not exist");
                }
                if (billWrapper.isPresent() && b==null) {
                    throw new UnAuthorizedLoginException("Unauthorized to access the bill!");
                }
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                statsDClient.recordExecutionTime("GetOneBillFromDatabase",duration);
                long end = System.currentTimeMillis();
                long time = (end - start);
                statsDClient.recordExecutionTime("GetOneBillApiCall",time);
                logger.info("Found bill successfully!");
                return b;
            } else {
                throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
            }
        } catch (Exception e) {
            logger.error(commonUtil.stackTraceString(e));
            throw e;
        }
    }

    @Override
    public Bill updateBill(String authHeader, UUID bill_id, Bill bill) throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException {

        statsDClient.incrementCounter("endpoint.bill.update.http.put");
        long start = System.currentTimeMillis();
        try {
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

                long startTime = System.currentTimeMillis();
                billRepo.save(savedBill);
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                statsDClient.recordExecutionTime("UpdateBillInDatabase:",duration);
                long end = System.currentTimeMillis();
                long time = (end - start);
                statsDClient.recordExecutionTime("UpdateBillApiCall",time);
                logger.info("Bill updated successfully!");
                return savedBill;
            } else {
                throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
            }
        } catch (Exception e) {
            logger.error(commonUtil.stackTraceString(e));
            throw e;
        }
    }

    @Override
    public ResponseEntity deleteBill(String authHeader, UUID bill_id) throws ValidationException, ResourceNotFoundException, UnAuthorizedLoginException, FileStorageException {

        statsDClient.incrementCounter("endpoint.bill.delete.http.delete");
        long start = System.currentTimeMillis();
        try {
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
                            long begin = System.currentTimeMillis();
                            s3client.deleteObject(
                                    new DeleteObjectRequest(bucketName, fileToDelete));
                            long terminate = System.currentTimeMillis();
                            long timeElapsed = (terminate - begin);
                            statsDClient.recordExecutionTime("DeleteBillFromS3Bucket",timeElapsed);
                        } catch (Exception e) {
                            throw new FileStorageException("File not stored in S3 bucket. File name: " + fileToDelete);
                        }
                    }

                }
                long startTime = System.currentTimeMillis();
                billRepo.delete(fetchedBill);
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                statsDClient.recordExecutionTime("DeleteBillFromDatabase",duration);
            } else {
                throw new ResourceNotFoundException("Bill not found with id: " + bill_id);
            }
            long end = System.currentTimeMillis();
            long time = (end - start);
            statsDClient.recordExecutionTime("DeleteBillApiCall",time);
            logger.info("Bill deleted successfully!");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error(commonUtil.stackTraceString(e));
            throw e;
        }
    }


    //implementation for bill attachment
    @Override
    public File saveAttachment(String auth, UUID bill_id, MultipartFile file)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException, FileStorageException, IOException, NoSuchAlgorithmException {

        statsDClient.incrementCounter("endpoint.file.create.http.post");
        long start = System.currentTimeMillis();
        try {
            ResponseEntity responseBody = authServiceImpl.checkIfUserExists(auth);
            if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                Bill fetchedBill = getBill(auth, bill_id);
                if (fetchedBill != null) {
                    if (fetchedBill.getAttachment() == null) {

                        String fileLocation = fileStorageService.storeFile(file);
                        File f = new File(fileLocation);
                        f.setSize(Long.toString(file.getSize()));
                        f.setMd5(commonUtil.computeMD5Hash(file.getBytes()));
                        f.setFile_name(commonUtil.getFileNameFromPath(fileLocation));

                        long startTime = System.currentTimeMillis();
                        File newfile = fileRepository.save(f);
                        fetchedBill.setAttachment(f);
                        billRepo.save(fetchedBill);
                        long endTime = System.currentTimeMillis();
                        long duration = (endTime - startTime);
                        statsDClient.recordExecutionTime("CreateAttachmentInDatabase",duration);

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
                                long beginTime = System.currentTimeMillis();
                                s3client.putObject(
                                        new PutObjectRequest(bucketName, fileNewName, file.getInputStream(), objectMeatadata).withCannedAcl(CannedAccessControlList.Private));
                                long lastTime = System.currentTimeMillis();
                                long timeTaken = (lastTime - beginTime);
                                statsDClient.recordExecutionTime("CreateAttachmentInS3Bucket",timeTaken);
                            } catch (Exception e) {

                                throw new FileStorageException("File not stored in S3 bucket. File name: " + fileNewName+""+e);
                            }
                            long startingTime = System.currentTimeMillis();
                            fileRepository.save(f);
                            fetchedBill.setAttachment(f);
                            billRepo.save(fetchedBill);
                            long endingTime = System.currentTimeMillis();
                            long timeElapsed = (endingTime - startingTime);
                            statsDClient.recordExecutionTime("CreateAttachmentInDatabase",timeElapsed);
                        }
                        long end = System.currentTimeMillis();
                        long time = (end - start);
                        statsDClient.recordExecutionTime("CreateAttachmentApiCall",time);
                        logger.info("Attachment created successfully!");
                        return newfile;
                    } else {
                        throw new ValidationException("File already exists!");
                    }
                } else {
                    throw new ValidationException("Bill does not exist!");
                }
            } else {
                throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
            }
        } catch (Exception e) {
            logger.error(commonUtil.stackTraceString(e));
            throw e;
        }
    }

    @Override
    public File getAttachment(String auth, UUID bill_id, UUID file_id)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException {

        statsDClient.incrementCounter("endpoint.file.get.http.get");
        long start = System.currentTimeMillis();
        try {
            ResponseEntity responseBody = authServiceImpl.checkIfUserExists(auth);
            if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                Bill fetchedBill = getBill(auth, bill_id);
                if (fetchedBill != null) {
                    long startTime = System.currentTimeMillis();
                    Optional <File> fileObj = fileRepository.findById(file_id);
                    if (fileObj.isPresent()) {
                        File file = fileObj.get();
                        File dbfile = fetchedBill.getAttachment();
                        if (dbfile != null && dbfile.equals(file)) {
                            long endTime = System.currentTimeMillis();
                            long duration = (endTime - startTime);
                            statsDClient.recordExecutionTime("GetAttachmentFromDatabase:",duration);
                            long end = System.currentTimeMillis();
                            long time = (end - start);
                            statsDClient.recordExecutionTime("GetAttachmentApiCall",time);
                            logger.info("Attachment found!");
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
            } else {
                throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
            }
        } catch (Exception e) {
            logger.error(commonUtil.stackTraceString(e));
            throw e;
        }
    }

    @Override
    public void deleteAttachment(String auth, UUID bill_id, UUID file_id)
            throws ValidationException, UnAuthorizedLoginException, FileStorageException, ResourceNotFoundException {

        statsDClient.incrementCounter("endpoint.file.delete.http.delete");
        long start = System.currentTimeMillis();
        try {
            ResponseEntity responseBody = authServiceImpl.checkIfUserExists(auth);
            if (responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
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
                                    long begin = System.currentTimeMillis();
                                    s3client.deleteObject(
                                            new DeleteObjectRequest(bucketName, fileToDelete));
                                    long terminate = System.currentTimeMillis();
                                    long timeTaken = (terminate - begin);
                                    statsDClient.recordExecutionTime("DeleteAttachmentFromS3Bucket",timeTaken);
                                } catch (Exception e) {
                                    throw new FileStorageException("File not stored in S3 bucket. File name: " + fileToDelete);
                                }
                            }
                            fetchedBill.setAttachment(null);
                            billRepo.save(fetchedBill);
                            long startTime = System.currentTimeMillis();
                            fileRepository.deleteById(file_id);
                            long endTime = System.currentTimeMillis();
                            long duration = (endTime - startTime);
                            statsDClient.recordExecutionTime("DeleteAttachmentFromDatabase",duration);
                            long end = System.currentTimeMillis();
                            long time = (end - start);
                            statsDClient.recordExecutionTime("DeleteAttachmentApiCall",time);
                            logger.info("Attachment deleted successfully!");
                        } else {
                            throw new ResourceNotFoundException("File not found with id: " + file_id);
                        }
                    } else {
                        throw new ResourceNotFoundException("File not found with id: " + file_id);
                    }
                } else {
                    throw new ResourceNotFoundException("Bill not found with id: " + bill_id);
                }
            } else {
                throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
            }
        } catch (Exception e) {
            logger.error(commonUtil.stackTraceString(e));
            throw e;
        }
    }

    // service to post bills to SNS topic and trigger lambda function
    @Override
    public List<Bill> getDueBills(String authHeader, Integer x_days)
            throws ValidationException, UnAuthorizedLoginException, ResourceNotFoundException, ServerException {

        statsDClient.incrementCounter("endpoint.bill.getDue.http.get");
        long start = System.currentTimeMillis();
        try {
            ResponseEntity responseBody = authServiceImpl.checkIfUserExists(authHeader);
            User u = (User) responseBody.getBody();
            String recipientEmail = u.getEmailAddress();

            if(responseBody.getStatusCode().equals(HttpStatus.NO_CONTENT))
            {

                long startTime = System.currentTimeMillis();
                List<Bill> bill_list = billRepo.findBillsForAUser(u.getId());
//                List<Bill> bill_list = billRepo.findAllByDueDateLessThanEqualAndEndDateGreaterThanEqual(endDate, dateToday);
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                statsDClient.recordExecutionTime("GetDueBillsFromDatabase",duration);

                if (bill_list.isEmpty()) {
                    throw new ResourceNotFoundException("There are no bills for the user");
                }

                LocalDate startDate = LocalDate.now();
                java.util.Date currentDate = java.sql.Date.valueOf(startDate);
                LocalDate dueDate =  LocalDate.now().plusDays(x_days);
                java.util.Date endDate = java.sql.Date.valueOf(dueDate);

                List<Bill> dueBillList = new ArrayList<Bill>();
                for (Bill bill : bill_list) {
                    Date billDueDate = bill.getDueDate();
                    if (billDueDate.compareTo(currentDate) == 0 || billDueDate.after(currentDate) && billDueDate.before(endDate)) {
                        dueBillList.add(bill);
                    }
                }

                if (profile.equalsIgnoreCase("aws")) {

//                    awssqsClient.publishToQueue("Hello, this is my first message!");

                    // create long polling SQS Queue
                    // https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-configure-long-polling-for-queue.html
                    // or
                    // https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/standard-queues-getting-started-java.html
                    // awssqsClient.createSQSQueue();

                    List<UUID> dueBillIds = new ArrayList<>();
                    for (Bill bill : dueBillList) {
                        dueBillIds.add(bill.getId());
                    }

//                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
//                    Map<String, List<UUID>> objMap = new HashMap<>();
//                    objMap.put(recipientEmail, dueBillIds);

                    // created POJO to pass key names in message body for the values
                    DueBill dueBill = new DueBill();
                    dueBill.setEmail(recipientEmail);
                    dueBill.setDueBillIdList(dueBillIds);

                    try{
                        awssqsClient.publishToQueue(mapper.writeValueAsString(dueBill));
                    } catch (JsonProcessingException jsonParsingException) {
                        logger.error("Unable to parse the request json", jsonParsingException);
                        throw new ServerException("Unable to parse the request json", jsonParsingException);
                    }

                    SQSPolling myThread = new SQSPolling(awssqsClient, awssqsClient.getQueueUrl());
                    myThread.start();

                }

                long end = System.currentTimeMillis();
                long time = (end - start);
                statsDClient.recordExecutionTime("GetDueBillsApiCall",time);

                logger.info("Found due bills successfully!");
                return dueBillList;

            } else {
                throw new UnAuthorizedLoginException(INVALID_CREDENTIALS);
            }

        } catch (Exception e) {
            logger.error(commonUtil.stackTraceString(e));
            throw e;
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
