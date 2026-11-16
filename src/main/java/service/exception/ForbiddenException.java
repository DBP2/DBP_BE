package service.exception;

public class ForbiddenException extends ServiceException {
    private static final long serialVersionUID = 1L;

    public ForbiddenException(String message) {
        super(403, message);
    }
}
