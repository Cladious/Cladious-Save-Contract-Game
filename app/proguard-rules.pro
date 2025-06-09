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

# =====================================================
# SMART CONTRACT GAME SPECIFIC OBFUSCATION RULES
# =====================================================

# Enable aggressive obfuscation
-dontskipnonpubliclibraryclasses
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-repackageclasses ''
-flattenpackagehierarchy 'sc.game'

# Keep attributes for debugging and reflection
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

# =====================================================
# PROTECT GAME DATA CLASSES FROM OBFUSCATION
# =====================================================

# Keep all data classes for Gson serialization
-keep class com.cladiousgame.attackergame.data.SmartContractChallenge { *; }
-keep class com.cladiousgame.attackergame.data.CodeGap { *; }
-keep class com.cladiousgame.attackergame.data.PlayerProgress { *; }
-keep class com.cladiousgame.attackergame.data.ChallengeStats { *; }
-keep class com.cladiousgame.attackergame.data.BattleChallenge { *; }

# Keep enums readable for gameplay
-keep enum com.cladiousgame.attackergame.data.VulnerabilityType { *; }
-keep enum com.cladiousgame.attackergame.data.Difficulty { *; }
-keep enum com.cladiousgame.attackergame.data.GapType { *; }
-keep enum com.cladiousgame.attackergame.data.PowerUpType { *; }
-keep enum com.cladiousgame.attackergame.data.BattleType { *; }

# Keep Repository methods that might be called dynamically
-keep class com.cladiousgame.attackergame.data.ChallengeRepository {
    public <methods>;
    private android.content.Context context;
    private android.content.SharedPreferences prefs;
    private com.google.gson.Gson gson;
}

# =====================================================
# GSON SERIALIZATION PROTECTION
# =====================================================

# Gson uses generic type information stored in a class file when working with fields
-keepattributes Signature

# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# =====================================================
# ANDROID FRAMEWORK PROTECTION
# =====================================================

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.appcompat.app.AppCompatActivity

# Keep View constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep onClick methods
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# =====================================================
# OBFUSCATION CUSTOMIZATION
# =====================================================

# Use built-in dictionary for maximum compatibility
# Comment out external dictionary files for now
# -classobfuscationdictionary obfuscation-class-dictionary.txt
# -obfuscationdictionary obfuscation-method-dictionary.txt
# -packageobfuscationdictionary obfuscation-package-dictionary.txt

# Rename packages to confusing names
-repackageclasses 'a'
-allowaccessmodification

# Make private fields and methods more aggressive
-keepclassmembers class * {
    !private <fields>;
    !private <methods>;
}

# =====================================================
# ADVANCED SECURITY OBFUSCATION
# =====================================================

# Hide source file names in stack traces
-renamesourcefileattribute ""

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove debugging code
-assumenosideeffects class java.lang.System {
    public static void out.println(...);
    public static void out.print(...);
    public static void err.println(...);
    public static void err.print(...);
}

# =====================================================
# CHALLENGE-SPECIFIC OBFUSCATION
# =====================================================

# Obfuscate method names but keep public interface
-keep class com.cladiousgame.attackergame.data.ChallengeRepository {
    public java.util.List getAllChallenges();
    public java.util.List getChallengesByDifficulty(...);
    public com.cladiousgame.attackergame.data.SmartContractChallenge getChallengeById(int);
    public void saveProgress(...);
    public com.cladiousgame.attackergame.data.PlayerProgress loadProgress();
}

# Heavily obfuscate internal implementation
-keepclassmembers class com.cladiousgame.attackergame.data.ChallengeRepository {
    !public <methods>;
}

# Keep challenge content but obfuscate implementation
-keepclassmembers class com.cladiousgame.attackergame.data.SmartContractChallenge {
    public java.lang.String title;
    public java.lang.String description;
    public java.lang.String vulnerableCode;
    public java.lang.String explanation;
    public java.util.List gaps;
    public int pointsReward;
}

# =====================================================
# THIRD PARTY LIBRARIES
# =====================================================

# Gson
-keepclassmembers class * {
    @com.google.gson.annotations.Expose <fields>;
}

# Keep generic signatures for Gson
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken

# AndroidX and Support Library
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Material Design
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }

# Security Crypto Library
-keep class androidx.security.crypto.** { *; }

# =====================================================
# ADDITIONAL SECURITY MEASURES
# =====================================================

# Obfuscate class names more aggressively
-obfuscationdictionary proguard-dict.txt

# Remove unused code aggressively
-dontwarn **

# Optimize and obfuscate aggressively
-optimizations !code/simplification/variable

# Remove debug information
-assumenosideeffects class * {
    void debug*(...);
    void Debug*(...);
    void DEBUG*(...);
}

# Anti-debugging measures
-assumenosideeffects class android.os.Debug {
    public static boolean isDebuggerConnected();
}

# String encryption (comment out if causing issues)
# -adaptclassstrings
# -adaptresourcefilenames
# -adaptresourcefilecontents

# Final protection: make everything as confusing as possible
-verbose

# Keep BuildConfig for security checks
-keep class **.BuildConfig { *; }

# R8 full mode optimizations
-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively