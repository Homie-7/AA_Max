// Native Android Auto (Car App Library) adapter for Maps.
// From Dutch App Patches (JohninNL, GPLv3) as fixed for Maps 26.44 in
// shuili1013/Maps-AA-AnyMode-Morphe (GPLv3). Plain Java, no dependencies.
extension { name = "extensions/maps-gearslip.mpe" }

android {
    namespace = "nl.johninnl.maps.gearslip"
    defaultConfig { minSdk = 29 }
}

configurations.configureEach {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    exclude(group = "org.jetbrains", module = "annotations")
}
