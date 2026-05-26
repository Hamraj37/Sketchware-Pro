package pro.sketchware.activities.main.fragments.projects_store.api;

import android.util.Log;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import pro.sketchware.utility.Network;

public class SWBHubAPI {
    private static final String TAG = "SWBHubAPI";
    private static final String BASE_URL = "https://swbstore-data-default-rtdb.firebaseio.com/.json";
    private final Network network = new Network();
    private final Gson gson = new Gson();

    public void getEditorsChoicerProjects(Consumer<ProjectModel> consumer) {
        network.get(BASE_URL, response -> {
            if (response != null && !response.isEmpty()) {
                try {
                    SWBHubResponse swbResponse = gson.fromJson(response, SWBHubResponse.class);
                    List<ProjectModel.Project> combined = new ArrayList<>();

                    // Add some projects (top 3)
                    List<ProjectModel.Project> projects = convertToProjectList(swbResponse);
                    combined.addAll(projects.stream().limit(3).collect(Collectors.toList()));

                    // Add some components (top 1)
                    if (swbResponse.components != null) {
                        swbResponse.components.entrySet().stream().limit(1).forEach(entry -> {
                            ProjectModel.Project p = new ProjectModel.Project();
                            SWBHubResponse.SWBHubComponent val = entry.getValue();
                            p.setId(entry.getKey());
                            p.setTitle(val.componentName);
                            p.setDescription(val.componentDescription);
                            p.setIcon("res:ic_mtrl_component");
                            p.setCategory("Component");
                            
                            Map<String, String> screenshots = val.screenshotUrls;
                            if (screenshots != null && !screenshots.isEmpty()) {
                                p.setScreenshot1(screenshots.get("screen_0"));
                                p.setScreenshot2(screenshots.get("screen_1"));
                                p.setScreenshot3(screenshots.get("screen_2"));
                                p.setScreenshot4(screenshots.get("screen_3"));
                                p.setScreenshot5(screenshots.get("screen_4"));
                            }

                            p.setWhatsnew("");
                            p.setIsEditorChoice("1");
                            p.setIsVerified("1");
                            p.setDemoLink(val.dataUrl);
                            p.setDownloads(String.valueOf(val.downloads));
                            p.setProjectSize(val.projectSize != null ? val.projectSize : "Unknown");
                            p.setUserName(val.userName);
                            p.setPublishedTimestamp(String.valueOf(val.timestamp));
                            combined.add(p);
                        });
                    }

                    // Add some blocks (top 1)
                    if (swbResponse.blocks != null) {
                        swbResponse.blocks.entrySet().stream().limit(1).forEach(entry -> {
                            ProjectModel.Project p = new ProjectModel.Project();
                            SWBHubResponse.SWBHubBlock val = entry.getValue();
                            p.setId(entry.getKey());
                            p.setTitle(val.blockName);
                            p.setDescription(val.blockDescription);
                            p.setIcon("res:ic_mtrl_block");
                            p.setCategory("Block");
                            
                            Map<String, String> screenshots = val.screenshotUrls;
                            if (screenshots != null && !screenshots.isEmpty()) {
                                p.setScreenshot1(screenshots.get("screen_0"));
                                p.setScreenshot2(screenshots.get("screen_1"));
                                p.setScreenshot3(screenshots.get("screen_2"));
                                p.setScreenshot4(screenshots.get("screen_3"));
                                p.setScreenshot5(screenshots.get("screen_4"));
                            }

                            p.setWhatsnew("");
                            p.setIsEditorChoice("1");
                            p.setIsVerified("1");
                            p.setDemoLink(val.dataUrl);
                            p.setDownloads(String.valueOf(val.downloads));
                            p.setProjectSize(val.projectSize != null ? val.projectSize : "Unknown");
                            p.setUserName(val.userName);
                            p.setPublishedTimestamp(String.valueOf(val.timestamp));
                            combined.add(p);
                        });
                    }

                    ProjectModel model = new ProjectModel();
                    model.setStatus("success");
                    model.setProjects(combined);
                    consumer.accept(model);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse response", e);
                    consumer.accept(null);
                }
            } else {
                consumer.accept(null);
            }
        });
    }

