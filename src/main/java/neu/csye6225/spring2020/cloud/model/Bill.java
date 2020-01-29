package neu.csye6225.spring2020.cloud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bill_data")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"created_ts", "updated_ts"},
        allowGetters = true)
@JsonPropertyOrder({ "id", "created_ts", "updated_ts", "owner_id", "vendor", "bill_date", "due_date", "amount_due", "categories" })
public class Bill {

    @JsonProperty(value="id",access= JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.StandardRandomStrategy") })
    @Column(name = "bill_id")
    UUID id;

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
    @UniqueElements
    //@Column(unique = true)
    @ElementCollection
    @CollectionTable(name="categories")
    List <String> categories;

    @JsonProperty(value="paymentStatus")
    @Enumerated(EnumType.STRING)
    PaymentStatusType paymentStatus;
}
