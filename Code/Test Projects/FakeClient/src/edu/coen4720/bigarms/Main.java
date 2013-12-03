package edu.coen4720.bigarms;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		FakeClient fc = new FakeClient();
		Scanner scanner = new Scanner(System.in);
		System.out.println("enter commands:");
		int currentSpeed = 15;
		boolean exit = false;
		int divider = 22; //inside = 41
		int offset = 1750; //inside = 1700
		
		while(!exit) {
			String inString = scanner.nextLine();
			int sleep;
			
			switch(inString) {
				case "forward":
					fc.sendMessage("moveR100L100");
					//fc.sendMessage("forward");
					break;
				case "backward":
					fc.sendMessage("moveR-100L-100");
					//fc.sendMessage("backward");
					break;
				case "left":
					fc.sendMessage("moveR100L-100");
					//fc.sendMessage("left");
					break;
				case "right":
					fc.sendMessage("moveR-100L100");
					//fc.sendMessage("right");
					break;
				case "stop":
					fc.sendMessage("stop");
					break;
				case "speedup":
					currentSpeed++;
					if(currentSpeed < 10) {
						fc.sendMessage("speed0" + currentSpeed);
					}
					else {
						fc.sendMessage("speed" + currentSpeed);
					}
					break;
				case "speeddown":
					currentSpeed--;
					if(currentSpeed < 10) {
						fc.sendMessage("speed0" + currentSpeed);
					}
					else {
						fc.sendMessage("speed" + currentSpeed);
					}
					break;
				case "exit":
					exit = true;
					fc.sendMessage("exit");
					break;
				case "r90":
					sleep = offset + (1000*90)/divider;
					System.out.println("sleep = " + sleep);
					fc.sendMessage("moveR-100L100");
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					fc.sendMessage("stop");
					break;
				case "r360":
					sleep = offset + (1000*360)/divider;
					System.out.println("sleep = " + sleep);
					fc.sendMessage("moveR-100L100");
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					fc.sendMessage("stop");
					break;
				default:
					fc.sendMessage(inString);
					break;
			}
		}
		scanner.close();
	}
}
