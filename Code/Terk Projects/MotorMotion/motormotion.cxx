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

int main(int argc, char **argv)
{
    CTextLcd &lcd = CTextLcd::GetRef();
    // get motor reference
    CQEMotorTraj &motor = CQEMotorTraj::GetRef();
    RobotMotors motors;

    printf("Hello World!");
    lcd.printf("Hello World!");
    //sleep is in seconds, so this will display Hello World for 2 seconds before moving
    sleep(2);

    lcd.Clear();
    lcd.printf("Moving Forward");

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

    motors.stop();

    CTextLcd::Release();
    return 0;
}
