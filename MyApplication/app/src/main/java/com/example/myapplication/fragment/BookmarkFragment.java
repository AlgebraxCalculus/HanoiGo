package com.example.myapplication.fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.BookmarkAdapter;
import com.example.myapplication.adapter.SavedListAdapter;
import com.example.myapplication.api.BookmarkApi;
import com.example.myapplication.model.SavedList;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BookmarkFragment extends Fragment {

    private BottomSheetBehavior<View> bottomSheetBehavior;

    // List view
    private View listView;
    private RecyclerView recyclerView;
    private SavedListAdapter adapter;
    private List<SavedList> savedLists = new ArrayList<>();

    // Detail view
    private View detailView;
    private TextView tvPlacesCount;
    private TextView tvDescription;
    private RecyclerView recyclerBookmarks;
    private BookmarkAdapter bookmarkAdapter;
    private ArrayList<JSONObject> bookmarks = new ArrayList<>();

    // Current list
    private SavedList currentList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        setupBottomSheet(view);
        setupNewListButton(view);
        setupDetailView(view);
        return view;
    }

    private void setupBottomSheet(View view) {
        View bottomSheet = view.findViewById(R.id.bookmarkBottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // IMPORTANT: Prevent bottomsheet from expanding to fullscreen automatically
        bottomSheetBehavior.setFitToContents(false);
        bottomSheetBehavior.setHalfExpandedRatio(0.6f);
        bottomSheetBehavior.setPeekHeight(450);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        listView = view.findViewById(R.id.listView);
        detailView = view.findViewById(R.id.detailView);

        ImageView btnClose = view.findViewById(R.id.closeSavedButton);
        btnClose.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

        setupSavedList(view);
    }

    private void setupSavedList(View view) {
        recyclerView = view.findViewById(R.id.savedListsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SavedListAdapter(savedLists);
        adapter.setOnItemClickListener(new SavedListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SavedList savedList) {
                openListDetail(savedList);
            }

            @Override
            public void onMenuClick(SavedList savedList) {
                showListMenu(savedList);
            }
        });
        recyclerView.setAdapter(adapter);

        // Load bookmark lists from backend
        loadBookmarkLists();
    }

    private void setupNewListButton(View view) {
        CardView newListButton = view.findViewById(R.id.newListButton);
        newListButton.setOnClickListener(v -> showNewListDialog());
    }

    private void loadBookmarkLists() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        String jwtToken = prefs.getString("jwt_token", null);

        if (jwtToken == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        BookmarkApi.getMyBookmarkLists(jwtToken, requireContext(), new BookmarkApi.BookmarkListCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> bookmarkLists) {
                requireActivity().runOnUiThread(() -> {
                    savedLists.clear();
                    for (JSONObject json : bookmarkLists) {
                        try {
                            SavedList list = new SavedList(
                                    json.getString("id"),
                                    json.optString("icon", "bookmark"),
                                    json.getString("name"),
                                    json.getLong("bookmarkCount")
                            );
                            savedLists.add(list);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load lists: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showNewListDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_new_bookmark_list, null);

        EditText editListName = dialogView.findViewById(R.id.editListName);
        ImageView iconBookmark = dialogView.findViewById(R.id.iconBookmark);
        ImageView iconHeart = dialogView.findViewById(R.id.iconHeart);
        ImageView iconFlag = dialogView.findViewById(R.id.iconFlag);

        final String[] selectedIcon = {"bookmark"}; // Default

        // Set default selection
        iconBookmark.setBackgroundColor(Color.LTGRAY);

        iconBookmark.setOnClickListener(v -> {
            selectedIcon[0] = "bookmark";
            iconBookmark.setBackgroundColor(Color.LTGRAY);
            iconHeart.setBackgroundColor(Color.TRANSPARENT);
            iconFlag.setBackgroundColor(Color.TRANSPARENT);
        });

        iconHeart.setOnClickListener(v -> {
            selectedIcon[0] = "heart";
            iconHeart.setBackgroundColor(Color.LTGRAY);
            iconBookmark.setBackgroundColor(Color.TRANSPARENT);
            iconFlag.setBackgroundColor(Color.TRANSPARENT);
        });

        iconFlag.setOnClickListener(v -> {
            selectedIcon[0] = "flag";
            iconFlag.setBackgroundColor(Color.LTGRAY);
            iconBookmark.setBackgroundColor(Color.TRANSPARENT);
            iconHeart.setBackgroundColor(Color.TRANSPARENT);
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnCreate).setOnClickListener(v -> {
            String listName = editListName.getText().toString().trim();
            if (listName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a list name", Toast.LENGTH_SHORT).show();
                return;
            }
            createNewList(listName, selectedIcon[0]);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createNewList(String name, String icon) {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        String jwtToken = prefs.getString("jwt_token", null);

        if (jwtToken == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        BookmarkApi.createBookmarkList(jwtToken, name, icon, requireContext(), new BookmarkApi.SingleBookmarkListCallback() {
            @Override
            public void onSuccess(JSONObject bookmarkList) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "List created successfully!", Toast.LENGTH_SHORT).show();
                    loadBookmarkLists(); // Reload lists
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to create list: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void openListDetail(SavedList savedList) {
        currentList = savedList;

        // Toggle views
        listView.setVisibility(View.GONE);
        detailView.setVisibility(View.VISIBLE);

        // Keep bottomsheet at half-expanded state (not fullscreen!)
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

        // Update detail info
        tvPlacesCount.setText(savedList.getPlaceCount() + " places");
        tvDescription.setText("description bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla"); // TODO: get from backend

        // Load bookmarks
        loadBookmarksForList(savedList.getId());
    }

    private void showListMenu(SavedList savedList) {
        String[] options = {"Delete List", "Edit Name"};

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(savedList.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Delete
                        showDeleteListDialog(savedList);
                    } else if (which == 1) {
                        // Edit
                        Toast.makeText(requireContext(), "Edit name feature coming soon", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void setupDetailView(View view) {
        tvPlacesCount = view.findViewById(R.id.tvPlacesCount);
        tvDescription = view.findViewById(R.id.tvDescription);
        recyclerBookmarks = view.findViewById(R.id.recyclerBookmarks);

        recyclerBookmarks.setLayoutManager(new LinearLayoutManager(requireContext()));
        bookmarkAdapter = new BookmarkAdapter(bookmarks);
        bookmarkAdapter.setOnBookmarkClickListener(new BookmarkAdapter.OnBookmarkClickListener() {
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
        recyclerBookmarks.setAdapter(bookmarkAdapter);

        // Setup buttons
        ImageView btnCloseDetail = view.findViewById(R.id.btnCloseDetail);
        ImageView btnDelete = view.findViewById(R.id.btnDelete);
        ImageView btnEdit = view.findViewById(R.id.btnEdit);
        ImageView btnBookmark = view.findViewById(R.id.btnBookmark);

        // Close button - back to list view but keep bottomsheet position
        btnCloseDetail.setOnClickListener(v -> backToListViewKeepPosition());

        btnDelete.setOnClickListener(v -> {
            if (currentList != null) {
                showDeleteListDialog(currentList);
            }
        });

        btnEdit.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Edit list feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnBookmark.setOnClickListener(v -> {
            // TODO: Add location to this list
            Toast.makeText(requireContext(), "Add location to list", Toast.LENGTH_SHORT).show();
        });
    }

    private void backToListView() {
        listView.setVisibility(View.VISIBLE);
        detailView.setVisibility(View.GONE);
        currentList = null;
        bookmarks.clear();
        bookmarkAdapter.notifyDataSetChanged();

        // Back to collapsed state
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void backToListViewKeepPosition() {
        listView.setVisibility(View.VISIBLE);
        detailView.setVisibility(View.GONE);
        currentList = null;
        bookmarks.clear();
        bookmarkAdapter.notifyDataSetChanged();

        // DO NOT change bottomsheet state - keep current position!
    }

    private void loadBookmarksForList(String listId) {
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
                    bookmarkAdapter.notifyDataSetChanged();

                    // Update count
                    tvPlacesCount.setText(bookmarks.size() + " places");
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

            EditText editText = new EditText(requireContext());
            editText.setText(currentNote);
            editText.setHint("Enter your note");
            editText.setPadding(50, 50, 50, 50);

            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Edit Note")
                    .setView(editText)
                    .setPositiveButton("Save", (d, w) -> {
                        String newNote = editText.getText().toString().trim();
                        updateBookmarkNote(locationId, newNote);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateBookmarkNote(String locationId, String newNote) {
        if (currentList == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        String jwtToken = prefs.getString("jwt_token", null);

        if (jwtToken == null) return;

        // Remove and re-add with new note
        BookmarkApi.removeBookmark(jwtToken, locationId, currentList.getId(), requireContext(), new BookmarkApi.VoidCallback() {
            @Override
            public void onSuccess() {
                BookmarkApi.addBookmark(jwtToken, locationId, currentList.getId(), newNote, requireContext(), new BookmarkApi.SingleBookmarkCallback() {
                    @Override
                    public void onSuccess(JSONObject bookmark) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Note updated!", Toast.LENGTH_SHORT).show();
                            loadBookmarksForList(currentList.getId());
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

    private void showDeleteListDialog(SavedList savedList) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete List")
                .setMessage("Are you sure you want to delete \"" + savedList.getTitle() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
                    String jwtToken = prefs.getString("jwt_token", null);

                    if (jwtToken == null) return;

                    BookmarkApi.deleteBookmarkList(jwtToken, savedList.getId(), requireContext(), new BookmarkApi.VoidCallback() {
                        @Override
                        public void onSuccess() {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "List deleted!", Toast.LENGTH_SHORT).show();
                                backToListViewKeepPosition(); // Keep bottomsheet position
                                loadBookmarkLists(); // Reload
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Failed to delete: " + errorMessage, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
