/*
 * main.cpp
 *
 *  Created on: Sep 10, 2013
 *      Author: jacob
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <termios.h>
#include <string.h>
#include <textlcd.h>
#include "RobotMotors.h"

// Starts listening over the UART serial input to which Bluetooth is connected.
// Each command sent over Bluetooth is expected to be terminated by a newline
// character ('\n').
int main(int argc, char **argv)
{
	printf("Hello World!\n");
	CTextLcd &lcd = CTextLcd::GetRef();
	//RobotMotors motors;
	int keeprunning = 1;

	/*struct termios tio;
	struct termios stdio;
	int tty_fd;

	unsigned char c = 'D';

	memset(&stdio, 0, sizeof(stdio));
	stdio.c_iflag = 0;
	stdio.c_oflag = 0;
	stdio.c_cflag = 0;
	stdio.c_lflag = 0;
	stdio.c_cc[VMIN] = 1;
	stdio.c_cc[VTIME] = 0;
	tcsetattr(STDOUT_FILENO, TCSANOW, &stdio);
	tcsetattr(STDOUT_FILENO, TCSAFLUSH, &stdio);
	fcntl(STDIN_FILENO, F_SETFL, O_NONBLOCK);

	memset(&tio,0,sizeof(tio));
	tio.c_iflag = 0;
	tio.c_oflag = 0;
	tio.c_cflag = CS8 | CREAD | CLOCAL;
	tio.c_lflag = 0;
	tio.c_cc[VMIN] = 1;
	tio.c_cc[VTIME] = 5;

	tty_fd = open("/dev/ttyAM1", O_RDWR | O_NONBLOCK);
	cfsetospeed(&tio, B115200);
	cfsetispeed(&tio, B115200);

	tcsetattr(tty_fd, TCSANOW, &tio);
	char cmd[128];
	int pos = 0;*/

	lcd.printf("Hello World");
	//lcd.Clear();
	printf("Enter your commands:\n");
	char text[256];

	while (1 == keeprunning)
	{
		scanf("%s", text);
		printf("echo: %s\n",text);
		if(0 == strncmp("Exit",text,4))
		{
			printf("This should really Exit\n");
			keeprunning = 0;
			break;
		}
		/*if(0 == strncmp("Move_Forward",text,12))
		{
			// test moving forward
			motors.moveForward(10000);
			sleep(30);
			motors.stop();
		}*/
		memset(text,0,256);
		printf("end memset");
	    /*if (read(tty_fd, &c, 1) > 0) {
	        if (c == '\n') {
	        	lcd.Clear();
	        	cmd[pos] = '\0';
	        	pos = 0;
	        	printf("%s : %c\n", cmd, cmd[0]);
	        	lcd.printf("%s : %c\n", cmd, cmd[0]);
	        }
	        else {
	        	cmd[pos++] = c;
	        }
	    }*/

	}
	//close(tty_fd);
	CTextLcd::Release();
	printf("released");
	return 0;
}
