package neu.csye6225.spring2020.cloud.aws;

import java.util.Collections;
import java.util.List;

import com.amazonaws.services.sns.model.*;
import neu.csye6225.spring2020.cloud.controller.EntryController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


public class SQSPolling extends Thread {

    AmazonSQS sqs;
    String queueUrl;

    @Value("${AWS_TOPIC_NAME}")
    private String topicName;

    @Autowired
    EntryController entryController;

    private static final Logger logger = LoggerFactory.getLogger(SQSPolling.class);

    public SQSPolling(AmazonSQS sqs, String queueUrl) {
        // TODO Auto-generated constructor stub
        this.sqs = sqs;
        this.queueUrl = queueUrl;

    }

    public void run() {
        while(true) {

            logger.info("Polling started...");
            final ReceiveMessageRequest receive_request =new ReceiveMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withWaitTimeSeconds(20);
            logger.info("Message request recieved...");
            List<Message> result =  Collections.synchronizedList(sqs.receiveMessage(receive_request).getMessages());
            logger.info("List of messages recieved..."+ result.size());
            for (Message message : result) {
                logger.info("Inside for loop to publish each message to topic");
                CreateTopicRequest topicReq = new CreateTopicRequest("csye6225-sns-topic");
                AmazonSNSClient snsClient =  new AmazonSNSClient();
                CreateTopicResult topicRes = snsClient.createTopic(topicReq);
                PublishRequest publishReq =new PublishRequest(topicRes.getTopicArn(), message.getBody());
                PublishResult publishResult = snsClient.publish(publishReq);
                System.out.println(publishResult.getMessageId());
                logger.info(publishResult.getMessageId());
                sqs.deleteMessage(queueUrl, message.getReceiptHandle());
                logger.info("deleting message from queue)");
            }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        }
    }

//    public void run() {
//        while(true) {
//
//            logger.info("Polling started...");
//            final ReceiveMessageRequest receive_request = new ReceiveMessageRequest()
//                    .withQueueUrl(queueUrl)
//                    .withWaitTimeSeconds(20);
//            logger.info("Message request recieved...");
//
//            List<Message> result =  Collections.synchronizedList(sqs.receiveMessage(receive_request).getMessages());
//            logger.info("Messages recieved..." + result.size());
//
//            for (Message message : result) {
//
//                logger.info("Entered the for loop to publish messages!");
//                snsClient.publishToTopic(message.getBody());
//                logger.info("Message published to topic successfully!");
//
//            }
//
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }

}