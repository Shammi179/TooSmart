package com.toosmart.note;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.toosmart.MainActivity;
import com.toosmart.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditNote extends AppCompatActivity {

    Intent data;
    EditText editNoteTitle,editNoteContent;
    FirebaseFirestore fStore;
    FirebaseUser user;
    TextView editTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");


        fStore = fStore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        data = getIntent();


        String noteTitle = data.getStringExtra("title");
        String noteContent = data.getStringExtra("content");

        editNoteContent = findViewById(R.id.editNoteContent);
        editNoteTitle = findViewById(R.id.editnoteTitle);

        editNoteTitle.setText(noteTitle);
        editNoteContent.setText(noteContent);
        editTime = findViewById(R.id.edited_time);




        FloatingActionButton fab = findViewById(R.id.saveEditNote);
        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                String nTitle = editNoteTitle.getText().toString();
                String nContent = editNoteContent.getText().toString();
                if (nContent.isEmpty() || nTitle.isEmpty()){
                    Toast.makeText(EditNote.this,"Can't Save note with Empty Field!",Toast.LENGTH_SHORT).show();
                    return;
                }

                DocumentReference dcref = fStore.collection("Notes").document(user.getUid()).collection("myNotes").document(data.getStringExtra("noteID"));
                Map<String,Object> note = new HashMap<>();
                note.put("title",nTitle);
                note.put("content",nContent);

                Toast.makeText(EditNote.this,"Updated!",Toast.LENGTH_SHORT).show();
                closeKeyboard();

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM YY");
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                String date = dateFormat.format(calendar.getTime());
                String time = timeFormat.format(calendar.getTime());
                editTime.setText("Edited: "+date+", "+time);
                dcref.update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditNote.this, "Error, Try Again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if(view!=null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_toolbar_icon,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()== android.R.id.home){
            onBackPressed();
        }
        if(item.getItemId() == R.id.share){
            ApplicationInfo api = getApplicationContext().getApplicationInfo();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,"Title : "+editNoteTitle.getText()+"\nContent : \n"+editNoteContent.getText());
            startActivity(Intent.createChooser(intent,"Share Via"));
        }
        return super.onOptionsItemSelected(item);
    }

}
