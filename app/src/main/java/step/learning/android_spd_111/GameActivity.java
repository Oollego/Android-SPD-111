package step.learning.android_spd_111;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.LinkedList;
import java.util.Random;


public class GameActivity extends AppCompatActivity {

    private static final int FIELD_WIDTH = 16;
    private static final int FIELD_HEIGHT = 24;
    private TextView[][] gameField;
    private final LinkedList<Vector2> snake = new LinkedList<>();

    private final Handler handler = new Handler();
    private final Handler pillHandler = new Handler();
    private int fieldColor;
    private int snakeColor;
    private Direction moveDirection;
    private boolean isPlaying;
    private static final String food = new String(Character.toChars(0x1F34E));
    private static final String pill = new String(Character.toChars(0x1f48a));
    private Vector2 foodPosition;
    private Vector2 pillPosition;
    private int snakeSpeed = 700;
    private Integer score;
    private static final Random _random = new Random();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });


        findViewById(R.id.main).setOnTouchListener(new OnSwipeListener(this){
            @Override
            public void onSwipeBottom(){
                if(moveDirection != Direction.top ){
                    moveDirection = Direction.bottom;
                }
            }
            public void onSwipeLeft(){
                if(moveDirection != Direction.right ){
                    moveDirection = Direction.left;
                }
            }
            public void onSwipeRight(){
                if(moveDirection != Direction.left ){
                    moveDirection = Direction.right;
                }
            }
            public void onSwipeTop(){
                if(moveDirection != Direction.bottom ){
                    moveDirection = Direction.top;
                }
            }
        });
        fieldColor = getResources().getColor(R.color.game_field, getTheme());
        snakeColor = getResources().getColor(R.color.game_snake, getTheme());
        initField();
        newGame();

    }

    private void step(){
        if(!isPlaying) return;


        Vector2 head = snake.getFirst();
        Vector2 newHead = new Vector2(head.x, head.y);
        switch (moveDirection){
            case bottom: newHead.y += 1; break;
            case left: newHead.x -= 1; break;
            case right: newHead.x += 1; break;
            case top: newHead.y -= 1; break;
        }
        if(newHead.x < 0 || newHead.x >= FIELD_WIDTH ||
        newHead.y < 0 || newHead.y >= FIELD_HEIGHT){
            gameOver();
            return;
        }

        if(newHead.x == foodPosition.x && newHead.y == foodPosition.y){
            gameField[foodPosition.x][foodPosition.y].setText( "" );
            do{
                foodPosition = Vector2.random();
            }while(isCellInSnack(foodPosition));
            gameField[foodPosition.x][foodPosition.y].setText( food );

            score++;
            TextView scoreView = findViewById(R.id.textView_Score_result);
            scoreView.setText(String.valueOf(score));

            if( score != 0 && (score % 2) == 0 && snakeSpeed > 200){
                snakeSpeed = snakeSpeed - 30;
            }

            if( score != 0 && (score % 5) == 0 ){
                do{
                    pillPosition = Vector2.random();
                }while(isCellInSnack(pillPosition) && (!foodPosition.equals(pillPosition)));
                gameField[pillPosition.x][pillPosition.y].setText( pill );
                pillHandler.postDelayed(this::deletePill, 10000);
            }
        }
        else{
            Vector2 tail = snake.getLast();
            snake.remove(tail);
            gameField[tail.x][tail.y].setBackgroundColor(fieldColor);
        }

        if(newHead.equals(pillPosition)){
            Vector2 tail = snake.getLast();
            snake.remove(tail);
            gameField[tail.x][tail.y].setBackgroundColor(fieldColor);
            deletePill();
        }

        if(isCellInSnack(newHead)){
            gameOver();
        }

        snake.addFirst(newHead);
        gameField[newHead.x][newHead.y].setBackgroundColor(snakeColor);



        handler.postDelayed(this::step, snakeSpeed);
    }
    private void deletePill(){
        gameField[pillPosition.x][pillPosition.y].setText("");
    }
    private boolean isCellInSnack(Vector2 cell){
        for(Vector2 v : snake ){
            if(v.x == cell.x && v.y == cell.y) return true;
        }
        return false;
    }
    private void initField(){
        LinearLayout field = findViewById(R.id.game_field);

        LinearLayout.LayoutParams tvLayoutParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        tvLayoutParams.weight = 1f;
        tvLayoutParams.setMargins(4, 4,4,4);

        LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0
        );
        rowLayoutParams.weight = 1f;


        gameField = new TextView[FIELD_WIDTH][FIELD_HEIGHT];

        for(int j = 0; j<FIELD_HEIGHT; j++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams( rowLayoutParams );
            for(int i = 0; i<FIELD_WIDTH; i++) {

                TextView tv = new TextView(this);
                tv.setBackgroundColor(fieldColor);
                //tv.setText("0");
                tv.setLayoutParams(tvLayoutParams);
                row.addView(tv);
                gameField[i][j] = tv;
            }
            field.addView(row);
        }
    }
    private void newGame(){
        snakeSpeed = 700;
        for(Vector2 v : snake){
            gameField[v.x][v.y].setBackgroundColor( fieldColor );
        }
        snake.clear();
        if(foodPosition != null){
            gameField[foodPosition.x][foodPosition.y].setText( "" );
        }

        score = 0;
        snake.add( new Vector2(7, 10));
        snake.add( new Vector2(7, 11));
        snake.add( new Vector2(7, 12));
        snake.add( new Vector2(7, 13));
        snake.add( new Vector2(7, 14));
        for(Vector2 v : snake){
            gameField[v.x][v.y].setBackgroundColor(snakeColor);
        }
        foodPosition = new Vector2(3,14);
        gameField[foodPosition.x][foodPosition.y].setText( food );

        moveDirection = Direction.top;
        isPlaying = true;
        step();
    }
    private void gameOver(){
        isPlaying = false;

        new AlertDialog.Builder(this).setMessage("Game Over")
                .setMessage("Play one more time?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> newGame())
                .setNegativeButton("No", (dialog, which) -> finish())
                .show();
    }
    static class Vector2{
        int x;
        int y;

        public Vector2(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public static Vector2 random(){
            return new Vector2(_random.nextInt(FIELD_WIDTH), _random.nextInt(FIELD_HEIGHT));
        }

        @Override
        public boolean equals(Object obj){

            if (!(obj instanceof Vector2)) return false;
            Vector2 v = (Vector2)obj;
            return (v.x == this.x && v.y == this.y);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPlaying = false;
    }

    @Override
    protected void onResume() {

        super.onResume();
        if(!isPlaying){
            isPlaying = true;
            step();
        }

    }

    enum Direction{
        bottom,
        left,
        right,
        top

    }
}