package careplus.common.net;

import java.io.Serializable;

public class CarePlusResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Object data;

    public CarePlusResponse() {
    }

    public CarePlusResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static CarePlusResponse ok(String message, Object data) {
        return new CarePlusResponse(true, message, data);
    }

    public static CarePlusResponse fail(String message) {
        return new CarePlusResponse(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
