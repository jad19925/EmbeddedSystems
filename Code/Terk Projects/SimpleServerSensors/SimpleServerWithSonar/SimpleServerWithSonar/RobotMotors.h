/*
 * RobotMotors.h
 *
 *  Created on: Sep 7, 2013
 *      Author: jacob
 */
#include <qemotortraj.h>

#ifndef ROBOTMOTORS_H_
#define ROBOTMOTORS_H_

#define LEFT_FRONT	0
#define RIGHT_FRONT 1
#define LEFT_REAR	2
#define RIGHT_REAR	3

class RobotMotors {
public:
	RobotMotors();
	virtual ~RobotMotors();

	CQEMotorTraj * pmotor;
	//get/set speed factors on left/right sides of vex
	//default multipliers are 1.0
	void setMultiplierLeft(double m);
	void setMultiplierRight(double m);
	double getMultiplierLeft();
	double getMultiplierRight();
	//method to move the robot forward, speed ranges from 5000-?
	//10000 is a normal, relatively slow value
	void moveForward(int speed);
	//method to move the robot backward, speed ranges from 5000-?
	//10000 is a normal, relatively slow value
	void moveBackward(int speed);
	//method to turn the robot right, speed ranges from 2000-?
	//10000 is a normal, relatively slow value
	void turnRight(int speed);
	//method to turn the robot left, speed ranges from 2000-?
	//10000 is a normal, relatively slow value
	void turnLeft(int speed);
	//stops the robot
	void stop();
	//specify the amount of left and right movement as a percent, and a desired speed
	void drive(int leftSpeed, int rightSpeed, int speed);

private:
	double mLeft;
	double mRight;
};

#endif /* ROBOTMOTORS_H_ */
