package none.rg.downloader.web;

import none.rg.downloader.service.DownloadManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainActions {
    
    @Autowired
    private DownloadManager downloadManager;
    
    @RequestMapping({ "/", "/index" })
    public String index() {
        return "index";
    }
    
    @RequestMapping("/success")
    public String success() {
        return "success";
    }
    
    @RequestMapping("/download")
    public String download(@RequestParam("link") String link, Model model) {
        System.err.println("DOWNLOAD LINK: " + link);
        String result = downloadManager.addDownload(link);
        
        if (result == null) {
            return "redirect:success";
        }
        
        model.addAttribute("message", result);
        return "failure";
    }
    
    @RequestMapping("/state")
    public String state(Model model) {
        model.addAttribute("active", downloadManager.getActiveList());
        model.addAttribute("finished", downloadManager.getFinishedList());
        return "state";
    }
    
    @RequestMapping("/stop")
    public String state(@RequestParam("link") String link, Model model) {
        downloadManager.stop(link);
        return "redirect:state";
    }
    
}
