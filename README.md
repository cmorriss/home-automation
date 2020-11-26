# Home Automation Server
This is a super simple home automation server I whipped up for my own purposes. 

Server and nodes are written in Kotlin and utilize the Ktor framework.

UI is written in Typescript and built on the Angular 2 and Ionic frameworks.

# Installing

To install the server, run `gradle distTar` and take the distribution file under `build/distributions/home-automation-server-<version>.tar` and untar it in a directory like `/usr/lib`.

To add the UI to it, make sure you have `npm` installed. Install Ionic by running `npm install -g ionic`. Build the UI by running `ionic build --engine=browser`. Finally, copy the files under the `ui/www` directory to wherever you untarred the home-automation-server build in a new `app` directory.

Crank it up by running `bin/home-automation-server`.

# FAQ
**Why didn't you use OpenHab2 or some other automation server that already does this?**

I did for a while, but it didn't do everything I wanted. I have the power to code and as it so happens love doing it.

**Why did you write this in Kotlin? I thought that was just for Android.**

Once I tried it out, it was hard to go back to Java anywhere. In fact, I've gotten many teams I work with in Amazon to convert to it and they're all backend services.

For me, it's just a great JVM language that is significantly better than Java.

**Are you going to spend tons of time making this totally generic and a competitor to other home automation frameworks?**

Unlikely. If you find it useful, feel free to use, clone, or contribute. I don't really have the time to make this any more than I need it to be.

**Your code sucks. Why did you hard code strings everywhere and not make it more generic?**

This isn't something I would ever put in production at work. I took some shortcuts as it's good enough for my purposes.
