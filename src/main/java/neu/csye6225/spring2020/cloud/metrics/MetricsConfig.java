package neu.csye6225.spring2020.cloud.metrics;

import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
public class MetricsConfig {

    @Value("${print.metrics}")
    private Boolean printMetrics;

    @Value("${metrics.server.hostname}")
    private String metricServerHost;

    @Value("${metrics.server.port}")
    private int metricServerPort;

    @Bean
    public StatsDClient statsDClient() {
        if (printMetrics){
            return new NonBlockingStatsDClient("csye6225-spring2020", metricServerHost, metricServerPort);
        }
        return new NoOpStatsDClient();
    }

//    @Bean
//    public MethodProfiler methodProfiler(StatsDClient statsDClient) {
//        return new MethodProfiler(statsDClient);
//    }
}
