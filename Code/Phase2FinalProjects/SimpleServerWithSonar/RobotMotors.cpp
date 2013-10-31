/*
 * RobotMotors.cpp
 *
 *  Created on: Sep 7, 2013
 *      Author: jacob
 */

#include "RobotMotors.h"

RobotMotors::RobotMotors() {
	pmotor = CQEMotorTraj::GetPtr();
	pmotor->SetPIDVGains(LEFT_FRONT, 100, 0, 500, 0);
	pmotor->SetPIDVGains(RIGHT_FRONT, 100, 0, 500, 0);
	pmotor->SetPIDVGains(LEFT_REAR, 100, 0, 500, 0);
	pmotor->SetPIDVGains(RIGHT_REAR, 100, 0, 500, 0);
	mLeft = 1.0;
	mRight = 1.008;
}

RobotMotors::~RobotMotors() {
}

void RobotMotors::setMultiplierLeft(double m)
{
	mLeft = m;
}

void RobotMotors::setMultiplierRight(double m)
{
	mRight = m;
}

double RobotMotors::getMultiplierLeft()
{
	return mLeft;
}

double RobotMotors::getMultiplierRight()
{
	return mRight;
}

void RobotMotors::moveForward(int speed){
	pmotor->MoveVelocity(LEFT_FRONT,speed * mLeft,(speed*mLeft)/2);
	pmotor->MoveVelocity(RIGHT_FRONT,(-1*speed*mRight),(speed*mRight)/2);
	pmotor->MoveVelocity(LEFT_REAR,speed*mLeft,(speed*mLeft)/2);
	pmotor->MoveVelocity(RIGHT_REAR,(-1*speed*mRight),(speed*mRight)/2);
}

void RobotMotors::moveBackward(int speed){
	pmotor->MoveVelocity(LEFT_FRONT,(-1*speed/**mLeft*/),(speed/**mLeft*/)/2);
	pmotor->MoveVelocity(RIGHT_FRONT,speed*mRight,(speed*mRight)/2);
	pmotor->MoveVelocity(LEFT_REAR,(-1*speed/**mLeft*/),(speed/**mLeft*/)/2);
	pmotor->MoveVelocity(RIGHT_REAR,speed*mRight,(speed*mRight)/2);
}

void RobotMotors::turnRight(int speed){
	pmotor->MoveVelocity(LEFT_FRONT,speed,speed/2);
	pmotor->MoveVelocity(RIGHT_FRONT,speed,speed/2);
	pmotor->MoveVelocity(LEFT_REAR,speed,speed/2);
	pmotor->MoveVelocity(RIGHT_REAR,speed,speed/2);
	/*pmotor->MoveVelocity(LEFT_FRONT,speed,speed/2);
	pmotor->MoveVelocity(RIGHT_FRONT,0,speed/2);
	pmotor->MoveVelocity(LEFT_REAR,speed,speed/2);
	pmotor->MoveVelocity(RIGHT_REAR,0,speed/2);*/
}

void RobotMotors::turnLeft(int speed){
	pmotor->MoveVelocity(LEFT_FRONT,(-1*speed),speed/2);
	pmotor->MoveVelocity(RIGHT_FRONT,(-1*speed),speed/2);
	pmotor->MoveVelocity(LEFT_REAR,(-1*speed),speed/2);
	pmotor->MoveVelocity(RIGHT_REAR,(-1*speed),speed/2);
	/*pmotor->MoveVelocity(LEFT_FRONT,0,speed/2);
	pmotor->MoveVelocity(RIGHT_FRONT,(-1*speed),speed/2);
	pmotor->MoveVelocity(LEFT_REAR,0,speed/2);
	pmotor->MoveVelocity(RIGHT_REAR,(-1*speed),speed/2);*/
}

void RobotMotors::stop(){
	pmotor->Stop(LEFT_FRONT);
	pmotor->Stop(RIGHT_FRONT);
	pmotor->Stop(LEFT_REAR);
	pmotor->Stop(RIGHT_REAR);
}
