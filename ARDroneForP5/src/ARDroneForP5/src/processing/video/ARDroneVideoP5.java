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
package processing.video;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import processing.ardrone.ARDroneForP5;

import newardrone.video.ImageListener;
import newardrone.video.ReadRawFileImage;

public class ARDroneVideoP5 extends Thread{
	private static final int VIDEO_PORT=5555;

	private ARDroneForP5 control=null;
	private DatagramSocket videoSocket=null;
	private InetAddress inetaddr=null;
	
	private ReadRawFileImage rrfi=null;
	
	private ImageListener imageListener;
		
	public ARDroneVideoP5(ARDroneForP5 control, InetAddress inetaddr){
		this.control=control;
		this.inetaddr=inetaddr;
		rrfi=new ReadRawFileImage();
		initialize();
	}
	
	private void initialize(){
		try {
			videoSocket=new DatagramSocket(VIDEO_PORT);
			videoSocket.setSoTimeout(3000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		tickleVideoPort();
		enableVideoData();
		
		
        byte[] buf=new byte[153600];
        DatagramPacket packet=new DatagramPacket(buf, buf.length);
        BufferedImage image=null;
        while (true) {
        	try {
        		videoSocket.receive(packet);
        		image=rrfi.readUINT_RGBImage(buf);
        		if(imageListener!=null){
        			imageListener.imageUpdated(image);
        		}
        		//System.out.println("Video Received: "+packet.getLength()+" bytes");
        		//videoSocket.send(packet);//?
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        }
		
	}
	
	private void tickleVideoPort(){
        byte[] buf={0x01, 0x00, 0x00, 0x00};
        DatagramPacket packet=new DatagramPacket(buf, buf.length, inetaddr, VIDEO_PORT);
        try {
			videoSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void enableVideoData(){
		control.enableVideoData();
	}
		
	public void close(){
		videoSocket.close();
	}
	
	public void setImageListener(ImageListener imageListener){
		this.imageListener=imageListener;
	}
	
	public void removeImageListener(){
		this.imageListener=null;
	}
}