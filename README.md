
# Overview

- **Academic Year**: 2019-2020
- **Project Title**: Smart Lock
- **Students**: Edoardo Cagnes\[1\], Andrea Giorgi
- **CFUs**: 9 

In this project we will build an IoT system about checking the status of a Lock.
We used ad ESP8266 to detect whether the lock is closed or not: a button inside the hole of the bolt act as a sensor and whenerver it's pressed or released its send a signal to a Web Server.

![device.png](/Images/device.png)

For the users, we developed an Android application that queries the Web Server about the status and show it to the user.

![app.png](/Images/app.png)


# Tools and Techniques

The main tools and techniques we will employ are:
- **Android Studio**: for the Android application \[2\].
- **Arduino IDE**: for programming the ESP8266 \[3\].
- **PHP Storm**: for the file on the PHP web server \[4\].


# Expected Outcomes

We expect the following outcomes from this project:
- A compact device able to detect when the lock is closed or open. This is achieved using ESP8266 a small chip with integrated WiFi module.  
- A Web Server that receives the messages from the device and stores it into a Database.
- An Android application that fetch the status from the Server


# Summary
The main purpose of this project is to implement an IoT device that can simplify everyday operation. 
Working at our company, we noticed that occasionally colleagues find themselves heading towards the bathroom and then finding it busy. Our idea of checking the status of a lock started from this: minimize any waiting time.
Later, during development, we have discovered through the needfinding process that, knowing a status of a lock, can be very useful also in safety fields (eg. to detect if someone has entered your house)
So, with these concepts in mind we created and developed the Smart Lock system.

# Project Documents
Final Report: [https://github.com/a-giorgi/SmartLock/blob/master/SmartLock_Final_Report.pdf](https://github.com/a-giorgi/SmartLock/blob/master/SmartLock_Final_Report.pdf)

# Bibliography

\[1\] https://github.com/isca107

\[2\] https://developer.android.com/studio

\[3\] https://www.arduino.cc/en/main/software

\[4\] https://www.jetbrains.com/phpstorm/
