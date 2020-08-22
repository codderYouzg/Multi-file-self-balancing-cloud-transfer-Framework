package edu.youzg.multifile_cloud_transfer.exception;

/**
 * 要搜索的文件片段 不在 范围内 异常
 */
public class ReceiveSectionOutOfRangeException extends Exception {
    private static final long serialVersionUID = 8276351870677265195L;

    public ReceiveSectionOutOfRangeException() {
    }

    public ReceiveSectionOutOfRangeException(String message) {
        super(message);
    }

    public ReceiveSectionOutOfRangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReceiveSectionOutOfRangeException(Throwable cause) {
        super(cause);
    }

}
