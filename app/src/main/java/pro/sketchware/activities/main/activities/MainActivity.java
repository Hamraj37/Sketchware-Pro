package pro.sketchware.activities.main.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.besome.sketch.SketchStoreFragment;
import com.besome.sketch.lib.base.BasePermissionAppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import a.a.a.DB;
import a.a.a.GB;
import mod.hey.studios.project.backup.BackupFactory;
import mod.hey.studios.project.backup.BackupRestoreManager;
import mod.hey.studios.util.Helper;
import mod.hilal.saif.activities.tools.ConfigActivity;
import mod.tyron.backup.SingleCopyTask;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import pro.sketchware.BuildConfig;
import pro.sketchware.R;
import pro.sketchware.activities.about.AboutActivity;
import pro.sketchware.activities.main.fragments.profile.ProfileFragment;
import pro.sketchware.activities.main.fragments.projects.ProjectsFragment;
import pro.sketchware.data.model.Project;
import pro.sketchware.databinding.MainBinding;
import pro.sketchware.lib.base.BottomSheetDialogView;
import pro.sketchware.utility.DataResetter;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.SketchwareUtil;
import pro.sketchware.utility.UI;

public class MainActivity extends BasePermissionAppCompatActivity {
    private static final String PROJECTS_FRAGMENT_TAG = "projects_fragment";
    private static final String SKETCH_STORE_FRAGMENT_TAG = "sketch_store_fragment";
    private static final String PROFILE_FRAGMENT_TAG = "profile_fragment";
    private static final int REQUEST_CODE_PICK_IMAGE = 101;
    private static final int REQUEST_CODE_PICK_SWB = 102;

    private ActionBarDrawerToggle drawerToggle;
    private DB u;
    private Snackbar storageAccessDenied;
    private MainBinding binding;
    private ImageView selectedImageView;
    private Uri logoUri, swbFileUri;
    private List<Uri> screenshotUris = new ArrayList<>();

