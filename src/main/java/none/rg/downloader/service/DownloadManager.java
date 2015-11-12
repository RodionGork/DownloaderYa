package none.rg.downloader.service;

import java.util.HashMap;
import java.util.Map;
import none.rg.downloader.model.DownloadItem;
import none.rg.downloader.model.DownloadRequest;
import none.rg.downloader.model.DownloadResponse;
import none.rg.downloader.model.DownloadState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DownloadManager {
    
    @Autowired
    private DownloadCore downloadCore;
    
    public String addDownload(String url) {
        DownloadRequest req = new DownloadRequest();
        req.setLink(url);
        DownloadResponse resp = downloadCore.request(req);
        return resp.isSuccess() ? null : resp.getMessage();
    }

    public Map<String, String> getActiveList() {
        return statesFromMap(downloadCore.getItems());
    }
    
    public Object getFinishedList() {
        return statesFromMap(downloadCore.getRemoved());
    }
    
    private Map<String, String> statesFromMap(Map<String, DownloadItem> source) {
        Map map = new HashMap<>();
        for (Map.Entry<String, DownloadItem> entry : source.entrySet()) {
            String state = entry.getValue().getState().toString();
            if (entry.getValue().getState() == DownloadState.IN_PROGRESS) {
                state += String.format(" %d / %d", entry.getValue().getCur(), entry.getValue().getSize());
            }
            map.put(entry.getKey(), state);
            
        }
        return map;
    }

    public void stop(String link) {
        downloadCore.requestStopFor(link);
    }

}
