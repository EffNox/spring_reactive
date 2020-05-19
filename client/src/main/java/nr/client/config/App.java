package nr.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class App {

    private @Value("${config.endpoint}") String url;
    
    @Bean
    public WebClient registerWebClient() {
        return WebClient.create(url);
    }
    
}
