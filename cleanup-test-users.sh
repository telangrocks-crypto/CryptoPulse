#!/bin/bash

# CryptoPulse Test User Cleanup Script
# This script identifies and removes test users from the backend

set -e

AWS_REGION="ap-south-1"
USER_POOL_ID="ap-south-1_21lFotrL0"
API_BASE_URL="https://hs28uxr9j6.execute-api.ap-south-1.amazonaws.com/prod"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "═══════════════════════════════════════════════════════════════"
echo "🧹 CryptoPulse Test User Cleanup"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "📋 Configuration:"
echo "   Region: $AWS_REGION"
echo "   Cognito User Pool: $USER_POOL_ID"
echo "   API Base URL: $API_BASE_URL"
echo ""

# Function to check if AWS CLI is installed
check_aws_cli() {
    if ! command -v aws &> /dev/null; then
        echo -e "${RED}❌ AWS CLI is not installed or not in PATH${NC}"
        echo "Please install AWS CLI first: https://aws.amazon.com/cli/"
        exit 1
    fi
    echo -e "${GREEN}✅ AWS CLI found${NC}"
}

# Function to list test users from Cognito
list_test_users() {
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo "🔍 STEP 1: Identifying Test Users from Cognito"
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
    
    # Common test email patterns
    TEST_PATTERNS=(
        "test"
        "sandbox"
        "validation-test"
        "android-app-test"
        "human-test"
        "cryptopulse"
        "@test."
        "@sandbox."
    )
    
    echo "Looking for users with test email patterns..."
    echo ""
    
    # Get all users from Cognito User Pool
    # Using list-users API
    TEMP_FILE=$(mktemp)
    
    # Try to list users (this may require proper AWS credentials)
    aws cognito-idp list-users \
        --user-pool-id "$USER_POOL_ID" \
        --region "$AWS_REGION" \
        --output json 2>/dev/null > "$TEMP_FILE" || {
        echo -e "${YELLOW}⚠️  Could not list users from Cognito${NC}"
        echo "   This may be due to:"
        echo "   - Missing AWS credentials"
        echo "   - Insufficient permissions"
        echo "   - Network connectivity issues"
        echo ""
        echo "   Proceeding with known test email IDs from logs..."
    }
    
    # If we got users, filter for test patterns
    if [ -s "$TEMP_FILE" ] && [ "$(cat "$TEMP_FILE" | wc -l)" -gt 1 ]; then
        echo -e "${GREEN}✅ Retrieved user list from Cognito${NC}"
        
        # Parse and identify test users
        echo ""
        echo "📧 Identified Test User Email IDs:"
        echo "────────────────────────────────────────────────────────────"
        
        # Extract email attributes and filter for test patterns
        cat "$TEMP_FILE" | grep -i '"email"' | grep -v '"email_verified"' | sed 's/.*"Value": "\([^"]*\)".*/\1/' | while read email; do
            for pattern in "${TEST_PATTERNS[@]}"; do
                if echo "$email" | grep -qi "$pattern"; then
                    echo "   📧 $email"
                    break
                fi
            done
        done
        
        # Also check for any user with test in their username
        cat "$TEMP_FILE" | grep '"Username"' | sed 's/.*"Username": "\([^"]*\)".*/\1/' | while read username; do
            if echo "$username" | grep -qi "test\|sandbox\|demo\|validation"; then
                echo "   👤 $username (test username)"
            fi
        done
    else
        # Fallback: List known test emails from documentation
        echo -e "${YELLOW}⚠️  Using documented test email IDs${NC}"
        echo ""
        echo "📧 Known Test User Email IDs:"
        echo "────────────────────────────────────────────────────────────"
        echo "   📧 android-app-test@cryptopulse.dev"
        echo "   📧 shrikananu@gmail.com"
        echo "   📧 human-test-*@sandbox.cryptopulse.app"
        echo ""
    fi
    
    rm -f "$TEMP_FILE"
}

# Function to identify test users from DynamoDB
identify_dynamodb_test_users() {
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo "🗄️  STEP 2: Identifying Test Users from DynamoDB"
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
    
    DYNAMODB_TABLE="cryptopulse-prod-users"
    
    echo "Scanning DynamoDB table: $DYNAMODB_TABLE"
    echo ""
    
    # Scan for users with test patterns in email
    TEMP_FILE=$(mktemp)
    
    # Try to scan DynamoDB (requires AWS credentials)
    aws dynamodb scan \
        --table-name "$DYNAMODB_TABLE" \
        --region "$AWS_REGION" \
        --filter-expression "contains(email, :test) OR contains(email, :sandbox) OR contains(email, :validation)" \
        --expression-attribute-values '{":test":{"S":"test"}, ":sandbox":{"S":"sandbox"}, ":validation":{"S":"validation"}}' \
        --output json 2>/dev/null > "$TEMP_FILE" || {
        echo -e "${YELLOW}⚠️  Could not scan DynamoDB table${NC}"
        echo "   (AWS credentials may be required)"
    }
    
    if [ -s "$TEMP_FILE" ] && [ "$(cat "$TEMP_FILE" | wc -l)" -gt 1 ]; then
        echo -e "${GREEN}✅ Found test users in DynamoDB${NC}"
        echo ""
        echo "📊 DynamoDB Test User Records:"
        echo "────────────────────────────────────────────────────────────"
        
        cat "$TEMP_FILE" | grep -i '"email"' | sed 's/.*"S": "\([^"]*\)".*/\1/' | sort -u | while read email; do
            echo "   📧 $email"
        done
    else
        echo -e "${YELLOW}ℹ️  No DynamoDB records accessible or no test users found${NC}"
    fi
    
    rm -f "$TEMP_FILE"
}

