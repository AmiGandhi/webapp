package neu.csye6225.spring2020.cloud.mockTest;

import neu.csye6225.spring2020.cloud.exception.ValidationException;
import neu.csye6225.spring2020.cloud.model.Bill;
import neu.csye6225.spring2020.cloud.model.User;
import neu.csye6225.spring2020.cloud.repository.BillRepository;
import neu.csye6225.spring2020.cloud.repository.UserRepository;
import neu.csye6225.spring2020.cloud.service.AuthService;
import neu.csye6225.spring2020.cloud.service.BillService;
import neu.csye6225.spring2020.cloud.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestApp {


    @Autowired
    private UserService service;

    @Autowired
    private AuthService authService;
    @MockBean
    private UserRepository repository;

    @Test
    public void saveUserTest() throws ValidationException {
        User user = new User();
        user.setFirst_name("Ami");
        user.setLast_name("Gandhi");
        user.setEmailAddress("gandhi.am@husky.neu.edu");
        user.setPassword("Password@2703");

        when(repository.save(user)).thenReturn(user);
        assertEquals(user, service.createUser(user));
    }

    @Test
    public void getUserbyEmailTest() {
        User user = new User();
        user.setFirst_name("Ami");
        user.setLast_name("Gandhi");
        user.setEmailAddress("gandhi.am@husky.neu.edu");
        user.setPassword("Password@2703");
        String email = "gandhi.am@husky.neu.edu";
        when(repository.findByEmailAddress(email))
                .thenReturn(user);
        assertEquals(user, authService.findByEmail_address(email));
    }

}

