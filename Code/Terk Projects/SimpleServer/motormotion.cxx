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

int main(int argc, char **argv)
{
    CTextLcd &lcd = CTextLcd::GetRef();
    // get motor reference
    //CQEMotorTraj &motor = CQEMotorTraj::GetRef();
    RobotMotors motors;

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

    /*---Create streaming socket---*/
    if ( (sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0 )
    {
    	perror("Socket");
    	exit(errno);
    }

    /*---Initialize address/port structure---*/
    bzero(&self, sizeof(self));
    self.sin_family = AF_INET;
    self.sin_port = htons(MY_PORT);
    self.sin_addr.s_addr = INADDR_ANY;

    /*---Assign a port number to the socket---*/
    if ( bind(sockfd, (struct sockaddr*)&self, sizeof(self)) != 0 )
    {
    	perror("socket--bind");
    	exit(errno);
    }

    /*---Make it a "listening socket"---*/
    if ( listen(sockfd, 20) != 0 )
    {
    	perror("socket--listen");
    	exit(errno);
    }

    printf("ready\n");

    //make connection loop
    int clientfd;
    struct sockaddr_in client_addr;
    socklen_t addrlen=sizeof(client_addr);

    /*---accept a connection (creating a data pipe)---*/
    clientfd = accept(sockfd, (struct sockaddr*)&client_addr, &addrlen);
    printf("%s:%d connected\n", inet_ntoa(client_addr.sin_addr), ntohs(client_addr.sin_port));

    /*---Forever... ---*/
    //message loop
    while (1)
    {
    	printf("loop\n");
    	//int clientfd;
    	//struct sockaddr_in client_addr;
    	//socklen_t addrlen=sizeof(client_addr);

    	/*---accept a connection (creating a data pipe)---*/
    	//clientfd = accept(sockfd, (struct sockaddr*)&client_addr, &addrlen);
    	//printf("%s:%d connected\n", inet_ntoa(client_addr.sin_addr), ntohs(client_addr.sin_port));

    	/*---Echo back anything sent---*/
    	//send(clientfd, buffer, recv(clientfd, buffer, MAXBUF, 0), 0);
    	printf("%s",buffer);

    	if(0 == strncmp("Forward",buffer,7))
    	{
    		motors.moveForward(10000);
    	}
    	else if(0 == strncmp("Stop",buffer,4))
    	{
    		motors.stop();
    	}
    	else if(0 == strncmp("Exit",buffer,4))
    	{
    		break;
    	}

    	/*---Close data connection---*/
    	//close(clientfd);
    }
    close(clientfd);

    /*---Clean up (should never get here!)---*/
    close(sockfd);

    CTextLcd::Release();
    return 0;
}
