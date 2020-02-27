package neu.csye6225.spring2020.cloud.service.impl;

import neu.csye6225.spring2020.cloud.exception.FileStorageException;
import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.property.FileStorageProperties;
import neu.csye6225.spring2020.cloud.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;



@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;


    @Value("${spring.profiles.active}")
    private String profile;

    public FileStorageServiceImpl() throws FileStorageException {
        this.fileStorageLocation = Paths.get(System.getProperty("user.home")).toAbsolutePath().normalize();
    }


    @Override
    public String storeFile(MultipartFile file) throws FileStorageException, ValidationException {

        String fileName = generateFileName(file);

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            String fileExtentions = ".png,.jpg,.jpeg,.pdf";
            int lastIndex = fileName.lastIndexOf('.');
            String substring = fileName.substring(lastIndex, fileName.length());

            if (!fileExtentions.contains(substring)) {
                throw new ValidationException("File format not supported. Only .png,.jpg,.jpeg,.pdf files allowed!");
            } else {
                // Copy file to the target location (Replacing existing file with the same name)
                Path targetLocation = this.fileStorageLocation.resolve(fileName);
                if(!profile.equalsIgnoreCase("aws")) {
                    Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                }

                return targetLocation.toString();
            }

        }
        catch(ValidationException ex) {
            throw new ValidationException("File type not supported!");
        }
        catch (Exception ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }


    @Override
    public void deleteFile(String fileUrl) throws FileStorageException {
        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileUrl);
            Files.delete(targetLocation);
        } catch (Exception e) {
            throw new FileStorageException("Could not delete file " + fileUrl + ". Please try again!", e);
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
