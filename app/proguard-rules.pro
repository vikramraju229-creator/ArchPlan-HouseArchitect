# ArchPlan ProGuard Rules

# Keep iText
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Keep Room entities
-keep class com.archplan.data.model.** { *; }

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
