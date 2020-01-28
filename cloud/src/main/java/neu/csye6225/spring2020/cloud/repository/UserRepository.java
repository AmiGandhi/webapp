package neu.csye6225.spring2020.cloud.repository;

import java.util.List;
import java.util.UUID;
import neu.csye6225.spring2020.cloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, UUID> {

    User findByEmailAddress(String email_address);

}

