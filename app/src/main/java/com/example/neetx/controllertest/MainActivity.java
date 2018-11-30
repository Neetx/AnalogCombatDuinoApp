package com.example.neetx.controllertest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(new MyView(this));
    }

    public class MyView extends View
    {
        final SocketAddress sockaddr = new InetSocketAddress("192.168.43.57", 80);
        final Socket socket = new Socket();
        Sender senderObj = new Sender(sockaddr, socket);
        Paint paint = null;
        Circle controller = new Circle();
        Circle shooter = new Circle();
        List<Boolean> events = new ArrayList<Boolean>();

        public MyView(Context context)
        {
            super(context);
            paint = new Paint();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);
            int x = getWidth();
            int y = getHeight();
            controller.setRadius(getWidth()/3);
            shooter.setRadius(getWidth()/6);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.DKGRAY);
            canvas.drawPaint(paint);
            // Use Color.parseColor to define HTML colors
            paint.setColor(Color.parseColor("#CD5C5C"));

            controller.setCenterX(x/2);
            controller.setCenterY(y/4);

            shooter.setCenterX(x/2);
            shooter.setCenterY(y-(y/3));

            canvas.drawCircle((float)controller.getCenterX(), (float)controller.getCenterY(), controller.getRadius(), paint);
            canvas.drawCircle((float)shooter.getCenterX(),(float) shooter.getCenterY(), shooter.getRadius(), paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            int action = MotionEventCompat.getActionMasked(event);
            int x = (int) event.getX();
            int y = (int) event.getY();
            double distance;
            Log.d("DOWN ACTION","EVENT");

            switch (action) {
                case (MotionEvent.ACTION_DOWN):
                    if(controller.checkDistance(x, y)){
                        senderObj.sendCommand(controller.getDegrees(x,y), controller.getDistance(x,y));
                    }else if(shooter.checkDistance(x, y)) {
                        Log.d("DOWN_shooter", "SHOOT SIGNAL");
                        senderObj.sendShoot();
                    }
                    return true;
                case (MotionEvent.ACTION_MOVE):
                    if(controller.checkDistance(x, y)){
                        events.add(true);
                        senderObj.sendCommand(controller.getDegrees(x,y), controller.getDistance(x,y));
                    }else if(shooter.checkDistance(x, y)) {
                        events.add(true);
                        senderObj.sendShoot();
                    } else {
                        if(events.contains(true)){
                            Log.d("SIGNAL","STOP EXCEPTION");
                            senderObj.sendStop();
                            events.clear();
                        }
                    }
                    return true;
                case (MotionEvent.ACTION_UP):
                    if(controller.checkDistance(x, y)){
                        senderObj.sendStop();
                        Log.d("SIGNAL","STOP SIGNAL");
                    }else if(shooter.checkDistance(x, y)) {
                        Log.d("UP_shooter", "SHOOT SIGNAL");
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
    private int radius;

    Circle(){};

    Circle(int x, int y, int radius){
        this.center.setX(x);
        this.center.setY(y);
        this.radius = radius;
    }

    public void setRadius(int radius){
        this.radius = radius;
    }
    public int getRadius(){
        return this.radius;
    }

    public void setCenterX(int x){
        this.center.setX(x);
    }
    public double getCenterX(){
        return this.center.getX();
    }

    public void setCenterY(int y){
        this.center.setY(y);
    }
    public double getCenterY(){
        return this.center.getY();
    }

    boolean checkDistance(double x,double y) {

        double dx = Math.abs(x - this.center.getX());
        double dy = Math.abs(y - this.center.getY());

        if (dx > this.radius) {
            return false;
        } else if (dy > this.radius) {
            return false;
        } else {
            return true;
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
    private String lastcommand ="";

    Sender(SocketAddress sockaddr, Socket socket) {
        this.sockaddr = sockaddr;
        this.socket = socket;
    }

    void sendShoot(){ new SenderTask("Shoot", socket, sockaddr).execute();}

    void sendStop(){
        new SenderTask("STOP", socket, sockaddr).execute();
    }

    void sendCommand(Double angle, Double radius){
        String str = "";
        if(angle>=45 && angle<=135){
            //Todo: UP
            str="On";
            if(!str.equals(lastcommand)){
                Log.d("SIGNAL", "STOP DIFFERENT COMMAND");
                new SenderTask("STOP", socket, sockaddr).execute();
            }
            lastcommand=str;
        }else if(angle<45 && angle>-45){
            //Todo: RIGHT
            str="Right";
            if(!str.equals(lastcommand)){
                Log.d("SIGNAL", "STOP DIFFERENT COMMAND");
                new SenderTask("STOP", socket, sockaddr).execute();
            }
            lastcommand=str;
        }else if(angle>135 || angle<-135){
            //Todo: LEFT
            str="Left";
            if(!str.equals(lastcommand)){
                Log.d("SIGNAL", "STOP DIFFERENT COMMAND");
                new SenderTask("STOP", socket, sockaddr).execute();
            }
            lastcommand=str;
        }else if(angle>=-135 && angle<=-45){
            //Todo: BEHIND
            str="Behind";
            if(!str.equals(lastcommand)){
                Log.d("SIGNAL", "STOP DIFFERENT COMMAND");
                new SenderTask("STOP", socket, sockaddr).execute();
            }
            lastcommand=str;
        }
        if(!str.equals("")){
            Log.d("SIGNAL", str);
            new SenderTask(str, socket, sockaddr).execute();
        }
    }
}

class SenderTask extends AsyncTask<String, Void, Void> {
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
    protected Void doInBackground(String... params) {
        try {
            if(!socket.isConnected()){
                socket.connect(sockaddr);
            }
            OutputStream out = socket.getOutputStream();
            param += "\r\n";
            out.write(param.getBytes());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

        /*protected void onProgressUpdate(Integer... progress) {
        }
        protected void onPostExecute(Long result) {
        }*/
}