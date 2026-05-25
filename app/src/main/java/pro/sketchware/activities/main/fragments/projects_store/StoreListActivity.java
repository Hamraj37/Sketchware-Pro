package pro.sketchware.activities.main.fragments.projects_store;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.besome.sketch.lib.base.BaseAppCompatActivity;

import pro.sketchware.activities.main.fragments.projects_store.adapters.StoreProjectsAdapter;
import pro.sketchware.activities.main.fragments.projects_store.api.SWBHubAPI;
import pro.sketchware.databinding.ActivityStoreListBinding;
import pro.sketchware.utility.UI;

public class StoreListActivity extends BaseAppCompatActivity {

    public static final String EXTRA_TYPE = "type";
    public static final int TYPE_PROJECTS = 0;
    public static final int TYPE_COMPONENTS = 1;
    public static final int TYPE_BLOCKS = 2;

    private ActivityStoreListBinding binding;
    private final SWBHubAPI swbHubAPI = new SWBHubAPI();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdgeNoContrast();
        super.onCreate(savedInstanceState);

        binding = ActivityStoreListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        UI.addSystemWindowInsetToPadding(binding.appbar, true, true, true, false);
        UI.addSystemWindowInsetToPadding(binding.recyclerView, true, false, true, true);

        int type = getIntent().getIntExtra(EXTRA_TYPE, TYPE_PROJECTS);
        loadData(type);
    }

    private void loadData(int type) {
        switch (type) {
            case TYPE_PROJECTS -> {
                setTitle("Recent Projects");
                swbHubAPI.getRecentProjects(model -> {
                    if (model != null) {
                        binding.recyclerView.setAdapter(new StoreProjectsAdapter(model.getProjects(), this));
                    }
                });
            }
            case TYPE_COMPONENTS -> {
                setTitle("Recent Components");
                swbHubAPI.getRecentComponents(model -> {
                    if (model != null) {
                        binding.recyclerView.setAdapter(new StoreProjectsAdapter(model.getProjects(), this));
                    }
                });
            }
            case TYPE_BLOCKS -> {
                setTitle("Recent Blocks");
                swbHubAPI.getRecentBlocks(model -> {
                    if (model != null) {
                        binding.recyclerView.setAdapter(new StoreProjectsAdapter(model.getProjects(), this));
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
