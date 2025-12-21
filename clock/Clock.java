package clock;

import java.awt.*;
import java.applet.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;

import myutil.*;

public class Clock extends Applet
{
	ClockCanvas cv;

	public static void main(String[] args) {
		new AppletFrame(new Clock(), 320, 340);
	}

	public void init()
	{
	    setLayout(new FlowLayout(FlowLayout.LEFT));
	    setBackground(Color.white);
	    cv = new ClockCanvas();
	    add(cv);
	    cv.setSize(300,300);
	    validate();
	}

	public void start()
	{
	    Thread clockThread = new Thread(() -> {
		while (true) {
		    try {
			Thread.sleep(1000);
		    } catch(InterruptedException ie){
			break;
		    }

		    Date now = new Date();
		    int hour = now.getHours();
		    int min  = now.getMinutes();
		    int sec  = now.getSeconds();

		    cv.setClock(hour, min, sec);
		}
	    });
	    clockThread.setDaemon(true);
	    clockThread.start();
	}
}

class ClockCanvas extends Canvas
{
	int hour, min, sec;

	public void setClock(int h, int m, int s)
	{
		hour = h;
		min  = m;
		sec  = s;
		repaint();
	}

	public void update (Graphics g)
	{
		paint(g);
	}

	public void paint (Graphics g)
	{
		int j;
		double r, theta, dhour, dmin, dsec;

		GL.ginit(g, getSize().width, getSize().height, this);
		GL.ortho2(-1.2,1.2,-1.2,1.2);
		GL.color(Color.red);
		GL.circf(0,0,1);
		GL.color(Color.white);
		GL.circf(0,0,0.9);
		for (j=0; j<12; j++) {
			r = 0.9;
			theta = Math.PI/2 - (2*Math.PI*j)/12;
			GL.move2( r*Math.cos(theta), r*Math.sin(theta) );
			GL.draw2(   Math.cos(theta),   Math.sin(theta) );
		}
		GL.color(Color.black);
		for (j=0; j<60; j++) {
			if (j%5 != 0) {
			    r = 0.94;
			    theta = Math.PI/2 - (2*Math.PI*j)/60;
			    GL.move2( r*Math.cos(theta), r*Math.sin(theta) );
			    GL.draw2(   Math.cos(theta),   Math.sin(theta) );
			}
		}

		r = 0.9;
		dsec = sec;
		theta = Math.PI/2 - (2*Math.PI*dsec)/60;
		GL.move2(0,0);
		GL.draw2( r*Math.cos(theta), r*Math.sin(theta) );

		GL.color(Color.green);
		r = 0.8;
		dmin = min + sec/60.;
		theta = Math.PI/2 - (2*Math.PI*dmin)/60;
		// GL.move2(0,0);
		// GL.draw2( r*Math.cos(theta), r*Math.sin(theta) );
		GL.pmv2(  0.1*Math.sin(theta), -0.1*Math.cos(theta) );
		GL.pdr2( -0.1*Math.sin(theta),  0.1*Math.cos(theta) );
		GL.pdr2(    r*Math.cos(theta),    r*Math.sin(theta) );
		GL.pclos();

		GL.color(Color.yellow);
		r = 0.4;
		dhour = hour + min/60. + sec/3600.;
		theta = Math.PI/2 - (2*Math.PI*dhour)/12;
		// GL.move2(0,0);
		// GL.draw2( r*Math.cos(theta), r*Math.sin(theta) );
		GL.pmv2(  0.1*Math.sin(theta), -0.1*Math.cos(theta) );
		GL.pdr2( -0.1*Math.sin(theta),  0.1*Math.cos(theta) );
		GL.pdr2(    r*Math.cos(theta),    r*Math.sin(theta) );
		GL.pclos();

		GL.swapbuffers();
	}
}
