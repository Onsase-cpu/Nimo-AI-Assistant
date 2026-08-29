package com.nimo.assistant;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;
import java.util.*;
import okhttp3.*;
import org.json.*;

public class MainActivity extends Activity {
    private LinearLayout root, chat; private EditText input, email, password; private TextView status; private SpeechRecognizer recognizer; private TextToSpeech tts; private final OkHttpClient http = new OkHttpClient();
    private static final String API = "http://10.0.2.2:8080";
    private int green=Color.rgb(47,143,107), blue=Color.rgb(40,120,168), orange=Color.rgb(242,140,40), ink=Color.rgb(17,42,42), paper=Color.rgb(247,245,238);
    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setStatusBarColor(paper); showAuth(); }
    private TextView label(String s,int size){ TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(ink); v.setPadding(4,8,4,8); return v; }
    private GradientDrawable bg(int c,float r){ GradientDrawable g=new GradientDrawable(); g.setColor(c); g.setCornerRadius(r); return g; }
    private Button button(String text,int color){ Button b=new Button(this); b.setText(text); b.setTextColor(Color.WHITE); b.setAllCaps(false); b.setTextSize(15); b.setBackground(bg(color,28)); b.setPadding(18,8,18,8); return b; }
    private void base(){ root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,28,24,20); root.setBackgroundColor(paper); setContentView(root); }
    private void showAuth(){ base(); root.addView(label("nimo",38),new LinearLayout.LayoutParams(-1,70)); root.addView(label("Your calm, capable voice assistant.",18));
        email=new EditText(this); email.setHint("Email"); email.setInputType(33); root.addView(email,new LinearLayout.LayoutParams(-1,60));
        password=new EditText(this); password.setHint("Password"); password.setInputType(129); root.addView(password,new LinearLayout.LayoutParams(-1,60));
        Button login=button("Log in",green), signup=button("Create account",blue); root.addView(login); root.addView(signup); status=label("Demo mode works offline. Add API_URL in settings for live AI.",13); root.addView(status);
        login.setOnClickListener(v->auth(false)); signup.setOnClickListener(v->auth(true)); }
    private void auth(boolean create){ if(email.getText().length()<5||password.getText().length()<4){ status.setText("Enter a valid email and a 4+ character password."); return; } getSharedPreferences("nimo",0).edit().putBoolean("signed",true).apply(); showAssistant(); }
    private void showAssistant(){ base(); LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); TextView title=label("Nimo",30); top.addView(title,new LinearLayout.LayoutParams(0,70,1)); Button out=button("Sign out",orange); top.addView(out,new LinearLayout.LayoutParams(110,52)); out.setOnClickListener(v->{getSharedPreferences("nimo",0).edit().clear().apply();showAuth();}); root.addView(top);
        status=label("Ready when you are.",15); status.setTextColor(green); root.addView(status); chat=new LinearLayout(this); chat.setOrientation(LinearLayout.VERTICAL); ScrollView scroll=new ScrollView(this); scroll.addView(chat); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout row=new LinearLayout(this); input=new EditText(this); input.setHint("Ask Nimo anything…"); input.setSingleLine(false); row.addView(input,new LinearLayout.LayoutParams(0,62,1)); Button mic=button("Talk",blue); row.addView(mic,new LinearLayout.LayoutParams(90,62)); Button send=button("Send",green); row.addView(send,new LinearLayout.LayoutParams(90,62)); root.addView(row);
        send.setOnClickListener(v->ask(input.getText().toString())); mic.setOnClickListener(v->listen()); addBubble("Nimo","Hi, I’m Nimo. Try: ‘open YouTube’, ‘set a timer’, or ask me anything.",true); initTts(); }
    private void addBubble(String who,String text,boolean bot){ TextView v=label(who+"\n"+text,16); v.setTextColor(bot?ink:Color.WHITE); v.setBackground(bg(bot?Color.rgb(220,239,227):blue,24)); v.setPadding(18,14,18,14); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,8,0,8); chat.addView(v,p); }
    private void initTts(){ tts=new TextToSpeech(this, s->{if(s==TextToSpeech.SUCCESS){ tts.setLanguage(Locale.US); for(TextToSpeech.Voice v:tts.getVoices()) if(v.getName().toLowerCase().contains("female")){tts.setVoice(v);break;}}}); }
    private void listen(){ if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},5);return;} recognizer=SpeechRecognizer.createSpeechRecognizer(this); recognizer.setRecognitionListener(new RecognitionListener(){public void onReadyForSpeech(Bundle b){status.setText("Listening…");} public void onResults(Bundle b){ArrayList<String> x=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION); if(x!=null&&!x.isEmpty()) ask(x.get(0));} public void onError(int e){status.setText("I didn’t catch that. Try again.");} public void onBeginningOfSpeech(){}public void onRmsChanged(float r){}public void onBufferReceived(byte[] b){}public void onEndOfSpeech(){}public void onPartialResults(Bundle b){}public void onEvent(int a,Bundle b){}}); Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); recognizer.startListening(i); }
    private void ask(String q){ if(q==null||q.trim().isEmpty())return; input.setText(""); addBubble("You",q,false); status.setText("Thinking…"); String local=localCommand(q); if(local!=null){reply(local);return;} RequestBody body=RequestBody.create("{\"message\":"+JSONObject.quote(q)+"}",MediaType.parse("application/json")); Request r=new Request.Builder().url(API+"/api/chat").post(body).build(); http.newCall(r).enqueue(new Callback(){public void onFailure(Call c,java.io.IOException e){runOnUiThread(()->reply("I’m offline right now. I can still help with timers, opening websites, and local commands."));}public void onResponse(Call c,Response res)throws java.io.IOException{String s=res.body().string();try{String a=new JSONObject(s).optString("reply","I’m here.");runOnUiThread(()->reply(a));}catch(Exception e){runOnUiThread(()->reply("I couldn’t read that response."));}}}); }
    private String localCommand(String q){String x=q.toLowerCase(); if(x.contains("open youtube")){startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://youtube.com")));return "Opening YouTube.";} if(x.contains("open google")){startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://google.com")));return "Opening Google.";} if(x.contains("time"))return "It’s "+new java.text.SimpleDateFormat("h:mm a",Locale.US).format(new Date())+"."; if(x.contains("set a timer")){new Handler().postDelayed(()->reply("Your timer is complete."),60000);return "Timer set for one minute.";} return null;}
    private void reply(String s){addBubble("Nimo",s,true);status.setText("Ready when you are.");if(tts!=null)tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"nimo");}
    @Override protected void onDestroy(){if(tts!=null)tts.shutdown();if(recognizer!=null)recognizer.destroy();super.onDestroy();}
}
