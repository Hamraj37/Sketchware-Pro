package com.besome.sketch.editor.manage.font;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.besome.sketch.beans.ProjectResourceBean;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import a.a.a.jC;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pro.sketchware.BuildConfig;
import pro.sketchware.R;

public class GoogleFontsFragment extends Fragment {

    private RecyclerView fontList;
    private String sc_id;
    private FontAdapter adapter;
    private TextInputEditText searchEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.google_fonts_fragment, container, false);
        fontList = view.findViewById(R.id.font_list);
        fontList.setLayoutManager(new LinearLayoutManager(getContext()));
        if (getArguments() != null) {
            sc_id = getArguments().getString("sc_id");
        }
        searchEditText = view.findViewById(R.id.search_edit_text);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchGoogleFonts();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void fetchGoogleFonts() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://www.googleapis.com/webfonts/v1/webfonts?key=" + BuildConfig.GOOGLE_FONTS_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle failure
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Gson gson = new Gson();
                    Map<String, Object> fontData = gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
                    List<Map<String, Object>> items = (List<Map<String, Object>>) fontData.get("items");

                    if (items != null && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            adapter = new FontAdapter(items);
                            fontList.setAdapter(adapter);
                        });
                    }
                }
            }
        });
    }

    private void downloadFont(String fontName, String fontUrl) {
        final String finalFontName = fontName.toLowerCase().replace(' ', '_');
        ManageFontActivity activity = (ManageFontActivity) getActivity();
        if (activity != null && activity.projectFontsFragment != null) {
            if (activity.projectFontsFragment.isResourceNameExist(finalFontName)) {
                Toast.makeText(getContext(), "Font '" + fontName + "' already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Toast.makeText(getContext(), "Downloading " + fontName + "...", Toast.LENGTH_SHORT).show();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(fontUrl).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Download failed", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    String dirPath = jC.d(sc_id).j();
                    File fontFile = new File(dirPath, finalFontName + ".ttf");
                    try (InputStream in = response.body().byteStream();
                         OutputStream out = new FileOutputStream(fontFile)) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) != -1) {
                            out.write(buf, 0, len);
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Font downloaded: " + fontName, Toast.LENGTH_SHORT).show();
                                ManageFontActivity activity = (ManageFontActivity) getActivity();
                                if (activity != null && activity.projectFontsFragment != null) {
                                    ProjectResourceBean newBean = new ProjectResourceBean(ProjectResourceBean.PROJECT_RES_TYPE_FILE, finalFontName, fontFile.getName());
                                    activity.projectFontsFragment.projectResourceBeans.add(newBean);
                                    jC.d(sc_id).a(activity.projectFontsFragment.projectResourceBeans);
                                    jC.d(sc_id).y();
                                    activity.projectFontsFragment.notifyDataSetChanged();
                                }
                            });
                        }
                    } catch (IOException e) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to save font", Toast.LENGTH_SHORT).show());
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Download failed", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private class FontAdapter extends RecyclerView.Adapter<FontAdapter.ViewHolder> implements Filterable {

        private final List<Map<String, Object>> fonts;
        private List<Map<String, Object>> filteredFonts;

        public FontAdapter(List<Map<String, Object>> fonts) {
            this.fonts = fonts;
            this.filteredFonts = new ArrayList<>(fonts);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.google_font_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> font = filteredFonts.get(position);
            String family = (String) font.get("family");
            holder.fontName.setText(family);
            holder.itemView.setOnClickListener(v -> {
                Map<String, String> files = (Map<String, String>) font.get("files");
                if (files != null) {
                    String fontUrl = files.get("regular");
                    if (fontUrl != null) {
                        downloadFont(family, fontUrl);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return filteredFonts.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    String charString = constraint.toString();
                    if (charString.isEmpty()) {
                        filteredFonts = new ArrayList<>(fonts);
                    } else {
                        List<Map<String, Object>> filteredList = new ArrayList<>();
                        for (Map<String, Object> row : fonts) {
                            if (((String) row.get("family")).toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        }
                        filteredFonts = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = filteredFonts;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredFonts = (ArrayList<Map<String, Object>>) results.values;
                    notifyDataSetChanged();
                }
            };
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView fontName;

            public ViewHolder(View view) {
                super(view);
                fontName = view.findViewById(R.id.tv_font_name);
            }
        }
    }
}
