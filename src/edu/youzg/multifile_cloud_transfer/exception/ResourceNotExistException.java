package edu.youzg.multifile_cloud_transfer.exception;

/**
 * 资源不存在 异常
 */
public class ResourceNotExistException extends Exception {
    private static final long serialVersionUID = 652988492896479819L;

    public ResourceNotExistException() {
        super();
    }

    public ResourceNotExistException(String message) {
        super(message);
    }

    public ResourceNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotExistException(Throwable cause) {
        super(cause);
    }

}
