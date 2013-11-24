package edu.coen4720.bigarms;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FakeClient fc = new FakeClient();
		Scanner scanner = new Scanner(System.in);
		System.out.println("enter commands:");
		int currentSpeed = 15;
		boolean exit = false;
		
		while(!exit) {
			String inString = scanner.nextLine();
			
			switch(inString) {
				case "forward":
					//fc.sendMessage("MoveR100L100");
					fc.sendMessage("forward");
					break;
				case "backward":
					//fc.sendMessage("MoveR-100L-100");
					fc.sendMessage("backward");
					break;
				case "left":
					fc.sendMessage("left");
					break;
				case "right":
					fc.sendMessage("right");
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
				default:
					fc.sendMessage(inString);
					break;
			}
		}
	}
}
