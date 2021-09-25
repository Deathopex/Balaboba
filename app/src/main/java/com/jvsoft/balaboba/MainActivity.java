package com.jvsoft.balaboba;

import android.widget.*;
import android.view.*;
import java.net.*;
import java.nio.charset.*;
import java.io.*;
import android.app.*;
import android.os.*;
import java.util.*;
import org.json.*;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.SearchView.*;
import android.widget.EditText;
import android.content.Context;
import android.text.ClipboardManager;
import android.content.Intent;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

	private static String result;
	private static ProgressBar loading;
	private static Button genButton;
	private static ImageButton copyButton;
	private static ImageButton clearButton;
	private static ImageButton shareButton;
	private static Timer timer = new Timer();

	public static EditText input;
	public static ClipboardManager clipboard;
	public static Context context;
	public static Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		input = (EditText)findViewById(R.id.inputField);
		genButton = (Button) findViewById(R.id.genButton);
		loading = (ProgressBar) findViewById(R.id.loading);
		copyButton = (ImageButton) findViewById(R.id.copyButton);
		clearButton = (ImageButton) findViewById(R.id.clearButton);
		shareButton = (ImageButton) findViewById(R.id.shareButton);
		clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        context = getApplicationContext();

		copyButton.setOnClickListener(new copyListener());
		genButton.setOnClickListener(new balabListener());
		clearButton.setOnClickListener(new clearListener());
		shareButton.setOnClickListener(new shareListener());
		setupLoadingPosRelativeToInput();
		createDataFolder();
	  }

	String balaboba(String text) {
		try {
			URLConnection http = new URL("https://zeapi.yandex.net/lab/api/yalm/text3").openConnection();
			http.setRequestProperty("Content-Type", "application/json");
			http.setDoOutput(true);
			http.setDoInput(true);
			http.connect();
			String rq = "{\"query\":\"" + text + "\",\"intro\":0,\"filter\":1}";
			http.getOutputStream().write(rq.getBytes(StandardCharsets.UTF_8));
			BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
			String line = reader.readLine();
			reader.close();
			JSONObject obj = new JSONObject(line);
			return obj.get("query") + " " + obj.getString("text");
		  }
		catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		  }
	  }

	void setLoading(Boolean state) {
		if (state) {
			runOnUiThread(new Runnable() {
				  public void run() {
					  loading.setVisibility(View.VISIBLE);
					  genButton.setEnabled(false);
					}
				});
		  }
		else {
			runOnUiThread(new Runnable() {
				  public void run() {
					  loading.setVisibility(View.GONE);
					  genButton.setEnabled(true);
					}
				});
		  }
	  }

	void setupLoadingPosRelativeToInput() {
        timer.schedule(new TimerTask() {
			  public void run() {
				  runOnUiThread(new Runnable() {
						public void run() {
						    loading.setVisibility(View.GONE);
							int [] location = new int[2];
							MainActivity.input.getLocationOnScreen(location);
							int x = location[0];
							int y = location[1];
							int height = MainActivity.input.getHeight();
							int width = MainActivity.context.getDisplay().getWidth();
							loading.setTranslationX(width / 2 - 75);
							loading.setTranslationY(y);
						  }
					  });
				}
			}, 500);
	  }

	void createDataFolder() {
		try {
			File file = new File("/data/data/com.jvsoft.balaboba/test/");
			if (!file.exists()) file.mkdir();
		  }
		catch (Exception e) {
			input.setText(e.toString());
		  }
	  }

	class balabListener implements android.view.View.OnClickListener {
		@Override
		public void onClick(View p1) {
			Thread thr = new Thread(new Runnable() {
				  @Override
				  public void run() {
					  setLoading(true);
					  result = balaboba(input.getText().toString());
					  setLoading(false);
					  runOnUiThread(new Runnable() {
							public void run() {
								input.setText(result);
							  }
						  });
					}
				});
			thr.start();
		  }
	  }

	class copyListener implements android.view.View.OnClickListener {
		@Override
		public void onClick(View p1) {
			copy(MainActivity.input.getText().toString());
		  }

		void copy(String text) {
			MainActivity.clipboard.setText(text);
		  }
	  }
  }

class clearListener implements android.view.View.OnClickListener {
	@Override
	public void onClick(View p1) {
		MainActivity.input.setText("");
	  }
  }


class shareListener implements android.view.View.OnClickListener {
	@Override
	public void onClick(View p1) {
		if (MainActivity.input.getText().toString().equals("")) {
			Toast.makeText(MainActivity.context, "Пустой текст. Сначала набалабольте", 
						   Toast.LENGTH_LONG).show();
		  }
		else {
			share(MainActivity.input.getText().toString());
		  }
	  }

	void share(String text) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, text);
		sendIntent.setType("text/plain");
		Intent shareIntent = Intent.createChooser(sendIntent, null);
		shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MainActivity.context.startActivity(shareIntent);
	  }
  }
  
  

