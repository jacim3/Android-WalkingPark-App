package com.example.walkingpark.data.enum

enum class Enums(){}

enum class ADDRESS(val x:Char
) {
    DO('도'),
    SI('시'),
    GUN('군'),
    GU('구'),
    EUP('읍'),
    MUN('면'),
    DONG('동')
}

enum class WEATHER(val x:String) {
    RAIN_RATIO("POP"),
    RAIN_TYPE("PTY"),
    HUMIDITY("REH"),
    SNOW("SNO"),
    SKY("SKY"),
    TEMPERATURE("TMP"),
    TEMPERATURE_LOW("TMN"),
    TEMPERATURE_HIGH("TMX"),
    WIND_SPEED("WSD"),
    WIND_SPEED_EW("UUU"),
    WIND_SPEED_NS("VVV"),
    WAVE_HEIGHT("VEC")
}

enum class AIR(val x: String) {
    PM25_24HOUR("pm25Value24"),
    UNIFIED_ATMOSPHERE_VALUE("khaiValue"),
    UNIFIED_ATMOSPHERE_GRADE("khaiGrade"),
    GRADE_AH_WHANG_SAN("so2Grade"),
    GRADE_IL_SAN_HWA("coGrade"),
    GRADE_OH_ZONE("o3Grade"),
    GRADE_E_SAN_HWA("no2Grade"),
    GRADE_PM10_24HOUR("pm10Grade"),
    GRADE_PM25_24HOUR("pm25Grade"),
    GRADE_PM10_01HOUR("pm10Grade1h"),
    GRADE_PM25_01HOUR("pm25Grade1h"),
    FLAG_AH_WHANG_SAN("so2Flag"),
    FLAG_IL_SAN_HWA("coFlag"),
    FLAG_OH_ZONE("o3Flag"),
    FLAG_E_SAN_HWA("no2Flag"),
    FLAG_PM10("pm10Flag"),
    FLAG_PM25("pm25Flag"),
}