package edu.youzg.multifile_cloud_transfer.exception;

/**
 * 注册中心 ip为null 异常
 */
public class RegistryIpIsNullException extends Exception {
    private static final long serialVersionUID = -9189386627890881352L;

    public RegistryIpIsNullException() {
    }

    public RegistryIpIsNullException(String message) {
        super(message);
    }

    public RegistryIpIsNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistryIpIsNullException(Throwable cause) {
        super(cause);
    }

}
