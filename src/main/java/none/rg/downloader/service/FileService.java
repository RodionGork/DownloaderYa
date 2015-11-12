package none.rg.downloader.service;

import java.io.FileOutputStream;
import java.io.IOException;
import org.springframework.stereotype.Service;

/*
 * Quite stupid implementation which reopens file each time write is necessary.
 * Surely, opened files should be cached instead.
 */
@Service
public class FileService {
    
    public void append(String fileName, byte[] bytes) {
        try (FileOutputStream out = new FileOutputStream(fileName, true)) {
            out.write(bytes);
        } catch (IOException e) {
            System.err.println("FILE OPERATION FAILED: " + e);
        }
    }
    
    public void finish(String fileName) {
    }
    
}
