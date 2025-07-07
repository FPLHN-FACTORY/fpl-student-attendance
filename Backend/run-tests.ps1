param (
    [string]$TestPattern = "*"
)

# Set environment variables for encoding
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

# Use gradlew with explicit encoding parameters
.\gradlew.bat test --tests $TestPattern `
    "-Dfile.encoding=UTF-8" `
    "-Dsun.jnu.encoding=UTF-8" `
    "-Duser.language=en" `
    "-Duser.country=US" `
    "--info"

# Reset environment variables
$env:JAVA_TOOL_OPTIONS = ""

Write-Host "Tests completed. Check the test report for details."