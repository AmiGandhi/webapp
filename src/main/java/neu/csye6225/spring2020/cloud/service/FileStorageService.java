package neu.csye6225.spring2020.cloud.service;

import neu.csye6225.spring2020.cloud.exception.FileStorageException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    public String storeFile(MultipartFile file) throws FileStorageException, ValidationException;

    public void deleteFile(String fileUrl) throws FileStorageException;

}
