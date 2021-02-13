package org.openpolicyagent.voter;

import java.util.Map;

public class OPADataResponse {

    Map<String, Object> result;

    public OPADataResponse() {
    }

    public Map<String, Object> getResult() {
        return this.result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

}