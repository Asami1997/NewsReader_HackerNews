package com.example.asami.newsreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    SQLiteDatabase idDatabase;
    static  ArrayList<String> titles;
    static ArrayList<String> urls;
   static ArrayList<String> info;
    static  ArrayAdapter<String> arrayAdapter;


    public class GetIds extends AsyncTask<String,Void,String> {


        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;

            try {
                //downloading the ids
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = httpURLConnection.getInputStream();

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int read = inputStreamReader.read();

                while (read != -1) {
                    char temp = (char) read;
                    result += temp;
                    read = inputStreamReader.read();


                }

                JSONArray jsonArray = new JSONArray(result);

                int number = 20;
                //clear the databse when starting app again to avoid duplication
                idDatabase.execSQL("DELETE FROM articalc");
                //downloading the details for every id
                for(int i = 0 ; i<5;i++)
                {
                    int id ;

                    id= jsonArray.getInt(i);

                    url = new URL("https://hacker-news.firebaseio.com/v0/item/"+id+".json?print=pretty");

                    httpURLConnection  = (HttpURLConnection) url.openConnection();

                    inputStream = httpURLConnection.getInputStream();
                    inputStreamReader = new InputStreamReader(inputStream);

                    read = inputStreamReader.read();

                    String articalInfo ="";
                    while (read!=-1)
                       {
                           char current = (char) read;

                           articalInfo+=current;

                           read = inputStreamReader.read();


                       }


                       //Downloading the html content
                        JSONObject jsonObj = new JSONObject(articalInfo);
                        if(!jsonObj.isNull("title") && !jsonObj.isNull("url"))
                          {
                              String title = jsonObj.getString("title");
                              String articalurl = jsonObj.getString("url");


                              url = new URL(articalurl);
                              httpURLConnection = (HttpURLConnection) url.openConnection();
                              int status = httpURLConnection.getResponseCode();
                              if (status != HttpURLConnection.HTTP_OK)
                                 {
                                     inputStream = httpURLConnection.getErrorStream();
                                 }else
                                     {
                                         inputStream = httpURLConnection.getInputStream();
                                     }



                              inputStreamReader = new InputStreamReader(inputStream);

                               read = inputStreamReader.read();

                              String articalCode ="";

                              while (read != -1) {
                                  char temp = (char) read;
                                  articalCode += temp;
                                  read = inputStreamReader.read();

                              }

                              String sql = "INSERT INTO articalc (id,title,content) VALUES (?,?,?)";
                              SQLiteStatement stat = idDatabase.compileStatement(sql);
                              stat.bindString(1,String.valueOf(id));
                              stat.bindString(2,title);
                              stat.bindString(3,articalCode);

                              stat.execute();

                              Log.i("titles",title);




                          }

                }



            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            updateListView();
        }
    }

    public void updateListView()

       {
           Cursor cursor = idDatabase.rawQuery("SELECT * FROM articalc ",null);

           int titleIndex = cursor.getColumnIndex("title");
           int contentIndex = cursor.getColumnIndex("content");


           if(cursor.moveToFirst())
             {

                 Toast.makeText(getApplicationContext(),cursor.getString(titleIndex),  Toast.LENGTH_SHORT).show();
                titles.clear();
                 info.clear();

                 do{


                     titles.add(cursor.getString(titleIndex));
                     info.add(cursor.getString(contentIndex));

                 }while(cursor.moveToNext());


                 arrayAdapter.notifyDataSetChanged();
             }


       }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        titles = new ArrayList<String>();
        urls = new ArrayList<String>();
        info = new ArrayList<String>();

        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,titles);
        listView = (ListView) findViewById(R.id.listView);

        listView.setAdapter(arrayAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent  = new Intent(getApplicationContext(),DisplayContent.class);

                intent.putExtra("position",position);

                startActivity(intent);

            }
        });

        try{

            idDatabase = this.openOrCreateDatabase("IDS",MODE_PRIVATE,null);
            idDatabase.execSQL("CREATE TABLE IF NOT EXISTS articalc (id INTEGER PRIMARY KEY,title VARCHAR,content VARCHAR)");
            updateListView();
        }catch (Exception e)
             {
               e.printStackTrace();
             }





        GetIds getIds = new GetIds();

        try {
            getIds.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");

        }catch (Exception e)
           {
               e.printStackTrace();
           }








    }
}
