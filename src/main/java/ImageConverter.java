import com.moodysalem.phantomjs.wrapper.PhantomJS;
import com.moodysalem.phantomjs.wrapper.RenderException;
import com.moodysalem.phantomjs.wrapper.beans.BannerInfo;
import com.moodysalem.phantomjs.wrapper.beans.Margin;
import com.moodysalem.phantomjs.wrapper.beans.PaperSize;
import com.moodysalem.phantomjs.wrapper.beans.ViewportDimensions;
import com.moodysalem.phantomjs.wrapper.enums.RenderFormat;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by deichhor on 20.09.17.
 */
public class ImageConverter {

    public static void main(String[] args) throws IOException, RenderException {

                URL url = new URL("https://blog.squix.org");
                final InputStream html = url.openStream();
                final InputStream pdf = PhantomJS.render(null, html, PaperSize.A4, ViewportDimensions.VIEW_1280_1024,
                        Margin.ZERO, BannerInfo.EMPTY, BannerInfo.EMPTY, RenderFormat.PNG, 10000L, 100L);


            Path dest = Paths.get("file.png");
            Files.deleteIfExists(dest);
            Files.copy(pdf, dest);


    }
}
