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

#include <errno.h>
#include <sys/socket.h>
#include <resolv.h>
#include <arpa/inet.h>

#define MY_PORT		9923//9999
#define MAXBUF		1024
#define UDP			1

int main(int argc, char **argv)
{
    CTextLcd &lcd = CTextLcd::GetRef();
    // get motor reference
    //CQEMotorTraj &motor = CQEMotorTraj::GetRef();
    RobotMotors motors;
    int speed = 10000;

    printf("Hello World!");
    lcd.printf("Hello World!");
    //sleep is in seconds, so this will display Hello World for 2 seconds before moving
    //sleep(2);

    //lcd.Clear();
    //lcd.printf("Moving Forward");

    // test moving forward
    //motors.moveForward(10000);
    //sleep(30);

    //test moving backward
    //motors.moveBackward(10000);
    //sleep(30);

    //test turning right
    //motors.turnRight(7500);
    //sleep(15);

    //test turning left
    //motors.turnLeft(7500);
    //sleep(5);

    //motors.stop();

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

    /*---Forever... ---*/
    //message loop
    while (1)
    {
    	//printf("loop\n");
    	//int clientfd;
    	//struct sockaddr_in client_addr;
    	//socklen_t addrlen=sizeof(client_addr);

    	/*---accept a connection (creating a data pipe)---*/
    	//clientfd = accept(sockfd, (struct sockaddr*)&client_addr, &addrlen);
    	//printf("%s:%d connected\n", inet_ntoa(client_addr.sin_addr), ntohs(client_addr.sin_port));

    	/*---Echo back anything sent---*/
    	//send(clientfd, buffer, recv(clientfd, buffer, MAXBUF, 0), 0);
#ifndef UDP
    	ret = recv(clientfd, buffer, MAXBUF, 0);
#else
    	ret = recvfrom(sockfd,buffer,MAXBUF,0,(struct sockaddr *)&client_addr,&addrlen);
#endif
    	printf("recv ret: %d\n", ret);
    	if(ret <= 0)
    	{
    		continue;
    	}
    	printf("loop\n");
    	//printf("%s\n",buffer);

    	if(0 == strncmp("forward",buffer,7))
    	{
    		motors.moveForward(speed);
    	}
    	else if(0 == strncmp("backward",buffer,8))
    	{
    		motors.moveBackward(speed);
    	}
    	else if(0 == strncmp("right",buffer,5))
    	{
    		motors.turnRight(speed/2);
    	}
    	else if(0 == strncmp("left",buffer,4))
    	{
    		motors.turnLeft(speed/2);
    	}
    	else if(0 == strncmp("stop",buffer,4))
    	{
    		motors.stop();
    	}
    	else if(0 == strncmp("speed",buffer,5))
    	{
    		//parse speed
    		speed = 1000*atoi(&buffer[5]);
    		printf("Speed = %d\n",speed);
    	}
    	else if(0 == strncmp("exit",buffer,4))
    	{
    		break;
    	}

    	printf("%s\n",buffer);

    	//zero out buffer
    	memset(buffer, 0, MAXBUF);

    	/*---Close data connection---*/
    	//close(clientfd);
    }
#ifndef UDP
    close(clientfd);
#endif
    /*---Clean up (should never get here!)---*/
    close(sockfd);

    CTextLcd::Release();
    return 0;
}
