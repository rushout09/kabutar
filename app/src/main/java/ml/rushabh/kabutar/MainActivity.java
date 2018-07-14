package ml.rushabh.kabutar;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser mUser;
    private DatabaseReference mDatabase;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;
    private Button mSubmit;
    private EditText mMessage;
    private ImageButton mPhotoPickerButton;

    private RecyclerView mRecyclerView;

    private ProgressBar mProgressBar;

    private FirebaseRecyclerAdapter mAdapter;

    private static final int RC_PHOTO_PICKER = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            setContentView(R.layout.activity_main);
            mProgressBar = (ProgressBar)findViewById(R.id.loading_pb);
            mMessage = (EditText) findViewById(R.id.message_ET);
            mSubmit = (Button) findViewById(R.id.submit_btn);
            mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);

            mFirebaseStorage = FirebaseStorage.getInstance();

            mDatabase = FirebaseDatabase.getInstance().getReference("messages");

            mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");

            mRecyclerView = (RecyclerView)findViewById(R.id.list_rv);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // ImagePickerButton shows an image picker to upload a image for a message
            mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
                }
            });

            mSubmit.setEnabled(true);

            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMessage.getText() == null || mMessage.getText().toString() == "") {
                    } else {
                        mSubmit.setEnabled(false);
                        Toast.makeText(getApplicationContext(), "Sending Message...", Toast.LENGTH_SHORT).show();

                        Message message = new Message(mMessage.getText().toString(), mUser.getDisplayName(), null);
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

            class MessageHolder extends RecyclerView.ViewHolder {

                TextView messageTextView;
                TextView senderTextView;
                TextView timeTextView;
                ImageView photoImageView;

                public MessageHolder(View itemView) {
                    super(itemView);
                    photoImageView = (ImageView) itemView.findViewById(R.id.photoImageView);
                    messageTextView = (TextView) itemView.findViewById(R.id.message_tv);
                    senderTextView = (TextView) itemView.findViewById(R.id.sender_tv);
                    timeTextView = (TextView) itemView.findViewById(R.id.time_tv);


                }

                public void setMessageTextView(String title) {
                    messageTextView.setText(title);
                }

                public void setSenderTextView(String sender) {
                    senderTextView.setText(sender);
                }

                public void setTimeTextView(long time) {
                    timeTextView.setText(DateFormat.format("HH:mm, dd-MM-yy",
                            time));
                }

                public void setPhoto(Message message) {
                    messageTextView.setVisibility(View.GONE);
                    photoImageView.setVisibility(View.VISIBLE);
                    Glide.with(photoImageView.getContext())
                            .load(message.getPhotoUrl())
                            .into(photoImageView);

                }

                public void setMessage(Message message) {
                    messageTextView.setVisibility(View.VISIBLE);
                    photoImageView.setVisibility(View.GONE);
                    messageTextView.setText(message.getMessageText());
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

                    mProgressBar.setVisibility(View.INVISIBLE);

                    boolean isPhoto = model.getPhotoUrl() != null;
                    Message message = getItem(position);
                    if (isPhoto) {
                        holder.setPhoto(message);
                    } else {
                        holder.setMessage(message);
                    }

                    holder.setSenderTextView(message.getSender());
                    holder.setTimeTextView(message.getMessageTime());

                }
            };

            mRecyclerView.setAdapter(mAdapter);



            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    mRecyclerView.getLayoutManager().scrollToPosition(mAdapter.getItemCount());
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    mRecyclerView.getLayoutManager().scrollToPosition(mAdapter.getItemCount());
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();

            // Get a reference to store file at chat_photos/<FILENAME>
            final StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());


            // Upload file to Firebase Storage
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // When the image has successfully uploaded, we get its download URL
                            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUrl = uri;
                                    // Set the download URL to the message box, so that the user can send it to the database
                                    Message message = new Message(null, mUser.getDisplayName(), downloadUrl.toString());
                                    mDatabase.push().setValue(message);
                                }
                            });

                        }
                    });
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
