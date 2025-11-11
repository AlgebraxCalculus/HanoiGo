package com.example.myapplication.fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.BookmarkAdapter;
import com.example.myapplication.api.BookmarkApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BookmarkListDetailFragment extends Fragment {

    private String listId;
    private String listName;
    private String listDescription;
    private long bookmarkCount;

    private TextView tvListTitle;
    private TextView tvPlacesCount;
    private TextView tvDescription;
    private RecyclerView recyclerBookmarks;
    private BookmarkAdapter adapter;
    private ArrayList<JSONObject> bookmarks = new ArrayList<>();

    public static BookmarkListDetailFragment newInstance(String listId, String listName, String description, long count) {
        BookmarkListDetailFragment fragment = new BookmarkListDetailFragment();
        Bundle args = new Bundle();
        args.putString("listId", listId);
        args.putString("listName", listName);
        args.putString("description", description);
        args.putLong("count", count);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listId = getArguments().getString("listId");
            listName = getArguments().getString("listName");
            listDescription = getArguments().getString("description", "");
            bookmarkCount = getArguments().getLong("count", 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark_list_detail, container, false);

        setupViews(view);
        setupButtons(view);
        loadBookmarks();

        return view;
    }

    private void setupViews(View view) {
        tvListTitle = view.findViewById(R.id.tvListTitle);
        tvPlacesCount = view.findViewById(R.id.tvPlacesCount);
        tvDescription = view.findViewById(R.id.tvDescription);
        recyclerBookmarks = view.findViewById(R.id.recyclerBookmarks);

        tvListTitle.setText(listName);
        tvPlacesCount.setText(bookmarkCount + " places");
        tvDescription.setText(listDescription.isEmpty() ? "No description" : listDescription);

        recyclerBookmarks.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookmarkAdapter(bookmarks);
        adapter.setOnBookmarkClickListener(new BookmarkAdapter.OnBookmarkClickListener() {
            @Override
            public void onBookmarkClick(JSONObject bookmark) {
                // TODO: Open location detail
                try {
                    String locationName = bookmark.getString("locationName");
                    Toast.makeText(requireContext(), "Opening " + locationName, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onEditNoteClick(JSONObject bookmark) {
                showEditNoteDialog(bookmark);
            }
        });
        recyclerBookmarks.setAdapter(adapter);
    }

    private void setupButtons(View view) {
        ImageView btnBack = view.findViewById(R.id.btnBack);
        ImageView btnBookmark = view.findViewById(R.id.btnBookmark);
        ImageView btnDelete = view.findViewById(R.id.btnDelete);
        ImageView btnEdit = view.findViewById(R.id.btnEdit);
        ImageView btnClose = view.findViewById(R.id.btnClose);

        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        btnBookmark.setOnClickListener(v -> {
            // TODO: Add location to this list
            Toast.makeText(requireContext(), "Add location to list", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            showDeleteListDialog();
        });

        btnEdit.setOnClickListener(v -> {
            showEditListDialog();
        });

        btnClose.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private void loadBookmarks() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        String jwtToken = prefs.getString("jwt_token", null);

        if (jwtToken == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        BookmarkApi.getBookmarksInList(jwtToken, listId, requireContext(), new BookmarkApi.BookmarkCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> bookmarkList) {
                requireActivity().runOnUiThread(() -> {
                    bookmarks.clear();
                    bookmarks.addAll(bookmarkList);
                    adapter.notifyDataSetChanged();

                    // Update count
                    bookmarkCount = bookmarks.size();
                    tvPlacesCount.setText(bookmarkCount + " places");
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load bookmarks: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showEditNoteDialog(JSONObject bookmark) {
        try {
            String currentNote = bookmark.optString("description", "");
            String locationId = bookmark.getString("locationId");

            View dialogView = LayoutInflater.from(requireContext()).inflate(android.R.layout.simple_list_item_1, null);
            EditText editText = new EditText(requireContext());
            editText.setText(currentNote);
            editText.setHint("Enter your note");
            editText.setPadding(50, 50, 50, 50);

            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Edit Note")
                    .setView(editText)
                    .setPositiveButton("Save", (d, w) -> {
                        String newNote = editText.getText().toString().trim();
                        updateBookmarkNote(locationId, newNote);
                    })
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateBookmarkNote(String locationId, String newNote) {
        // Remove old bookmark and add new one with updated note
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        String jwtToken = prefs.getString("jwt_token", null);

        if (jwtToken == null) return;

        // First remove
        BookmarkApi.removeBookmark(jwtToken, locationId, listId, requireContext(), new BookmarkApi.VoidCallback() {
            @Override
            public void onSuccess() {
                // Then add with new note
                BookmarkApi.addBookmark(jwtToken, locationId, listId, newNote, requireContext(), new BookmarkApi.SingleBookmarkCallback() {
                    @Override
                    public void onSuccess(JSONObject bookmark) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Note updated!", Toast.LENGTH_SHORT).show();
                            loadBookmarks(); // Reload
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Failed to update note", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to update note", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showDeleteListDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete List")
                .setMessage("Are you sure you want to delete this list? All bookmarks will be removed.")
                .setPositiveButton("Delete", (d, w) -> {
                    deleteList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteList() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        String jwtToken = prefs.getString("jwt_token", null);

        if (jwtToken == null) return;

        BookmarkApi.deleteBookmarkList(jwtToken, listId, requireContext(), new BookmarkApi.VoidCallback() {
            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "List deleted!", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to delete list: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showEditListDialog() {
        EditText editText = new EditText(requireContext());
        editText.setText(listName);
        editText.setPadding(50, 50, 50, 50);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit List Name")
                .setView(editText)
                .setPositiveButton("Save", (d, w) -> {
                    String newName = editText.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        // TODO: Add update list name API endpoint
                        Toast.makeText(requireContext(), "Update list name feature coming soon", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
