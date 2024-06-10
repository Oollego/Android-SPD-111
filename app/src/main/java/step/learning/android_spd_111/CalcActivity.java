package step.learning.android_spd_111;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalcActivity extends AppCompatActivity {
    private TextView tvHistory;
    private Animation btnAnim;
    private Animation opacityAnim;
    private AnimationSet comboAnimation;
    private TextView tvResult;
    private String operationBuffer;
    private String historyFuncBuffer;
    private String historyBuffer;
    private char operation;
   private boolean isOperation = false;

    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvHistory = findViewById( R.id.calc_tv_history );
        tvResult = findViewById( R.id.calc_tv_result );
        if(savedInstanceState == null ){
            tvResult.setText("0");
        }
        for(int i = 0; i< 10; i++){
            findViewById(
                    getResources().getIdentifier(
                            "calc_btn_" + i,
                            "id",
                            getPackageName()
                    )
            ).setOnClickListener( this::onDigitButtonClick );
        }
        findViewById(R.id.calc_btn_inverse).setOnClickListener(this::onInverseClick);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::onBackspaceClick);
        findViewById(R.id.calc_btn_ce).setOnClickListener(this::onClearEntryClick);
        findViewById(R.id.calc_btn_c).setOnClickListener(this::onClearClick);
        findViewById(R.id.calc_btn_sqrt).setOnClickListener(this::onSqrtClick);
        findViewById(R.id.calc_btn_square).setOnClickListener(this::onSquareClick);
        findViewById(R.id.calc_btn_sign).setOnClickListener(this::onSignClick);
        findViewById(R.id.calc_btn_multiply).setOnClickListener(this::onMultiplyClick);
        findViewById(R.id.calc_btn_equal).setOnClickListener(this::onEqualsClick);
        findViewById(R.id.calc_btn_percent).setOnClickListener(this::onPercentClick);
        findViewById(R.id.calc_btn_divide).setOnClickListener(this::onDivideClick);
        findViewById(R.id.calc_btn_subtract).setOnClickListener(this::onSubtractClick);
        findViewById(R.id.calc_btn_add).setOnClickListener(this::onAddClick);
        findViewById(R.id.calc_btn_comma).setOnClickListener(this::onCommaClick);

        btnAnim = AnimationUtils.loadAnimation(this, R.anim.calc);
        opacityAnim = AnimationUtils.loadAnimation(this, R.anim.opacity_btn_calk);
        comboAnimation = new AnimationSet(false);
        comboAnimation.addAnimation(btnAnim);
        comboAnimation.addAnimation(opacityAnim);
        findViewById(R.id.calc_lay_result).setOnTouchListener(new OnSwipeListener(this){
            public void onSwipeRight(){
                onBackspaceClick(findViewById(R.id.calc_lay_result));
            }

        });

    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence( "tvResult", tvResult.getText() );
    }
    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText( savedInstanceState.getCharSequence("tvResult") );
    }

    private void onDigitButtonClick(View view){
        view.startAnimation(comboAnimation);
        String result = "";
        if(!isOperation){
            result = tvResult.getText().toString();
        }
        if(result.length() >= 13){
            Toast.makeText(this, R.string.calc_limit_exceeded, Toast.LENGTH_SHORT).show();
            return;
        }
        if(result.equals("0")){
            result = "";
        }
        result += ((Button) view).getText();
        tvResult.setText( result );
        isOperation = false;
    }
    private void onInverseClick(View view){
        view.startAnimation(comboAnimation);
        String result = tvResult.getText().toString();
        double x = Double.parseDouble(result);
        if( x == 0){
            Toast.makeText(this, R.string.calc_zero_division, Toast.LENGTH_SHORT).show();
            return;
        }
        x = 1.0 / x;
        String str = ( x == (int) x)? String.valueOf( (int) x) : String.valueOf(x);
        if(str.length() > 13 ){
            str = str.substring(0, 13);
        }
        tvResult.setText( str );
        if(operationBuffer == null){
            operationBuffer = result;
        }

        if(historyFuncBuffer == null){
            historyFuncBuffer = "1/("+operationBuffer+")";
        }
        else{
            historyFuncBuffer = "1/("+historyFuncBuffer+")";
        }
        String histStr;
        if(historyBuffer!=null){
            histStr = historyBuffer + historyFuncBuffer;
        }
        else {
            histStr = historyFuncBuffer;
        }
        tvHistory.setText(histStr);
    }
    private void onSignClick(View view){
        view.startAnimation(comboAnimation);
        String result = tvResult.getText().toString();
        if(result.equals("0")) return;

        double x = Double.parseDouble(result);
        x*= (-1);
        String str = ( x == (int) x)? String.valueOf( (int) x) : String.valueOf(x);
        tvResult.setText( str );
    }
    private void onSqrtClick(View view){
        view.startAnimation(comboAnimation);
        String result = tvResult.getText().toString();

        if(result.equals("0")) return;

        double x = Double.parseDouble(result);
        x = Math.sqrt(x);
        String str = ( x == (int) x)? String.valueOf( (int) x) : String.valueOf(x);
        if(str.length() > 13 ){
            str = str.substring(0, 13);
        }
        tvResult.setText( str );
        if(operationBuffer == null){
            operationBuffer = result;
        }
        if(historyFuncBuffer == null){
            historyFuncBuffer = "√" +"("+operationBuffer+")";
        }
        else{
            historyFuncBuffer = "√" +"("+historyFuncBuffer+")";
        }
        String histStr;
        if(historyBuffer!=null){
            histStr = historyBuffer + historyFuncBuffer;
        }
        else {
            histStr = historyFuncBuffer;
        }
        tvHistory.setText(histStr);
    }
    private void onSquareClick(View view){
        view.startAnimation(comboAnimation);
        String result = tvResult.getText().toString();

        if(result.equals("0")) return;

        double x = Double.parseDouble(result);
        x *= x;
        String str = ( x == (int) x)? String.valueOf( (int) x) : String.valueOf(x);
        if(str.length() > 13 ){
            str = str.substring(0, 13);
        }

        tvResult.setText( str );

        tvResult.setText( str );
        if(operationBuffer == null){
            operationBuffer = result;
        }
        if(historyFuncBuffer == null){
            historyFuncBuffer = "sqr("+operationBuffer+")";
        }
        else{
            historyFuncBuffer = "sqr("+historyFuncBuffer+")";
        }
        String histStr;
        if(historyBuffer!=null){
            histStr = historyBuffer + historyFuncBuffer;
        }
        else {
            histStr = historyFuncBuffer;
        }
        tvHistory.setText(histStr);

    }
    private void onBackspaceClick(View view){
        view.startAnimation(comboAnimation);
        String tvResultStr = tvResult.getText().toString();
        if(tvResultStr.equals("0")){
            return;
        }
        if(tvResultStr.length() > 1)
            tvResultStr = tvResultStr.substring( 0, tvResultStr.length() - 1);
        else{
            tvResultStr = "0";
        }
        tvResult.setText(( CharSequence )tvResultStr);

    }
    private void onClearClick(View view){
        view.startAnimation(comboAnimation);
        tvResult.setText("0");
        tvHistory.setText("");
        operationBuffer = null;
        historyFuncBuffer = null;
        historyBuffer = null;
        operation = '0';
    }
    private void onClearEntryClick(View view){
        view.startAnimation(comboAnimation);
        tvResult.setText("0");
        tvHistory.setText(historyBuffer);
        historyFuncBuffer = null;
    }

    private void onMultiplyClick(View view){
        String result = tvResult.getText().toString();
        view.startAnimation(comboAnimation);

        if(!isOperation && operationBuffer != null){
            double x = Double.parseDouble(result) * Double.parseDouble(operationBuffer) ;
            result = ( x == (int) x)? String.valueOf( (int) x) : String.valueOf(x);
        }
        operationBuffer = result;
        String resultTemp = result + " × ";

        isOperation = true;
        operation = '×';
        historyBuffer = resultTemp;
        tvHistory.setText(resultTemp);
        tvResult.setText(result);
    }
    private void onDivideClick(View view){
        String result = tvResult.getText().toString();
        view.startAnimation(comboAnimation);

        if(!isOperation && operationBuffer != null){
            double x;
            if(operationBuffer.equals("0")){
                 x = 0;
            }else{
                 x = Double.parseDouble(operationBuffer) / Double.parseDouble(result) ;
            }
             result = ( x == (int) x)? String.valueOf( (int) x) : String.valueOf(x);
        }
        if(result.length() > 13 ){
            result = result.substring(0, 13);
        }
        operationBuffer = result;
        operation = '÷';
        String resultTemp = result + " ÷ ";

        isOperation = true;
        historyBuffer = resultTemp;
        tvHistory.setText(resultTemp);
        tvResult.setText(result);
    }
    private void onAddClick(View view){
        String result = tvResult.getText().toString();
        view.startAnimation(comboAnimation);

        if(!isOperation && operationBuffer != null){
            double x = Double.parseDouble(result) + Double.parseDouble(operationBuffer) ;
            result = ( x == (int) x)? String.valueOf( (int) x) : String.valueOf(x);
            operationBuffer = result;

        }
        String resultTemp = result + " + ";

        isOperation = true;
        operation = '+';
        historyBuffer = resultTemp;
        tvHistory.setText(resultTemp);
        tvResult.setText(result);
    }
    private void onSubtractClick(View view){
        view.startAnimation(comboAnimation);

        String result = tvResult.getText().toString();
        String resultTemp;
        if(!isOperation && operationBuffer != null){
            double x =  Double.parseDouble(operationBuffer) - Double.parseDouble(result);
            result = ( x == (int) x)? String.valueOf( (int) x) : String.valueOf(x);


        }
        operationBuffer = result;
        resultTemp = operationBuffer + " - ";
        isOperation = true;
        operation = '-';
        historyBuffer = resultTemp;
        tvHistory.setText(resultTemp);
        tvResult.setText(result);
    }
    private void onEqualsClick(View view){
        String result;
        view.startAnimation(comboAnimation);

        switch(operation){
            case '×':
                result = tvResult.getText().toString();
                if(!isOperation && operationBuffer != null){
                    double x = Double.parseDouble(operationBuffer) * Double.parseDouble(result) ;
                    String res = ( x == (int) x)? String.valueOf( (int) x) : String.valueOf(x);
                    tvResult.setText(res);
                    //isOperation = true;
                    String tvHistoryEquals = operationBuffer + " × " + result + " =";
                    tvHistory.setText(tvHistoryEquals);
                }
                break;
            case '÷':
                result = tvResult.getText().toString();
                if(result.equals("0")){
                    tvResult.setText("Деление на ноль невозможно");
                    return;
                }
                if(!isOperation && operationBuffer != null){
                    double x;
                    if(operationBuffer.equals("0")){
                        x = 0;
                    }else{
                        x = Double.parseDouble(operationBuffer) / Double.parseDouble(result) ;
                    }

                    String res = ( x == (int) x)? String.valueOf( (int) x) : String.valueOf(x);
                    if(res.length() > 13 ){
                        res = res.substring(0, 13);
                    }
                    tvResult.setText(res);
                    String tvHistoryEquals = operationBuffer + " ÷ " + result + " =";
                    //String tvHistoryEquals = tvHistory.getText().toString() + result + " =";
                    tvHistory.setText(tvHistoryEquals);
                    isOperation = true;
                }
                break;
            case '+':
                result = tvResult.getText().toString();
                if(!isOperation && operationBuffer != null){
                    double x = Double.parseDouble(result) + Double.parseDouble(operationBuffer) ;
                    String res = ( x == (int) x)? String.valueOf( (int) x) : String.valueOf(x);
                    tvResult.setText(res);
                    //isOperation = true;
                    String tvHistoryEquals = operationBuffer + " + " + result + " =";
                    tvHistory.setText(tvHistoryEquals);
                }
                break;
            case '-':
                result = tvResult.getText().toString();
                if(!isOperation && operationBuffer != null){
                    double x = Double.parseDouble(operationBuffer) - Double.parseDouble(result);
                    String res = ( x == (int) x)? String.valueOf( (int) x) : String.valueOf(x);
                    tvResult.setText(res);
                    //isOperation = true;
                    String tvHistoryEquals = operationBuffer + " - " + result + " =";
                    tvHistory.setText(tvHistoryEquals);
                }
                break;
        }
    }
    private void onPercentClick(View view){
        view.startAnimation(comboAnimation);

        if(operationBuffer != null){
            String result = tvResult.getText().toString();
            double x = Double.parseDouble(result) / 100;
            String res =( x == (int) x)? String.valueOf( (int) x) : String.valueOf(x);
            tvResult.setText(res);
            String resultTemp = historyBuffer + res;
            tvHistory.setText(resultTemp);
        }
        else{
            tvResult.setText("0");
            tvHistory.setText("");
        }
    }
    private void onCommaClick(View view){
        view.startAnimation(comboAnimation);
        String result = tvResult.getText().toString();
        if(result.indexOf('.') == -1){
            String commaStr = result + ".";
            tvResult.setText(commaStr);
        }

    }

}