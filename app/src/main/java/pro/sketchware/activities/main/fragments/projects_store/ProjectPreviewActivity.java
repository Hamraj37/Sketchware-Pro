package pro.sketchware.activities.main.fragments.projects_store;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import androidx.core.widget.NestedScrollView;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import mod.hey.studios.project.backup.BackupRestoreManager;
import pro.sketchware.activities.main.fragments.projects_store.adapters.ProjectScreenshotsAdapter;
import pro.sketchware.activities.main.fragments.projects_store.api.ProjectModel;
import pro.sketchware.databinding.FragmentStoreProjectPreviewBinding;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.Network;
import pro.sketchware.utility.SketchwareUtil;
import pro.sketchware.utility.UI;

public class ProjectPreviewActivity extends BaseAppCompatActivity {
    private static final long TITLE_CONTAINER_FADE_DURATION = 150L;

    private FragmentStoreProjectPreviewBinding binding;
    private ProjectModel.Project project;
    private boolean isTitleContainerShown;
    private long downloadId = -1;

    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadId != -1 && downloadId == id) {
                downloadId = -1; // Reset to prevent double triggering
                handleDownloadComplete(id);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdgeNoContrast();
        super.onCreate(savedInstanceState);

        binding = FragmentStoreProjectPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(downloadReceiver, filter);
        }

        loadProjectData(getIntent().getExtras());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadReceiver);
    }

    private void loadProjectData(Bundle bundle) {
        if (bundle == null) return;

        String json = bundle.getString("project_json");
        project = new Gson().fromJson(json, ProjectModel.Project.class);

        binding.name.setText(project.getTitle());
        binding.author.setText(project.getUserName());
        binding.description.setText(project.getDescription());

        String whatIsNew = project.getWhatsnew();
        if (whatIsNew == null || whatIsNew.isEmpty()) {
            binding.cardWhatIsNew.setVisibility(View.GONE);
        } else {
            binding.cardWhatIsNew.setVisibility(View.VISIBLE);
            binding.whatIsNew.setText(whatIsNew);
        }

        if ("1".equals(project.getIsEditorChoice())) {
            addChip("Editor's Choice");
        }

        if ("1".equals(project.getIsVerified())) {
            addChip("Verified");
        }

        addChip(project.getCategory());

        String size = project.getProjectSize();
        if (size == null || size.equals("Unknown")) {
            binding.filesize.setText("Size: Calculating...");
            new Network().getFileSize(project.getDemoLink(), length -> {
                if (length > 0) {
                    binding.filesize.setText("Size: " + FileUtil.formatFileSize(length));
                } else {
                    binding.filesize.setText("Size: Unknown");
                }
            });
        } else {
            binding.filesize.setText("Size: " + size);
        }

        binding.cardDescription.setOnClickListener(v -> copyToClipboard(project.getDescription()));
        binding.cardWhatIsNew.setOnClickListener(v -> copyToClipboard(project.getWhatsnew()));

        String timestamp = project.getPublishedTimestamp();
        if (timestamp != null && !timestamp.isEmpty() && !timestamp.equals("null")) {
            try {
                binding.timestamp.setText("Released: " + DateFormat.getDateInstance().format(new Date(Long.parseLong(timestamp))));
            } catch (NumberFormatException e) {
                binding.timestamp.setText("Released: Unknown");
            }
        } else {
            binding.timestamp.setText("Released: Unknown");
        }

        binding.btnComments.setOnClickListener(v -> openCommentsSheet());
        binding.btnDownload.setOnClickListener(v -> downloadFile());
        binding.btnOpenIn.setOnClickListener(v -> openProject());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.toolbarTitle.setSelected(true);
        binding.toolbarTitle.setText(project.getTitle());
        binding.toolbarSubtitle.setText(project.getUserName());

        ArrayList<String> screenshots = new ArrayList<>();
        for (int i = 0; i <= 4; i++) {
            String screenshot = getScreenshot(i);
            if (screenshot != null && !screenshot.isEmpty() && screenshot.startsWith("http")) {
                screenshots.add(screenshot);
            }
        }

        if (screenshots.isEmpty()) {
            binding.textScreenshots.setVisibility(View.GONE);
            binding.screenshots.setVisibility(View.GONE);
        } else {
            binding.textScreenshots.setVisibility(View.VISIBLE);
            binding.screenshots.setVisibility(View.VISIBLE);
            binding.screenshots.setAdapter(new ProjectScreenshotsAdapter(screenshots));
        }

        // If it's a block or component and screenshots are missing/fallback, try fetching from dataUrl
        if (project.getCategory() != null && (project.getCategory().equalsIgnoreCase("Block") || project.getCategory().equalsIgnoreCase("Component"))) {
            fetchExtraMetadata();
        }

        UI.loadImageFromUrl(binding.icon, project.getIcon());
        UI.addSystemWindowInsetToPadding(binding.content, true, true, true, true);
        UI.addSystemWindowInsetToMargin(binding.buttonsContainer, true, false, true, true);
        UI.addSystemWindowInsetToPadding(binding.topScrim, false, true, false, false);
        UI.addSystemWindowInsetToPadding(binding.toolbar, true, true, true, false);

        binding.scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, v1, v2, v3, v4) -> {
            int[] location = new int[2];
            binding.author.getLocationOnScreen(location);

            if (location[1] + binding.author.getHeight() + UI.getStatusBarHeight(this) < binding.toolbar.getHeight()) {
                if (isTitleContainerShown) return;
                isTitleContainerShown = true;

                binding.toolbarTitleContainer.setVisibility(View.VISIBLE);
                binding.toolbarTitleContainer.setTranslationY(24f);

                binding.topScrim.animate().alpha(1f).setDuration(TITLE_CONTAINER_FADE_DURATION).start();
                binding.toolbarTitleContainer.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(TITLE_CONTAINER_FADE_DURATION)
                        .start();
            } else {
                if (!isTitleContainerShown) return;
                isTitleContainerShown = false;

                binding.topScrim.animate().alpha(0f).setDuration(TITLE_CONTAINER_FADE_DURATION).start();
                binding.toolbarTitleContainer.animate()
                        .translationY(24f)
                        .alpha(0f)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(TITLE_CONTAINER_FADE_DURATION)
                        .start();
            }
        });
    }

    private void addChip(String name) {
        Chip chip = new Chip(binding.chipsContainer.getContext());
        chip.setText(name);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -1);
        params.setMarginEnd(SketchwareUtil.dpToPx(12f));
        binding.chipsContainer.addView(chip, params);
    }

    private void openCommentsSheet() {
        Bundle bundle = new Bundle();
        bundle.putString("project_id", project.getId());
        bundle.putString("project_json", new Gson().toJson(project));
        CommentsBottomSheet sheet = new CommentsBottomSheet();
        sheet.setArguments(bundle);
        sheet.show(getSupportFragmentManager(), /* tag= */ CommentsBottomSheet.class.getSimpleName());
    }

    private String getScreenshot(int index) {
        return switch (index) {
            case 0 -> project.getScreenshot1();
            case 1 -> project.getScreenshot2();
            case 2 -> project.getScreenshot3();
            case 3 -> project.getScreenshot4();
            case 4 -> project.getScreenshot5();
            default -> null;
        };
    }

    private void fetchExtraMetadata() {
        final String url = project.getDemoLink();
        if (url == null || url.isEmpty() || !url.startsWith("http")) return;

        // Robust URL cleaning for Github Raw links with spaces
        String encodedUrl = url.trim().replace(" ", "%20");
        android.util.Log.d("SWBHub", "fetchExtraMetadata: Fetching from " + encodedUrl);

        new Network().get(encodedUrl, responseBody -> {
            if (responseBody == null || responseBody.isEmpty()) {
                android.util.Log.e("SWBHub", "fetchExtraMetadata: Empty response from " + encodedUrl);
                return;
            }

            try {
                String response = responseBody.trim();
                // Handle UTF-8 BOM if present
                if (response.startsWith("\ufeff")) {
                    response = response.substring(1);
                }

                android.util.Log.d("SWBHub", "fetchExtraMetadata: Received response length: " + response.length());

                Gson gson = new Gson();
                java.util.Map<String, Object> data = gson.fromJson(response, new com.google.gson.reflect.TypeToken<java.util.Map<String, Object>>(){}.getType());
                
                if (data == null) return;

                ArrayList<String> newScreenshots = new ArrayList<>();
                
                // Priority 1: standard store format map
                if (data.get("screenshotUrls") instanceof java.util.Map) {
                    java.util.Map<?, ?> map = (java.util.Map<?, ?>) data.get("screenshotUrls");
                    for (int i = 0; i <= 15; i++) {
                        Object s = map.get("screen_" + i);
                        if (s instanceof String && !((String) s).isEmpty() && ((String) s).startsWith("http")) {
                            newScreenshots.add((String) s);
                        }
                    }
                }
                
                // Priority 2: direct root keys screen_0, screen_1 etc
                if (newScreenshots.isEmpty()) {
                    for (int i = 0; i <= 15; i++) {
                        Object s = data.get("screen_" + i);
                        if (s instanceof String && !((String) s).isEmpty() && ((String) s).startsWith("http")) {
                            newScreenshots.add((String) s);
                        }
                    }
                }
                
                // Priority 3: check for "screenshots" array/list
                if (newScreenshots.isEmpty() && data.get("screenshots") instanceof java.util.List) {
                    java.util.List<?> list = (java.util.List<?>) data.get("screenshots");
                    for (Object item : list) {
                        if (item instanceof String && !((String) item).isEmpty()) {
                            newScreenshots.add((String) item);
                        }
                    }
                }

                if (!newScreenshots.isEmpty()) {
                    android.util.Log.d("SWBHub", "fetchExtraMetadata: Found " + newScreenshots.size() + " screenshots. Updating UI.");
                    final ArrayList<String> finalScreenshots = newScreenshots;
                    runOnUiThread(() -> {
                        binding.screenshots.setAdapter(new ProjectScreenshotsAdapter(finalScreenshots));
                        binding.textScreenshots.setVisibility(View.VISIBLE);
                        binding.screenshots.setVisibility(View.VISIBLE);
                    });
                } else {
                    android.util.Log.w("SWBHub", "fetchExtraMetadata: No valid screenshots found in the fetched metadata");
                }
                
                // update description if it was missing/short
                Object descObj = data.get("description");
                if (descObj == null) descObj = data.get("componentDescription");
                if (descObj == null) descObj = data.get("blockDescription");
                if (descObj == null) descObj = data.get("projectDescription");
                
                if (descObj instanceof String && !((String) descObj).isEmpty()) {
                    final String fetchedDesc = (String) descObj;
                    if (project.getDescription() == null || project.getDescription().length() < 5) {
                        runOnUiThread(() -> {
                            project.setDescription(fetchedDesc);
                            binding.description.setText(fetchedDesc);
                        });
                    }
                }

                // parse comments from metadata if they exist
                Object commentsObj = data.get("comments");
                if (commentsObj instanceof java.util.Map) {
                    java.util.Map<?, ?> commentsMap = (java.util.Map<?, ?>) commentsObj;
                    ArrayList<ProjectModel.Comment> metaComments = new ArrayList<>();
                    commentsMap.forEach((id, val) -> {
                        if (val instanceof java.util.Map) {
                            java.util.Map<?, ?> cMap = (java.util.Map<?, ?>) val;
                            ProjectModel.Comment c = new ProjectModel.Comment();
                            c.setId(String.valueOf(id));
                            
                            Object commentText = cMap.get("comment");
                            if (commentText == null) commentText = cMap.get("text");
                            c.setComment(String.valueOf(commentText));
                            
                            c.setUserName(String.valueOf(cMap.get("userName")));
                            
                            Object profilePic = cMap.get("profilePicUrl");
                            if (profilePic == null) profilePic = cMap.get("photoURL");
                            c.setUserProfilePic(String.valueOf(profilePic));

                            c.setTimestamp(String.valueOf(cMap.get("timestamp")));
                            metaComments.add(c);
                        }
                    });
                    if (!metaComments.isEmpty()) {
                        project.setCommentsList(metaComments);
                    }
                }

            } catch (Exception e) {
                android.util.Log.e("SWBHub", "fetchExtraMetadata: Exception during parsing", e);
            }
        });
    }

    private void openProject() {
        String category = project.getCategory();
        String fileName = "details.html";
        if (category != null) {
            if (category.equalsIgnoreCase("Block")) {
                fileName = "block-details.html";
            } else if (category.equalsIgnoreCase("Component")) {
                fileName = "component-details.html";
            }
        }

        String url = String.format("https://swbhub.web.app/%s?title=%s&ts=%s",
                fileName,
                Uri.encode(project.getTitle()),
                project.getPublishedTimestamp());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private void copyToClipboard(String text) {
        if (text == null || text.isEmpty()) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("description", text);
        clipboard.setPrimaryClip(clip);
        SketchwareUtil.toast("Copied to clipboard");
    }

    private void downloadFile() {
        String url = project.getDemoLink();
        if (url == null || url.isEmpty() || !url.startsWith("http")) {
            SketchwareUtil.toastError("Invalid download URL");
            return;
        }

        String fileName = project.getTitle();
        String extension = "";
        if (url.endsWith(".swb")) {
            extension = ".swb";
        } else if (url.endsWith(".json")) {
            extension = ".json";
        } else if (url.contains(".swb")) {
            extension = ".swb";
        } else if (url.contains(".json")) {
            extension = ".json";
        }

        if (!fileName.endsWith(extension)) {
            fileName += extension;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Downloading " + project.getTitle());
        request.setDescription("SWB Hub");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadId = downloadManager.enqueue(request);
            SketchwareUtil.toast("Download started...");
        } else {
            SketchwareUtil.toastError("Download Manager not available");
        }
    }

    private void handleDownloadComplete(long id) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) return;

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        try (Cursor cursor = downloadManager.query(query)) {
            if (cursor != null && cursor.moveToFirst()) {
                int statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (statusColumn != -1 && DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(statusColumn)) {
                    // Method 1: Use getUriForDownloadedFile and convert it
                    Uri downloadUri = downloadManager.getUriForDownloadedFile(id);
                    String path = null;
                    if (downloadUri != null) {
                        path = FileUtil.convertUriToFilePath(this, downloadUri);
                    }

                    // Method 2: Use local URI column
                    if (path == null) {
                        int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                        if (uriIndex != -1) {
                            String uriString = cursor.getString(uriIndex);
                            if (uriString != null) {
                                path = FileUtil.convertUriToFilePath(this, Uri.parse(uriString));
                            }
                        }
                    }

                    // Method 3: Fallback to direct path from URI
                    if (path == null) {
                        int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                        if (uriIndex != -1) {
                            String uriString = cursor.getString(uriIndex);
                            if (uriString != null) {
                                path = Uri.parse(uriString).getPath();
                            }
                        }
                    }

                    if (path != null) {
                        String lowerPath = path.toLowerCase();
                        if (lowerPath.endsWith(".swb")) {
                            SketchwareUtil.toast("Download complete, restoring project...");
                            new BackupRestoreManager(this).doRestore(path, true);
                        } else if (lowerPath.endsWith(".json")) {
                            importJsonFile(path);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void importJsonFile(String path) {
        String category = project.getCategory();
        if (category == null) return;

        if (category.equalsIgnoreCase("Component")) {
            importComponent(path);
        } else if (category.equalsIgnoreCase("Block")) {
            importBlock(path);
        }
    }

    private void importComponent(String path) {
        var readResult = mod.hilal.saif.components.ComponentsHandler.readComponents(path);
        if (readResult.first.isPresent()) {
            SketchwareUtil.toastError("Failed to read component: " + readResult.first.get());
            return;
        }

        java.util.List<java.util.HashMap<String, Object>> components = readResult.second;
        if (components.isEmpty()) return;

        String componentDir = a.a.a.wq.getCustomComponent();
        String content = FileUtil.readFile(componentDir);
        java.util.List<java.util.HashMap<String, Object>> originalComponentsList;
        if (content.isEmpty()) {
            originalComponentsList = new java.util.ArrayList<>();
        } else {
            originalComponentsList = new com.google.gson.Gson().fromJson(content, 
                new com.google.gson.reflect.TypeToken<java.util.List<java.util.HashMap<String, Object>>>(){}.getType());
        }

        for (java.util.HashMap<String, Object> component : components) {
            if (mod.hilal.saif.components.ComponentsHandler.isValidComponent(component)) {
                originalComponentsList.add(component);
            }
        }

        FileUtil.writeFile(componentDir, new com.google.gson.Gson().toJson(originalComponentsList));
        SketchwareUtil.toast("Component imported successfully");
    }

    private void importBlock(String path) {
        String content = FileUtil.readFile(path);
        if (content.isEmpty()) return;

        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.util.HashMap<String, Object> blockMap = gson.fromJson(content, 
                new com.google.gson.reflect.TypeToken<java.util.HashMap<String, Object>>(){}.getType());
            
            String blocksDir = a.a.a.wq.getAbsolutePathOf(".sketchware/data/block/custom_blocks");
            String originalBlocksContent = FileUtil.readFile(blocksDir);
            java.util.List<java.util.HashMap<String, Object>> allBlocksList;
            if (originalBlocksContent.isEmpty()) {
                allBlocksList = new java.util.ArrayList<>();
            } else {
                allBlocksList = gson.fromJson(originalBlocksContent, 
                    new com.google.gson.reflect.TypeToken<java.util.List<java.util.HashMap<String, Object>>>(){}.getType());
            }

            allBlocksList.add(blockMap);
            FileUtil.writeFile(blocksDir, gson.toJson(allBlocksList));
            SketchwareUtil.toast("Block imported successfully");
        } catch (Exception e) {
            SketchwareUtil.toastError("Failed to import block: " + e.getMessage());
        }
    }
}
