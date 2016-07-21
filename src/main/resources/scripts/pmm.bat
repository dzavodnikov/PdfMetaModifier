@ECHO OFF
REM ============================================================================
REM  Run PdfMetaModifier in a command line.
REM  ===========================================================================

SET JAR_FILE=pmm.jar
SET JVM_OPT=-Xms64m -Xmx512m

SET DIR_PATH=%~dp0
SET SCRIPT_PARAMS=%*

java %JVM_OPT% -jar "%DIR_PATH%\%JAR_FILE%" %SCRIPT_PARAMS%

