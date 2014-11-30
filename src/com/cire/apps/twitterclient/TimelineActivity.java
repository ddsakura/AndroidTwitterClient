
package com.cire.apps.twitterclient;

import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.cire.apps.twitterclient.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

public class TimelineActivity extends Activity {
    private TwitterClient client;
    private ArrayList<Tweet> tweets;
    private TweetArrayAdapter tweetAdapter;
    private ListView lvTweets;
    private String maxId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        client = TwitterClientApp.getRestClient();
        lvTweets = (ListView)findViewById(R.id.lvTweets);
        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.d("debug", "onLoadMore: " + "total: " + totalItemsCount + " page: " + page);
                populateTimeline();
            }
        });

        tweets = new ArrayList<Tweet>();
        populateTimeline();

        tweetAdapter = new TweetArrayAdapter(this);
        lvTweets.setAdapter(tweetAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ComposeActivity.COMPOSE) {
            if (resultCode == RESULT_OK) {
                tweetAdapter.clear();
                maxId = null;
                populateTimeline();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_compose) {
            Intent intent = new Intent();
            intent.setClass(this, ComposeActivity.class);
            startActivityForResult(intent, ComposeActivity.COMPOSE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void populateTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, JSONArray json) {
                ObjectMapper mapper = new ObjectMapper();
                tweets.clear();
                int s = (maxId == null) ? 0 : 1;
                for (int i = s; i < json.length(); i++) {
                    try {
                        JSONObject tweetJson = json.getJSONObject(i);
                        Tweet tweet = mapper.readValue(tweetJson.toString(), Tweet.class);
                        tweets.add(tweet);
                        maxId = String.valueOf(tweet.getId());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (JsonParseException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                tweetAdapter.addAll(tweets);
                tweetAdapter.notifyDataSetChanged();

                Log.d("debug", "maxId: " + maxId + " count: " + tweetAdapter.getCount());
            }

            @Override
            public void onFailure(Throwable arg0, JSONArray json) {
                super.onFailure(arg0, json);
            }
        }, maxId);
    }
}
