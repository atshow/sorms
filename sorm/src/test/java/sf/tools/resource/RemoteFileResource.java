package sf.tools.resource;

import sf.common.log.LogUtil;
import sf.tools.IOUtils;
import sf.tools.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;


public class RemoteFileResource extends FileResource {

    public RemoteFileResource(URL url) {
        super(url);
        String filename = url.toString();
        filename = StringUtils.substringBeforeLast(filename, "/") + "."
                + StringUtils.getCRC(filename);
        file = new File(System.getProperty("java.io.tmpdir"), filename);
        if (file.exists()) {
            if (System.currentTimeMillis() - file.lastModified() < 86400000L) {
                LogUtil.info("The remote resource is redirect to:" + file.getAbsolutePath());
                return;
            }
        }
        try {
            LogUtil.info("The remote resource is saved as:" + file.getAbsolutePath());
            IOUtils.saveAsFile(file, openStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