    private final OnBackPressedCallback closeDrawer = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            setEnabled(false);
            binding.drawerLayout.closeDrawers();
        }
    };
    private ProjectsFragment projectsFragment;
    private SketchStoreFragment sketchStoreFragment;
    private ProfileFragment profileFragment;
    private Fragment activeFragment;
    @IdRes
    private int currentNavItemId = R.id.item_projects;

    private static boolean isFirebaseInitialized(Context context) {
        try {
            return FirebaseApp.getApps(context) != null && !FirebaseApp.getApps(context).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    // onRequestPermissionsResult but for Storage access only, and only when granted
    public void g(int i) {
        if (i == 9501) {
            allFilesAccessCheck();

            if (activeFragment instanceof ProjectsFragment) {
                projectsFragment.refreshProjectsList();
            }
        }
    }

    @Override
    public void h(int i) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
        startActivityForResult(intent, i);
    }

    @Override
    public void l() {
    }

    @Override
    public void m() {
    }

    public void n() {
        if (activeFragment instanceof ProjectsFragment) {
            projectsFragment.refreshProjectsList();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_PICK_IMAGE:
                    if (data != null && data.getData() != null) {
                        if (selectedImageView != null) {
                            selectedImageView.setImageURI(data.getData());
                            if (selectedImageView.getId() == R.id.project_logo) {
                                logoUri = data.getData();
                            } else {
                                screenshotUris.add(data.getData());
                            }
                        }
                    }
                    break;
                case REQUEST_CODE_PICK_SWB:
                    if (data != null && data.getData() != null) {
                        String path = data.getData().getPath();
                        if (path != null && path.endsWith(".swb")) {
                            swbFileUri = data.getData();
                            Toast.makeText(this, "Selected file: " + path, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Invalid file type. Please select a .swb file.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case 105:
                    DataResetter.a(this, data.getBooleanExtra("onlyConfig", true));
                    break;

                case 111:
                    invalidateOptionsMenu();
                    break;

                case 113:
                    if (data != null && data.getBooleanExtra("not_show_popup_anymore", false)) {
                        u.a("U1I2", (Object) false);
                    }
                    break;

                case 212:
                    if (!(data.getStringExtra("save_as_new_id") == null ? "" : data.getStringExtra("save_as_new_id")).isEmpty() && isStoragePermissionGranted()) {
                        if (activeFragment instanceof ProjectsFragment) {
                            projectsFragment.refreshProjectsList();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        enableEdgeToEdgeNoContrast();

        binding = MainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        binding.statusBarOverlapper.setMinimumHeight(UI.getStatusBarHeight(this));
        UI.addSystemWindowInsetToPadding(binding.appbar, true, false, true, false);

        u = new DB(getApplicationContext(), "U1");
        int u1I0 = u.a("U1I0", -1);
        long u1I1 = u.e("U1I1");
        if (u1I1 <= 0) {
            u.a("U1I1", System.currentTimeMillis());
        }
        if (System.currentTimeMillis() - u1I1 > /* (a day) */ 1000 * 60 * 60 * 24) {
            u.a("U1I0", Integer.valueOf(u1I0 + 1));
        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        drawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, R.string.app_name, R.string.app_name);
        binding.drawerLayout.addDrawerListener(drawerToggle);
        binding.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                closeDrawer.setEnabled(true);
                getOnBackPressedDispatcher().addCallback(closeDrawer);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        boolean hasStorageAccess = isStoragePermissionGranted();
        if (!hasStorageAccess) {
            showNoticeNeedStorageAccess();
        }
        if (hasStorageAccess) {
            allFilesAccessCheck();
        }

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            Uri data = getIntent().getData();
            if (data != null) {
                new SingleCopyTask(this, new SingleCopyTask.CallBackTask() {
                    @Override
                    public void onCopyPreExecute() {
                    }

                    @Override
                    public void onCopyProgressUpdate(int progress) {
                    }

                    @Override
                    public void onCopyPostExecute(@NonNull String path, boolean wasSuccessful, @NonNull String reason) {
                        if (wasSuccessful) {
                            BackupRestoreManager manager = new BackupRestoreManager(MainActivity.this, projectsFragment);

                            if (BackupFactory.zipContainsFile(path, "local_libs")) {
                                new MaterialAlertDialogBuilder(MainActivity.this)
                                        .setTitle("Warning")
                                        .setMessage(BackupRestoreManager.getRestoreIntegratedLocalLibrariesMessage(false, -1, -1, null))
                                        .setPositiveButton("Copy", (dialog, which) -> manager.doRestore(path, true))
                                        .setNegativeButton("Don't copy", (dialog, which) -> manager.doRestore(path, false))
                                        .setNeutralButton(R.string.common_word_cancel, null)
                                        .show();
                            } else {
                                manager.doRestore(path, true);
                            }

                            // Clear intent so it doesn't duplicate
                            getIntent().setData(null);
                        } else {
                            SketchwareUtil.toastError("Failed to copy backup file to temporary location: " + reason, Toast.LENGTH_LONG);
                        }
                    }
                }).copyFile(data);
            }
        } else if (!ConfigActivity.isSettingEnabled(ConfigActivity.SETTING_CRITICAL_UPDATE_REMINDER)) {
            BottomSheetDialogView bottomSheetDialog = getBottomSheetDialogView();
            bottomSheetDialog.getPositiveButton().setEnabled(false);

            CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    bottomSheetDialog.setPositiveButtonText(millisUntilFinished / 1000 + "");
                }

                @Override
                public void onFinish() {
                    bottomSheetDialog.setPositiveButtonText("View changes");
                    bottomSheetDialog.getPositiveButton().setEnabled(true);
                }
            };
            countDownTimer.start();

            if (!isFinishing()) bottomSheetDialog.show();
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.item_projects) {
                navigateToProjectsFragment();
                return true;
            } else if (id == R.id.item_sketchub) {
                navigateToSketchubFragment();
                return true;
            } else if (id == R.id.item_profile) {
                navigateToProfileFragment();
                return true;
            }
            return false;
        });

        binding.uploadProject.setOnClickListener(v -> {
            screenshotUris.clear();
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload_project, null);
            ImageView logo = dialogView.findViewById(R.id.project_logo);
            ImageView screenshot1 = dialogView.findViewById(R.id.screenshot1);
            ImageView screenshot2 = dialogView.findViewById(R.id.screenshot2);
            ImageView screenshot3 = dialogView.findViewById(R.id.screenshot3);
            Button pickSwbButton = dialogView.findViewById(R.id.pick_swb_button);
            EditText projectName = dialogView.findViewById(R.id.project_name);
            EditText projectDescription = dialogView.findViewById(R.id.project_description);

            View.OnClickListener imageClickListener = v1 -> {
                selectedImageView = (ImageView) v1;
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            };

            logo.setOnClickListener(imageClickListener);
            screenshot1.setOnClickListener(imageClickListener);
            screenshot2.setOnClickListener(imageClickListener);
            screenshot3.setOnClickListener(imageClickListener);

            pickSwbButton.setOnClickListener(v1 -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/octet-stream");
                startActivityForResult(intent, REQUEST_CODE_PICK_SWB);
            });

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setView(dialogView);
            builder.setPositiveButton("Upload", (dialog, which) -> {
                uploadToGitHub(projectName.getText().toString(), projectDescription.getText().toString());
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        if (savedInstanceState != null) {
            projectsFragment = (ProjectsFragment) getSupportFragmentManager().findFragmentByTag(PROJECTS_FRAGMENT_TAG);
            sketchStoreFragment = (SketchStoreFragment) getSupportFragmentManager().findFragmentByTag(SKETCH_STORE_FRAGMENT_TAG);
            profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag(PROFILE_FRAGMENT_TAG);
            currentNavItemId = savedInstanceState.getInt("selected_tab_id");
            Fragment current = getFragmentForNavId(currentNavItemId);
            if (current instanceof ProjectsFragment) {
                navigateToProjectsFragment();
            } else if (current instanceof SketchStoreFragment) {
                navigateToSketchubFragment();
            } else if (current instanceof ProfileFragment) {
                navigateToProfileFragment();
            }

            return;
        }

        navigateToProjectsFragment();
    }

    private byte[] getBytesFromUri(Uri uri) throws IOException {
        InputStream iStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = iStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private String uploadFileToGitHub(String token, String owner, String repo, String path, Uri fileUri, String commitMessage) throws Exception {
        OkHttpClient client = new OkHttpClient();

        byte[] fileBytes = getBytesFromUri(fileUri);
        String encodedContent = Base64.encodeToString(fileBytes, Base64.NO_WRAP);

        JSONObject json = new JSONObject();
        json.put("message", commitMessage);
        json.put("content", encodedContent);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path)
                .header("Authorization", "token " + token)
                .put(body)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        JSONObject responseJson = new JSONObject(response.body().string());
        return responseJson.getJSONObject("content").getString("download_url");
    }

    private void uploadToGitHub(String projectName, String projectDescription) {
        String token = BuildConfig.GITHUB_API_KEY;
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "GitHub API key not found.", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be logged in to upload a project.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                String owner = "Hamraj37";
                String repo = "Sketch-Store";

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("projects");
                String projectId = databaseReference.push().getKey();
                String projectPath = "projects/" + projectId;
                List<String> screenshotUrls = new ArrayList<>();
                String logoDownloadUrl = "";
                String swbDownloadUrl = "";

                if (swbFileUri != null) {
                    String swbPath = projectPath + "/" + projectName + ".swb";
                    swbDownloadUrl = uploadFileToGitHub(token, owner, repo, swbPath, swbFileUri, "Add .swb for " + projectName);
                }

                if (logoUri != null) {
                    String logoPath = projectPath + "/logo.png";
                    logoDownloadUrl = uploadFileToGitHub(token, owner, repo, logoPath, logoUri, "Add logo for " + projectName);
                }

                int i = 1;
                for (Uri screenshotUri : screenshotUris) {
                    String screenshotPath = projectPath + "/screenshot" + i++ + ".png";
                    screenshotUrls.add(uploadFileToGitHub(token, owner, repo, screenshotPath, screenshotUri, "Add screenshot for " + projectName));
                }

                Project project = new Project(projectName, projectDescription, swbDownloadUrl, logoDownloadUrl, screenshotUrls, user.getPhotoUrl().toString(), user.getDisplayName());
                project.setProjectId(projectId);
                databaseReference.child(projectId).setValue(project);

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Project uploaded successfully.", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to upload project.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    private Fragment getFragmentForNavId(int navItemId) {
        if (navItemId == R.id.item_projects) {
            return projectsFragment;
        } else if (navItemId == R.id.item_sketchub) {
            return sketchStoreFragment;
        } else if (navItemId == R.id.item_profile) {
            return profileFragment;
        }
        throw new IllegalArgumentException();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_tab_id", currentNavItemId);
    }

    private void navigateToProjectsFragment() {
        if (projectsFragment == null) {
            projectsFragment = new ProjectsFragment();
        }

        boolean shouldShow = true;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        binding.createNewProject.show();
        binding.uploadProject.hide();
        setAppBarVisibility(View.VISIBLE);
        if (activeFragment != null) transaction.hide(activeFragment);
        if (fm.findFragmentByTag(PROJECTS_FRAGMENT_TAG) == null) {
            shouldShow = false;
            transaction.add(binding.container.getId(), projectsFragment, PROJECTS_FRAGMENT_TAG);
        }
        if (shouldShow) transaction.show(projectsFragment);
        transaction.commit();

        activeFragment = projectsFragment;
        currentNavItemId = R.id.item_projects;
    }

    private void navigateToSketchubFragment() {
        if (sketchStoreFragment == null) {
            sketchStoreFragment = new SketchStoreFragment();
        }

        boolean shouldShow = true;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        binding.createNewProject.hide();
        binding.uploadProject.show();
        setAppBarVisibility(View.GONE);
        if (activeFragment != null) transaction.hide(activeFragment);
        if (fm.findFragmentByTag(SKETCH_STORE_FRAGMENT_TAG) == null) {
            shouldShow = false;
            transaction.add(binding.container.getId(), sketchStoreFragment, SKETCH_STORE_FRAGMENT_TAG);
        }
        if (shouldShow) transaction.show(sketchStoreFragment);
        transaction.commit();

        activeFragment = sketchStoreFragment;
        currentNavItemId = R.id.item_sketchub;
    }

    private void navigateToProfileFragment() {
        if (profileFragment == null) {
            profileFragment = new ProfileFragment();
        }

        boolean shouldShow = true;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        binding.createNewProject.hide();
        binding.uploadProject.hide();
        setAppBarVisibility(View.VISIBLE);
        if (activeFragment != null) transaction.hide(activeFragment);
        if (fm.findFragmentByTag(PROFILE_FRAGMENT_TAG) == null) {
            shouldShow = false;
            transaction.add(binding.container.getId(), profileFragment, PROFILE_FRAGMENT_TAG);
        }
        if (shouldShow) transaction.show(profileFragment);
        transaction.commit();

        activeFragment = profileFragment;
        currentNavItemId = R.id.item_profile;
    }

    @NonNull
    private BottomSheetDialogView getBottomSheetDialogView() {
        BottomSheetDialogView bottomSheetDialog = new BottomSheetDialogView(this);
        bottomSheetDialog.setTitle("Major changes in v7.0.0");
        bottomSheetDialog.setDescription("""
                There have been major changes since v6.3.0 fix1, \
                and it's very important to know them all if you want your projects to still work.
                
                You can view all changes whenever you want at the About Sketchware Pro screen.""");

        bottomSheetDialog.setPositiveButton("View changes", (dialog, which) -> {
            ConfigActivity.setSetting(ConfigActivity.SETTING_CRITICAL_UPDATE_REMINDER, true);
            Intent launcher = new Intent(this, AboutActivity.class);
            launcher.putExtra("select", "changelog");
            startActivity(launcher);
        });
        bottomSheetDialog.setCancelable(false);
        return bottomSheetDialog;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
        if (isFirebaseInitialized(this)) {
            FirebaseMessaging.getInstance().subscribeToTopic("all");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        /* Check if the device is running low on storage space */
        long freeMegabytes = GB.c();
        if (freeMegabytes < 100 && freeMegabytes > 0) {
            showNoticeNotEnoughFreeStorageSpace();
        }
        if (isStoragePermissionGranted() && storageAccessDenied != null && storageAccessDenied.isShown()) {
            storageAccessDenied.dismiss();
        }
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainActivity");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity");
        mAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    private void allFilesAccessCheck() {
        if (Build.VERSION.SDK_INT > 29) {
            File optOutFile = new File(getFilesDir(), ".skip_all_files_access_notice");
            boolean granted = Environment.isExternalStorageManager();

            if (!optOutFile.exists() && !granted) {
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
                dialog.setIcon(R.drawable.ic_expire_48dp);
                dialog.setTitle("Android 11 storage access");
                dialog.setMessage("Starting with Android 11, Sketchware Pro needs a new permission to avoid " + "taking ages to build projects. Don't worry, we can't do more to storage than " + "with current granted permissions.");
                dialog.setPositiveButton(Helper.getResString(R.string.common_word_settings), (v, which) -> {
                    FileUtil.requestAllFilesAccessPermission(this);
                    v.dismiss();
                });
                dialog.setNegativeButton("Skip", null);
                dialog.setNeutralButton("Don't show anymore", (v, which) -> {
                    try {
                        if (!optOutFile.createNewFile())
                            throw new IOException("Failed to create file " + optOutFile);
                    } catch (IOException e) {
                        Log.e("MainActivity", "Error while trying to create 'Don\'t show Android 11 hint' dialog file: " + e.getMessage(), e);
                    }
                    v.dismiss();
                });
                dialog.show();
            }
        }
    }

    private void showNoticeNeedStorageAccess() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setTitle(Helper.getResString(R.string.common_message_permission_title_storage));
        dialog.setIcon(R.drawable.color_about_96);
        dialog.setMessage(Helper.getResString(R.string.common_message_permission_need_load_project));
        dialog.setPositiveButton(Helper.getResString(R.string.common_word_ok), (v, which) -> {
            v.dismiss();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 9501);
        });
        dialog.show();
    }

    private void showNoticeNotEnoughFreeStorageSpace() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setTitle(Helper.getResString(R.string.common_message_insufficient_storage_space_title));
        dialog.setIcon(R.drawable.high_priority_96_red);
        dialog.setMessage(Helper.getResString(R.string.common_message_insufficient_storage_space));
        dialog.setPositiveButton(Helper.getResString(R.string.common_word_ok), null);
        dialog.show();
    }

    public void s() {
        if (storageAccessDenied == null || !storageAccessDenied.isShown()) {
            storageAccessDenied = Snackbar.make(binding.layoutCoordinator, Helper.getResString(R.string.common_message_permission_denied), Snackbar.LENGTH_INDEFINITE);
            storageAccessDenied.setAction(Helper.getResString(R.string.common_word_settings), v -> {
                storageAccessDenied.dismiss();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 9501);
            });
            storageAccessDenied.setActionTextColor(Color.YELLOW);
            storageAccessDenied.show();
        }
    }

    public void setAppBarVisibility(int visibility) {
        binding.appbar.setVisibility(visibility);
    }
}
