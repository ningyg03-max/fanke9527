package org.json;
/** 测试替身：仅供编译，不实现真实解析 */
public class JSONObject {
    public JSONObject() { }
    public JSONObject(String s) { }
    public JSONArray optJSONArray(String name) { return null; }
    public JSONObject optJSONObject(int i) { return null; }
    public String optString(String name) { return ""; }
    public boolean optBoolean(String name) { return false; }
    public int optInt(String name) { return 0; }
    public Object opt(String name) { return null; }
    public java.util.Iterator<String> keys() { return null; }
    public int length() { return 0; }
}
