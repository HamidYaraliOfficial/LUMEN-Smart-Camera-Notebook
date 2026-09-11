# LUMEN ProGuard rules
-keep class com.lumen.app.data.local.entity.** { *; }
-keep class com.lumen.app.domain.model.** { *; }
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn org.tensorflow.**
-keep class com.google.mlkit.** { *; }
