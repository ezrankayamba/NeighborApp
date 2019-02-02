package tz.co.nezatech.neighborapp.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import tz.co.nezatech.neighborapp.MainActivity;
import tz.co.nezatech.neighborapp.R;
import tz.co.nezatech.neighborapp.adapter.GroupAdapter;
import tz.co.nezatech.neighborapp.adapter.MemberAdapter;
import tz.co.nezatech.neighborapp.model.Contact;
import tz.co.nezatech.neighborapp.model.Group;
import tz.co.nezatech.neighborapp.model.Member;
import tz.co.nezatech.neighborapp.pref.UserPreference;
import tz.co.nezatech.neighborapp.signup.Response;
import tz.co.nezatech.neighborapp.signup.WelcomeActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManageGroupActivity extends AppCompatActivity {

    Group group;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_group);

        Toolbar toolbar = findViewById(R.id.toolbar);
        Serializable serializable = Objects.requireNonNull(getIntent().getExtras()).getSerializable(MainActivity.DATA_SELECTED_GROUP);
        if (serializable != null && serializable instanceof Group) {
            group = ((Group) serializable);
            toolbar.setTitle(group.getName());
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    ListView groupList;
    List<Member> members;
    MemberAdapter customAdapter;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void init() {
        if (group !=null) {
            members = group.getMembers();
        } else {
            members = new ArrayList<>();
        }
        Log.d("Group members: ", members.size() + "");
        TextView tv=findViewById(R.id.txtCountMembers);
        tv.setText(members.size()+" neighbors");

        groupList = findViewById(R.id.groupMembersList);
        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int pos, long l) {
                Member item = (Member) adapterView.getItemAtPosition(pos);
                Toast.makeText(ManageGroupActivity.this, item.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        groupList.setTextFilterEnabled(true);

        customAdapter = new MemberAdapter(this, R.layout.contacts_list_item, members);
        groupList.setAdapter(customAdapter);

        Button btn=findViewById(R.id.btnAddNeighbors);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewGroupActivity.class);
                intent.putExtra(MainActivity.DATA_SELECTED_GROUP, group);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}
