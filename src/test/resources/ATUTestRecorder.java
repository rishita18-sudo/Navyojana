
public class ATUTestRecorder {
	import atu.testrecorder.ATUTestRecorder;
	import atu.testrecorder.exceptions.ATUTestRecorderException;
	import java.text.SimpleDateFormat;
	import java.util.Date;

	public class TestRecorderUtil {
	    private static ATUTestRecorder recorder;

	    public static void startRecording(String testName) {
	        try {
	            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	            recorder = new ATUTestRecorder("test-recordings/", testName + "_" + timestamp, false);
	            recorder.start();
	            System.out.println("🎥 Recording Started: " + testName);
	        } catch (ATUTestRecorderException e) {
	            System.out.println("[ERROR] Failed to Start Recording: " + e.getMessage());
	        }
	    }

	    public static void stopRecording() {
	        try {
	            if (recorder != null) {
	                recorder.stop();
	                System.out.println("🎥 Recording Stopped.");
	            }
	        } catch (ATUTestRecorderException e) {
	            System.out.println("[ERROR] Failed to Stop Recording: " + e.getMessage());
	        }
	    }
	}

}
