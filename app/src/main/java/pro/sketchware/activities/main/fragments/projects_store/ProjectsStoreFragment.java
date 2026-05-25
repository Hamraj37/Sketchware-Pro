package pro.sketchware.activities.main.fragments.projects_store;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.material.transition.MaterialFadeThrough;

import pro.sketchware.R;
import pro.sketchware.BuildConfig;
import pro.sketchware.activities.main.fragments.projects_store.adapters.StorePagerProjectsAdapter;
import pro.sketchware.activities.main.fragments.projects_store.adapters.StoreProjectsAdapter;
import pro.sketchware.activities.main.fragments.projects_store.api.SWBHubAPI;
import pro.sketchware.activities.main.fragments.projects_store.classes.CenterZoomListener;
import pro.sketchware.databinding.FragmentProjectsStoreBinding;
import pro.sketchware.utility.UI;

public class ProjectsStoreFragment extends Fragment {
    private FragmentProjectsStoreBinding binding;
    private SWBHubAPI swbHubAPI;
    private StorePagerProjectsAdapter editorsChoiceAdapter;
    private StoreProjectsAdapter recentProjectsAdapter;
    private StoreProjectsAdapter componentsAdapter;
    private StoreProjectsAdapter blocksAdapter;
    private MenuProvider menuProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialFadeThrough());
        setReturnTransition(new MaterialFadeThrough());
        setExitTransition(new MaterialFadeThrough());
        setReenterTransition(new MaterialFadeThrough());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProjectsStoreBinding.inflate(inflater, container, false);
        swbHubAPI = new SWBHubAPI();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.swipeRefresh.setOnRefreshListener(this::fetchData);

        setupRecyclerView(binding.editorsChoiceProjectsRecyclerView);
        fetchData();

        binding.btnSeeAllRecent.setOnClickListener(v -> openStoreList(StoreListActivity.TYPE_PROJECTS));
        binding.btnSeeAllComponents.setOnClickListener(v -> openStoreList(StoreListActivity.TYPE_COMPONENTS));
        binding.btnSeeAllBlocks.setOnClickListener(v -> openStoreList(StoreListActivity.TYPE_BLOCKS));

        setupMenu();

        UI.addSystemWindowInsetToPadding(binding.textEditorsChoice, true, false, true, false);
        UI.addSystemWindowInsetToPadding(binding.editorsChoiceProjectsRecyclerView, true, false, true, false);
        UI.addSystemWindowInsetToPadding(binding.textRecent, true, false, true, false);
        UI.addSystemWindowInsetToPadding(binding.recentProjectsRecyclerView, true, false, true, false);
        UI.addSystemWindowInsetToPadding(binding.textComponents, true, false, true, false);
        UI.addSystemWindowInsetToPadding(binding.componentsRecyclerView, true, false, true, false);
        UI.addSystemWindowInsetToPadding(binding.textBlocks, true, false, true, false);
        UI.addSystemWindowInsetToPadding(binding.blocksRecyclerView, true, false, true, true);
        UI.addSystemWindowInsetToMargin(binding.cardWarning, true, false, true, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (menuProvider != null) {
            requireActivity().removeMenuProvider(menuProvider);
        }
        binding = null; // avoid memory leaks
    }

    private void setupMenu() {
        menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.projects_fragment_menu, menu);
                MenuItem searchItem = menu.findItem(R.id.searchProjects);
                SearchView searchView = (SearchView) searchItem.getActionView();
                if (searchView != null) {
                    searchView.setQueryHint("Search SWB Hub...");
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            filterAll(newText);
                            return true;
                        }
                    });
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        };
        requireActivity().addMenuProvider(menuProvider);
    }

    private void filterAll(String query) {
        if (editorsChoiceAdapter != null) editorsChoiceAdapter.filterData(query);
        if (recentProjectsAdapter != null) recentProjectsAdapter.filterData(query);
        if (componentsAdapter != null) componentsAdapter.filterData(query);
        if (blocksAdapter != null) blocksAdapter.filterData(query);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (getActivity() == null) return;
        if (hidden) {
            requireActivity().removeMenuProvider(menuProvider);
        } else {
            requireActivity().addMenuProvider(menuProvider);
        }
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new CenterZoomListener());

        recyclerView.setClipToPadding(false);
        recyclerView.setClipChildren(false);

        ViewParent parent = recyclerView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).setClipChildren(false);
            ((ViewGroup) parent).setClipToPadding(false);
        }
    }

    private void fetchData() {
        var activity = getActivity();
        final int[] finishedRequests = {0};
        final int totalRequests = 4;

        Runnable checkFinished = () -> {
            finishedRequests[0]++;
            if (finishedRequests[0] >= totalRequests) {
                binding.swipeRefresh.setRefreshing(false);
            }
        };

        swbHubAPI.getEditorsChoicerProjects(projectModel -> {
            if (projectModel != null) {
                editorsChoiceAdapter = new StorePagerProjectsAdapter(projectModel.getProjects(), activity);
                binding.editorsChoiceProjectsRecyclerView.setAdapter(editorsChoiceAdapter);
            }
            checkFinished.run();
        });
        swbHubAPI.getRecentProjects(projectModel -> {
            if (projectModel != null) {
                recentProjectsAdapter = new StoreProjectsAdapter(projectModel.getProjects(), activity);
                binding.recentProjectsRecyclerView.setAdapter(recentProjectsAdapter);
            }
            checkFinished.run();
        });
        swbHubAPI.getRecentComponents(projectModel -> {
            if (projectModel != null) {
                componentsAdapter = new StoreProjectsAdapter(projectModel.getProjects(), activity);
                binding.componentsRecyclerView.setAdapter(componentsAdapter);
            }
            checkFinished.run();
        });
        swbHubAPI.getRecentBlocks(projectModel -> {
            if (projectModel != null) {
                blocksAdapter = new StoreProjectsAdapter(projectModel.getProjects(), activity);
                binding.blocksRecyclerView.setAdapter(blocksAdapter);
            }
            checkFinished.run();
        });
    }

    private void openStoreList(int type) {
        Intent intent = new Intent(getContext(), StoreListActivity.class);
        intent.putExtra(StoreListActivity.EXTRA_TYPE, type);
        startActivity(intent);
    }
}
