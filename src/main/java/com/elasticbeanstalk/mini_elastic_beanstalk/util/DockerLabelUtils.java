package com.elasticbeanstalk.mini_elastic_beanstalk.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class DockerLabelUtils {

    public Map<String, String> createServerLabels(String serverId) {
        Map<String, String> labels = new HashMap<>();
        labels.put("serverId", serverId);
        labels.put("managed-by", "mini-elastic-beanstalk");
        labels.put("created-at", LocalDateTime.now().toString());
        return labels;
    }

    public Map<String, String> createServerFilter(String serverId) {
        return Map.of("serverId", serverId);
    }

    public String extractServerId(Map<String, String> labels) {
        return labels.get("serverId");
    }
}