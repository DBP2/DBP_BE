package service.exception;

public class MethodNotAllowedException extends ServiceException {
    private static final long serialVersionUID = 1L;

    public MethodNotAllowedException(String message) {
        super(405, message);
    }
}
