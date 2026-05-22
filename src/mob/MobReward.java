package mob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Model đại diện cho một cấu hình drop item từ Database
 * Tương ứng với bảng `mob_reward`
 */
public class MobReward {

    public int id;
    public int mobId = -1;
    public int mapId = -1;
    public int itemTemplateId;
    public int rate = 100;
    public int quantityMin = 1;
    public int quantityMax = 1;
    public int gender = -1;
    public String eventKey;
    public String mapType;
    public String conditionType;
    public String dropGroup = "NORMAL";
    public boolean isRandomRange;
    public int randomRange;
    public boolean notifyGlobal;
    public String description;
    public boolean isActive = true;

    public List<MobRewardOption> defaultOptions = new ArrayList<>();
    public Map<Integer, List<MobRewardOption>> itemOptions = new HashMap<>();

    /**
     * Parse options_json, ho tro 2 format:
     * Legacy:  [{"id":30,"param":0},{"id":31,"param":5}]
     * New:     {"default": [...], "items": {"663": [...], "664": [...]}}
     * Option fixed: {"id":47,"param":800}
     * Option random: {"id":47,"min":800,"max":900}
     */
    public void parseOptions(String optionsJson) {
        defaultOptions.clear();
        itemOptions.clear();

        if (optionsJson == null || optionsJson.trim().isEmpty()) {
            return;
        }
        try {
            Object parsed = JSONValue.parse(optionsJson);
            if (parsed == null) {
                System.err.println("MobReward[" + id + "] options_json parse returned null (invalid JSON): " + optionsJson);
                return;
            }
            if (parsed instanceof JSONArray) {
                defaultOptions = parseOptionList((JSONArray) parsed);
            } else if (parsed instanceof JSONObject) {
                JSONObject root = (JSONObject) parsed;
                if (root.containsKey("default")) {
                    try {
                        defaultOptions = parseOptionList((JSONArray) root.get("default"));
                    } catch (Exception e) {
                        System.err.println("MobReward[" + id + "] error parsing default options: " + e.getMessage());
                    }
                }
                if (root.containsKey("items")) {
                    try {
                        JSONObject itemsObj = (JSONObject) root.get("items");
                        for (Object key : itemsObj.keySet()) {
                            String keyStr = (String) key;
                            try {
                                int itemId = Integer.parseInt(keyStr);
                                JSONArray itemArr = (JSONArray) itemsObj.get(keyStr);
                                List<MobRewardOption> itemOpts = parseOptionList(itemArr);
                                itemOptions.put(itemId, itemOpts);
                            } catch (Exception e) {
                                System.err.println("MobReward[" + id + "] error parsing item options for key " + keyStr + ": " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("MobReward[" + id + "] error parsing items section: " + e.getMessage());
                    }
                }
            } else {
                System.err.println("MobReward[" + id + "] options_json is neither array nor object: " + optionsJson);
            }
        } catch (Exception e) {
            System.err.println("MobReward[" + id + "] error parsing options_json: " + optionsJson + " - " + e.getMessage());
        }
    }

    private List<MobRewardOption> parseOptionList(JSONArray arr) {
        List<MobRewardOption> result = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            try {
                Object obj = arr.get(i);
                JSONObject json = (JSONObject) obj;
                MobRewardOption opt = new MobRewardOption();
                opt.id = ((Number) json.get("id")).intValue();
                if (json.containsKey("min") && json.containsKey("max")) {
                    opt.min = ((Number) json.get("min")).intValue();
                    opt.max = ((Number) json.get("max")).intValue();
                } else if (json.containsKey("param")) {
                    opt.param = ((Number) json.get("param")).intValue();
                }
                if (opt.isValid()) {
                    result.add(opt);
                } else {
                    System.err.println("MobReward[" + id + "] skipping invalid option at index " + i + ": " + json.toJSONString());
                }
            } catch (Exception e) {
                System.err.println("MobReward[" + id + "] error parsing option at index " + i + ": " + e.getMessage());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "MobReward{" +
                "id=" + id +
                ", itemTemplateId=" + itemTemplateId +
                ", rate=" + rate +
                ", dropGroup='" + dropGroup + '\'' +
                ", eventKey='" + eventKey + '\'' +
                ", mapType='" + mapType + '\'' +
                '}';
    }
}
