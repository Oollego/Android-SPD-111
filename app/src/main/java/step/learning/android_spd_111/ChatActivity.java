package step.learning.android_spd_111;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

//import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import step.learning.android_spd_111.orm.ChatMessage;
import step.learning.android_spd_111.orm.ChatResponse;

public class ChatActivity extends AppCompatActivity {
    private static final String CHAT_URL = "https://chat.momentfor.fun/";
    private final byte[] buffer = new byte[8096];
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Animation messageAnim;
    private Animation btnAnim;
    private EditText etNik;
    private EditText etMessage;
    private ScrollView chatScroller;
    private LinearLayout container;
    private MediaPlayer newMessageSound;
    private ImageView soundImg;
    private boolean isSoundOn = true ;
    private final Handler handler = new Handler();
    @Override
    protected void onDestroy() {
        executorService.shutdownNow();
        super.onDestroy();
    }
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        messageAnim = AnimationUtils.loadAnimation(this, R.anim.message_anim);
        btnAnim = AnimationUtils.loadAnimation(this, R.anim.chat_btn_anim);
        updateChat();

        urlToImageView(
                "https://cdn-icons-png.flaticon.com/512/5962/5962463.png",
                findViewById(R.id.chat_iv_logo));

        etNik = findViewById(R.id.chat_et_nik);
        etMessage = findViewById(R.id.chat_et_message);
        chatScroller = findViewById(R.id.chat_scroller);
        container = findViewById(R.id.chat_container);
        newMessageSound = MediaPlayer.create(this, R.raw.pickup);

        soundImg = findViewById(R.id.chat_iv_sound);
        soundImg.setOnClickListener((v) -> soundSwitcher());

