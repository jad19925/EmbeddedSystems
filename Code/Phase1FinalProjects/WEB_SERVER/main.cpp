/*
 * main.cpp
 *
 *  Created on: Sep 17, 2013
 *      Author: Alex
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <termios.h>
#include <string.h>
#include <textlcd.h>
#include <iostream.h>
#include <fstream.h>
#include "RobotMotors.h"
#include <qegpioint.h>
RobotMotors motors;

void backUpAndStop()
{
	motors.stop();
	sleep(0.5);
	motors.moveBackward(10000);
	sleep(2);
	motors.stop();
}
void forwardAndStop()
{
	motors.stop();
	sleep(0.5);
	motors.moveForward(10000);
	sleep(2);
	motors.stop();
}

int main(int argc, char **argv)
{
	CTextLcd &lcd = CTextLcd::GetRef();
	CQEGpioInt &io = CQEGpioInt::GetRef(); // for reading digital pins
	int keeprunning = 1;

	lcd.printf("ALEX_SSH");
	//lcd.Clear();
	printf("Enter your commands:\n");
	char command[10];
	char prevcommand[10];
	memset(command,0,10);

	while (1 == keeprunning)
	{
		ifstream commandFile;
		commandFile.open("/srv/www/cgi-bin/command.txt");
		commandFile >> command;
		printf("echo: %s\n", command);
		lcd.Clear();
		lcd.printf("%s", command);
		commandFile.close();

		if(0 != strncmp(command, prevcommand, 9))
		{
			if(0 == strncmp("Exit",command,4))
			{
				printf("This should really Exit\n");
				keeprunning = 0;
				break;
			}
			if(0 == strncmp("forward",command,7))
			{
				motors.moveForward(8000);
			}
			else if(0 == strncmp("backward",command,8))
			{
				motors.moveBackward(8000);
			}
			else if(0 == strncmp("left",command,4))
			{
				motors.turnLeft(8000);
			}
			else if(0 == strncmp("right",command,5))
			{
				motors.turnRight(8000);
			}
			else if(0 == strncmp("stop",command,4))
			{
				motors.stop();
			}
		}

		bool bump1IsOn = !(io.GetData() & 0x0001); //bump sensor 1 connected to digital port 1
		bool bump2IsOn = !(io.GetData() & 0x0002); //bump sensor 2

		if(bump1IsOn || bump2IsOn)
		{
			motors.stop();
			lcd.Clear();
			lcd.printf("BUMP");
		}

		/*while(1)
		{
			//printf("checking bump\n");
			bool bump1IsOn = !(io.GetData() & 0x0001); //bump sensor 1 connected to digital port 1
			bool bump2IsOn = !(io.GetData() & 0x0002); //bump sensor 2
			if(bump1IsOn)
			{
				printf("bump1\n");
				backUpAndStop();
				break;
			}
			else if(bump2IsOn)
			{
				printf("bump2\n");
				forwardAndStop();
				break;
			}
			sleep(0.05);
		}*/
		/*printf("%d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d",
				io.GetData() & 0x0000,
				io.GetData() & 0x0001,
				io.GetData() & 0x0002,
				io.GetData() & 0x0004,
				io.GetData() & 0x0008,
				io.GetData() & 0x0010,
				io.GetData() & 0x0020,
				io.GetData() & 0x0040,
				io.GetData() & 0x0080,
				io.GetData() & 0x0100,
				io.GetData() & 0x0200,
				io.GetData() & 0x0400,
				io.GetData() & 0x0800,
				io.GetData() & 0x1000,
				io.GetData() & 0x2000,
				io.GetData() & 0x4000,
				io.GetData() & 0x8000);*/
		//sleep(3);
		//motors.stop();
		strncpy(prevcommand, command, 9);
		sleep(0.1);
		printf("end\n");
		memset(command,0,10);

	}
	CTextLcd::Release();
	printf("released");
	return 0;
}
