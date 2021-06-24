package cn.edu.nju.videoencode.util;

public class FFmpegException extends Exception {

    private final long serialVersionUID = 1L;

    public FFmpegException() {
        super();
    }

    public FFmpegException(String message) {
        super(message);
    }

    public FFmpegException(Throwable cause) {
        super(cause);
    }

    public FFmpegException(String message, Throwable cause) {
        super(message, cause);
    }
}

