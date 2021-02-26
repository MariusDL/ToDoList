package com.marius.todo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private ProgressDialog loader;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userId;

    private String key = "", taskTitle, taskDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.homeToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("To do list");

        recyclerView = findViewById(R.id.homeRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("tasks").child(userId);


        floatingActionButton = findViewById(R.id.floatingButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });


    }

    private void addTask(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.input_task, null);
        dialog.setView(myView);

        final AlertDialog alertDialog = dialog.create();
        alertDialog.setCancelable(false);

        final EditText task = myView.findViewById(R.id.taskEditText);
        final EditText description = myView.findViewById(R.id.descriptionEditText);
        Button save = myView.findViewById(R.id.saveButton);
        Button cancel = myView.findViewById(R.id.cancelButton);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskText = task.getText().toString().trim();
                String descriptionText = description.getText().toString().trim();
                String id = databaseReference.push().getKey();
                String date = DateFormat.getDateInstance().format(new Date());

                if(TextUtils.isEmpty(taskText)){
                    task.setError("Enter task title");
                    return;
                }

                if(TextUtils.isEmpty(descriptionText)){
                    description.setError("Enter task description");
                    return;
                } else {
                    loader.setMessage("Adding task");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    Task task = new Task(taskText, descriptionText, id, date);
                    databaseReference.child(id).setValue(task).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(HomeActivity.this, "Task successfully added", Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(HomeActivity.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                        }
                    });
                }

                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void updateTask(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.update_task, null);
        myDialog.setView(view);

        final AlertDialog dialog = myDialog.create();

        final EditText upTask = view.findViewById(R.id.updateTaskTitle);
        final EditText upDescription = view.findViewById(R.id.updateTaskDescription);

        upTask.setText(taskTitle);
        upTask.setSelection(taskTitle.length());

        upDescription.setText(taskDescription);
        upDescription.setSelection(taskDescription.length());

        Button deleteTaskButton = view.findViewById(R.id.deleteTaskButton);
        Button updateTaskButton = view.findViewById(R.id.updateTaskButton);

        updateTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskTitle = upTask.getText().toString().trim();
                taskDescription = upDescription.getText().toString().trim();
                String date = DateFormat.getDateInstance().format(new Date());

                Task task = new Task(taskTitle, taskDescription, key, date);
                databaseReference.child(key).setValue(task).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(HomeActivity.this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            String err = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Failed: " + err, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });

        deleteTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(HomeActivity.this, "Task successfully deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            String err = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Failed: " + err, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Task> options = new FirebaseRecyclerOptions.Builder<Task>()
                .setQuery(databaseReference, Task.class).build();

        FirebaseRecyclerAdapter<Task, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Task, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, final int position, @NonNull final Task model) {
                holder.setDate(model.getDate());
                holder.setTask(model.getTitle());
                holder.setDescription(model.getDescription());

                holder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        key = getRef(position).getKey();
                        taskTitle = model.getTitle();
                        taskDescription = model.getDescription();

                        updateTask();
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieved_tasks, parent,false);
                return new MyViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View myView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
        }
        public void setTask(String task){
            TextView taskTextView = myView.findViewById(R.id.receivedTask);
            taskTextView.setText(task);
        }

        public void setDescription(String description){
            TextView descriptionTextView = myView.findViewById(R.id.receivedDescription);
            descriptionTextView.setText(description);
        }
        public void setDate(String date){
            TextView dateTextView = myView.findViewById(R.id.receivedDate);
            dateTextView.setText(date);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                mAuth.signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}