package pro.sketchware.activities.main.fragments.projects_store;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import pro.sketchware.activities.main.fragments.projects_store.adapters.CommentsAdapter;
import pro.sketchware.activities.main.fragments.projects_store.api.ProjectModel;
import pro.sketchware.databinding.FragmentStoreProjectPreviewCommentsBinding;

public class CommentsBottomSheet extends BottomSheetDialogFragment {
    private FragmentStoreProjectPreviewCommentsBinding binding;
    private CommentsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStoreProjectPreviewCommentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new CommentsAdapter();
        binding.recyclerView.setAdapter(adapter);

        String projectId = getArguments() != null ? getArguments().getString("project_id") : null;
        String projectJson = getArguments() != null ? getArguments().getString("project_json") : null;

        if (projectJson != null) {
            ProjectModel.Project project = new com.google.gson.Gson().fromJson(projectJson, ProjectModel.Project.class);
            if (project.getCommentsList() != null && !project.getCommentsList().isEmpty()) {
                adapter.setComments(project.getCommentsList());
                binding.noCommentsText.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
            }
        }

        if (projectId != null) {
            new pro.sketchware.activities.main.fragments.projects_store.api.SWBHubAPI().getComments(projectId, comments -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (comments.isEmpty()) {
                            binding.noCommentsText.setVisibility(View.VISIBLE);
                            binding.recyclerView.setVisibility(View.GONE);
                        } else {
                            binding.noCommentsText.setVisibility(View.GONE);
                            binding.recyclerView.setVisibility(View.VISIBLE);
                            adapter.setComments(comments);
                        }
                    });
                }
            });
        }

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null) return;
                int first = lm.findFirstCompletelyVisibleItemPosition();
                int last = lm.findLastCompletelyVisibleItemPosition();
                int total = adapter.getItemCount();
                binding.dividerTop.setVisibility(first > 0 ? View.VISIBLE : View.GONE);
                binding.dividerBottom.setVisibility(last < total - 1 ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
