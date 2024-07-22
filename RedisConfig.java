<dependencies>
    <dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
    <version>2.6.4</version> <!-- Use the appropriate version -->
    </dependency>
    <dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-core</artifactId>
    <version>5.5.9</version> <!-- Use the appropriate version -->
    </dependency>
    <dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-redis</artifactId>
    <version>5.5.9</version> <!-- Use the appropriate version -->
    </dependency>
    <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>5.3.15</version> <!-- Use the appropriate version -->
    </dependency>
    <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>5.3.15</version> <!-- Use the appropriate version -->
    </dependency>
    </dependencies>


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.redis.inbound.RedisInboundChannelAdapter;
import org.springframework.integration.redis.outbound.RedisPublishingMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class IntegrationConfig {

  @Bean
  public MessageChannel publishChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel subscribeChannel() {
    return new DirectChannel();
  }

  @Bean
  @ServiceActivator(inputChannel = "publishChannel")
  public MessageHandler redisMessagePublisher(RedisTemplate<String, Object> redisTemplate) {
    RedisPublishingMessageHandler handler = new RedisPublishingMessageHandler(redisTemplate);
    handler.setTopic("data-change");
    return handler;
  }

  @Bean
  @ServiceActivator(inputChannel = "subscribeChannel")
  public MessageHandler redisMessageSubscriber() {
    return message -> {
      String payload = (String) message.getPayload();
      System.out.println("Received message: " + payload);
      // Handle the data change event
    };
  }

  @Bean
  public RedisInboundChannelAdapter redisInbound(RedisConnectionFactory redisConnectionFactory) {
    RedisInboundChannelAdapter adapter = new RedisInboundChannelAdapter(redisConnectionFactory);
    adapter.setTopics("data-change");
    adapter.setOutputChannel(subscribeChannel());
    return adapter;
  }
}




import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.integration.support.MessageBuilder;
    import org.springframework.messaging.MessageChannel;
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/broadcast")
public class BroadcastController {

  @Autowired
  private MessageChannel publishChannel;

  @PostMapping
  @ResponseBody
  public String broadcastChange() {
    String message = "Data has changed";
    publishChannel.send(MessageBuilder.withPayload(message).build());
    return "Broadcast successful";
  }
}



