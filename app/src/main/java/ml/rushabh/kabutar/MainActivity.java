package ml.rushabh.kabutar;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser mUser;
    private DatabaseReference mDatabase;
    private Button mSubmit;
    private EditText mMessage;

    private RecyclerView mRecyclerView;

    private ChildEventListener mComplainListener;

    private FirebaseRecyclerAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            setContentView(R.layout.activity_main);
            mMessage = (EditText) findViewById(R.id.message_ET);
            mSubmit = (Button) findViewById(R.id.submit_btn);
            mDatabase = FirebaseDatabase.getInstance().getReference("messages");

            mRecyclerView = (RecyclerView)findViewById(R.id.list_rv);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            mSubmit.setEnabled(true);

            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMessage.getText() == null || mMessage.getText().toString() == "") {
                    } else {
                        mSubmit.setEnabled(false);
                        Toast.makeText(getApplicationContext(), "Sending Message...", Toast.LENGTH_SHORT).show();

                        Message message = new Message(mMessage.getText().toString(), mUser.getDisplayName());
                        mDatabase.push().setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Message Sent.", Toast.LENGTH_SHORT).show();
                                mSubmit.setEnabled(true);
                                mMessage.setText("");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Message not sent.", Toast.LENGTH_SHORT).show();
                                mSubmit.setEnabled(true);
                            }
                        });
                    }
                }
            });

            class MessageHolder extends RecyclerView.ViewHolder{

                TextView messageTextView;
                TextView senderTextView;

                public MessageHolder(View itemView) {
                    super(itemView);
                    messageTextView = (TextView)itemView.findViewById(R.id.message_tv);
                    senderTextView = (TextView)itemView.findViewById(R.id.sender_tv);

                }

                public void setMessageTextView(String title) {
                    messageTextView.setText(title);
                }

                public void setSenderTextView(String sender) {
                    senderTextView.setText(sender);
                }
            }
            Query query = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("messages")
                    .limitToLast(50);

            FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>()
                    .setQuery(query, Message.class)
                    .build();

            mAdapter = new FirebaseRecyclerAdapter<Message, MessageHolder>(options){
                @NonNull
                @Override
                public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_message, parent, false);
                    return new MessageHolder(view);
                }

                @Override
                protected void onBindViewHolder(@NonNull MessageHolder holder, int position, @NonNull Message model) {
                    Message message = getItem(position);
                    holder.setMessageTextView(message.getMessageText());
                    holder.setSenderTextView(message.getSender());

                }
            };

            mRecyclerView.setAdapter(mAdapter);

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    // ...
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    // ...
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    // ...
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // ...
                }
            };
            query.addChildEventListener(childEventListener);

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        if(R.id.sign_out == item.getItemId()){
            AuthUI.getInstance()
                    .signOut(this);
            intent = new Intent(MainActivity.this,SignInActivity.class);
            startActivity(intent);
            finish();

        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}