    public void getMostDownloadedProjects(Consumer<ProjectModel> consumer) {
        getProjects(projects -> {
            ProjectModel model = new ProjectModel();
            model.setStatus("success");
            model.setProjects(projects.stream()
                    .sorted((p1, p2) -> Integer.compare(
                            Integer.parseInt(p2.getDownloads() != null ? p2.getDownloads() : "0"),
                            Integer.parseInt(p1.getDownloads() != null ? p1.getDownloads() : "0")))
                    .collect(Collectors.toList()));
            consumer.accept(model);
        });
    }

    public void getRecentProjects(Consumer<ProjectModel> consumer) {
        getProjects(projects -> {
            ProjectModel model = new ProjectModel();
            model.setStatus("success");
            model.setProjects(projects.stream()
                    .sorted((p1, p2) -> Long.compare(
                            Long.parseLong(p2.getTimestamp() != null ? p2.getTimestamp() : "0"),
                            Long.parseLong(p1.getTimestamp() != null ? p1.getTimestamp() : "0")))
                    .collect(Collectors.toList()));
            consumer.accept(model);
        });
    }

    public void getRecentComponents(Consumer<ProjectModel> consumer) {
        network.get(BASE_URL, response -> {
            if (response != null && !response.isEmpty()) {
                try {
                    SWBHubResponse swbResponse = gson.fromJson(response, SWBHubResponse.class);
                    List<ProjectModel.Project> components = new ArrayList<>();
                    if (swbResponse.components != null) {
                        swbResponse.components.forEach((id, swbComp) -> {
                            ProjectModel.Project p = new ProjectModel.Project();
                            p.setId(id);
                            p.setTitle(swbComp.componentName);
                            p.setDescription(swbComp.componentDescription);
                            p.setIcon("res:ic_mtrl_component");
                            p.setDownloads(String.valueOf(swbComp.downloads));
                            p.setUid(swbComp.userId);
                            p.setUserName(swbComp.userName);
                            p.setUserProfilePic(swbComp.profilePicUrl);
                            p.setTimestamp(String.valueOf(swbComp.timestamp));
                            p.setPublishedTimestamp(String.valueOf(swbComp.timestamp));
                            p.setDemoLink(swbComp.dataUrl);
                            p.setCategory("Component");
                            
                            Map<String, String> screenshots = swbComp.screenshotUrls;
                            if (screenshots != null && !screenshots.isEmpty()) {
                                p.setScreenshot1(screenshots.get("screen_0"));
                                p.setScreenshot2(screenshots.get("screen_1"));
                                p.setScreenshot3(screenshots.get("screen_2"));
                                p.setScreenshot4(screenshots.get("screen_3"));
                                p.setScreenshot5(screenshots.get("screen_4"));
                            }

                            p.setIsEditorChoice("0");
                            p.setIsVerified("1");
                            p.setDownloads(String.valueOf(swbComp.downloads));
                            p.setProjectSize(swbComp.projectSize != null ? swbComp.projectSize : "Unknown");
                            p.setWhatsnew("");
                            components.add(p);
                        });
                    }
                    ProjectModel model = new ProjectModel();
                    model.setStatus("success");
                    model.setProjects(components.stream()
                            .sorted((p1, p2) -> Long.compare(
                                    Long.parseLong(p2.getTimestamp() != null ? p2.getTimestamp() : "0"),
                                    Long.parseLong(p1.getTimestamp() != null ? p1.getTimestamp() : "0")))
                            .collect(Collectors.toList()));
                    consumer.accept(model);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse response", e);
                    consumer.accept(null);
                }
            } else {
                consumer.accept(null);
            }
        });
    }

