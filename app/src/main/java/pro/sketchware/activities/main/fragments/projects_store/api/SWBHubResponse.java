package pro.sketchware.activities.main.fragments.projects_store.api;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class SWBHubResponse {
    @SerializedName("projects")
    public Map<String, SWBHubProject> projects;
    @SerializedName("components")
    public Map<String, SWBHubComponent> components;
    @SerializedName("blocks")
    public Map<String, SWBHubBlock> blocks;
    @SerializedName("comments")
    public Map<String, Map<String, SWBHubComment>> comments;

    public static class SWBHubComment {
        public String comment;
        public String text;
        public String userId;
        public String userName;
        public String profilePicUrl;
        public String photoURL;
        public long timestamp;
    }

    public static class SWBHubProject {
        public String customUid;
        public String logoUrl;
        public String profilePicUrl;
        public String projectDescription;
        public String projectName;
        public String swbUrl;
        public long timestamp;
        public String userEmail;
        public String userId;
        public String userName;
        public int downloads;
        public String projectSize;
        @SerializedName("screenshotUrls")
        public Map<String, String> screenshotUrls;
        @SerializedName("comments")
        public Map<String, SWBHubComment> comments;
    }

    public static class SWBHubComponent {
        public String componentName;
        public String componentDescription;
        public String dataUrl;
        public String logoUrl;
        public String profilePicUrl;
        public String userId;
        public String userName;
        public long timestamp;
        public int downloads;
        public String projectSize;
        @SerializedName("screenshotUrls")
        public Map<String, String> screenshotUrls;
        @SerializedName("comments")
        public Map<String, SWBHubComment> comments;
    }

    public static class SWBHubBlock {
        public String blockName;
        public String blockDescription;
        public String dataUrl;
        public String profilePicUrl;
        public String userId;
        public String userName;
        public long timestamp;
        public int downloads;
        public String projectSize;
        @SerializedName("screenshotUrls")
        public Map<String, String> screenshotUrls;
        @SerializedName("comments")
        public Map<String, SWBHubComment> comments;
    }
}
