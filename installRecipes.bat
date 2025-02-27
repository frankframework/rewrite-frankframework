@echo off

set profiles=default 7_8_0 7_9_0 8_0_1 8_1_0 8_2_0 8_3_0 8_1_0 8_4_0 8_5_0 8_6_0 8_7_0 8_8_0 8_9_0 9_0_0

for %%p in (%profiles%) do (
    if "%%p"=="default" (
        echo Building with default profile...
        call ./mvnw clean package install -U
    ) else (
        echo Building with profile: %%p
        call ./mvnw clean package install -U -P%%p
    )
)
pause
