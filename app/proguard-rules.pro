# Keep Room entities/DAOs
-keep class com.linkresolver.app.data.local.** { *; }

# Keep kotlinx.serialization models
-keepattributes *Annotation*, InnerClasses
-keep class com.linkresolver.app.domain.model.** { *; }
