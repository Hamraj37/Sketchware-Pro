package pro.sketchware.activities.main.fragments.projects_store;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
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
    public static final int TYPE_MOST_DOWNLOADED = 3;

    private ActivityStoreListBinding binding;
    private final SWBHubAPI swbHubAPI = new SWBHubAPI();
    private StoreProjectsAdapter adapter;

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
        binding.swipeRefresh.setOnRefreshListener(() -> loadData(type));

        loadData(type);
    }

    private void loadData(int type) {
        switch (type) {
            case TYPE_PROJECTS -> {
                setTitle("Recent Projects");
                swbHubAPI.getRecentProjects(model -> {
                    if (model != null) {
                        adapter = new StoreProjectsAdapter(model.getProjects(), this);
                        binding.recyclerView.setAdapter(adapter);
                    }
                    binding.swipeRefresh.setRefreshing(false);
                });
            }
            case TYPE_COMPONENTS -> {
                setTitle("Recent Components");
                swbHubAPI.getRecentComponents(model -> {
                    if (model != null) {
                        adapter = new StoreProjectsAdapter(model.getProjects(), this);
                        binding.recyclerView.setAdapter(adapter);
                    }
                    binding.swipeRefresh.setRefreshing(false);
                });
            }
            case TYPE_BLOCKS -> {
                setTitle("Recent Blocks");
                swbHubAPI.getRecentBlocks(model -> {
                    if (model != null) {
                        adapter = new StoreProjectsAdapter(model.getProjects(), this);
                        binding.recyclerView.setAdapter(adapter);
                    }
                    binding.swipeRefresh.setRefreshing(false);
                });
            }
            case TYPE_MOST_DOWNLOADED -> {
                setTitle("Most Downloaded");
                swbHubAPI.getMostDownloadedProjects(model -> {
                    if (model != null) {
                        adapter = new StoreProjectsAdapter(model.getProjects(), this);
                        binding.recyclerView.setAdapter(adapter);
                    }
                    binding.swipeRefresh.setRefreshing(false);
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(pro.sketchware.R.menu.projects_fragment_menu, menu);
        MenuItem searchItem = menu.findItem(pro.sketchware.R.id.searchProjects);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (adapter != null) {
                        adapter.filterData(newText);
                    }
                    return true;
                }
            });
        }
        return true;
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
