# C0inFlipp0r


OpenJDK 8 is required to run this software. Other commercial JDK's ( Sun, Oracle) will not work.


For Stand-Alone usage, just download the C0inFlipp0r.tar.gz package. After unpacking it, you can use it described as follows:

# starting Server Console:

java -jar C0inFlipp0r.jar --server

In case you have different java versions parallel installed, just add the full path to the wished java binary. For example:

/usr/lib/jvm/java-1.8.0-openjdk-am4/bin/java -jar C0inFlipp0r.jar --server


# starting Client Gui:

java -jar C0inFlipp0r.jar

Same thing for different java versions here:

/usr/lib/jvm/java-1.8.0-openjdk-am4/bin/java -jar C0inFlipp0r.jar

It is recommended to use an updated version of OpenJDK 8, because older versions cointains a known issue with ImageIO. More information about that on: 

http://stackoverflow.com/questions/28664812/workaround-for-imageio-read-bug-in-openjdk-8
 
Anyway, if you want to use the Client-Gui with older versions of OpenJDK 8 there is an option "enableGraphics" in config file. If this option is set to false,
ImageIO parts were skipped and the Client runs without pictures and problems.