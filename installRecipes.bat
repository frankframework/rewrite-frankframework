@echo off

set profiles=default 7_4 7_5 7_6 7_7 7_8 7_9 8_0 8_1 8_2 8_3 9_0

for %%p in (%profiles%) do (
    if "%%p"=="default" (
        echo Building with default profile...
        call ./mvnw clean package install -U -DargLine="-XX:+EnableDynamicAgentLoading"
    ) else (
        echo Building with profile: %%p
        call ./mvnw clean package install -U -P%%p -DargLine="-XX:+EnableDynamicAgentLoading"
    )
)
pause
