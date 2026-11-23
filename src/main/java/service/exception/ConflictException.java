package service.exception;

public class ConflictException extends ServiceException {
    private static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super(409, message);
    }
}
