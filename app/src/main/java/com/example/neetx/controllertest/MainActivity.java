package com.example.neetx.controllertest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(new MyView(this));
        ((MyApplication) this.getApplication()).setFinished(true);
        ((MyApplication) this.getApplication()).setChance(0);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /* CONNECTION DIALOG Todo: Make a button for this and catch user errors */
        LinearLayout ll = new LinearLayout(this);

        final EditText txtip = new EditText(this);
        final EditText txtport = new EditText(this);
        txtip.setText("192.168.43.57");
        txtport.setText("80");

        ll.addView(txtip);
        ll.addView(txtport);

        new AlertDialog.Builder(this)
                .setTitle("Target Setup")
                .setMessage("Please insert IP and PORT.")
                .setView(ll)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String ip = txtip.getText().toString();
                        String port = txtport.getText().toString();
                        Log.i("DIALOG", ip);
                        Log.i("DIALOG", port);
                        SocketAddress sockaddr = new InetSocketAddress(ip, Integer.parseInt(port));
                        Socket socket = new Socket();
                        MyView.senderObj.setConnection(sockaddr, socket);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
        /* CONNECTION BUTTON END*/
    }

    public static class MyView extends View
    {
        private static SocketAddress sockaddr = new InetSocketAddress("192.168.43.57", 80);
        private static Socket socket =  new Socket();
        public static Sender senderObj = new Sender(sockaddr, socket);
        Paint paint = null;
        private Context _context;

        Circle controller = new Circle();
        Circle shooter = new Circle();
        Circle analog = new Circle();
        Circle check = new Circle();

        List<Boolean> events = new ArrayList<Boolean>();
        long time = System.currentTimeMillis();

        public Canvas mcanvas = new Canvas();
        private float mPivotX = (float) analog.getCenterX();
        private float mPivotY = (float) analog.getCenterY();

        private Boolean start = true;

        public MyView(Context context)
        {
            super(context);
            paint = new Paint();
            this._context = context;
        }

        public void myDrawCircle(int x, int y) {
            this.mPivotX = x;
            this.mPivotY = y;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            mcanvas = canvas;
            super.onDraw(mcanvas);
            int x = getWidth();
            int y = getHeight();
            controller.setRadius(y/2);
            shooter.setRadius(y/6);
            analog.setRadius(controller.getRadius()/6);
            check.setRadius(y/10);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.DKGRAY);
            mcanvas.drawPaint(paint);

            paint.setColor(Color.parseColor("#CD5C5C"));

            controller.setCenterX((x-(x/4)));
            controller.setCenterY(y/2);

            shooter.setCenterX(x/4);
            shooter.setCenterY(y/2);

            analog.setCenterX(controller.getCenterX());
            analog.setCenterY(controller.getCenterY());

            check.setCenterX(x/11);
            check.setCenterY(y/7);

            mcanvas.drawCircle((float)controller.getCenterX(), (float)controller.getCenterY(), (float) controller.getRadius(), paint);
            mcanvas.drawCircle((float)shooter.getCenterX(),(float) shooter.getCenterY(), (float)shooter.getRadius(), paint);
            paint.setColor((Color.BLUE));
            mcanvas.drawCircle((float)check.getCenterX(), (float) check.getCenterY(), (float) check.getRadius(), paint);
            paint.setColor(Color.rgb(87,87,87) );
            if(start){
                mPivotX = (float) analog.getCenterX();
                mPivotY = (float) analog.getCenterY();
                start = false;
            }
            mcanvas.drawCircle(mPivotX, mPivotY, (float)analog.getRadius(), paint);
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            mcanvas.drawText("Shoot", (float) shooter.getCenterX()- (float) shooter.getRadius()/2, (float) shooter.getCenterY()+25, paint);
            paint.setTextSize(35);
            mcanvas.drawText("Check", (float) check.getCenterX()- (float) check.getRadius()/2, (float) check.getCenterY()+15, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            //Log.i("TIME", Long.toString(time));

            int action = MotionEventCompat.getActionMasked(event);
            int x = (int) event.getX();
            int y = (int) event.getY();

            //Log.i("DOWN ACTION","EVENT");

            switch (action) {
                case (MotionEvent.ACTION_DOWN):
                    if(controller.checkDistance(x, y)){
                        senderObj.sendCommand(controller.getDegrees(x,y), controller.getDistance(x,y));
                        this.myDrawCircle(x,y);
                    }else if(shooter.checkDistance(x, y)) {
                        //Log.i("DOWN_shooter", "SHOOT SIGNAL");
                        senderObj.sendShoot();
                    }else if(check.checkDistance(x, y)) {
                        //Log.i("VARIABLE", String.valueOf(((MyApplication) _context.getApplicationContext()).getFinished()));
                        if (((MyApplication) _context.getApplicationContext()).getFinished() || ((MyApplication) _context.getApplicationContext()).getChance() >= 1) {
                            ((MyApplication) _context.getApplicationContext()).setFinished(false);
                            ((MyApplication) _context.getApplicationContext()).setDelay(System.currentTimeMillis());
                            senderObj.sendCheck(this._context);
                        } else {
                            //Log.i("RESPONSE", "BLOCKED");
                        }
                    }
                    return true;
                case (MotionEvent.ACTION_MOVE):
                    if(controller.checkDistance(x, y)){
                        this.myDrawCircle(x,y);
                        if(System.currentTimeMillis() >= time+200) {
                            events.add(true);
                            senderObj.sendCommand(controller.getDegrees(x, y), controller.getDistance(x, y));
                            //Log.i("TIME", "SENDED");
                            time = System.currentTimeMillis();
                        }
                    }else if(shooter.checkDistance(x, y)) {
                        events.add(true);
                        senderObj.sendShoot();
                    } else {
                        if(events.contains(true)){
                            //Log.i("SIGNAL","STOP EXCEPTION");
                            this.myDrawCircle((int) controller.getCenterX(), (int) controller.getCenterY());
                            senderObj.sendStop();
                            events.clear();
                        }
                    }
                    return true;
                case (MotionEvent.ACTION_UP):
                    if(controller.checkDistance(x, y)){
                        senderObj.sendStop();
                        this.myDrawCircle((int) controller.getCenterX(),(int) controller.getCenterY());
                        //Log.i("SIGNAL","STOP SIGNAL");
                    }else if(shooter.checkDistance(x, y)) {
                        //Log.i("UP_shooter", "SHOOT SIGNAL");
                    }
                    return true;
                default:
                    return super.onTouchEvent(event);
            }
        }
    }
}

