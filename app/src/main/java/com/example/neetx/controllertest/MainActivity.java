package com.example.neetx.controllertest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
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
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MyView(this));
        long time = System.currentTimeMillis();
    }

    public class MyView extends View
    {
        final SocketAddress sockaddr = new InetSocketAddress("192.168.43.57", 80);
        final Socket socket = new Socket();
        Sender senderObj = new Sender(sockaddr, socket);
        Paint paint = null;


        Circle controller = new Circle();
        Circle shooter = new Circle();
        Circle analog = new Circle();

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

            mcanvas.drawCircle((float)controller.getCenterX(), (float)controller.getCenterY(), (float) controller.getRadius(), paint);
            mcanvas.drawCircle((float)shooter.getCenterX(),(float) shooter.getCenterY(), (float)shooter.getRadius(), paint);
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
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            Log.i("TIME", Long.toString(time));

            int action = MotionEventCompat.getActionMasked(event);
            int x = (int) event.getX();
            int y = (int) event.getY();

            Log.i("DOWN ACTION","EVENT");

            switch (action) {
                case (MotionEvent.ACTION_DOWN):
                    if(controller.checkDistance(x, y)){
                        senderObj.sendCommand(controller.getDegrees(x,y), controller.getDistance(x,y));
                        this.myDrawCircle(x,y);
                    }else if(shooter.checkDistance(x, y)) {
                        Log.i("DOWN_shooter", "SHOOT SIGNAL");
                        senderObj.sendShoot();
                    }
                    return true;
                case (MotionEvent.ACTION_MOVE):
                    if(controller.checkDistance(x, y)){
                        this.myDrawCircle(x,y);
                        if(System.currentTimeMillis() >= time+200) {
                            events.add(true);
                            senderObj.sendCommand(controller.getDegrees(x, y), controller.getDistance(x, y));
                            Log.i("TIME", "SENDED");
                            time = System.currentTimeMillis();
                        }
                    }else if(shooter.checkDistance(x, y)) {
                        events.add(true);
                        senderObj.sendShoot();
                    } else {
                        if(events.contains(true)){
                            Log.i("SIGNAL","STOP EXCEPTION");
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
                        Log.i("SIGNAL","STOP SIGNAL");
                    }else if(shooter.checkDistance(x, y)) {
                        Log.i("UP_shooter", "SHOOT SIGNAL");
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
        Log.i("CHECK dx",Double.toString(dx));
        Log.i("CHECK dy",Double.toString(dy));
        Log.i("CHECK radius", Double.toString(radius));
        Log.i("CHECK dist", Double.toString(getDistance(x,y)));

        if (this.radius >= getDistance(x,y)){
            Log.i("CHECK","OK");
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
        String str = "R"+String.format(Locale.getDefault(),"%.2f", radius)+"A"+String.format(Locale.getDefault(),"%.2f", angle);
        Log.i("str",str);
        new SenderTask(str, socket, sockaddr).execute();
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