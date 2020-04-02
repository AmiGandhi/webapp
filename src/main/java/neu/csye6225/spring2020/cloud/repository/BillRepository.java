package neu.csye6225.spring2020.cloud.repository;

import neu.csye6225.spring2020.cloud.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {

    //Bill getAllBillsForUser(User user);

    @Query(value="select * from bill_data b where  b.owner_id =?", nativeQuery= true)
    public List<Bill> findBillsForAUser(UUID id);

    @Query(value="select * from bill_data b where  b.owner_id =? AND b.bill_id=?", nativeQuery= true)
    public Bill findBillById(UUID id, UUID billId);

/*    @Query(value = "select a from Bill a where a.attachment = :attachment")
    public Bill getAllAttachmentsForUser(File file);*/

    public void deleteById(UUID id);

//    public List<Bill> findAllByDueDateLessThanEqualAndEndDateGreaterThanEqual(Date endDate, Date startDate);

}