        findViewById( R.id.chat_btn_send ).setOnClickListener(this::onSendClick);
        container.setOnClickListener((v) -> hideSoftInput() );
    }
    private void soundSwitcher(){
//
//        if (newMessageSound != null) newMessageSound.release();
//        else
//        newMessageSound.setVolume(0,0);
        if(isSoundOn){
            isSoundOn = false;
            soundImg.setImageResource(R.drawable.outline_volume_off_24);
        }
        else{
            isSoundOn = true;
            soundImg.setImageResource(R.drawable.baseline_volume_up_24);
        }
    }
    private void updateChat(){
        if( executorService.isShutdown()) return;

        CompletableFuture.supplyAsync( this::loadChat, executorService )
                .thenApplyAsync(this::processChatResponse )
                .thenAcceptAsync(this::displayChatMessage);
        handler.postDelayed(this::updateChat, 3000);


    }
    private void hideSoftInput(){
        View focusedView = getCurrentFocus();
        if(focusedView != null){
            InputMethodManager manager = (InputMethodManager)
                    getSystemService( Context.INPUT_METHOD_SERVICE );
            manager.hideSoftInputFromWindow( focusedView.getWindowToken(), 0 );
            focusedView.clearFocus();
        }
    }

    private void onSendClick(View v){
        String author = etNik.getText().toString();
        String message = etMessage.getText().toString();
        ImageView btnSend = findViewById(R.id.btnSend);
        btnSend.startAnimation(btnAnim);
        if(author.isEmpty()){
            Toast.makeText(this, "Enter 'Nik''", Toast.LENGTH_SHORT).show();
            return;
        }
        if(message.isEmpty()){
            Toast.makeText(this, "Enter message", Toast.LENGTH_SHORT).show();
            return;
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setAuthor( author );
        chatMessage.setText( message );

        CompletableFuture
                .runAsync( ()-> sendChatMessage( chatMessage ), executorService);
        etNik.setEnabled(false);
        etMessage.setText("");

    }
    private void sendChatMessage (ChatMessage chatMessage){
        try{
            URL url = new URL (CHAT_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setChunkedStreamingMode( 0 );
            connection.setDoOutput( true );
            connection.setDoInput( true );
            connection.setRequestMethod( "POST" );

            connection.setRequestProperty( "Accept", "application/json" );
            connection.setRequestProperty( "Content_Type", "application/x-www-form-urlencoded" );
            connection.setRequestProperty( "Connection", "close" );

            OutputStream  connectionOutput = connection.getOutputStream();
            String body = String.format(
                    "author=%s&msg=%s",
                    URLEncoder.encode( chatMessage.getAuthor(), StandardCharsets.UTF_8.name()),
                    URLEncoder.encode( chatMessage.getText(), StandardCharsets.UTF_8.name())
            );
            connectionOutput.write( body.getBytes( StandardCharsets.UTF_8 ) );

            connectionOutput.flush();

            connectionOutput.close();

            int statusCode = connection.getResponseCode();

            if( statusCode ==201 ){
                updateChat();
            }
            else{
                InputStream connectionInput = connection.getErrorStream();
                body = readString( connectionInput) ;
                connectionInput.close();
                Log.e("sendChatMessage", body);
            }
            connection.disconnect();
        }
        catch(Exception ex){
            Log.e("sendChatMessage", ex.getMessage());
        }
    }
    private String loadChat(){
        try(InputStream chatStream = new URL( CHAT_URL ).openStream()){

            return readString(chatStream);
        }
        catch (Exception ex ){
            Log.e("ChatActivity::loadChat()",
                    ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
        }
        return null;
    }
    private boolean processChatResponse (String response){
        boolean wasNewMessage = false;
        boolean isFirstProcess = this.chatMessages.isEmpty();

        try{
            ChatResponse chatResponse = ChatResponse.fromJsonString( response );
            for(ChatMessage message : chatResponse.getData() ){
                if(this.chatMessages.stream().noneMatch(
                        m->m.getId().equals( message.getId()))){
                    this.chatMessages.add(message);
                    wasNewMessage = true;

                }
            }
            if(isFirstProcess){
                this.chatMessages.sort(Comparator.comparing(ChatMessage::getMoment));
            }
//            else if(wasNewMessage){
//                newMessageSound.start();
//            }
        }
        catch (IllegalArgumentException ex){
            Log.e("ChatActivity::processChatResponce",
                    ex.getMessage() == null ? ex.getClass().getName():ex.getMessage());
        }
        return wasNewMessage;
    }
    private void displayChatMessage(boolean wasNewMessage) {
        if (!wasNewMessage) return;

        Drawable myBackground = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.chat_msg_my);
        Drawable otherBackground = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.chat_msg_other);

        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        msgParams.setMargins(20,10,20,10);
        msgParams.gravity = Gravity.START;



        String strDateFormat = "hh:mm dd.MM.yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat, Locale.ENGLISH);


        runOnUiThread(() -> {



//        StringBuilder sb = new StringBuilder();
            for (ChatMessage message : this.chatMessages) {

                if(message.getView() != null){
                    continue;
                }
                LinearLayout msgContainer = new LinearLayout(this);
                msgContainer.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams textContParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                textContParams.setMargins(20, 10, 20, 10);
                if (message.getAuthor().equals(etNik.getText().toString())){

                    textContParams.gravity = Gravity.END;
                    msgContainer.setBackground(myBackground);

                }
                else{
                    textContParams.gravity = Gravity.START;
                    msgContainer.setBackground(otherBackground);

                    if(newMessageSound != null && isSoundOn ) newMessageSound.start();
                }

                msgContainer.setLayoutParams(textContParams);




               // textContainer.setPadding(15,5,15,5);
              //extContainer.setGravity(Gravity.END);

                TextView dateTv = new TextView(this);
                dateTv.setText(sdf.format(message.getMoment()));

                dateTv.setLayoutParams(msgParams);

                //dateTv.setGravity(Gravity.END);
                LinearLayout txtContainer = new LinearLayout(this);
                txtContainer.setLayoutParams(msgParams);

                TextView tvAuthor = new TextView(this);
                tvAuthor.setText(message.getAuthor() + ": ");
                tvAuthor.setTypeface(Typeface.create("monospace", Typeface.BOLD));
                tvAuthor.setTextSize(18);
                txtContainer.addView(tvAuthor);

                TextView tv = new TextView(this);
                tv.setText( message.getText());
                tv.setTextSize(16);
                txtContainer.addView(tv);
                //tv.setBackground(myBackground);
                //tv.setGravity(Gravity.END);
                //tv.setPadding(15,5,15,5);
                //tv.setLayoutParams(msgParams);
                //container.addView(tv);

                msgContainer.addView(txtContainer);
                msgContainer.addView(dateTv);

                container.addView(msgContainer);
                message.setView(msgContainer);
                msgContainer.startAnimation(messageAnim);
            }
//           chatScroller.fullScroll(View.FOCUS_DOWN);
            chatScroller.post(
                    ()->chatScroller.fullScroll( View.FOCUS_DOWN )
            );

            // runOnUiThread(() -> ((TextView) findViewById(R.id.chat_tv_title)).setText(sb.toString()));
        });
    }

    private String readString (InputStream stream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        int len;
        while((len = stream.read(buffer)) != -1){
            byteBuilder.write(buffer, 0, len);
        }
        String res = byteBuilder.toString();
        byteBuilder.close();
        return res;
    }

    private void urlToImageView(String url, ImageView imageView) {
        CompletableFuture
                .supplyAsync( () -> {
                try ( java.io.InputStream is = new URL(url).openConnection().getInputStream() ) {
                    return BitmapFactory.decodeStream( is );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, executorService )
                .thenAccept( imageView::setImageBitmap );
    }
}
