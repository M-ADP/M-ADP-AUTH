package madp.auth.global.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.net.ServerSocket;
import redis.embedded.RedisServer;

@Configuration
public class EmbeddedRedisConfig {

    private RedisServer redisServer;
    private int port;

    @PostConstruct
    public void startRedis() {
        try {
            port = findAvailablePort();
            System.setProperty("spring.data.redis.port", String.valueOf(port));
            redisServer = new RedisServer(port);
            redisServer.start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start embedded Redis", e);
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer == null || !redisServer.isActive()) return;
        try {
            redisServer.stop();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to stop embedded Redis", e);
        }
    }

    private int findAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }
}
