package com.example.taskd101_androidflaskmongodbcrudexample;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView listView;
    private LinearLayout loadingContainer;
    private Button addNoteButton;
    private ArrayList<String> noteItems;
    private ArrayAdapter<String> adapter;
    private ArrayList<Note> notes;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        loadingContainer = findViewById(R.id.loadingContainer);
        addNoteButton = findViewById(R.id.addNoteButton);
        noteItems = new ArrayList<>();
        notes = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, noteItems);
        listView.setAdapter(adapter);
        queue = Volley.newRequestQueue(this);

        // This run to check if any existing notes are present
        fetchNotes();


        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                startActivity(intent);
            }
        });

        // Click on list item to edit the item; takes to new activity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = notes.get(position);
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                intent.putExtra("NOTE_ID", note.get_id());
                intent.putExtra("TITLE", note.getTitle());
                intent.putExtra("CONTENT", note.getContent());
                intent.putExtra("CATEGORY", note.getCategory());
                startActivity(intent);
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchNotes();
    }


//    To get all notes from backend
    private void fetchNotes() {
        String url = "http://10.0.2.2:5000/notes";
        loadingContainer.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingContainer.setVisibility(View.GONE);
                        try {
                            Log.i(TAG, "Response: " + response.toString());
                            JSONArray notesArray = response.getJSONArray("notes");
                            noteItems.clear();
                            notes.clear();

                            for (int i = 0; i < notesArray.length(); i++) {
                                JSONObject noteJson = notesArray.getJSONObject(i);
                                Note note = new Note(
                                        noteJson.getString("_id"),
                                        noteJson.getString("title"),
                                        noteJson.getString("content"),
                                        noteJson.getString("category")
                                );
                                notes.add(note);
                                String noteText = String.format(
                                        "Title: %s\nCategory: %s\nContent: %s",
                                        note.getTitle(), note.getCategory(), note.getContent()
                                );
                                noteItems.add(noteText);
                            }
                            adapter.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, "Notes loaded!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing JSON: " + e.getMessage(), e);
                            Toast.makeText(MainActivity.this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingContainer.setVisibility(View.GONE);
                        String errorMsg = error.networkResponse != null ? "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data) : error.getMessage() != null ? error.getMessage() : "Unknown error";
                        Log.e(TAG, "Error fetching notes: " + errorMsg, error);
                        Toast.makeText(MainActivity.this, "Error fetching notes: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });

        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }
}