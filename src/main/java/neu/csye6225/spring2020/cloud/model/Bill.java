package neu.csye6225.spring2020.cloud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "bill_data")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"created_ts", "updated_ts"},
        allowGetters = true)
@JsonPropertyOrder({ "bill_id", "created_ts", "updated_ts", "owner_id", "vendor", "bill_date", "due_date", "amount_due", "categories", "paymentStatus"})
public class Bill {

    @JsonProperty(value="bill_id",access= JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.StandardRandomStrategy") })
    @Column(name = "bill_id")
    UUID billId;

    @JsonProperty(value= "created_ts",access= JsonProperty.Access.READ_ONLY)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    @Column(name = "created_ts")
    Date createdAt;

    @JsonProperty(value="updated_ts", access= JsonProperty.Access.READ_ONLY)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @Column(name = "updated_ts")
    Date updatedAt;

    @JsonProperty(value="owner_id", access= JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.StandardRandomStrategy") })
    @Column(name = "owner_id")
    UUID ownerId;

    @JsonProperty(value="vendor")
    String vendor;

    @JsonProperty(value="bill_date")
    @Temporal(TemporalType.DATE)
    @Column(name = "bill_date")
    Date billDate;

    @JsonProperty(value="due_date")
    @Temporal(TemporalType.DATE)
    @Column(name = "due_date")
    Date dueDate;

    @JsonProperty(value="amount_due")
    @DecimalMin("0.01")
    @Column(name = "amount_due")
    Double amountDue;

    @JsonProperty(value="categories")
    //@UniqueElements
    //@Column(unique = true)
    @ElementCollection
    @CollectionTable(name="categories")
    Set<String> categories;

    @JsonProperty(value="paymentStatus")
    @Enumerated(EnumType.STRING)
    PaymentStatusType paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "email_address", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;

    // getters and setters


    public UUID getBillId() {
        return billId;
    }

    public void setBillId(UUID billId) {
        this.billId = billId;
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

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
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
}
