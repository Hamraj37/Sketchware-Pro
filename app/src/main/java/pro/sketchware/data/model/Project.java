package pro.sketchware.data.model;

import com.google.firebase.database.Exclude;

import java.util.List;

public class Project {
    @Exclude
    public String projectId;
    public String projectName;
    public String projectDescription;
    public String swbUrl;
    public String logoUrl;
    public List<String> screenshotUrls;
    public String profilePicUrl;
    public String userName;

    public Project() {
        // Default constructor required for calls to DataSnapshot.getValue(Project.class)
    }

    public Project(String projectName, String projectDescription, String swbUrl, String logoUrl, List<String> screenshotUrls, String profilePicUrl, String userName) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.swbUrl = swbUrl;
        this.logoUrl = logoUrl;
        this.screenshotUrls = screenshotUrls;
        this.profilePicUrl = profilePicUrl;
        this.userName = userName;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
