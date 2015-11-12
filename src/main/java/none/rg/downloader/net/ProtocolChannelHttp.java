package none.rg.downloader.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import none.rg.downloader.model.DownloadItem;
import none.rg.downloader.model.DownloadState;
import none.rg.downloader.service.DownloadCore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;


@Service
@Scope("prototype")
public class ProtocolChannelHttp implements ProtocolChannel {
    
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5;
    private static final int DEFAULT_READWRITE_TIMEOUT = 15;
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    
    @Autowired
    private ChannelService channelService;
    
    @Autowired
    private DownloadCore downloadCore;
    
    private AsynchronousSocketChannel channel;
    
    private DownloadItem item;
    
    private ByteBuffer buffer;
    
    private String hostAddr;
    
    @Override
    public boolean start(DownloadItem downloadItem) {
        item = downloadItem;
        hostAddr = item.getUrl().getHost();
        System.err.println("CONNECTING TO HOST: " + hostAddr);
        
        try {
            channel = AsynchronousSocketChannel.open(channelService.getChannelGroup());
        } catch (IOException e) {
            return false;
        }
        
        int port = item.getUrl().getPort();
        if (port < 0) {
            port = item.getUrl().getDefaultPort();
        }
        channel.connect(
                new InetSocketAddress(hostAddr, port),
                item.getLink(), new ConnectionHandler());
        
        return true;
    }
    
    private class ConnectionHandler implements CompletionHandler<Void, String> {

        @Override
        public void completed(Void result, String url) {
            String localUrl = item.getUrl().getPath();
            if (localUrl.isEmpty()) {
                localUrl = "/";
            }
            String request = "GET " + localUrl + " HTTP/1.1\r\n"
                    + "Host: " + hostAddr + "\r\n\r\n";
            System.err.println("REQUEST: " + request);
            
            channel.write(StandardCharsets.UTF_8.encode(request), DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS,
                    url, new WriteRequestHandler());
        }

        @Override
        public void failed(Throwable exc, String url) {
            System.err.println("CONNECT FAILED FOR: " + url);
            item.setState(DownloadState.FAILED);
            downloadCore.dismiss(url);
        }
    }
    
    private class WriteRequestHandler implements CompletionHandler<Integer, String> {

        @Override
        public void completed(Integer result, String url) {
            buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
            channel.read(buffer, DEFAULT_READWRITE_TIMEOUT, TimeUnit.SECONDS,
                    url, new FetchResponseHandler());
            item.setState(DownloadState.IN_PROGRESS);
        }

        @Override
        public void failed(Throwable exc, String url) {
            System.err.println("WRITE REQUEST FAILED FOR: " + url);
            item.setState(DownloadState.FAILED);
            downloadCore.dismiss(url);
        }
    }
    
    private class FetchResponseHandler implements CompletionHandler<Integer, String> {
        
        private boolean headerParsed;
        
        @Override
        public void completed(Integer result, String url) {
            
            if (item.getState() == DownloadState.ABORT_REQUESTED) {
                System.err.println("DOWNLOADING ABORTED FOR: " + url);
                item.setState(DownloadState.ABORTED);
                downloadCore.dismiss(url);
                return;
            }
            
            if (!headerParsed) {
                byte[] bytes = buffer.array();
                int headerLen = parseHeader(bytes);
                
                if (headerLen < 0) {
                    System.err.println("HEADER NOT RECOGNIZED FOR: " + url);
                    item.setState(DownloadState.FAILED);
                    downloadCore.dismiss(url);
                    return;
                }
                
                System.err.println("HEADER LENGTH: " + headerLen);
                
                headerParsed = true;
                downloadCore.storeChunk(url, Arrays.copyOfRange(bytes, headerLen, Math.min(bytes.length, result)));
                buffer.clear();
            } else if (result > 0) {
                downloadCore.storeChunk(url, Arrays.copyOf(buffer.array(), result));
            } else if (result == -1) {
                finished(url);
                return;
            }
            
            buffer.clear();
            channel.read(buffer, DEFAULT_READWRITE_TIMEOUT, TimeUnit.SECONDS,
                    url, FetchResponseHandler.this);
        }
        
        private int parseHeader(byte[] bytes) {
            // returns size of header or -1 if end of header not found
            int pos = 0;
            int lineStart = 0;

            while (pos < bytes.length) {
                if (bytes[pos] == '\r') {
                    if (bytes[pos + 1] == '\n') {
                        String line = new String(bytes, lineStart, pos - lineStart);
                        if (line.replaceFirst("\\:.*", "").equalsIgnoreCase("Content-Length")) {
                            try {
                                item.setSize(Integer.parseInt(line.replaceFirst("[^\\:]\\:\\s+", "")));
                            } catch (NumberFormatException e) {
                                System.err.println("CONTENT-LENGTH PARSING FAILED");
                            }
                        } else if (line.isEmpty()) {
                            return pos + 2;
                        }
                        lineStart = pos + 2;
                        pos++;
                    }
                }
                pos++;
            }

            return -1;
        }

        private void finished(String url) {
            try {
                channel.close();
            } catch (IOException e) {
                System.err.println("TROUBLE ON CLOSING FOR: " + url);
            }
            System.err.println("DOWNLOAD COMPLETE FOR: " + url);
            item.setState(DownloadState.COMPLETED);
            downloadCore.dismiss(url);
        }
        
        @Override
        public void failed(Throwable exc, String url) {
            System.err.println("FETCH RESPONSE FAILED FOR: " + url);
            item.setState(DownloadState.FAILED);
            downloadCore.dismiss(url);
        }
    }
}
