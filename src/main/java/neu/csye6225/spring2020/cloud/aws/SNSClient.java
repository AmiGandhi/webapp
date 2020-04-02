package neu.csye6225.spring2020.cloud.aws;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class SNSClient {

    private AmazonSNS snsClient;

    @Value("${aws.topic.name}")
    private String topicName;

    @Value("${amazonProperties.clientRegion}")
    private String clientRegion;

    private static final Logger logger = LoggerFactory.getLogger(SNSClient.class);

    @PostConstruct
    private void initializeAmazon() {
        this.snsClient = AmazonSNSClientBuilder.standard()
                .withRegion(clientRegion)
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
   }


    public void publishToTopic(String msg) {

        logger.info("Inside function to publish to topic");
        List<Topic> topics = snsClient.listTopics().getTopics();
        for(Topic topic: topics)
        {
            if(topic.getTopicArn().endsWith("csye6225-sns-topic")) {

                logger.info("Found the topic ending with csye6225-sns-topic");
                PublishRequest req = new PublishRequest(topic.getTopicArn(),msg);
                snsClient.publish(req);
                break;
            }
        }


    }

}
