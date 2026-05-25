package pro.sketchware.activities.main.fragments.projects_store.adapters;

import static pro.sketchware.utility.UI.loadImageFromUrl;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

import pro.sketchware.activities.main.fragments.projects_store.ProjectPreviewActivity;
import pro.sketchware.activities.main.fragments.projects_store.api.ProjectModel;
import pro.sketchware.databinding.ViewStoreProjectItemBinding;

public class StoreProjectsAdapter extends RecyclerView.Adapter<StoreProjectsAdapter.ViewHolder> {

    private final List<ProjectModel.Project> originalProjects;
    private List<ProjectModel.Project> filteredProjects;
    private final FragmentActivity context;
    private final Gson gson = new Gson();

    public StoreProjectsAdapter(List<ProjectModel.Project> projects, FragmentActivity context) {
        this.originalProjects = projects;
        this.filteredProjects = projects;
        this.context = context;
    }

    @NonNull
    @Override
    public StoreProjectsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewStoreProjectItemBinding binding = ViewStoreProjectItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(StoreProjectsAdapter.ViewHolder holder, int position) {
        ProjectModel.Project project = filteredProjects.get(position);

        holder.binding.title.setText(project.getTitle());
        holder.binding.likes.setText(project.getLikes());
        holder.binding.downloads.setText(project.getDownloads());
        loadImageFromUrl(holder.binding.icon, project.getIcon());

        holder.binding.getRoot().setOnClickListener(v -> openProject(project));
    }

    @Override
    public int getItemCount() {
        if (filteredProjects == null) {
            return 0;
        }
        return filteredProjects.size();
    }

    public void filterData(String query) {
        if (query.isEmpty()) {
            filteredProjects = originalProjects;
        } else {
            filteredProjects = originalProjects.stream()
                    .filter(project -> project.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                            (project.getDescription() != null && project.getDescription().toLowerCase().contains(query.toLowerCase())))
                    .toList();
        }
        notifyDataSetChanged();
    }

    private void openProject(ProjectModel.Project project) {
        var bundle = new Bundle();
        bundle.putString("project_json", gson.toJson(project));

        var intent = new Intent(context, ProjectPreviewActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewStoreProjectItemBinding binding;

        public ViewHolder(ViewStoreProjectItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
