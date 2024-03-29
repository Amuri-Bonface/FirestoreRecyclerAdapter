package com.guidoapps.surebets;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GoalGoal extends AppCompatActivity {
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.friend_list)
    RecyclerView friendList;

    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goal_goal);
        ButterKnife.bind(this);
        init();
        getFriendList();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView newdoc= findViewById(R.id.newrecord);
        newdoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GoalGoal.this, GoalGoalEditDb.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void init(){
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        friendList.setLayoutManager(linearLayoutManager);
        db = FirebaseFirestore.getInstance();
    }

    private void getFriendList(){
        Query query = db.collection("goalgoal");

        FirestoreRecyclerOptions<PredictionModel> response = new FirestoreRecyclerOptions.Builder<PredictionModel>()
                .setQuery(query, PredictionModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<PredictionModel, FriendsHolder>(response) {
            @Override
            public void onBindViewHolder(FriendsHolder holder, int position, PredictionModel model) {
                progressBar.setVisibility(View.GONE);
                holder.leagueName.setText(model.getLeagueName());
                holder.homeTeam.setText(model.getHomeTeam());
                holder.awayTeam.setText(model.getAwayTeam());
                String docId=getSnapshots().getSnapshot(position).getId();;
                holder.dateTime.setText(model.getDateTime());
                holder.odds.setText(model.getOdds());
                holder.prediction.setText("Prediction:"+model.getPrediction());
                holder.result.setText("Result:"+model.getResult());
                try {
                    String mstatus = model.getStatus();

                    if (mstatus.equals("WIN") || mstatus.equals("win")) {
                        holder.status.setTextColor(Color.GREEN);
                        holder.status.setText("Status:" + mstatus);
                    } else if (mstatus.equals("LOSE") || mstatus.equals("lose")) {
                        holder.status.setTextColor(Color.RED);
                        holder.status.setText("Status:" + mstatus);
                    }
                }catch (Exception ex){}
               /* Glide.with(getApplicationContext())
                        .load(model.getImage())
                        .into(holder.imageView);

                Glide.with(getApplicationContext())
                        .load(model.getAwayImage())
                        .into(holder.awayImage);
                Glide.with(getApplicationContext())
                        .load(model.getHomeImage())
                        .into(holder.homeImage);*/

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        db.collection("goalgoal").document(docId)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                        Toast.makeText(getApplicationContext(),"Success"+docId,Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //Log.w(TAG, "Error deleting document", e);
                                    }
                                });
                        return true;
                    }
                });

                holder.itemView.setOnClickListener(v -> {

                    Intent intent= new Intent(GoalGoal.this ,GoalGoalEditDb.class);
                    intent.putExtra("leagueName", model.getLeagueName());
                    intent.putExtra("homeTeam", model.getHomeTeam());
                    intent.putExtra("awayTeam", model.getAwayTeam());
                    intent.putExtra("status", model.getStatus());
                    intent.putExtra("prediction", model.getPrediction());
                    intent.putExtra("dateTime", model.getDateTime());
                    intent.putExtra("odds", model.getOdds());
                    intent.putExtra("result", model.getResult());
                    intent.putExtra("id", docId);
                    startActivity(intent);

                });
            }
            @Override
            public FriendsHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.list_item, group, false);

                return new FriendsHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        adapter.notifyDataSetChanged();
        friendList.setAdapter(adapter);
    }

    public class FriendsHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.leagueName)
        TextView leagueName;
        @BindView(R.id.homeTeam)
        TextView homeTeam;
        @BindView(R.id.awayTeam)
        TextView awayTeam;
        @BindView(R.id.dateTime)
        TextView dateTime;
        @BindView(R.id.odds)
        TextView odds;
        @BindView(R.id.prediction)
        TextView prediction;
        @BindView(R.id.result)
        TextView result;
        @BindView(R.id.status)
        TextView status;

      /*  @BindView(R.id.image)
        CircleImageView imageView;
        @BindView(R.id.homeImage)
        CircleImageView homeImage;
        @BindView(R.id.awayImage)
        CircleImageView awayImage;
*/
        public FriendsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}
