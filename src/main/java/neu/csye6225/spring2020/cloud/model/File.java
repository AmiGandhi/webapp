package neu.csye6225.spring2020.cloud.model;

import com.fasterxml.jackson.annotation.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "user_file_data")
@EntityListeners(AuditingEntityListener.class)
public class File {

    public File() {
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @NotNull("File name is mandatory!")
    String file_name;

    @JsonProperty(value="file_id", access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.StandardRandomStrategy") })
    @Column(name = "file_id")
    private UUID id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @NotNull("File url is mandatory!")
    String url;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @NotNull(message="File upload date is mandatory!")
    Date upload_date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Bill bill;

    // getter and setter


    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(Date upload_date) {
        this.upload_date = upload_date;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }
}
