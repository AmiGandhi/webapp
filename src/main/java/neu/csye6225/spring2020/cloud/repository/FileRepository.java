package neu.csye6225.spring2020.cloud.repository;

import neu.csye6225.spring2020.cloud.model.Bill;
import neu.csye6225.spring2020.cloud.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {

    @Query(value = "select a from File a where a.bill = :bill")
    public File getAllAttachmentsForUser(Bill bill);

}

