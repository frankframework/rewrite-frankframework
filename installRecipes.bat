@echo off

set profiles=default 7_8 7_9_0 8_0_1 8_1_0 8_2_0 8_3_0 8_1_0 8_4_0

for %%p in (%profiles%) do (
    if "%%p"=="default" (
        echo Building with default profile...
        call ./mvnw install
    ) else (
        echo Building with profile: %%p
        call ./mvnw install -P%%p
    )
)
pause
