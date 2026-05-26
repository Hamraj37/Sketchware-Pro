package pro.sketchware.activities.main.fragments.projects_store.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pro.sketchware.activities.main.fragments.projects_store.api.ProjectModel;
import pro.sketchware.databinding.ViewStoreProjectPreviewCommentBinding;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    private final List<ProjectModel.Comment> comments = new ArrayList<>();

    @NonNull
    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewStoreProjectPreviewCommentBinding binding = ViewStoreProjectPreviewCommentBinding.inflate(inflater, parent, false);
        return new CommentsAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.ViewHolder holder, int position) {
        ProjectModel.Comment comment = comments.get(position);
        holder.binding.userName.setText(comment.getUserName());
        holder.binding.userComment.setText(comment.getComment());
        pro.sketchware.utility.UI.loadImageFromUrl(holder.binding.userAvatar, comment.getUserProfilePic());

        String timestamp = comment.getTimestamp();
        if (timestamp != null && !timestamp.isEmpty() && !timestamp.equals("null")) {
            try {
                long time = Long.parseLong(timestamp);
                holder.binding.commentTime.setText(android.text.format.DateUtils.getRelativeTimeSpanString(time));
            } catch (NumberFormatException e) {
                holder.binding.commentTime.setText("");
            }
        } else {
            holder.binding.commentTime.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<ProjectModel.Comment> comments) {
        this.comments.clear();
        this.comments.addAll(comments);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewStoreProjectPreviewCommentBinding binding;

        public ViewHolder(ViewStoreProjectPreviewCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
