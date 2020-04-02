package neu.csye6225.spring2020.cloud.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;


@Service
public class SQSClient {

    private AmazonSQS sqsClient;

    @Value("${amazonProperties.clientRegion}")
    private String clientRegion;

    @Value("${sqs.queue.name}")
    private String queueName;

//    @Value("${queue.receiveMessageWaitTime}")
//    private String receiveMessageWaitTime;

    @PostConstruct
    private void initializeAmazon() {
        this.sqsClient = AmazonSQSClientBuilder.standard()
                .withRegion(clientRegion)
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }

    public void publishToQueue(String msg) {

        String queueUrl = getQueueUrl();

        SendMessageRequest request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(msg)
                .withDelaySeconds(5);

        sqsClient.sendMessage(request);

    }

    public List<Message> receiveMessagesInQueue() {

        String queueUrl = getQueueUrl();
        return sqsClient.receiveMessage(queueUrl).getMessages();
    }

    public void deleteMessageInQueue() {

        List<Message> messageList = receiveMessagesInQueue();
        String queueUrl = getQueueUrl();
        for (Message msg : messageList) {
            sqsClient.deleteMessage(queueUrl, msg.getReceiptHandle());
        }

    }

    public String getQueueUrl() {

        String queueUrl = sqsClient.getQueueUrl(queueName).getQueueUrl();
        return queueUrl;
    }

//    public void createSQSQueue() {
//        try {
//            // Create a queue with long polling.
//            final CreateQueueRequest createQueueRequest = new CreateQueueRequest()
//                    .withQueueName(queueName)
//                    .addAttributesEntry(QueueAttributeName.ReceiveMessageWaitTimeSeconds
//                            .toString(), receiveMessageWaitTime);
//            sqsClient.createQueue(createQueueRequest);
//
//            System.out.println("Created queue " + queueName + " with " +
//                    "ReceiveMessage wait time set to " + receiveMessageWaitTime +
//                    " seconds.");
//
//        } catch (final AmazonServiceException ase) {
//            System.out.println("Caught an AmazonServiceException, which means " +
//                    "your request made it to Amazon SQS, but was " +
//                    "rejected with an error response for some reason.");
//            System.out.println("Error Message:    " + ase.getMessage());
//            System.out.println("HTTP Status Code: " + ase.getStatusCode());
//            System.out.println("AWS Error Code:   " + ase.getErrorCode());
//            System.out.println("Error Type:       " + ase.getErrorType());
//            System.out.println("Request ID:       " + ase.getRequestId());
//        } catch (final AmazonClientException ace) {
//            System.out.println("Caught an AmazonClientException, which means " +
//                    "the client encountered a serious internal problem while " +
//                    "trying to communicate with Amazon SQS, such as not " +
//                    "being able to access the network.");
//            System.out.println("Error Message: " + ace.getMessage());
//        }
//    }

}