package com.besome.sketch;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Added import for Toast

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView; // Corrected import for MaterialCardView
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pro.sketchware.R;
import pro.sketchware.activities.main.activities.MainActivity;
import pro.sketchware.data.model.Project;

public class SketchStoreFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProjectAdapter adapter;
    private List<Project> projectList;
    private List<Project> originalProjectList; // To keep a copy of all projects for filtering
    private DatabaseReference databaseReference;
    private RecyclerView bannerRecyclerView;
    private BannerAdapter bannerAdapter;
    private List<String> bannerImages;
    private MaterialCardView searchBarCard; // Changed from searchBar to searchBarCard
    private EditText searchEditText; // New EditText for search input
    private ImageView profileImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sketch_store_fragment, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        bannerRecyclerView = view.findViewById(R.id.banner_recycler_view);
        searchBarCard = view.findViewById(R.id.search_bar_card); // Initialized searchBarCard
        searchEditText = view.findViewById(R.id.search_edit_text); // Initialized searchEditText
        profileImage = view.findViewById(R.id.profile_image);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        projectList = new ArrayList<>();
        originalProjectList = new ArrayList<>();
        adapter = new ProjectAdapter(projectList);
        recyclerView.setAdapter(adapter);

        bannerImages = new ArrayList<>();
        bannerAdapter = new BannerAdapter(bannerImages);
        bannerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        bannerRecyclerView.setAdapter(bannerAdapter);

        // Set TextWatcher for the search bar
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed for this implementation
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProjects(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed for this implementation
            }
        });

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("projects");
        fetchProjects();
        updateProfileImage();

        return view;
    }

    private void fetchProjects() {
        Query query = databaseReference.limitToLast(5);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                projectList.clear();
                originalProjectList.clear(); // Clear original list as well
                bannerImages.clear();
                List<Project> tempProjects = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Project project = postSnapshot.getValue(Project.class);
                    if (project != null) {
                        tempProjects.add(project);
                        if (project.logoUrl != null && !project.logoUrl.isEmpty()) {
                            bannerImages.add(project.logoUrl);
                        }
                    }
                }
                Collections.reverse(tempProjects);
                originalProjectList.addAll(tempProjects); // Populate original list
                projectList.addAll(tempProjects); // Populate display list
                Collections.reverse(bannerImages);

                adapter.notifyDataSetChanged();
                bannerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
                // Log.w(TAG, "loadPost:onCancelled", error.toException());
            }
        });
    }

    private void filterProjects(String searchText) {
        List<Project> filteredList = new ArrayList<>();
        if (searchText.isEmpty()) {
            filteredList.addAll(originalProjectList);
        } else {
            String lowerCaseSearchText = searchText.toLowerCase();
            for (Project project : originalProjectList) {
                if (project.projectName != null && project.projectName.toLowerCase().contains(lowerCaseSearchText) ||
                        project.userName != null && project.userName.toLowerCase().contains(lowerCaseSearchText)) {
                    filteredList.add(project);
                }
            }
        }
        projectList.clear();
        projectList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (getActivity() instanceof MainActivity) {
            if (hidden) {
                ((MainActivity) getActivity()).setAppBarVisibility(View.VISIBLE);
            } else {
                ((MainActivity) getActivity()).setAppBarVisibility(View.GONE);
            }
        }
    }

    private void updateProfileImage() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Glide.with(requireContext()).load(currentUser.getPhotoUrl()).into(profileImage);
        }
    }

    private static class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

        private List<Project> projects;

        public ProjectAdapter(List<Project> projects) {
            this.projects = projects;
        }

        @NonNull
        @Override
        public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sketch_store_project_item, parent, false);
            return new ProjectViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
            Project project = projects.get(position);
            holder.projectName.setText(project.projectName);
            holder.publisherName.setText(project.userName);
            // Load images using Glide (ensure Glide dependency is added)
            Glide.with(holder.itemView.getContext())
                    .load(project.logoUrl)
                    .placeholder(R.drawable.default_project_image)
                    .into(holder.projectImage);

            Glide.with(holder.itemView.getContext())
                    .load(project.profilePicUrl)
                    .placeholder(R.drawable.default_profile_image)
                    .into(holder.publisherProfileImage);
        }

        @Override
        public int getItemCount() {
            return projects.size();
        }

        static class ProjectViewHolder extends RecyclerView.ViewHolder {
            ImageView projectImage;
            TextView projectName;
            TextView publisherName;
            ImageView publisherProfileImage;

            public ProjectViewHolder(@NonNull View itemView) {
                super(itemView);
                projectImage = itemView.findViewById(R.id.project_image);
                projectName = itemView.findViewById(R.id.project_name);
                publisherName = itemView.findViewById(R.id.publisher_name);
                publisherProfileImage = itemView.findViewById(R.id.publisher_profile_image);
            }
        }
    }

    private static class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

        private List<String> images;

        public BannerAdapter(List<String> images) {
            this.images = images;
        }

        @NonNull
        @Override
        public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item, parent, false);
            return new BannerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext())
                    .load(images.get(position))
                    .placeholder(R.drawable.default_project_image)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        static class BannerViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public BannerViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.slider_image);
            }
        }
    }
}
