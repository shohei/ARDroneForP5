/*
 * Copyright (c) <2011>, <Shigeo Yoshida>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
The names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import newardrone.ARDroneCtrl;
import newardrone.navdata.DroneState;
import newardrone.navdata.listener.AttitudeListener;
import newardrone.navdata.listener.BatteryListener;
import newardrone.navdata.listener.StateListener;
import newardrone.navdata.listener.VelocityListener;
import newardrone.video.ImageListener;

/**
 * sample program for Java application
 * @author shigeo
 *
 */
public class ARDroneTest extends JFrame{
	
	private static final long serialVersionUID = 1L;

	private ARDroneCtrl ardrone=null;
	private boolean shiftflag=false;
	
	private MyPanel myPanel;

	//private int num=228;
	
	public ARDroneTest(){
		initialize();
	}
	
	private void initialize(){
		ardrone=new ARDroneCtrl();
		System.out.println("connect drone controller");
		ardrone.connect("192.168.1.1");
		System.out.println("connect drone navdata");
		//ardrone.connectNav();
		System.out.println("connect drone video");
		ardrone.connectVideo();
		System.out.println("start drone");
		ardrone.start();
		
		this.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				int key=e.getKeyCode();
				
				switch(key){
				case KeyEvent.VK_SHIFT:
					shiftflag=true;
				case KeyEvent.VK_ENTER:
					ardrone.takeOff();
					break;
				case KeyEvent.VK_SPACE:
					ardrone.landing();
					break;
				case KeyEvent.VK_S:
					ardrone.stop();
					break;
				case KeyEvent.VK_LEFT:
					if(shiftflag){
						ardrone.spinLeft();
						shiftflag=false;
					}else
						ardrone.goLeft();
					break;
				case KeyEvent.VK_RIGHT:
					if(shiftflag){
						ardrone.spinRight();
						shiftflag=false;
					}else
						ardrone.goRight();
					break;
				case KeyEvent.VK_UP:
					if(shiftflag){
						ardrone.up();
						shiftflag=false;
					}else
						ardrone.forward();
					break;
				case KeyEvent.VK_DOWN:
					if(shiftflag){
						ardrone.down();
						shiftflag=false;
					}else
						ardrone.backward();
					break;
				case KeyEvent.VK_P:
					//ardrone.printARDroneInfo();
					break;
				case KeyEvent.VK_1://hori
					ardrone.setFrontCameraStreaming();
					break;
				case KeyEvent.VK_2://large hori, small vert
					ardrone.setFrontCameraWithSmallBellyStreaming();
					break;
				case KeyEvent.VK_3://large vert, small hori
					ardrone.setBellyCameraWithSmallFrontStreaming();
					break;
				case KeyEvent.VK_4://vert
					ardrone.setBellyCameraStreaming();
					break;
				case KeyEvent.VK_5://switch camera
					ardrone.setNextCamera();
					break;
				/*case KeyEvent.VK_U:
					ardrone.setCamera(++num);
					System.out.println(num);
					break;
				case KeyEvent.VK_D:
					ardrone.setCamera(--num);
					System.out.println(num);
					break;*/
					
				}
			}
		});
		
		ardrone.addImageUpdateListener(new ImageListener(){
			@Override
			public void imageUpdated(BufferedImage image) {
				if(myPanel!=null){
					myPanel.setImage(image);
					myPanel.repaint();
				}
			}
		});
		
		ardrone.addAttitudeUpdateListener(new AttitudeListener() {
			@Override
			public void attitudeUpdated(float pitch, float roll, float yaw, int altitude) {
				System.out.println("pitch: "+pitch+", roll: "+roll+", yaw: "+yaw+", altitude: "+altitude);
			}
		});
		
		ardrone.addBatteryUpdateListener(new BatteryListener() {
			@Override
			public void batteryLevelChanged(int percentage) {
				System.out.println("battery: "+percentage+" %");
			}
		});
				
		ardrone.addStateUpdateListener(new StateListener() {
			@Override
			public void stateChanged(DroneState state) {
				System.out.println("state: "+state);
			}
		});
		
		ardrone.addVelocityUpdateListener(new VelocityListener() {
			@Override
			public void velocityChanged(float vx, float vy, float vz) {
				System.out.println("vx: "+vx+", vy: "+vy+", vz: "+vz);
			}
		});
		
		this.setTitle("ardrone");
		this.setSize(400, 400);
		this.add(getMyPanel());
	}
	
	
	private JPanel getMyPanel(){
		if(myPanel==null){
			myPanel=new MyPanel();
		}
		return myPanel;
	}
	
	/**
	 * 描画用のパネル
	 * @author shigeo
	 *
	 */
	private class MyPanel extends JPanel{
		private static final long serialVersionUID = -7635284252404123776L;

		/** ardrone video image */
		private BufferedImage image=null;
		
		public MyPanel(){
		}
		
		public void setImage(BufferedImage image){
			this.image=image;
		}
		
		public void paint(Graphics g){
			g.setColor(Color.white);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			if(image!=null)
				g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
		}
	}
	
	public static void main(String args[]){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				final ARDroneTest thisClass=new ARDroneTest();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.addWindowListener(new WindowAdapter(){
					@Override
					public void windowOpened(WindowEvent e) {
						System.out.println("WindowOpened");
					}
					@Override
					public void windowClosing(WindowEvent e) {
						thisClass.dispose();
					}
				});
				thisClass.setVisible(true);
			}
		});
	}
}