package none.rg.downloader.net;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;


@Service
public class ChannelService implements BeanFactoryAware {
    
    private static final int DEFAULT_CORE_POOL_SIZE = 4;
    private static final int DEFAULT_MAX_POOL_SIZE = 8;
    private static final int DEFAULT_QUEUE_CAPACITY = 32;
    
    private BeanFactory beanFactory;
    
    private ThreadPoolTaskExecutor executor;
    
    private AsynchronousChannelGroup channelGroup;
    
    public ChannelService() throws IOException {
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(DEFAULT_CORE_POOL_SIZE);
        executor.setMaxPoolSize(DEFAULT_MAX_POOL_SIZE);
        executor.setQueueCapacity(DEFAULT_QUEUE_CAPACITY);
        executor.afterPropertiesSet();
        channelGroup = AsynchronousChannelGroup
                .withThreadPool(executor.getThreadPoolExecutor());
    }
    
    public ProtocolChannel createProtocolChannel(String key) {
        if ("http".equalsIgnoreCase(key)) {
            return beanFactory.getBean(ProtocolChannelHttp.class);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void setBeanFactory(BeanFactory bf) {
        beanFactory = bf;
    }

    AsynchronousChannelGroup getChannelGroup() {
        return channelGroup;
    }
    
}
