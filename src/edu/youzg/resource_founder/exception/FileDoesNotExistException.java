package edu.youzg.resource_founder.exception;

/**
 * 目标路径不存在 文件 异常
 */
public class FileDoesNotExistException extends Exception {
    private static final long serialVersionUID = -6217840007725641732L;

    public FileDoesNotExistException() {
    }

    public FileDoesNotExistException(String message) {
        super(message);
    }

    public FileDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileDoesNotExistException(Throwable cause) {
        super(cause);
    }

}
