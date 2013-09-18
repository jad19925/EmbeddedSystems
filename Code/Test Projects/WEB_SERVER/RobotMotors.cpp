/*
 * RobotMotors.cpp
 *
 *  Created on: Sep 7, 2013
 *      Author: jacob
 */

#include "RobotMotors.h"

RobotMotors::RobotMotors() {
	motor = CQEMotorTraj::GetPtr();
	motor->SetPIDVGains(LEFT_FRONT, 100, 0, 500, 0);
	motor->SetPIDVGains(RIGHT_FRONT, 100, 0, 500, 0);
	motor->SetPIDVGains(LEFT_REAR, 100, 0, 500, 0);
	motor->SetPIDVGains(RIGHT_REAR, 100, 0, 500, 0);
}

RobotMotors::~RobotMotors() {

}

void RobotMotors::moveForward(int speed){
	motor->MoveVelocity(LEFT_FRONT,speed,speed/2);
	motor->MoveVelocity(RIGHT_FRONT,(-1*speed),speed/2);
	motor->MoveVelocity(LEFT_REAR,speed,speed/2);
	motor->MoveVelocity(RIGHT_REAR,(-1*speed),speed/2);
}

void RobotMotors::moveBackward(int speed){
	motor->MoveVelocity(LEFT_FRONT,(-1*speed),speed/2);
	motor->MoveVelocity(RIGHT_FRONT,speed,speed/2);
	motor->MoveVelocity(LEFT_REAR,(-1*speed),speed/2);
	motor->MoveVelocity(RIGHT_REAR,speed,speed/2);
}

void RobotMotors::turnRight(int speed){
	motor->MoveVelocity(LEFT_FRONT,speed,speed/2);
	motor->MoveVelocity(RIGHT_FRONT,speed,speed/2);
	motor->MoveVelocity(LEFT_REAR,speed,speed/2);
	motor->MoveVelocity(RIGHT_REAR,speed,speed/2);
}

void RobotMotors::turnLeft(int speed){
	motor->MoveVelocity(LEFT_FRONT,(-1*speed),speed/2);
	motor->MoveVelocity(RIGHT_FRONT,(-1*speed),speed/2);
	motor->MoveVelocity(LEFT_REAR,(-1*speed),speed/2);
	motor->MoveVelocity(RIGHT_REAR,(-1*speed),speed/2);
}

void RobotMotors::stop(){
	motor->Stop(LEFT_FRONT);
	motor->Stop(RIGHT_FRONT);
	motor->Stop(LEFT_REAR);
	motor->Stop(RIGHT_REAR);
}
