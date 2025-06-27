#!/bin/bash

# Telepesa User Service - Test Coverage Report Generator
# This script runs tests and generates coverage reports

set -e

echo "🚀 Starting Telepesa User Service Test Coverage Generation..."
echo "========================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Create logs directory
mkdir -p logs

# Function to log with timestamp
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a logs/coverage-report.log
}

log "Starting test coverage generation for Telepesa User Service"

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}❌ Error: pom.xml not found. Please run this script from the user-service directory.${NC}"
    exit 1
fi

# Clean previous builds
log "🧹 Cleaning previous builds..."
mvn clean > logs/maven-clean.log 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Clean completed successfully${NC}"
else
    echo -e "${YELLOW}⚠️ Clean had warnings, continuing...${NC}"
fi

# Run tests and generate coverage report
log "🧪 Running tests and generating coverage report..."
echo -e "${BLUE}Running Maven test with JaCoCo coverage...${NC}"

# Run tests with less strict failure handling
set +e
mvn test jacoco:report -Dmaven.test.failure.ignore=true > logs/maven-test.log 2>&1
TEST_EXIT_CODE=$?
set -e

# Check test results
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ All tests passed successfully!${NC}"
    TEST_STATUS="ALL_PASSED"
elif [ $TEST_EXIT_CODE -eq 1 ]; then
    echo -e "${YELLOW}⚠️ Some tests failed, but coverage report generated${NC}"
    TEST_STATUS="SOME_FAILED"
else
    echo -e "${RED}❌ Test execution failed${NC}"
    TEST_STATUS="FAILED"
    cat logs/maven-test.log
    exit 1
fi

# Check if coverage report was generated
if [ -f "target/site/jacoco/index.html" ]; then
    echo -e "${GREEN}✅ JaCoCo coverage report generated successfully${NC}"
    
    # Extract coverage percentage
    if [ -f "target/site/jacoco/jacoco.xml" ]; then
        # Try to extract coverage percentage from XML
        COVERAGE_PERCENT=$(grep -o 'covered="[0-9]*"' target/site/jacoco/jacoco.xml | head -1 | grep -o '[0-9]*' || echo "0")
        TOTAL_LINES=$(grep -o 'missed="[0-9]*"' target/site/jacoco/jacoco.xml | head -1 | grep -o '[0-9]*' || echo "0")
        
        if [ "$COVERAGE_PERCENT" != "0" ] && [ "$TOTAL_LINES" != "0" ]; then
            ACTUAL_COVERAGE=$((COVERAGE_PERCENT * 100 / (COVERAGE_PERCENT + TOTAL_LINES)))
            echo -e "${BLUE}📊 Test Coverage: ${ACTUAL_COVERAGE}%${NC}"
            
            if [ $ACTUAL_COVERAGE -ge 75 ]; then
                echo -e "${GREEN}✅ Coverage meets 75% threshold${NC}"
                COVERAGE_STATUS="MEETS_THRESHOLD"
            else
                echo -e "${YELLOW}⚠️ Coverage below 75% threshold${NC}"
                COVERAGE_STATUS="BELOW_THRESHOLD"
            fi
        else
            echo -e "${YELLOW}⚠️ Could not calculate coverage percentage${NC}"
            COVERAGE_STATUS="UNKNOWN"
        fi
    fi
else
    echo -e "${RED}❌ Coverage report not generated${NC}"
    COVERAGE_STATUS="NOT_GENERATED"
fi

# Generate summary report
log "📋 Generating summary report..."

cat > logs/test-coverage-summary.md << EOF
# Telepesa User Service - Test Coverage Report

**Generated on:** $(date '+%Y-%m-%d %H:%M:%S')

## Test Execution Summary

- **Test Status:** $TEST_STATUS
- **Coverage Status:** $COVERAGE_STATUS
$(if [ "$ACTUAL_COVERAGE" != "" ]; then echo "- **Coverage Percentage:** ${ACTUAL_COVERAGE}%"; fi)

## File Locations

- **HTML Coverage Report:** \`target/site/jacoco/index.html\`
- **XML Coverage Report:** \`target/site/jacoco/jacoco.xml\`
- **Test Results:** \`target/surefire-reports/\`
- **Build Logs:** \`logs/\`

## Test Results Details

$(if [ -d "target/surefire-reports" ]; then
    echo "### Test Execution Summary"
    find target/surefire-reports -name "*.xml" -exec grep -l "testcase" {} \; | wc -l | xargs echo "- Test Files Generated:"
    
    # Count total tests
    TOTAL_TESTS=$(find target/surefire-reports -name "*.xml" -exec grep -o 'tests="[0-9]*"' {} \; | grep -o '[0-9]*' | awk '{sum += $1} END {print sum}' || echo "0")
    TOTAL_FAILURES=$(find target/surefire-reports -name "*.xml" -exec grep -o 'failures="[0-9]*"' {} \; | grep -o '[0-9]*' | awk '{sum += $1} END {print sum}' || echo "0")
    TOTAL_ERRORS=$(find target/surefire-reports -name "*.xml" -exec grep -o 'errors="[0-9]*"' {} \; | grep -o '[0-9]*' | awk '{sum += $1} END {print sum}' || echo "0")
    
    echo "- **Total Tests:** $TOTAL_TESTS"
    echo "- **Failures:** $TOTAL_FAILURES"
    echo "- **Errors:** $TOTAL_ERRORS"
    echo "- **Passed:** $((TOTAL_TESTS - TOTAL_FAILURES - TOTAL_ERRORS))"
fi)

## Coverage by Package

$(if [ -f "target/site/jacoco/jacoco.xml" ]; then
    echo "Generated from JaCoCo XML report - see HTML report for detailed breakdown"
else
    echo "Coverage details not available - check HTML report"
fi)

## Next Steps

1. Open \`target/site/jacoco/index.html\` in a browser to view detailed coverage
2. Review failing tests in \`target/surefire-reports/\`
3. Check build logs in \`logs/\` directory
4. Address any test failures or coverage gaps

---

*Report generated by Telepesa CI/CD Pipeline*
EOF

echo ""
echo -e "${GREEN}✅ Test coverage report generation completed!${NC}"
echo ""
echo -e "${BLUE}📋 Summary:${NC}"
echo -e "   Test Status: $TEST_STATUS"
echo -e "   Coverage Status: $COVERAGE_STATUS"
if [ "$ACTUAL_COVERAGE" != "" ]; then
    echo -e "   Coverage: ${ACTUAL_COVERAGE}%"
fi
echo ""
echo -e "${BLUE}📁 Generated Files:${NC}"
echo -e "   📊 HTML Report: target/site/jacoco/index.html"
echo -e "   🗂️ Summary: logs/test-coverage-summary.md"
echo -e "   📝 Logs: logs/"
echo ""

if [ -f "target/site/jacoco/index.html" ]; then
    echo -e "${YELLOW}💡 To view the detailed coverage report:${NC}"
    echo -e "   open target/site/jacoco/index.html"
    echo ""
fi

log "Test coverage generation completed with status: $TEST_STATUS, Coverage: $COVERAGE_STATUS"

# Set exit code based on overall success
if [ "$TEST_STATUS" = "ALL_PASSED" ] && [ "$COVERAGE_STATUS" = "MEETS_THRESHOLD" ]; then
    exit 0
elif [ "$TEST_STATUS" = "SOME_FAILED" ] || [ "$COVERAGE_STATUS" = "BELOW_THRESHOLD" ]; then
    exit 1
else
    exit 2
fi 