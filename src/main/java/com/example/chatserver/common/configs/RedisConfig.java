package com.example.chatserver.common.configs;

import com.example.chatserver.chat.service.RedisPubSubService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    //연결기본객체 (redis에 연결하기 위한)
    @Bean
    @Qualifier("chatPubSub")    // connection객체가 여러개일때 목적에 맞게 사용하고 @Qualifier를 사용하여 주입
    public RedisConnectionFactory chatPubSubFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        //redis pub/sub에서는 특정 데이터베이스에 의존적이지 않음.
//        configuration.setDatabase(0);

        return new LettuceConnectionFactory(configuration);
    }

    //publish 객체
    @Bean
    @Qualifier("chatPubSub")
    // 일반적으로는 RedisTemplate<key데이터타입, value데이터타입>을 사용, 근데 우리는 메시지만 주고 받으니 StringRedisTemplate을 씀
    public StringRedisTemplate stringRedisTemplate(@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory) {    //RedisConnectionFactory 중 우리가 정한거 쓴다
        return new StringRedisTemplate(redisConnectionFactory);
    }


    // subscribe객체
    // chat이라는 room에 메시지가 들어오면 나는 이걸 messageListenerAdapter 에 던지겠다
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter
    ){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("chat"));   //redis에서도 room(topic)같은 것을 chat이라 지정
        return container;
    }


    // redis에서 수신된 메시지를 처리하는 객체 생성
    // RedisPubSubService onMessage메서드에 위임
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisPubSubService redisPubSubService) {
        // RedisPubSubService의 특정 메서드가 수신된 메시지를 처리할 수 있도록 지정
        return new MessageListenerAdapter(redisPubSubService, "onMessage");

    }
}
