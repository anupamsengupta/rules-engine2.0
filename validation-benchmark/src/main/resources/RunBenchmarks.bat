@echo off
REM Script to run JMH benchmarks for expression evaluator performance comparison

echo Building benchmark module...
call mvn clean compile -q -pl validation-benchmark

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    exit /b 1
)

echo.
echo Running benchmarks...
echo.

REM Run all benchmarks using exec:java
call mvn exec:java ^
    -Dexec.args=".*Benchmark -f 2 -wi 5 -i 10" ^
    -pl validation-benchmark

