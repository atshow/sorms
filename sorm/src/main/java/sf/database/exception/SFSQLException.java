package sf.database.exception;

public class SFSQLException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SFSQLException() {
        super();
    }

    public SFSQLException(String message) {
        super(message);
    }

    public SFSQLException(String message, Throwable cause) {
        super(message, cause);
    }

    public SFSQLException(Throwable cause) {
        super(cause);
    }

    public SFSQLException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
