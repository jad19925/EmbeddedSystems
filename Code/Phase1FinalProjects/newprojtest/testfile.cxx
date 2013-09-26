#include <stdio.h>
#include "qemotortraj.h"
#include <textlcd.h>
#include <time.h>

int main(int argc, char **argv)
{
    CTextLcd &lcd = CTextLcd::GetRef();
	printf("Hello World!\n");
    lcd.printf("Hello World!");
    //sleep is in seconds, so this will display Hello World for 10 seconds
    sleep(10);
    CTextLcd::Release();


  /*const int axis = 0;

  // get motor reference
  CQEMotorTraj &motor = CQEMotorTraj::GetRef();

  // set PID gains for axis
  motor.SetPIDVGains(axis, 100, 0, 500, 0);

  printf("running\n");

  // move back and forth
  while(1)
    {
      motor.Move(axis, 40000, 20000, 8000);
      while(!motor.Done(axis));
      motor.Move(axis, -40000, 20000, 8000);
      while(!motor.Done(axis));
    }*/
}
