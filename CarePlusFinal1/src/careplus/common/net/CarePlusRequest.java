package careplus.common.net;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CarePlusRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String action;
    private Map<String, Object> values = new HashMap<String, Object>();

    public CarePlusRequest() {
    }

    public CarePlusRequest(String action) {
        this.action = action;
    }

    public CarePlusRequest with(String key, Object value) {
        values.put(key, value);
        return this;
    }

    public String getAction() {
        return action;
    }

    public Object get(String key) {
        return values.get(key);
    }

    public Map<String, Object> getValues() {
        return values;
    }
}
