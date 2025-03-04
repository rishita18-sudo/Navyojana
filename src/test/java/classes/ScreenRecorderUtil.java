package classes;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.monte.media.Format;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

public class ScreenRecorderUtil extends ScreenRecorder {
    private static ScreenRecorderUtil instance;
    private static File videoFolder;
    private static String fileName;
    private static File recordedFile;

    public ScreenRecorderUtil(GraphicsConfiguration cfg, File folder) throws IOException, AWTException {
        super(cfg, cfg.getBounds(),
                new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        DepthKey, 24, FrameRateKey, Rational.valueOf(15), QualityKey, 1.0f,
                        KeyFrameIntervalKey, 15 * 60),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black",
                        FrameRateKey, Rational.valueOf(30)),
                null, folder);
        videoFolder = folder;
    }

    public static void startRecording(String testName) throws Exception {
        if (instance == null) {
            File folder = new File("E:\\screenshots of navyojana test");
            if (!folder.exists()) folder.mkdirs();
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            instance = new ScreenRecorderUtil(gc, folder);
        }
        fileName = testName + "-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        instance.start();
    }

    public static String stopRecording() throws Exception {
        if (instance != null) {
            instance.stop();
            
            if (!instance.getCreatedMovieFiles().isEmpty()) {
                recordedFile = instance.getCreatedMovieFiles().get(0);
                File newFile = new File(videoFolder, fileName + ".avi");
                
                if (recordedFile.renameTo(newFile)) {
                    return newFile.getAbsolutePath();
                } else {
                    throw new IOException("Failed to rename recorded file.");
                }
            } else {
                throw new IOException("No recorded file found.");
            }
        }
        return null;
    }
}
