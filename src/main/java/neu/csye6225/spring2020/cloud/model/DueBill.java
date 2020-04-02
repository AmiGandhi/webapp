package neu.csye6225.spring2020.cloud.model;

import java.util.List;
import java.util.UUID;

public class DueBill {

    private String email;
    private List<UUID> dueBillIdList;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<UUID> getDueBillIdList() {
        return dueBillIdList;
    }

    public void setDueBillIdList(List<UUID> dueBillIdList) {
        this.dueBillIdList = dueBillIdList;
    }
}
