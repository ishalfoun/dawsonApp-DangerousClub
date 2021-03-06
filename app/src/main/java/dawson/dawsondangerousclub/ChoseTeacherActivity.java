package dawson.dawsondangerousclub;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import dawson.classes.Teacher;

/**
 * Activity for displaying the teacher list
 * @author Jacob
 */
public class ChoseTeacherActivity extends AppCompatActivity {
    private List<Teacher> teachers;	
	private final String TAG = "ChoseTeacherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chose_teacher);
        if (getIntent().hasExtra("teachers")) {
            teachers = getIntent().getExtras().getParcelableArrayList("teachers");
        }
        loadList();
    }

    private void loadList(){
    List<String> fullnames = new ArrayList<>();
    for (Teacher t : teachers){
        fullnames.add(t.getFull_name());
    }
        ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,fullnames);
        ListView listView = (ListView)findViewById(R.id.teacherListView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onClickTeacher);
        adapter.notifyDataSetChanged();
    }

    private AdapterView.OnItemClickListener onClickTeacher = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Intent intent = new Intent(ChoseTeacherActivity.this, TeacherContactActivity.class);
            intent.putExtra("teacher",teachers.get(position));
            startActivity(intent);
        }
    };
}
