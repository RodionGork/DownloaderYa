package none.rg.downloader.net;

import none.rg.downloader.model.DownloadItem;

public interface ProtocolChannel {
    
    boolean start(DownloadItem item);
    
}
