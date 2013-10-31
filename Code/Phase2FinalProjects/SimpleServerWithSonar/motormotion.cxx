/*
 * motormotion.cxx
 *
 *  Created on: Sep 7, 2013
 *      Author: jacob
 */
#include <stdio.h>
#include "qemotortraj.h"
#include <textlcd.h>
#include <time.h>
#include "RobotMotors.h"
#include <qegpioint.h>
#include <sonar.h>
#include <sonarcont.h>

#include <errno.h>
#include <sys/socket.h>
#include <resolv.h>
#include <arpa/inet.h>

#define MY_PORT		9923//9999
#define MAXBUF		1024
#define UDP			1

#define USPI 150
#define BIAS 300
#define CLIFF 3

#define ABS(x) (x > 0 ? x : -x)

RobotMotors motors;
CSonar *sonar = new CSonar(0, 1);

void bumpCallback(unsigned int i, struct timeval *stTimeval, void *userdata);
void touchCallback(unsigned int i, struct timeval *stTimeval, void *userdata);
void cSonarCallback(unsigned int port, int val);

int main(int argc, char **argv)
{
    CTextLcd &lcd = CTextLcd::GetRef();
    CQEGpioInt &io = CQEGpioInt::GetRef();
    CSonar *sonars[] = {sonar};
    CSonarController  *sonarC = new CSonarController(1, sonars, 50);
    // get motor reference
    int speed = 15000;
    bool isForwardCorrecting = false;
    bool isBackwardCorrecting = false;
    long prevLeftFrontPos = motors.pmotor->GetPosition(LEFT_FRONT);
    long prevLeftRearPos = motors.pmotor->GetPosition(LEFT_REAR);
    long prevRightFrontPos = motors.pmotor->GetPosition(RIGHT_FRONT);
    long prevRightRearPos = motors.pmotor->GetPosition(RIGHT_REAR);
//    long prevLeftFrontPos = 0;
//    long prevLeftRearPos = 0;
//    long prevRightFrontPos = 0;
//    long prevRightRearPos = 0;
    //void * test;

    printf("Hello World!\n");
    lcd.printf("Hello World!\n");

    int sockfd;
    struct sockaddr_in self;
   	char buffer[MAXBUF];
   	int ret = 0;

#ifdef UDP
   	/*---Create datagram socket---*/
   	if ( (sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0 )
   	    {
   		perror("Socket");
   		exit(errno);
   	}
#else
    /*---Create streaming socket---*/
    if ( (sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0 )
    {
    	perror("Socket");
    	exit(errno);
    }
#endif

    /*---Initialize address/port structure---*/
    //bzero(&self, sizeof(self));
    memset(&self,0,sizeof(self));
    self.sin_family = AF_INET;
    self.sin_port = htons(MY_PORT);
    self.sin_addr.s_addr = htonl(INADDR_ANY);

    /*---Assign a port number to the socket---*/
    if ( bind(sockfd, (struct sockaddr*)&self, sizeof(self)) != 0 )
    {
    	perror("socket--bind");
    	exit(errno);
    }

#ifndef UDP
    /*---Make it a "listening socket"---*/
    //check to see if this is the problem with recv stopping working
    if ( listen(sockfd, 20) != 0 )
    {
    	perror("socket--listen");
    	exit(errno);
    }
#endif

    printf("ready\n");

    //make connection loop
    int clientfd;
    struct sockaddr_in client_addr;
    socklen_t addrlen=sizeof(client_addr);

#ifndef UDP
    /*---accept a connection (creating a data pipe)---*/
    clientfd = accept(sockfd, (struct sockaddr*)&client_addr, &addrlen);
    printf("%s:%d connected\n", inet_ntoa(client_addr.sin_addr), ntohs(client_addr.sin_port));
#endif

    sonar->RegisterCallback(cSonarCallback);
    io.RegisterCallback(2, NULL, bumpCallback);
    io.RegisterCallback(3, NULL, bumpCallback);
    io.SetInterruptMode(2, QEG_INTERRUPT_NEGEDGE);
    io.SetInterruptMode(3, QEG_INTERRUPT_NEGEDGE);
    io.RegisterCallback(4, NULL, touchCallback);
    io.RegisterCallback(5, NULL, touchCallback);
    io.SetInterruptMode(4, QEG_INTERRUPT_NEGEDGE);
    io.SetInterruptMode(5, QEG_INTERRUPT_NEGEDGE);
    sonarC->SetFiring(1, sonars, 50);

	while (1)
	{
//		if(isForwardCorrecting || isBackwardCorrecting)
//		{
//			//printf("in while\n");
//			//begin movement correction algorithm:::
//			//printf("speed: %d, mLeft: %f, mRight: %f\n", speed, motors.getMultiplierLeft(), motors.getMultiplierRight());
//			long newLeftFrontPos = motors.pmotor->GetPosition(LEFT_FRONT);
//			long newLeftRearPos = motors.pmotor->GetPosition(LEFT_REAR);
//			long newRightFrontPos = motors.pmotor->GetPosition(RIGHT_FRONT);
//			long newRightRearPos = motors.pmotor->GetPosition(RIGHT_REAR);
//	//		long newLeftFrontPos = 0;
//	//		long newLeftRearPos = 0;
//	//		long newRightFrontPos = 0;
//	//		long newRightRearPos = 0;
//
//			long deltaLeft = (ABS(newLeftFrontPos-prevLeftFrontPos) + ABS(newLeftRearPos-prevLeftRearPos)) / 2;
//			long deltaRight = (ABS(prevRightFrontPos-newRightFrontPos) + ABS(prevRightRearPos-newRightRearPos)) / 2;
//			printf("deltaLeft: %ld, deltaRight: %ld, mLeft: %f, mRight: %f\n",
//					deltaLeft, deltaRight, motors.getMultiplierLeft(), motors.getMultiplierRight());
//
//			if(deltaLeft > deltaRight)
//			{
//				motors.setMultiplierLeft(motors.getMultiplierLeft() / 1.1);//- 0.1);
//				//motors.setMultiplierRight(motors.getMultiplierRight() + 0.1);
//				if(isForwardCorrecting) //reset speed
//					motors.moveForward(speed);
//				else
//					motors.moveBackward(speed);
//				sleep(10);
//			}
//			else if(deltaRight > deltaLeft)
//			{
//				motors.setMultiplierLeft(motors.getMultiplierLeft() * 1.1);// + 0.1);
//				//motors.setMultiplierRight(motors.getMultiplierRight() - 0.1   );
//				if(isForwardCorrecting) //reset speed
//					motors.moveForward(speed);
//				else
//					motors.moveBackward(speed);
//				sleep(10);
//			}
//
//			prevLeftFrontPos = newLeftFrontPos;
//			prevLeftRearPos = newLeftRearPos;
//			prevRightFrontPos = newRightFrontPos;
//			prevRightRearPos = newRightRearPos;
//		}
//		else {
//			prevLeftFrontPos = motors.pmotor->GetPosition(LEFT_FRONT);
//			prevLeftRearPos = motors.pmotor->GetPosition(LEFT_REAR);
//			prevRightFrontPos = motors.pmotor->GetPosition(RIGHT_FRONT);
//			prevRightRearPos = motors.pmotor->GetPosition(RIGHT_REAR);
//		}

#ifndef UDP
		ret = recv(clientfd, buffer, MAXBUF, 0);
#else
		ret = recvfrom(sockfd,buffer,MAXBUF,0,(struct sockaddr *)&client_addr,&addrlen);
#endif
		//printf("recv ret: %d\n", ret);
		if(ret <= 0)
		{
			continue;
		}
		printf("loop\n");
		//printf("%s\n",buffer);

		if(0 == strncmp("forward",buffer,7))
		{
			isForwardCorrecting = true;
			isBackwardCorrecting = true;
			motors.moveForward(speed);
		}
		else if(0 == strncmp("backward",buffer,8))
		{
			isBackwardCorrecting = true;
			isForwardCorrecting = false;
			motors.moveBackward(speed);
		}
		else if(0 == strncmp("right",buffer,5))
		{
			isBackwardCorrecting = false;
			isForwardCorrecting = false;
			motors.turnRight(speed/2);
			usleep(500000); //wait, and then store the new positions of the motors
		    prevLeftFrontPos = motors.pmotor->GetPosition(LEFT_FRONT);
		    prevLeftRearPos = motors.pmotor->GetPosition(LEFT_REAR);
		    prevRightFrontPos = motors.pmotor->GetPosition(RIGHT_FRONT);
		    prevRightRearPos = motors.pmotor->GetPosition(RIGHT_REAR);
		}
		else if(0 == strncmp("left",buffer,4))
		{
			isBackwardCorrecting = false;
			isForwardCorrecting = false;
			motors.turnLeft(speed/2);
			usleep(500000); //wait, and then store the new positions of the motors
		    prevLeftFrontPos = motors.pmotor->GetPosition(LEFT_FRONT);
		    prevLeftRearPos = motors.pmotor->GetPosition(LEFT_REAR);
		    prevRightFrontPos = motors.pmotor->GetPosition(RIGHT_FRONT);
		    prevRightRearPos = motors.pmotor->GetPosition(RIGHT_REAR);
		}
		else if(0 == strncmp("stop",buffer,4))
		{
			isBackwardCorrecting = false;
			isForwardCorrecting = false;
			motors.stop();
			usleep(500000); //wait, and then store the new positions of the motors
		    prevLeftFrontPos = motors.pmotor->GetPosition(LEFT_FRONT);
		    prevLeftRearPos = motors.pmotor->GetPosition(LEFT_REAR);
		    prevRightFrontPos = motors.pmotor->GetPosition(RIGHT_FRONT);
		    prevRightRearPos = motors.pmotor->GetPosition(RIGHT_REAR);
		}
		else if(0 == strncmp("speed",buffer,5))
		{
			//parse speed
			speed = 1000*atoi(&buffer[5]);
			printf("Speed = %d\n",speed);
		}
		else if(0 == strncmp("exit",buffer,4))
		{
			isBackwardCorrecting = false;
			isForwardCorrecting = false;
			break;
		}

		printf("%s\n",buffer);

		//zero out buffer
		memset(buffer, 0, MAXBUF);

		/*---Close data connection---*/
		//close(clientfd);

		usleep(100000); //TODO: remove this
	}
#ifndef UDP
	close(clientfd);
#endif
	/*---Clean up (should never get here!)---*/
	close(sockfd);

	CTextLcd::Release();
	return 0;
}

void bumpCallback(unsigned int i, struct timeval *stTimeval, void *usrdata)
{
	//printf("Bump interrupt on port %d\n", i);
	motors.stop();
}

void touchCallback(unsigned int i, struct timeval *stTimeval, void *usrdata)
{
	//printf("Touch interrupt on port %d\n", i);
	motors.stop();
}

void cSonarCallback(unsigned int port, int val)
{
	if(val > CLIFF)
	{
		  motors.stop();
	}
	//printf("port: %d, val: %d\n", port, val);

}