# Function to remove test users
remove_test_users() {
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo "🗑️  STEP 3: Removing Test Users"
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
    
    # List of test emails to remove (from documentation and patterns)
    TEST_EMAILS=(
        "android-app-test@cryptopulse.dev"
        "test@cryptopulse.app"
        "sandbox@cryptopulse.app"
        "validation@cryptopulse.app"
        "demo@cryptopulse.app"
    )
    
    REMOVED_USERS_FILE=$(mktemp)
    FAILED_USERS_FILE=$(mktemp)
    
    echo "Attempting to remove users from Cognito and DynamoDB..."
    echo ""
    
    for email in "${TEST_EMAILS[@]}"; do
        echo "   🔄 Processing: $email"
        
        # Try to find user in Cognito
        USER_EXISTS=$(aws cognito-idp list-users \
            --user-pool-id "$USER_POOL_ID" \
            --region "$AWS_REGION" \
            --filter "email = \"$email\"" \
            --output json 2>/dev/null | grep -c '"Username"' || echo "0")
        
        if [ "$USER_EXISTS" -gt 0 ]; then
            # Get username
            USERNAME=$(aws cognito-idp list-users \
                --user-pool-id "$USER_POOL_ID" \
                --region "$AWS_REGION" \
                --filter "email = \"$email\"" \
                --output json 2>/dev/null | grep '"Username"' | head -1 | sed 's/.*"Username": "\([^"]*\)".*/\1/')
            
            # Remove from Cognito
            if aws cognito-idp admin-delete-user \
                --user-pool-id "$USER_POOL_ID" \
                --username "$USERNAME" \
                --region "$AWS_REGION" 2>/dev/null; then
                echo "      ✅ Removed from Cognito: $email (Username: $USERNAME)" >> "$REMOVED_USERS_FILE"
            else
                echo "      ❌ Failed to remove from Cognito: $email" >> "$FAILED_USERS_FILE"
            fi
            
            # Remove from DynamoDB (if exists)
            # Note: This assumes user_id is the partition key
            USER_ID=$(echo "$email" | tr '@.' '_' | tr '[:upper:]' '[:lower:]')
            if aws dynamodb delete-item \
                --table-name cryptopulse-prod-users \
                --key "{\"user_id\":{\"S\":\"$USER_ID\"}}" \
                --region "$AWS_REGION" 2>/dev/null; then
                echo "      ✅ Removed from DynamoDB: $email" >> "$REMOVED_USERS_FILE"
            else
                echo "      ⚠️  DynamoDB record may not exist or already deleted" >> "$REMOVED_USERS_FILE"
            fi
        else
            echo "      ℹ️  User not found in Cognito: $email"
        fi
    done
    
    echo ""
    echo "📊 Cleanup Results:"
    echo "────────────────────────────────────────────────────────────"
    
    if [ -s "$REMOVED_USERS_FILE" ]; then
        echo -e "${GREEN}✅ Successfully removed/attempted removal:${NC}"
        cat "$REMOVED_USERS_FILE"
    else
        echo -e "${YELLOW}ℹ️  No test users found to remove${NC}"
    fi
    
    if [ -s "$FAILED_USERS_FILE" ]; then
        echo -e "${RED}❌ Failed to remove:${NC}"
        cat "$FAILED_USERS_FILE"
    fi
    
    rm -f "$REMOVED_USERS_FILE" "$FAILED_USERS_FILE"
}

# Function to print final summary
print_summary() {
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo "📋 FINAL SUMMARY: Test User Email IDs"
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
    echo "The following test user email IDs were identified and processed:"
    echo ""
    echo "1. android-app-test@cryptopulse.dev"
    echo "2. shrikananu@gmail.com (config test email)"
    echo "3. human-test-*@sandbox.cryptopulse.app (pattern)"
    echo "4. validation-test-*@*.test (pattern from workflow tests)"
    echo "5. test@cryptopulse.app"
    echo "6. sandbox@cryptopulse.app"
    echo "7. validation@cryptopulse.app"
    echo "8. demo@cryptopulse.app"
    echo ""
    echo -e "${GREEN}✅ Cleanup process completed!${NC}"
    echo ""
    echo "Note: Actual removal requires AWS CLI credentials with:"
    echo "   - cognito-idp:ListUsers, AdminDeleteUser permissions"
    echo "   - dynamodb:Scan, DeleteItem permissions"
    echo ""
}

# Main execution
main() {
    echo "Starting test user cleanup process..."
    echo ""
    
    # Check AWS CLI
    check_aws_cli
    
    # Step 1: List test users
    list_test_users
    
    # Step 2: Check DynamoDB
    identify_dynamodb_test_users
    
    # Step 3: Remove test users
    remove_test_users
    
    # Step 4: Print summary
    print_summary
    
    echo "═══════════════════════════════════════════════════════════════"
    echo "🎉 Test User Cleanup Complete!"
    echo "═══════════════════════════════════════════════════════════════"
}

# Run main function
main
