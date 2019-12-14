# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# seems these classes (as specified in the Paho client JAR META-INF\services\org.eclipse.paho.client.mqttv3.spi.NetworkModuleFactory)
# need to be manually kept
-keep public class org.eclipse.paho.client.mqttv3.internal.TCPNetworkModuleFactory
-keep public class org.eclipse.paho.client.mqttv3.internal.SSLNetworkModuleFactory
-keep public class org.eclipse.paho.client.mqttv3.internal.websocket.WebSocketNetworkModuleFactory
-keep public class org.eclipse.paho.client.mqttv3.internal.websocket.WebSocketSecureNetworkModuleFactory
