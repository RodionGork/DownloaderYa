package none.rg.downloader.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import none.rg.downloader.model.DownloadItem;
import none.rg.downloader.model.DownloadRequest;
import none.rg.downloader.model.DownloadResponse;
import none.rg.downloader.model.DownloadState;
import none.rg.downloader.net.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DownloadCore {
    
    private Map<String, DownloadItem> items;
    
    private Map<String, DownloadItem> removed;
    
    @Autowired
    private ChannelService channelService;
    
    @Autowired
    private FileService fileService;
    
    public DownloadCore() {
        items = new ConcurrentHashMap<>();
        removed = new ConcurrentHashMap<>();
    }
    
    public DownloadResponse request(DownloadRequest req) {
        DownloadItem item = new DownloadItem();
        DownloadResponse response = new DownloadResponse();
        item.setLink(req.getLink());
        
        try {
            item.setUrl(new URL(req.getLink()));
        } catch (MalformedURLException e) {
            response.setMessage("Malformed URL - use full form 'protocol://host[:port][/path]'!");
            return response;
        }
        
        item.setState(DownloadState.PREPARED);
        items.put(item.getLink(), item);
        
        channelService.createProtocolChannel(item.getUrl().getProtocol()).start(item);
        
        
        response.setSuccess(true);
        return response;
    }

    public void dismiss(String url) {
        fileService.finish(fileNameFromUrl(url));
        removed.put(url, items.remove(url));
    }
    
    public void storeChunk(String url, byte[] chunk) {
        fileService.append(fileNameFromUrl(url), chunk);
        DownloadItem item = items.get(url);
        item.setCur(item.getCur() + chunk.length);
    }
    
    private String fileNameFromUrl(String url) {
        return url.replaceAll("[^A-Za-z0-9]", "_");
    }
    
    public Map<String, DownloadItem> getItems() {
        return new HashMap<>(items);
    }
    
    public Map<String, DownloadItem> getRemoved() {
        return new HashMap<>(removed);
    }

    void requestStopFor(String link) {
        DownloadItem item = items.get(link);
        if (item == null) {
            return;
        }
        synchronized (item) {
            if (item.getState() == DownloadState.IN_PROGRESS
                    || item.getState() == DownloadState.PREPARED) {
                item.setState(DownloadState.ABORT_REQUESTED);
            }
        }
    }
}
