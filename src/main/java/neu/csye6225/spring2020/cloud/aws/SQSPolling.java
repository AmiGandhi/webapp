package neu.csye6225.spring2020.cloud.aws;

import java.util.Collections;
import java.util.List;

import com.amazonaws.services.sns.model.*;
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

    @Autowired
    private SQSClient sqsClient;

    @Autowired
    private SNSClient snsClient;

    @Value("${AWS_TOPIC_NAME}")
    private String topicName;

    private static final Logger logger = LoggerFactory.getLogger(SQSPolling.class);

    public SQSPolling(SQSClient sqs, String queueUrl) {
        // TODO Auto-generated constructor stub
        this.sqs = (AmazonSQS) sqsClient;
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
            logger.info("Messages recieved...");

            for (Message message : result) {

                snsClient.publishToTopic(message.getBody());
                logger.info("Message published to topic successfully!");

            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}