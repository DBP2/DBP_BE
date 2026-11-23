package service.exception;

public class UnauthorizedException extends ServiceException {
    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message) {
        super(401, message);
    }
}
