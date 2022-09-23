package threadUtil;

public final class ThreadUtil {
    private ThreadUtil()
    {}

    public static void sleep(long millisecond)  {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