class Circle{

    private Point center = new Point();
    private double radius;

    Circle(){};

    Circle(double x, double y, double radius){
        this.center.setX(x);
        this.center.setY(y);
        this.radius = radius;
    }

    public void setRadius(double radius){
        this.radius = radius;
    }
    public double getRadius(){
        return this.radius;
    }

    public void setCenterX(double x){
        this.center.setX(x);
    }
    public double getCenterX(){
        return this.center.getX();
    }

    public void setCenterY(double y){
        this.center.setY(y);
    }
    public double getCenterY(){
        return this.center.getY();
    }

    boolean checkDistance(double x,double y) {

        double dx = Math.abs(x - this.center.getX());
        double dy = Math.abs(y - this.center.getY());
        //Log.i("CHECK dx",Double.toString(dx));
        //Log.i("CHECK dy",Double.toString(dy));
        //Log.i("CHECK radius", Double.toString(radius));
        //Log.i("CHECK dist", Double.toString(getDistance(x,y)));

        if (this.radius >= getDistance(x,y)){
            //Log.i("CHECK","OK");
            return true;
        } else {
            return false;
        }
    }

    double getDistance(double x, double y){
        double dx = Math.abs(x - this.center.getX());
        double dy = Math.abs(y - this.center.getY());
        return Math.sqrt(( Math.pow(dx,2) + Math.pow(dy,2)));
    }
    double getDegrees(double x, double y){
        return Math.PI/2 - (Math.toDegrees(Math.atan2(y-getCenterY(), x-getCenterX())));
    }
}

class Point{
    private double x;
    private double y;
    Point(){}
    public double getX(){
        return this.x;
    }
    public double getY(){
        return this.y;
    }
    public void setX(double x){
        this.x = x;
    }
    public void setY(double y){
        this.y = y;
    }
}

class Sender {
    private SocketAddress sockaddr;
    private Socket socket;

    Sender(SocketAddress sockaddr, Socket socket) {
        this.sockaddr = sockaddr;
        this.socket = socket;
    }

    void setConnection(SocketAddress sockaddr, Socket socket){
        this.sockaddr = sockaddr;
        this.socket = socket;
    }

    void sendShoot(){

        new SenderTask("{\"CMD\":\"Shoot\",\"Params\":\"null\",\"Resp\":\"false\"}", socket, sockaddr).execute();
    }

    void sendStop(){
        new SenderTask("{\"CMD\":\"Stop\",\"Params\":\"null\",\"Resp\":\"false\"}", socket, sockaddr).execute();
    }

    void sendCheck(Context _context){
       new SenderTask("{\"CMD\":\"Check\",\"Params\":\"null\",\"Resp\":\"true\"}", socket, sockaddr).execute();//.get(1000, java.util.concurrent.TimeUnit.MILLISECONDS);
       new ReaderTask(_context, socket, sockaddr).execute();//.get(3000, TimeUnit.MILLISECONDS);
    }

