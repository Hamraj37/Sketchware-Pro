package com.besome.sketch.editor.component;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.besome.sketch.beans.ComponentBean;
import com.besome.sketch.beans.ProjectFileBean;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Locale;

import mod.hey.studios.util.Helper;
import mod.hilal.saif.components.ComponentsHandler;
import pro.sketchware.R;
import pro.sketchware.databinding.ComponentAddItemBinding;
import pro.sketchware.databinding.LogicAddComponentBinding;
import pro.sketchware.dialogs.InnerAddComponentBottomSheet;

public class AddComponentBottomSheet extends BottomSheetDialogFragment {
    private String sc_id;
    private ProjectFileBean projectFileBean;

    private ArrayList<ComponentBean> componentList;
    private ArrayList<ComponentBean> originalComponentList;
    private LogicAddComponentBinding binding;
    private OnComponentCreateListener onComponentCreateListener;

    public static AddComponentBottomSheet newInstance(String scId, ProjectFileBean projectFileBean, OnComponentCreateListener onComponentCreateListener) {
        AddComponentBottomSheet addComponentBottomSheet = new AddComponentBottomSheet();
        addComponentBottomSheet.setOnComponentCreateListener(onComponentCreateListener);
        Bundle args = new Bundle();
        args.putString("sc_id", scId);
        args.putParcelable("project_file_bean", projectFileBean);
        addComponentBottomSheet.setArguments(args);
        return addComponentBottomSheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        sc_id = args.getString("sc_id");
        projectFileBean = args.getParcelable("project_file_bean");

        initializeComponentBeans();
    }

    private void initializeComponentBeans() {
        originalComponentList = new ArrayList<>();
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_INTENT));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_SHAREDPREF));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_FILE_PICKER));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_CALENDAR));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_VIBRATOR));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_TIMERTASK));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_DIALOG));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_MEDIAPLAYER));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_SOUNDPOOL));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_OBJECTANIMATOR));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_CAMERA));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_GYROSCOPE));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_TEXT_TO_SPEECH));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_SPEECH_TO_TEXT));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_REQUEST_NETWORK));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_BLUETOOTH_CONNECT));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_LOCATION_MANAGER));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_PROGRESS_DIALOG));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_DATE_PICKER_DIALOG));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_TIME_PICKER_DIALOG));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_NOTIFICATION));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_FRAGMENT_ADAPTER));

        // Ads
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_INTERSTITIAL_AD));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_REWARDED_VIDEO_AD));

        // Firebase
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_FIREBASE));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_FIREBASE_AUTH));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_FIREBASE_STORAGE));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_FIREBASE_AUTH_PHONE));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_FIREBASE_CLOUD_MESSAGE));
        originalComponentList.add(new ComponentBean(ComponentBean.COMPONENT_TYPE_FIREBASE_AUTH_GOOGLE_LOGIN));

        ComponentsHandler.add(originalComponentList);
        componentList = new ArrayList<>(originalComponentList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LogicAddComponentBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(getContext(), FlexDirection.ROW, FlexWrap.WRAP);
        flexboxLayoutManager.setJustifyContent(JustifyContent.SPACE_BETWEEN);
        flexboxLayoutManager.setAlignItems(AlignItems.CENTER);

        binding.title.setText(Helper.getResString(R.string.component_title_add_component));
        binding.componentList.setHasFixedSize(true);
        binding.componentList.setAdapter(new ComponentsAdapter());
        binding.componentList.setLayoutManager(flexboxLayoutManager);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        binding.componentList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                FlexboxLayoutManager lm = (FlexboxLayoutManager) recyclerView.getLayoutManager();
                if (lm == null) return;
                int first = lm.findFirstCompletelyVisibleItemPosition();
                int last = lm.findLastCompletelyVisibleItemPosition();
                int total = binding.componentList.getAdapter().getItemCount();
                binding.dividerTop.setVisibility(first > 0 ? View.VISIBLE : View.GONE);
                binding.dividerBottom.setVisibility(last < total - 1 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void filter(String text) {
        componentList.clear();
        if (text.isEmpty()) {
            componentList.addAll(originalComponentList);
        } else {
            text = text.toLowerCase(Locale.getDefault());
            for (ComponentBean item : originalComponentList) {
                String componentName = ComponentBean.getComponentName(getContext(), item.type);
                if (componentName.toLowerCase(Locale.getDefault()).contains(text)) {
                    componentList.add(item);
                }
            }
        }
        binding.componentList.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        onComponentCreateListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("sc_id", sc_id);
        savedInstanceState.putParcelable("project_file", projectFileBean);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setOnComponentCreateListener(OnComponentCreateListener onComponentCreateListener) {
        this.onComponentCreateListener = onComponentCreateListener;
    }

    private void showAddComponentDialog(ComponentBean componentBean) {
        InnerAddComponentBottomSheet innerAddComponentBottomSheet = InnerAddComponentBottomSheet.newInstance(sc_id, projectFileBean, componentBean, sheet -> {
            sheet.dismiss();
            dismiss();
            onComponentCreateListener.invoke();
        });
        innerAddComponentBottomSheet.show(getParentFragmentManager(), null);
    }

    public interface OnComponentCreateListener {
        void invoke();
    }

    private class ComponentsAdapter extends RecyclerView.Adapter<ComponentsAdapter.ComponentBeanViewHolder> {
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(@NonNull ComponentBeanViewHolder holder, int position) {
            var componentBean = componentList.get(position);
            holder.bind(componentBean);
        }

        @Override
        @NonNull
        public ComponentBeanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ComponentAddItemBinding binding = ComponentAddItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ComponentBeanViewHolder(binding);
        }

        @Override
        public int getItemCount() {
            return componentList.size();
        }

        private class ComponentBeanViewHolder extends RecyclerView.ViewHolder {
            private final ComponentAddItemBinding binding;

            public ComponentBeanViewHolder(ComponentAddItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(ComponentBean componentBean) {
                String componentName = ComponentBean.getComponentName(itemView.getContext(), componentBean.type);
                binding.name.setText(componentName);
                binding.icon.setImageResource(ComponentBean.getIconResource(componentBean.type));
                binding.getRoot().setOnClickListener(v -> showAddComponentDialog(componentBean));
            }
        }
    }
}
