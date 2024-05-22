package step.learning.android_spd_111;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import java.util.ArrayList;
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

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();
        super.onDestroy();
    }
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        CompletableFuture.supplyAsync( this::loadChat, executorService )
                .thenApplyAsync(this::processChatResponse )
                .thenAcceptAsync(this::displayChatMessage);
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
        try{
            ChatResponse chatResponse = ChatResponse.fromJsonString( response );
            for(ChatMessage message : chatResponse.getData() ){
                if(this.chatMessages.stream().noneMatch(
                        m->m.getId().equals( message.getId()))){
                    this.chatMessages.add(message);
                    wasNewMessage = true;
                }
            }
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
            LinearLayout container = findViewById(R.id.chat_container);
            int i = 0;

//        StringBuilder sb = new StringBuilder();
            for (ChatMessage message : this.chatMessages) {
                i++;
                boolean evenOrNot = (i % 2 == 0);

                LinearLayout msgContainer = new LinearLayout(this);
                msgContainer.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams textContParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                textContParams.setMargins(20, 10, 20, 10);
                if (evenOrNot){

                    textContParams.gravity = Gravity.END;
                    msgContainer.setBackground(myBackground);
                }
                else{
                    textContParams.gravity = Gravity.START;
                    msgContainer.setBackground(otherBackground);

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
            }
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
}