    public void getRecentBlocks(Consumer<ProjectModel> consumer) {
        network.get(BASE_URL, response -> {
            if (response != null && !response.isEmpty()) {
                try {
                    SWBHubResponse swbResponse = gson.fromJson(response, SWBHubResponse.class);
                    List<ProjectModel.Project> blocks = new ArrayList<>();
                    if (swbResponse.blocks != null) {
                        swbResponse.blocks.forEach((id, swbBlock) -> {
                            ProjectModel.Project p = new ProjectModel.Project();
                            p.setId(id);
                            p.setTitle(swbBlock.blockName);
                            p.setDescription(swbBlock.blockDescription);
                            p.setIcon("res:ic_mtrl_block");
                            p.setUid(swbBlock.userId);
                            p.setUserName(swbBlock.userName);
                            p.setUserProfilePic(swbBlock.profilePicUrl);
                            p.setTimestamp(String.valueOf(swbBlock.timestamp));
                            p.setPublishedTimestamp(String.valueOf(swbBlock.timestamp));
                            p.setDemoLink(swbBlock.dataUrl);
                            p.setCategory("Block");
                            
                            Map<String, String> screenshots = swbBlock.screenshotUrls;
                            if (screenshots != null && !screenshots.isEmpty()) {
                                p.setScreenshot1(screenshots.get("screen_0"));
                                p.setScreenshot2(screenshots.get("screen_1"));
                                p.setScreenshot3(screenshots.get("screen_2"));
                                p.setScreenshot4(screenshots.get("screen_3"));
                                p.setScreenshot5(screenshots.get("screen_4"));
                            }

                            p.setIsEditorChoice("0");
                            p.setIsVerified("1");
                            p.setDownloads(String.valueOf(swbBlock.downloads));
                            p.setProjectSize(swbBlock.projectSize != null ? swbBlock.projectSize : "Unknown");
                            p.setWhatsnew("");
                            blocks.add(p);
                        });
                    }
                    ProjectModel model = new ProjectModel();
                    model.setStatus("success");
                    model.setProjects(blocks.stream()
                            .sorted((p1, p2) -> Long.compare(
                                    Long.parseLong(p2.getTimestamp() != null ? p2.getTimestamp() : "0"),
                                    Long.parseLong(p1.getTimestamp() != null ? p1.getTimestamp() : "0")))
                            .collect(Collectors.toList()));
                    consumer.accept(model);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse response", e);
                    consumer.accept(null);
                }
            } else {
                consumer.accept(null);
            }
        });
    }

    private void getProjects(Consumer<List<ProjectModel.Project>> consumer) {
        network.get(BASE_URL, response -> {
            if (response != null && !response.isEmpty()) {
                try {
                    SWBHubResponse swbResponse = gson.fromJson(response, SWBHubResponse.class);
                    List<ProjectModel.Project> projects = convertToProjectList(swbResponse);
                    consumer.accept(projects);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse response", e);
                    consumer.accept(new ArrayList<>());
                }
            } else {
                consumer.accept(new ArrayList<>());
            }
        });
    }

    private List<ProjectModel.Project> convertToProjectList(SWBHubResponse swbResponse) {
        List<ProjectModel.Project> projects = new ArrayList<>();
        if (swbResponse.projects != null) {
            swbResponse.projects.forEach((id, swbProject) -> {
                ProjectModel.Project p = new ProjectModel.Project();
                p.setId(id);
                p.setTitle(swbProject.projectName);
                p.setDescription(swbProject.projectDescription);
                p.setIcon(swbProject.logoUrl);
                p.setDownloads(String.valueOf(swbProject.downloads));
                p.setUid(swbProject.userId);
                p.setUserName(swbProject.userName);
                p.setUserProfilePic(swbProject.profilePicUrl);
                p.setTimestamp(String.valueOf(swbProject.timestamp));
                p.setPublishedTimestamp(String.valueOf(swbProject.timestamp));
                p.setDemoLink(swbProject.swbUrl); // Store SWB URL in demoLink for now
                
                Map<String, String> screenshots = swbProject.screenshotUrls;
                if (screenshots != null) {
                    p.setScreenshot1(screenshots.get("screen_0"));
                    p.setScreenshot2(screenshots.get("screen_1"));
                    p.setScreenshot3(screenshots.get("screen_2"));
                    p.setScreenshot4(screenshots.get("screen_3"));
                    p.setScreenshot5(screenshots.get("screen_4"));
                }

                p.setIsEditorChoice("0");
                p.setIsVerified("1");
                p.setCategory("SWB Hub");
                p.setProjectSize(swbProject.projectSize != null ? swbProject.projectSize : "Unknown");
                p.setWhatsnew("");
                projects.add(p);
            });
        }
        return projects;
    }
}
