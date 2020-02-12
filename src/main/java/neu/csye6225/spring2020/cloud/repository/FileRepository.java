package neu.csye6225.spring2020.cloud.repository;

import neu.csye6225.spring2020.cloud.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.UUID;

@Transactional
public interface FileRepository extends JpaRepository<File, UUID> {

//    @Query(value = "select a from File a where a.bill = :bill")
//    public File getAllAttachmentsForUser(Bill bill);

}

