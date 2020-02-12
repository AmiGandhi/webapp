package neu.csye6225.spring2020.cloud.model;

import com.fasterxml.jackson.annotation.*;

import neu.csye6225.spring2020.cloud.service.EnumNamePattern;
import org.hibernate.annotations.GenericGenerator;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

@Entity
@Table(name = "bill_data")
@EntityListeners(AuditingEntityListener.class)
public class Bill {

    @JsonProperty(value="bill_id")
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.StandardRandomStrategy") })
    @Column(name = "bill_id")
    private UUID id;

    @JsonProperty(value= "created_ts",access= JsonProperty.Access.READ_ONLY)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'z'", timezone="America/New_York")
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    @Column(name = "created_ts")
    Date createdAt;

    @JsonProperty(value="updated_ts", access= JsonProperty.Access.READ_ONLY)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'z'", timezone="America/New_York")
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @Column(name = "updated_ts")
    Date updatedAt;

    @NotNull(message="vendor field is mandatory")
    @JsonProperty(value="vendor")
    String vendor;

    @JsonProperty(value="bill_date")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @NotNull(message="bill_date is mandatory")
    @Column(name = "bill_date")
    Date billDate;

    @JsonProperty(value="due_date")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @NotNull(message="due_date is mandatory")
    @Column(name = "due_date")
    Date dueDate;

    @JsonProperty(value="amount_due")
    @DecimalMin("0.01")
    @NotNull(message="amount_due is mandatory")
    @Column(name = "amount_due")
    Double amountDue;

    @JsonProperty(value="categories")
    //@UniqueElements
    //@Column(unique = true)
    @Size(min=1, message="Atleast 1 category is mandatory!")
    @ElementCollection
    private Set <String> categories = new HashSet<String>() ;

    @JsonProperty(value="paymentStatus")
    @EnumNamePattern(regexp = "paid|due|past_due|no_payment_required", message="Value has to be either paid, due, past_due, no_payment_required")
    @NotNull(message="paymentStatus is mandatory")
    @Enumerated(EnumType.STRING)
    PaymentStatusType paymentStatus;

    @OneToOne
    @JoinColumn(name = "owner_id")
    @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("owner_id")
    private User user;


    @OneToOne(cascade = CascadeType.REMOVE)
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @JoinColumn(name = "file_id")
    private File attachment;

    // getters and setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Double getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(Double amountDue) {
        this.amountDue = amountDue;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public PaymentStatusType getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatusType paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public File getAttachment() {
        return attachment;
    }

    public void setAttachment(File attachment) {
        this.attachment = attachment;
    }
}
