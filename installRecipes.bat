@echo off

set profiles=default 7_8 7_9 8_0 8_1 8_2 8_3 9_0

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