    void sendCommand(Double angle, Double radius){
        String str = "{\"CMD\":\"Move\",\"Params\":\"{'R':'"+String.format(Locale.getDefault(),"%.2f", radius)+"','A':'"+String.format(Locale.getDefault(),"%.2f", angle)+"'}\",\"Resp\":\"false\"}";
        //String str = "R"+String.format(Locale.getDefault(),"%.2f", radius)+"A"+String.format(Locale.getDefault(),"%.2f", angle);
        //Log.i("str",str);
        new SenderTask(str, socket, sockaddr).execute();
    }
}

class SenderTask extends AsyncTask<String, Void, String> {
    String param;
    Socket socket;
    SocketAddress sockaddr;
    SenderTask(String param, Socket socket, SocketAddress sockaddr){
        super();
        this.param = param;
        this.socket = socket;
        this.sockaddr = sockaddr;
    }
    @Override
    protected String doInBackground(String... params) {
        //Log.i("RESPONSE","STO QUA");
        String response = "";
        try {
            if(socket.isClosed()){
                socket.connect(sockaddr);
                Log.i("RESPONSE", "RIAPERTO");
            }
            if(!socket.isConnected()){
                socket.connect(sockaddr);
                Log.i("RESPONSE", "RICONNESSO");
            }
            OutputStream out = socket.getOutputStream();
            Log.i("JSON",param);
            param += "\r\n";
            out.write(param.getBytes());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }
        /*protected void onProgressUpdate(Integer... progress) {
        }
        */
        /*protected void onPostExecute(String result) {
            //Log.i("RESPONSE", result);
        }*/
}

class ReaderTask extends AsyncTask<Context, Void, Map<String,Object>> {
    Context context;
    Socket socket;
    SocketAddress sockaddr;
    InputStream in;

    ReaderTask(Context _context, Socket socket, SocketAddress sockaddr){
        super();
        this.context = _context;
        this.socket = socket;
        this.sockaddr = sockaddr;
    }
    @Override
    protected Map<String, Object> doInBackground(Context... params) {
        String response = "";
        try {
            if(socket.isClosed()){
                socket.connect(sockaddr);
                Log.i("RESPONSE", "RIAPERTO");

            }
            if(!socket.isConnected()){
                socket.connect(sockaddr);
                Log.i("RESPONSE", "RICONNESSO");
            }
            this.socket.setSoTimeout(200);

            int size = 100;
            ByteArrayOutputStream bout = new ByteArrayOutputStream(size);
            byte [] buffer = new byte[size];
            int bytesRead;
            bout.reset();

            InputStream in = socket.getInputStream();

            while((bytesRead = in.read(buffer,0,buffer.length)) != -1){
                bout.write(buffer, 0, bytesRead);
                Log.i("READ", String.valueOf(bytesRead));
                response += bout.toString("UTF-8");
                //if(String.valueOf ((char)buffer[size-1]) != null){
                if(response.endsWith("\n")){
                    Log.i("DEBUG", "DEBUG");
                    break;
                }else{
                    Log.i("DEBUG","NULL");
                }
            }



            if(isCancelled())cancel(true);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Response", response);
        map.put("Context", this.context);
        if(isCancelled())cancel(true);

        return map;
    }

    protected void onPostExecute(Map<String, Object> result) {
        //Log.i("RESPONSE", (String) result.get("Response"));
        if((String) result.get("Response") != ""){
            Log.i("JSON", "OKAY");
            long delay =  System.currentTimeMillis() - ((MyApplication)context.getApplicationContext()).getDelay();
            //Log.i("DELAY", String.valueOf(delay));
            try {
                JSONObject obj = new JSONObject(result.get("Response").toString());
                Toast toast = Toast.makeText((Context) result.get("Context"), (String) obj.getString("resp") + "Milliseconds: " + delay, Toast.LENGTH_LONG);
                toast.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ((MyApplication)context.getApplicationContext()).setDelay(0);
            ((MyApplication)context.getApplicationContext()).setFinished(true);
            ((MyApplication)context.getApplicationContext()).setChance(0);
        }else{
            Log.i("JSON", "NADA");
            // ((MyApplication)context.getApplicationContext()).setChance(((MyApplication)context.getApplicationContext()).getChance()+1);
            try {

                int size = 100;
                ByteArrayOutputStream bout = new ByteArrayOutputStream(size);
                byte [] buffer = new byte[size];
                int bytesRead;
                bout.reset();
                InputStream in = socket.getInputStream();
                String response = "";
                while((bytesRead = in.read(buffer)) != -1){
                    bout.write(buffer, 0, bytesRead);
                    response += bout.toString("UTF-8");
                    if(response.endsWith("\n")){
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            new SenderTask("{\"CMD\":\"Check\",\"Params\":\"null\",\"Resp\":\"true\"}", socket, sockaddr).execute();
            new ReaderTask(context, socket, sockaddr).execute();
        }
    }

}

